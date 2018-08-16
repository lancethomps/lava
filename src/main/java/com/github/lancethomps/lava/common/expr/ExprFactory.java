package com.github.lancethomps.lava.common.expr;

import static com.github.lancethomps.lava.common.Checks.isBlank;
import static com.github.lancethomps.lava.common.expr.ScriptEngines.createJsEngineExpression;
import static com.github.lancethomps.lava.common.expr.ScriptEngines.createPyEngineExpression;
import static com.github.lancethomps.lava.common.expr.ScriptEngines.evalJs;
import static com.github.lancethomps.lava.common.expr.ScriptEngines.evalPy;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeLocator;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.cache.CacheException;
import com.github.lancethomps.lava.common.cache.SimpleLruCache;
import com.github.lancethomps.lava.common.expr.spel.NonSandboxedSpelTypeLocator;
import com.github.lancethomps.lava.common.expr.spel.SandboxedContextConfig;
import com.github.lancethomps.lava.common.expr.spel.SandboxedSpelConstructorResolver;
import com.github.lancethomps.lava.common.expr.spel.SandboxedSpelEvaluationContext;
import com.github.lancethomps.lava.common.expr.spel.SandboxedSpelMethodResolver;
import com.github.lancethomps.lava.common.expr.spel.SandboxedSpelPropertyAccessor;
import com.github.lancethomps.lava.common.expr.spel.SandboxedSpelTypeLocator;
import com.github.lancethomps.lava.common.lambda.ThrowingBiFunction;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.metrics.StatusMonitor;
import com.github.lancethomps.lava.common.properties.PropertyParser;
import com.github.lancethomps.lava.common.ser.MissingRequiredFieldException;
import com.github.lancethomps.lava.common.ser.OutputExpression;
import com.github.lancethomps.lava.common.ser.OutputExpressionRoot;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.time.Stopwatch;

import io.dropwizard.metrics5.Timer;
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

/**
 * The Class ExpressionUtil.
 */
public class ExprFactory {

	/** The Constant METRIC_REGISTRY. */
	public static final String METRIC_REGISTRY = "expressions";

	/** The Constant METRIC_REGISTRY_COMPILE. */
	public static final String METRIC_REGISTRY_COMPILE = "compile";

	/** The Constant METRIC_REGISTRY_EVAL. */
	public static final String METRIC_REGISTRY_EVAL = "eval";

	/** The Constant SPEL_PARSER. */
	public static final SpelExpressionParser SPEL_PARSER = new SpelExpressionParser(new SpelParserConfiguration(null, null, true, true, Integer.MAX_VALUE));

	/** The Constant SPEL_REGEX. */
	public static final Pattern SPEL_REGEX = Pattern.compile("#\\{(.*)\\}$");

	/** The Constant CACHED_EXPRESSIONS. */
	private static final SimpleLruCache<String, Object> CACHED_EXPRESSIONS = new SimpleLruCache<>(250);

	/** The default expr parser. */
	private static ExprParser defaultExprParser = ExprParser.SPEL;

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(ExprFactory.class);

	/** The log missing ognl properties. */
	private static boolean logMissingOgnlProperties;

	/** The Constant NON_SANDBOXED_CONTEXT. */
	private static final StandardEvaluationContext NON_SANDBOXED_CONTEXT;

	/** The Constant NON_SANDBOXED_SPEL_TYPE_LOCATOR. */
	private static final StandardTypeLocator NON_SANDBOXED_SPEL_TYPE_LOCATOR = registerDefaultSpringImports(new NonSandboxedSpelTypeLocator());

	/** The Constant OGNL_CREATOR. */
	private static final ThrowingBiFunction<String, Boolean, Node> OGNL_CREATOR = (e, sandbox) -> {
		Stopwatch watch = Stopwatch.createAndStart();
		try {
			return (Node) Ognl.parseExpression(e);
		} finally {
			logCreateTimer(watch, ExprParser.OGNL);
		}
	};

	/** The Constant OGNL_EVAL_TIMER. */
	private static final Timer OGNL_EVAL_TIMER = StatusMonitor.timer(ExprFactory.METRIC_REGISTRY, ExprParser.OGNL.name(), ExprFactory.METRIC_REGISTRY_EVAL);

	/** The Constant OGNL_NON_SANDBOXED_CONTEXT. */
	private static final OgnlContext OGNL_NON_SANDBOXED_CONTEXT = new OgnlContext(null, null, new OgnlMemberAccess(true));

	/** The Constant OGNL_CONTEXT. */
	private static final OgnlContext OGNL_SANDBOXED_CONTEXT = new OgnlContext(new ExprClassResolver(), null, new OgnlMemberAccess(true));

	/** The Constant SANDBOXED_CONTEXT. */
	private static final SandboxedSpelEvaluationContext SANDBOXED_CONTEXT;

	/** The Constant SANDBOXED_SPEL_CONSTRUCTOR_RESOLVER. */
	private static final SandboxedSpelConstructorResolver SANDBOXED_SPEL_CONSTRUCTOR_RESOLVER = new SandboxedSpelConstructorResolver();

	/** The Constant SANDBOXED_SPEL_METHOD_RESOLVER. */
	private static final SandboxedSpelMethodResolver SANDBOXED_SPEL_METHOD_RESOLVER = new SandboxedSpelMethodResolver();

	/** The Constant SANDBOXED_SPEL_PROPERTY_ACCESSOR. */
	private static final SandboxedSpelPropertyAccessor SANDBOXED_SPEL_PROPERTY_ACCESSOR = new SandboxedSpelPropertyAccessor();

	/** The Constant SANDBOXED_SPEL_TYPE_LOCATOR. */
	private static final SandboxedSpelTypeLocator SANDBOXED_SPEL_TYPE_LOCATOR = registerDefaultSpringImports(new SandboxedSpelTypeLocator());

	/** The Constant SPEL_EVAL_TIMER. */
	private static final Timer SPEL_EVAL_TIMER = StatusMonitor.timer(ExprFactory.METRIC_REGISTRY, ExprParser.SPEL.name(), ExprFactory.METRIC_REGISTRY_EVAL);

	static {
		NON_SANDBOXED_CONTEXT = createSpelNonSandboxedContext();
		SANDBOXED_CONTEXT = createSpelSandboxedContext();
		NON_SANDBOXED_SPEL_TYPE_LOCATOR.registerImport("com.github.lancethomps.lava.common");
		NON_SANDBOXED_SPEL_TYPE_LOCATOR.registerImport("com.github.lancethomps.lava.common.date");
		NON_SANDBOXED_SPEL_TYPE_LOCATOR.registerImport("com.google.common.collect");
	}

	/** The Constant SPEL_CREATOR. */
	private static final ThrowingBiFunction<String, Boolean, SpelExpression> SPEL_CREATOR = (p, sandbox) -> {
		Stopwatch watch = Stopwatch.createAndStart();
		try {
			SpelExpression expr = (SpelExpression) SPEL_PARSER.parseExpression(p);
			if (sandbox) {
				expr.setEvaluationContext(SANDBOXED_CONTEXT);
			} else {
				expr.setEvaluationContext(NON_SANDBOXED_CONTEXT);
			}
			return expr;
		} finally {
			logCreateTimer(watch, ExprParser.SPEL);
		}
	};

	/**
	 * Clear cache.
	 */
	public static void clearCache() {
		try {
			CACHED_EXPRESSIONS.clear();
		} catch (CacheException e) {
			Logs.logError(LOG, e, "Error clearing expressions cache.");
		}
	}

	/**
	 * Compile create expression.
	 *
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @param requirePath the require path
	 * @param verbose the verbose
	 * @param defaultType the default type
	 * @return the output expression
	 * @throws MissingRequiredFieldException the missing required field exception
	 * @throws ScriptException the script exception
	 * @throws ExpressionEvalException the expression eval exception
	 */
	public static OutputExpression compileCreateExpression(
		@Nonnull OutputExpression expr,
		boolean sandbox,
		boolean requirePath,
		boolean verbose,
		@Nonnull ExprParser defaultType
	) throws MissingRequiredFieldException,
		ScriptException,
		ExpressionEvalException {
		if (requirePath && isBlank(expr.getPath())) {
			throw new MissingRequiredFieldException("OutputExpression.path");
		}
		if (isBlank(expr.getExpression())) {
			throw new MissingRequiredFieldException("OutputExpression.expression");
		}
		if (expr.getType() == null) {
			expr.setType(defaultType);
		}
		if (isBlank(expr.getPath())) {
			expr.setPath('"' + expr.getExpression() + '"');
		}
		if ((expr.getGlobalVariables() != null) && (expr.getGlobalVariablesResolved() == null)) {
			expr.setGlobalVariablesResolved(evaluateOutputExpressionsWithException(expr.getGlobalVariables(), null, sandbox, null, expr.getType()));
		}
		if (expr.getCompiledExpression() == null) {
			expr.setCompiledExpression(compileExprWithException(expr.getType(), sandbox, expr.getExpression(), verbose, expr));
			if (expr.getCompiledExpression() == null) {
				throw new MissingRequiredFieldException("OutputExpression.compiledExpression");
			}
		}
		return expr;
	}

	/**
	 * Compile create expressions.
	 *
	 * @param sandbox the sandbox
	 * @param requirePath the require path
	 * @param verbose the verbose
	 * @param expressions the expressions
	 * @return the list
	 */
	public static List<OutputExpression> compileCreateExpressions(boolean sandbox, boolean requirePath, boolean verbose, OutputExpression... expressions) {
		return Checks.isEmpty(expressions) ? null : compileCreateExpressions(Arrays.asList(expressions), sandbox, requirePath, verbose);
	}

	/**
	 * Compile create expressions.
	 *
	 * @param expressions the expressions
	 * @param sandbox the sandbox
	 * @return the list
	 */
	public static List<OutputExpression> compileCreateExpressions(List<OutputExpression> expressions, boolean sandbox) {
		return compileCreateExpressions(expressions, sandbox, true);
	}

	/**
	 * Compile create expressions.
	 *
	 * @param expressions the expressions
	 * @param sandbox the sandbox
	 * @param requirePath the require path
	 * @return the list
	 */
	public static List<OutputExpression> compileCreateExpressions(List<OutputExpression> expressions, boolean sandbox, boolean requirePath) {
		return compileCreateExpressions(expressions, sandbox, requirePath, true);
	}

	/**
	 * Compile create expressions.
	 *
	 * @param expressions the expressions
	 * @param sandbox the sandbox
	 * @param requirePath the require path
	 * @param verbose the verbose
	 * @return the list
	 */
	public static List<OutputExpression> compileCreateExpressions(List<OutputExpression> expressions, boolean sandbox, boolean requirePath, boolean verbose) {
		return compileCreateExpressions(expressions, sandbox, requirePath, verbose, ExprFactory.getDefaultExprParser());
	}

	/**
	 * Compile create expressions.
	 *
	 * @param expressions the expressions
	 * @param sandbox the sandbox
	 * @param requirePath the require path
	 * @param verbose the verbose
	 * @param defaultType the default type
	 * @return the list
	 */
	public static List<OutputExpression> compileCreateExpressions(
		List<OutputExpression> expressions,
		boolean sandbox,
		boolean requirePath,
		boolean verbose,
		@Nonnull ExprParser defaultType
	) {
		return expressions == null ? null
			: expressions
				.stream()
				.filter(expr -> (!requirePath || isNotBlank(expr.getPath())) && isNotBlank(expr.getExpression()))
				.map(expr -> {
					try {
						return compileCreateExpression(expr, sandbox, requirePath, verbose, defaultType);
					} catch (MissingRequiredFieldException | ScriptException | ExpressionEvalException e) {
						Logs.logError(LOG, e, "Error compiling expression: %s", expr);
						return null;
					}
				})
				.filter(Checks::nonNull)
				.filter(expr -> expr.getCompiledExpression() != null)
				.collect(Collectors.toList());
	}

	/**
	 * Compile create expressions and remove invalid.
	 *
	 * @param expressions the expressions
	 * @param sandbox the sandbox
	 * @param requirePath the require path
	 * @param verbose the verbose
	 * @return the list
	 */
	public static List<OutputExpression> compileCreateExpressionsAndRemoveInvalid(List<OutputExpression> expressions, boolean sandbox, boolean requirePath, boolean verbose) {
		if (expressions == null) {
			return null;
		}
		expressions.removeIf(expr -> {
			try {
				return compileCreateExpression(expr, sandbox, requirePath, verbose, getDefaultExprParser()) == null;
			} catch (MissingRequiredFieldException | ScriptException | ExpressionEvalException e) {
				Logs.logError(LOG, e, "Error compiling expression: %s", expr);
				return true;
			}
		});
		return expressions;
	}

	/**
	 * Compile create expressions with exception.
	 *
	 * @param expressions the expressions
	 * @param sandbox the sandbox
	 * @param requirePath the require path
	 * @param verbose the verbose
	 * @return the list
	 * @throws MissingRequiredFieldException the missing required field exception
	 * @throws ScriptException the script exception
	 * @throws ExpressionEvalException the expression eval exception
	 */
	public static List<OutputExpression> compileCreateExpressionsWithException(
		List<OutputExpression> expressions,
		boolean sandbox,
		boolean requirePath,
		boolean verbose
	) throws MissingRequiredFieldException,
		ScriptException,
		ExpressionEvalException {
		return compileCreateExpressionsWithException(expressions, sandbox, requirePath, verbose, getDefaultExprParser());
	}

	/**
	 * Compile create expressions with exception.
	 *
	 * @param expressions the expressions
	 * @param sandbox the sandbox
	 * @param requirePath the require path
	 * @param verbose the verbose
	 * @param defaultType the default type
	 * @return the list
	 * @throws MissingRequiredFieldException the missing required field exception
	 * @throws ScriptException the script exception
	 * @throws ExpressionEvalException the expression eval exception
	 */
	public static List<OutputExpression> compileCreateExpressionsWithException(
		List<OutputExpression> expressions,
		boolean sandbox,
		boolean requirePath,
		boolean verbose,
		@Nonnull ExprParser defaultType
	) throws MissingRequiredFieldException,
		ScriptException,
		ExpressionEvalException {
		if (expressions == null) {
			return null;
		}
		for (OutputExpression expr : expressions) {
			compileCreateExpression(expr, sandbox, requirePath, verbose, defaultType);
		}
		return expressions;
	}

	/**
	 * Compile expr.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param sandbox the sandbox
	 * @param expr the expr
	 * @return the t
	 */
	public static <T> T compileExpr(ExprParser type, boolean sandbox, String expr) {
		return compileExpr(type, sandbox, expr, true);
	}

	/**
	 * Compile expr.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param sandbox the sandbox
	 * @param expr the expr
	 * @param verbose the verbose
	 * @return the t
	 */
	public static <T> T compileExpr(ExprParser type, boolean sandbox, String expr, boolean verbose) {
		return compileExpr(type, sandbox, expr, verbose, null);
	}

	/**
	 * Compile expr.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param sandbox the sandbox
	 * @param expr the expr
	 * @param verbose the verbose
	 * @param config the config
	 * @return the t
	 */
	public static <T> T compileExpr(ExprParser type, boolean sandbox, String expr, boolean verbose, OutputExpression config) {
		try {
			return compileExprWithException(type, sandbox, expr, verbose, config);
		} catch (ScriptException e) {
			Logs.logError(LOG, e, "Issue compiling expression: type=%s sandbox=%s verbose=%s config=%s expr=%s", type, sandbox, verbose, config, expr);
			return null;
		}
	}

	/**
	 * Compile expr.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param sandbox the sandbox
	 * @param expr the expr
	 * @param creator the creator
	 * @return the t
	 */
	public static <T> T compileExpr(ExprParser type, boolean sandbox, String expr, ThrowingBiFunction<String, Boolean, T> creator) {
		return compileExpr(type, sandbox, expr, creator, true);
	}

	/**
	 * Compile expr.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param sandbox the sandbox
	 * @param expr the expr
	 * @param creator the creator
	 * @param verbose the verbose
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T compileExpr(ExprParser type, boolean sandbox, String expr, ThrowingBiFunction<String, Boolean, T> creator, boolean verbose) {
		return (T) CACHED_EXPRESSIONS.computeIfAbsent(type.name() + '@' + sandbox + '@' + expr, k -> {
			try (Timer.Context watch = StatusMonitor.timerStart(METRIC_REGISTRY, type.name(), METRIC_REGISTRY_COMPILE)) {
				return creator.apply(expr, sandbox);
			} catch (Exception e) {
				Logs.logLevel(LOG, verbose ? Level.ERROR : Level.WARN, e, "Issue compiling [%s] (sandbox:[%s]) expression [%s]", type, sandbox, expr);
				return null;
			}
		});
	}

	/**
	 * Compile expr.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param sandbox the sandbox
	 * @param expr the expr
	 * @param verbose the verbose
	 * @param config the config
	 * @return the t
	 * @throws ScriptException the script exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> T compileExprWithException(ExprParser type, boolean sandbox, String expr, boolean verbose, OutputExpression config) throws ScriptException {
		ThrowingBiFunction<String, Boolean, ?> creator;
		switch (type) {
		case JS:
			return (T) ScriptEngines.createJsEngineExpressionWithException(expr, sandbox, config);
		case PY:
			return (T) ScriptEngines.createPyEngineExpressionWithException(expr, sandbox, config);
		case OGNL:
			creator = OGNL_CREATOR;
			break;
		case SPEL:
			if ((config != null) && Checks.isNotEmpty(config.getGlobalVariablesResolved())) {
				SpelExpression compiled = (SpelExpression) SPEL_PARSER.parseExpression(expr);
				if (sandbox) {
					SandboxedSpelEvaluationContext context = createSpelSandboxedContext();
					context.setVariables(config.getGlobalVariablesResolved());
					compiled.setEvaluationContext(context);
				} else {
					StandardEvaluationContext context = createSpelNonSandboxedContext();
					context.setVariables(config.getGlobalVariablesResolved());
					compiled.setEvaluationContext(context);
				}
				return (T) compiled;
			}
			creator = SPEL_CREATOR;
			break;
		default:
			throw new IllegalArgumentException();
		}
		return (T) compileExpr(type, sandbox, expr, creator, verbose);
	}

	/**
	 * Creates a new Expr object.
	 *
	 * @param expr the expr
	 * @return the node
	 */
	public static Node createOgnlExpression(String expr) {
		return createOgnlExpression(expr, true);
	}

	/**
	 * Creates a new Expr object.
	 *
	 * @param expr the expr
	 * @param verbose the verbose
	 * @return the node
	 */
	public static Node createOgnlExpression(String expr, boolean verbose) {
		return compileExpr(ExprParser.OGNL, true, expr, OGNL_CREATOR, verbose);
	}

	/**
	 * Creates a new Expr object.
	 *
	 * @return the standard evaluation context
	 */
	public static final StandardEvaluationContext createSpelNonSandboxedContext() {
		StandardEvaluationContext context = registerSpelDefaultFunctions(new StandardEvaluationContext());
		context.setTypeLocator(NON_SANDBOXED_SPEL_TYPE_LOCATOR);
		try {
			Method method = PropertyParser.class.getMethod("parseAndReplaceWithProps", String.class);
			context.registerFunction(method.getName(), method);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return context;
	}

	/**
	 * Creates a new Expr object.
	 *
	 * @return the sandboxed spel evaluation context
	 */
	public static final SandboxedSpelEvaluationContext createSpelSandboxedContext() {
		return registerSpelDefaultFunctions(
			new SandboxedSpelEvaluationContext(
				null,
				SANDBOXED_SPEL_CONSTRUCTOR_RESOLVER,
				SANDBOXED_SPEL_METHOD_RESOLVER,
				SANDBOXED_SPEL_PROPERTY_ACCESSOR,
				SANDBOXED_SPEL_TYPE_LOCATOR
			)
		);
	}

	/**
	 * Eval.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param val the val
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @return the t
	 */
	public static <T> T eval(ExprParser type, Object val, Object expr, boolean sandbox) {
		return eval(type, val, expr, sandbox, true);
	}

	/**
	 * Eval.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param val the val
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @param verbose the verbose
	 * @return the t
	 */
	public static <T> T eval(ExprParser type, Object val, Object expr, boolean sandbox, boolean verbose) {
		switch (type) {
		case OGNL:
			return evalOgnl(val, expr == null ? null : expr instanceof Node ? (Node) expr : createOgnlExpression(expr.toString(), verbose), sandbox, verbose);
		case JS:
			return evalJs(val, expr == null ? null : expr instanceof JsEngineExpression ? (JsEngineExpression) expr : createJsEngineExpression(expr.toString(), sandbox), verbose);
		case PY:
			return evalPy(val, expr == null ? null : expr instanceof PyEngineExpression ? (PyEngineExpression) expr : createPyEngineExpression(expr.toString(), sandbox), verbose);
		case SPEL:
		default:
			return evalSpel(val, expr == null ? null : expr instanceof SpelExpression ? (SpelExpression) expr : getSpelExpression(expr.toString(), sandbox, verbose), verbose);
		}
	}

	/**
	 * Eval.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @return the t
	 */
	public static <T> T eval(Object val, Object expr) {
		return eval(val, expr, true);
	}

	/**
	 * Eval.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @param verbose the verbose
	 * @return the t
	 */
	public static <T> T eval(Object val, Object expr, boolean verbose) {
		if (expr instanceof Node) {
			return evalOgnl(val, (Node) expr, verbose);
		} else if (expr instanceof SpelExpression) {
			return evalSpel(val, (SpelExpression) expr, verbose);
		} else if (expr instanceof JsEngineExpression) {
			return ScriptEngines.evalJs(val, (JsEngineExpression) expr, verbose);
		} else if (expr instanceof PyEngineExpression) {
			return ScriptEngines.evalPy(val, (PyEngineExpression) expr, verbose);
		} else if (expr == null) {
			return null;
		}
		return eval(ExprParser.SPEL, val, expr, true, verbose);
	}

	/**
	 * Eval compiled.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param val the val
	 * @param expr the expr
	 * @param verbose the verbose
	 * @return the t
	 */
	public static <T> T evalCompiled(@Nonnull ExprParser type, Object val, Object expr, boolean verbose) {
		switch (type) {
		case SPEL:
			return evalSpel(val, (SpelExpression) expr, verbose);
		case OGNL:
			return evalOgnl(val, (Node) expr, verbose);
		case JS:
			return ScriptEngines.evalJs(val, (JsEngineExpression) expr, verbose);
		case PY:
			return evalPy(val, (PyEngineExpression) expr, verbose);
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Eval ognl.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @return the t
	 */
	public static <T> T evalOgnl(Object val, Node expr) {
		return evalOgnl(val, expr, true);
	}

	/**
	 * Eval ognl.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @return the t
	 */
	public static <T> T evalOgnl(Object val, Node expr, boolean sandbox) {
		return evalOgnl(val, expr, sandbox, true);
	}

	/**
	 * Eval ognl.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @param verbose the verbose
	 * @return the t
	 */
	public static <T> T evalOgnl(Object val, Node expr, boolean sandbox, boolean verbose) {
		try {
			return evalOgnlWithException(val, expr, sandbox, verbose);
		} catch (ExpressionEvalException e) {
			if (verbose && (logMissingOgnlProperties || !contains(e.getCause().getMessage(), "is null for"))) {
				Logs.logLevel(LOG, verbose ? Level.ERROR : Level.WARN, e, "Issue evaluating OGNL expression [%s] for value [%s]", expr, val);
			}
			return null;
		}
	}

	/**
	 * Eval ognl.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @return the t
	 */
	public static <T> T evalOgnl(Object val, String expr) {
		return evalOgnl(val, expr, true);
	}

	/**
	 * Eval ognl.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @return the t
	 */
	public static <T> T evalOgnl(Object val, String expr, boolean sandbox) {
		return evalOgnl(val, expr, sandbox, true);
	}

	/**
	 * Eval ognl.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @param verbose the verbose
	 * @return the t
	 */
	public static <T> T evalOgnl(Object val, String expr, boolean sandbox, boolean verbose) {
		return evalOgnl(val, createOgnlExpression(expr), sandbox, verbose);
	}

	/**
	 * Eval ognl with exception.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @param verbose the verbose
	 * @return the t
	 * @throws ExpressionEvalException the expression eval exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> T evalOgnlWithException(Object val, Node expr, boolean sandbox, boolean verbose) throws ExpressionEvalException {
		try (Timer.Context watch = OGNL_EVAL_TIMER.time()) {
			return sandbox ? (T) Ognl.getValue(expr, OGNL_SANDBOXED_CONTEXT, val) : (T) Ognl.getValue(expr, OGNL_NON_SANDBOXED_CONTEXT, val);
		} catch (OgnlException e) {
			throw new ExpressionEvalException(ExprParser.OGNL, e);
		}
	}

	/**
	 * Eval output expression.
	 *
	 * @param <T> the generic type
	 * @param expr the expr
	 * @param root the root
	 * @return the object
	 */
	public static <T> T evalOutputExpression(@Nonnull OutputExpression expr, Object root) {
		return evalCompiled(Checks.defaultIfNull(expr.getType(), defaultExprParser), root, expr.getCompiledExpression(), true);
	}

	/**
	 * Eval spel.
	 *
	 * @param <T> the generic type
	 * @param parent the parent
	 * @param exp the exp
	 * @return the t
	 */
	public static <T extends Object> T evalSpel(Object parent, SpelExpression exp) {
		return evalSpel(parent, exp, true);
	}

	/**
	 * Eval spel.
	 *
	 * @param <T> the generic type
	 * @param parent the parent
	 * @param exp the exp
	 * @param verbose the verbose
	 * @return the t
	 */
	public static <T extends Object> T evalSpel(Object parent, SpelExpression exp, boolean verbose) {
		try {
			return evalSpelWithException(parent, exp, verbose);
		} catch (ExpressionEvalException e) {
			Logs.logLevel(LOG, verbose ? Level.ERROR : Level.WARN, e, "Error evaluating SpEL expression [%s]!", exp.toStringAST());
			if (verbose) {
				Logs.logDebug(LOG, "Object to eval for SpEL expression was [%s].", parent);
			}
			return null;
		}
	}

	/**
	 * Gets the value from path.
	 *
	 * @param <T> the generic type
	 * @param parent the parent
	 * @param path the path
	 * @return the value from path
	 */
	public static <T extends Object> T evalSpel(Object parent, String path) {
		return evalSpel(parent, path, true);
	}

	/**
	 * Eval spel.
	 *
	 * @param <T> the generic type
	 * @param parent the parent
	 * @param path the path
	 * @param sandbox the sandbox
	 * @return the t
	 */
	public static <T extends Object> T evalSpel(Object parent, String path, boolean sandbox) {
		return evalSpel(parent, path, sandbox, true);
	}

	/**
	 * Gets the value from path.
	 *
	 * @param <T> the generic type
	 * @param parent the parent
	 * @param path the path
	 * @param sandbox the sandbox
	 * @param verbose the verbose
	 * @return the value from path
	 */
	public static <T extends Object> T evalSpel(Object parent, String path, boolean sandbox, boolean verbose) {
		return evalSpel(parent, getSpelExpression(path, sandbox), verbose);
	}

	/**
	 * Gets the value from expression.
	 *
	 * @param <T> the generic type
	 * @param parent the parent
	 * @param exp the exp
	 * @param verbose the verbose
	 * @return the value from expression
	 * @throws ExpressionEvalException the expression eval exception
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Object> T evalSpelWithException(Object parent, SpelExpression exp, boolean verbose) throws ExpressionEvalException {
		if (exp == null) {
			Logs.logWarn(LOG, "SpEL to eval was null!");
			return null;
		}
		try (Timer.Context watch = SPEL_EVAL_TIMER.time()) {
			return (T) exp.getValue(parent);
		} catch (Throwable e) {
			throw new ExpressionEvalException(ExprParser.SPEL, e);
		}
	}

	/**
	 * Evaluate output expressions.
	 *
	 * @param expressions the expressions
	 * @param rootObject the root object
	 * @return the map
	 */
	public static Map<String, Object> evaluateOutputExpressions(List<OutputExpression> expressions, Object rootObject) {
		return evaluateOutputExpressions(expressions, rootObject, true);
	}

	/**
	 * Evaluate output expressions.
	 *
	 * @param expressions the expressions
	 * @param rootObject the root object
	 * @param sandboxDefault the sandbox default
	 * @return the map
	 */
	public static Map<String, Object> evaluateOutputExpressions(List<OutputExpression> expressions, Object rootObject, boolean sandboxDefault) {
		return evaluateOutputExpressions(expressions, rootObject, sandboxDefault, null);
	}

	/**
	 * Evaluate output expressions.
	 *
	 * @param expressions the expressions
	 * @param rootObject the root object
	 * @param sandboxDefault the sandbox default
	 * @param context the context
	 * @return the map
	 */
	public static Map<String, Object> evaluateOutputExpressions(List<OutputExpression> expressions, Object rootObject, boolean sandboxDefault, Map<String, Object> context) {
		return evaluateOutputExpressions(expressions, rootObject, sandboxDefault, context, getDefaultExprParser());
	}

	/**
	 * Evaluate output expressions.
	 *
	 * @param expressions the expressions
	 * @param rootObject the root object
	 * @param sandboxDefault the sandbox default
	 * @param context the context
	 * @param defaultType the default type
	 * @return the map
	 */
	public static Map<String, Object> evaluateOutputExpressions(
		List<OutputExpression> expressions,
		Object rootObject,
		boolean sandboxDefault,
		Map<String, Object> context,
		@Nonnull ExprParser defaultType
	) {
		Map<String, Object> created = new LinkedHashMap<>();
		OutputExpressionRoot root = new OutputExpressionRoot(rootObject, created, context);
		compileCreateExpressions(expressions, sandboxDefault, true, true, defaultType).stream().filter(expr -> expr.getCompiledExpression() != null).forEach(expr -> {
			Object val = ExprFactory.eval(root, expr.getCompiledExpression(), false);
			if ((val != null) && expr.testReturnsPathKeyMap()) {
				(val instanceof Map ? ((Map<?, ?>) val) : Serializer.toMap(val, false)).forEach((key, mapVal) -> {
					Serializer.addPathKeyToMap(created, key.toString(), mapVal);
				});
			} else {
				Serializer.addPathKeyToMap(created, expr.getPath(), val);
			}
		});
		return created;
	}

	/**
	 * Evaluate output expressions with custom root.
	 *
	 * @param expressions the expressions
	 * @param root the root
	 * @return the map
	 */
	public static Map<String, Object> evaluateOutputExpressionsWithCustomRoot(List<OutputExpression> expressions, Object root) {
		return evaluateOutputExpressionsWithCustomRoot(expressions, root, true);
	}

	/**
	 * Evaluate output expressions with map.
	 *
	 * @param expressions the expressions
	 * @param root the root
	 * @param sandboxDefault the sandbox default
	 * @return the map
	 */
	public static Map<String, Object> evaluateOutputExpressionsWithCustomRoot(List<OutputExpression> expressions, Object root, boolean sandboxDefault) {
		Map<String, Object> created = new LinkedHashMap<>();
		if (root instanceof ExprContextRootWithResult) {
			((ExprContextRootWithResult) root).setResult(created);
		}
		compileCreateExpressions(expressions, sandboxDefault).stream().filter(expr -> expr.getCompiledExpression() != null).forEach(expr -> {
			Object val = ExprFactory.eval(root, expr.getCompiledExpression(), false);
			if ((val != null) && expr.testReturnsPathKeyMap()) {
				(val instanceof Map ? ((Map<?, ?>) val) : Serializer.toMap(val, false)).forEach((key, mapVal) -> {
					Serializer.addPathKeyToMap(created, key.toString(), mapVal);
				});
			} else if (val != null) {
				Serializer.addPathKeyToMap(created, expr.getPath(), val);
			}
		});
		return created;
	}

	/**
	 * Evaluate output expressions with exception.
	 *
	 * @param expressions the expressions
	 * @param rootObject the root object
	 * @param sandboxDefault the sandbox default
	 * @param context the context
	 * @param defaultType the default type
	 * @return the map
	 * @throws ExpressionEvalException the expression eval exception
	 * @throws MissingRequiredFieldException the missing required field exception
	 * @throws ScriptException the script exception
	 */
	public static Map<String, Object> evaluateOutputExpressionsWithException(
		List<OutputExpression> expressions,
		Object rootObject,
		boolean sandboxDefault,
		Map<String, Object> context,
		@Nonnull ExprParser defaultType
	) throws ExpressionEvalException,
		MissingRequiredFieldException,
		ScriptException {
		Map<String, Object> created = new LinkedHashMap<>();
		OutputExpressionRoot root = new OutputExpressionRoot(rootObject, created, context);
		for (OutputExpression expr : compileCreateExpressionsWithException(expressions, sandboxDefault, true, true, defaultType)) {
			Object val = evalWithException(root, expr.getCompiledExpression(), false);
			if ((val != null) && expr.testReturnsPathKeyMap()) {
				(val instanceof Map ? ((Map<?, ?>) val) : Serializer.toMap(val, false)).forEach((key, mapVal) -> {
					Serializer.addPathKeyToMap(created, key.toString(), mapVal);
				});
			} else {
				Serializer.addPathKeyToMap(created, expr.getPath(), val);
			}
		}
		return created;
	}

	/**
	 * Eval with exception.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param val the val
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @param verbose the verbose
	 * @return the t
	 * @throws ExpressionEvalException the expression eval exception
	 */
	public static <T> T evalWithException(ExprParser type, Object val, Object expr, boolean sandbox, boolean verbose) throws ExpressionEvalException {
		switch (type) {
		case OGNL:
			return evalOgnlWithException(val, expr == null ? null : expr instanceof Node ? (Node) expr : createOgnlExpression(expr.toString(), verbose), sandbox, verbose);
		case JS:
			return ScriptEngines.evalJsWithException(
				val,
				expr == null ? null : expr instanceof JsEngineExpression ? (JsEngineExpression) expr : createJsEngineExpression(expr.toString(), sandbox),
				verbose
			);
		case PY:
			return ScriptEngines.evalPyWithException(
				val,
				expr == null ? null : expr instanceof PyEngineExpression ? (PyEngineExpression) expr : createPyEngineExpression(expr.toString(), sandbox),
				verbose
			);
		case SPEL:
		default:
			return evalSpelWithException(val, expr == null ? null : expr instanceof SpelExpression ? (SpelExpression) expr : getSpelExpression(expr.toString(), sandbox, verbose), verbose);
		}
	}

	/**
	 * Eval with exception.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @param verbose the verbose
	 * @return the t
	 * @throws ExpressionEvalException the expression eval exception
	 */
	public static <T> T evalWithException(Object val, Object expr, boolean verbose) throws ExpressionEvalException {
		if (expr instanceof Node) {
			return evalOgnlWithException(val, (Node) expr, true, verbose);
		} else if (expr instanceof SpelExpression) {
			return evalSpelWithException(val, (SpelExpression) expr, verbose);
		} else if (expr instanceof JsEngineExpression) {
			return ScriptEngines.evalJsWithException(val, (JsEngineExpression) expr, verbose);
		} else if (expr instanceof PyEngineExpression) {
			return ScriptEngines.evalPyWithException(val, (PyEngineExpression) expr, verbose);
		} else if (expr == null) {
			return null;
		}
		return evalWithException(ExprParser.SPEL, val, expr, true, verbose);
	}

	/**
	 * Gets the cached expressions count.
	 *
	 * @return the cached expressions count
	 */
	public static int getCachedExpressionsCount() {
		return CACHED_EXPRESSIONS.size();
	}

	/**
	 * Gets the default expr parser.
	 *
	 * @return the defaultExprParser
	 */
	public static ExprParser getDefaultExprParser() {
		return defaultExprParser;
	}

	/**
	 * Gets the spel expression.
	 *
	 * @param path the path
	 * @return the spel expression
	 */
	public static SpelExpression getSpelExpression(String path) {
		return getSpelExpression(path, true);
	}

	/**
	 * Gets the spel expression.
	 *
	 * @param path the path
	 * @param sandbox the sandbox
	 * @return the spel expression
	 */
	public static SpelExpression getSpelExpression(String path, boolean sandbox) {
		return getSpelExpression(path, sandbox, true);
	}

	/**
	 * Gets the spel expression.
	 *
	 * @param path the path
	 * @param sandbox the sandbox
	 * @param verbose the verbose
	 * @return the spel expression
	 */
	public static SpelExpression getSpelExpression(String path, boolean sandbox, boolean verbose) {
		return compileExpr(ExprParser.SPEL, sandbox, path, SPEL_CREATOR, verbose);
	}

	/**
	 * Gets the value from path.
	 *
	 * @param <T> the generic type
	 * @param parent the parent
	 * @param path the path
	 * @return the value from path
	 */
	public static <T extends Object> T getValueFromPath(Object parent, String path) {
		return getValueFromPath(parent, path, true);
	}

	/**
	 * Gets the value from path.
	 *
	 * @param <T> the generic type
	 * @param parent the parent
	 * @param path the path
	 * @param sandbox the sandbox
	 * @return the value from path
	 */
	public static <T extends Object> T getValueFromPath(Object parent, String path, boolean sandbox) {
		return getValueFromPath(parent, path, sandbox, true);
	}

	/**
	 * Gets the value from path.
	 *
	 * @param <T> the generic type
	 * @param parent the parent
	 * @param path the path
	 * @param sandbox the sandbox
	 * @param verbose the verbose
	 * @return the value from path
	 */
	public static <T extends Object> T getValueFromPath(Object parent, String path, boolean sandbox, boolean verbose) {
		switch (defaultExprParser) {
		case JS:
			return evalJs(parent, createJsEngineExpression(path, sandbox), verbose);
		case PY:
			return evalPy(parent, createPyEngineExpression(path, sandbox), verbose);
		case OGNL:
			return evalOgnl(parent, path, sandbox, verbose);
		case SPEL:
		default:
			return evalSpel(parent, path, sandbox, verbose);
		}
	}

	/**
	 * Checks if is log missing ognl properties.
	 *
	 * @return the logMissingOgnlProperties
	 */
	public static boolean isLogMissingOgnlProperties() {
		return logMissingOgnlProperties;
	}

	/**
	 * Checks if is spel string.
	 *
	 * @param test the test
	 * @return the pair
	 */
	public static Pair<Boolean, String> isSpelString(String test) {
		if (StringUtils.isBlank(test)) {
			return Pair.of(false, null);
		}
		Matcher matcher = SPEL_REGEX.matcher(test);
		boolean spel = matcher.find();
		return Pair.of(spel, spel ? matcher.group(1) : test);
	}

	/**
	 * Log create timer.
	 *
	 * @param watch the watch
	 * @param type the type
	 */
	public static void logCreateTimer(Stopwatch watch, ExprParser type) {
		Logs.logTimer(LOG, watch, String.format("Expression Create - %s", type), Level.TRACE);
	}

	/**
	 * Log eval timer.
	 *
	 * @param watch the watch
	 * @param type the type
	 */
	public static void logEvalTimer(Stopwatch watch, ExprParser type) {
		Logs.logTimer(LOG, watch, String.format("Expression Eval - %s", type), Level.TRACE);
	}

	/**
	 * Parses the and consume expressions.
	 *
	 * @param expressions the expressions
	 * @param consumer the consumer
	 * @return true, if successful
	 */
	public static boolean parseAndConsumeExpressions(List<String> expressions, Consumer<List<Node>> consumer) {
		if (expressions == null) {
			consumer.accept(null);
			return true;
		}
		AtomicBoolean success = new AtomicBoolean(true);
		consumer.accept(expressions.stream().map(ExprFactory::createOgnlExpression).filter(expr -> {
			if (expr == null) {
				success.set(false);
				return false;
			}
			return true;
		}).collect(Collectors.toList()));
		return success.get();
	}

	/**
	 * Register default imports.
	 *
	 * @param <T> the generic type
	 * @param locator the locator
	 * @return the standard type locator
	 */
	public static <T extends StandardTypeLocator> T registerDefaultSpringImports(T locator) {
		locator.registerImport("org.apache.commons.collections4");
		locator.registerImport("org.apache.commons.lang3");
		locator.registerImport("org.apache.commons.lang3.math");
		return locator;
	}

	/**
	 * Register spel default functions.
	 *
	 * @param <T> the generic type
	 * @param context the context
	 * @return the t
	 */
	public static <T extends StandardEvaluationContext> T registerSpelDefaultFunctions(@Nonnull T context) {
		Stream.of(ExprFunctions.class.getDeclaredMethods(), Checks.class.getDeclaredMethods()).map(Arrays::asList).flatMap(Collection::stream).forEach(method -> {
			context.registerFunction(method.getName(), method);
		});
		return context;
	}

	/**
	 * Sets the default expr parser.
	 *
	 * @param defaultExprParser the defaultExprParser to set
	 */
	public static void setDefaultExprParser(ExprParser defaultExprParser) {
		ExprFactory.defaultExprParser = defaultExprParser;
	}

	/**
	 * Sets the log missing ognl properties.
	 *
	 * @param logMissingOgnlProperties the logMissingOgnlProperties to set
	 */
	public static void setLogMissingOgnlProperties(boolean logMissingOgnlProperties) {
		ExprFactory.logMissingOgnlProperties = logMissingOgnlProperties;
	}

	/**
	 * Sets the sandboxed spel constructor resolver config.
	 *
	 * @param sandboxedSpelConstructorResolverConfig the new sandboxed spel constructor resolver config
	 */
	public static void setSandboxedSpelConstructorResolverConfig(SandboxedContextConfig sandboxedSpelConstructorResolverConfig) {
		SANDBOXED_CONTEXT.getConstructorResolver().setConfig(sandboxedSpelConstructorResolverConfig);
	}

	/**
	 * Sets the sandboxed spel method resolver config.
	 *
	 * @param sandboxedSpelMethodResolverConfig the new sandboxed spel method resolver config
	 */
	public static void setSandboxedSpelMethodResolverConfig(SandboxedContextConfig sandboxedSpelMethodResolverConfig) {
		SANDBOXED_CONTEXT.getMethodResolver().setConfig(sandboxedSpelMethodResolverConfig);
	}

	/**
	 * Sets the sandboxed spel property accessor config.
	 *
	 * @param sandboxedSpelPropertyAccessorConfig the new sandboxed spel property accessor config
	 */
	public static void setSandboxedSpelPropertyAccessorConfig(SandboxedContextConfig sandboxedSpelPropertyAccessorConfig) {
		SANDBOXED_CONTEXT.getPropertyAccessor().setConfig(sandboxedSpelPropertyAccessorConfig);
	}

	/**
	 * Sets the sandboxed spel type locator config.
	 *
	 * @param sandboxedSpelTypeLocatorConfig the new sandboxed spel type locator config
	 */
	public static void setSandboxedSpelTypeLocatorConfig(SandboxedContextConfig sandboxedSpelTypeLocatorConfig) {
		SANDBOXED_CONTEXT.getTypeLocator().setConfig(sandboxedSpelTypeLocatorConfig);
	}

}
