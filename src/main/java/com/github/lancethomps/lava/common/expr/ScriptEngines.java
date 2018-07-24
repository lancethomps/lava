package com.github.lancethomps.lava.common.expr;

import static com.github.lancethomps.lava.common.expr.ExprParser.JS;
import static com.github.lancethomps.lava.common.expr.ExprParser.PY;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.metrics.StatusMonitor;
import com.github.lancethomps.lava.common.ser.OutputExpression;
import com.github.lancethomps.lava.common.time.Stopwatch;
import com.google.common.collect.Sets;

import io.dropwizard.metrics5.Timer;

/**
 * The Class ScriptEngines.
 */
public class ScriptEngines {

	/** The allow sandboxed. */
	private static boolean allowSandboxed;

	/** The current engine. */
	private static final Map<ExprParser, AtomicInteger> CURRENT_NUM_ENGINES = new HashMap<>();

	/** The Constant CUSTOM_ENGINE_INITIALIZERS. */
	private static final Map<ExprParser, Function<ScriptEngineManager, ScriptEngine>> CUSTOM_ENGINE_INITIALIZERS = new ConcurrentHashMap<>();

	/** The engine manager. */
	private static ScriptEngineManager engineManager;

	/** The Constant JS_COMPILE_TIMER. */
	private static final Timer JS_COMPILE_TIMER = StatusMonitor.timer(ExprFactory.METRIC_REGISTRY, ExprParser.JS.name(), ExprFactory.METRIC_REGISTRY_COMPILE);

	/** The Constant JS_EVAL_TIMER. */
	private static final Timer JS_EVAL_TIMER = StatusMonitor.timer(ExprFactory.METRIC_REGISTRY, ExprParser.JS.name(), ExprFactory.METRIC_REGISTRY_EVAL);

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(ScriptEngines.class);

	/** The max engines. */
	private static int maxEngines = 4;

	/** The Constant PY_COMPILE_TIMER. */
	private static final Timer PY_COMPILE_TIMER = StatusMonitor.timer(ExprFactory.METRIC_REGISTRY, ExprParser.PY.name(), ExprFactory.METRIC_REGISTRY_COMPILE);

	/** The Constant PY_EVAL_TIMER. */
	private static final Timer PY_EVAL_TIMER = StatusMonitor.timer(ExprFactory.METRIC_REGISTRY, ExprParser.PY.name(), ExprFactory.METRIC_REGISTRY_EVAL);

	/** The Constant SHARED_ENGINES. */
	private static final Map<ExprParser, ConcurrentLinkedQueue<ScriptEngine>> SCRIPT_ENGINES = new HashMap<>();

	/** The Constant SHARED_ENGINES_SET. */
	private static final Map<ExprParser, Set<ScriptEngine>> SHARED_ENGINES_SET = new HashMap<>();

	static {
		if (Checks.isBlank(System.getProperty("nashorn.args"))) {
			System.setProperty("nashorn.args", "-scripting --language=es6");
		}
	}

	static {
		CURRENT_NUM_ENGINES.put(JS, new AtomicInteger(0));
		CURRENT_NUM_ENGINES.put(PY, new AtomicInteger(0));
		SCRIPT_ENGINES.put(JS, new ConcurrentLinkedQueue<>());
		SCRIPT_ENGINES.put(PY, new ConcurrentLinkedQueue<>());
		SHARED_ENGINES_SET.put(JS, Sets.newConcurrentHashSet());
		SHARED_ENGINES_SET.put(PY, Sets.newConcurrentHashSet());
	}

	/**
	 * Adds the custom engine initializer.
	 *
	 * @param type the type
	 * @param function the function
	 */
	public static void addCustomEngineInitializer(ExprParser type, Function<ScriptEngineManager, ScriptEngine> function) {
		CUSTOM_ENGINE_INITIALIZERS.put(type, function);
		SHARED_ENGINES_SET.get(type).clear();
		SCRIPT_ENGINES.get(type).clear();
		CURRENT_NUM_ENGINES.get(type).set(0);
	}

	/**
	 * Compile script.
	 *
	 * @param script the script
	 * @param sandbox the sandbox
	 * @param verbose the verbose
	 * @return the compiled script
	 */
	public static CompiledScript compileJsScript(String script, boolean sandbox, boolean verbose) {
		return compileScript(JS, script, sandbox, verbose);
	}

	/**
	 * Compile py script.
	 *
	 * @param script the script
	 * @param sandbox the sandbox
	 * @param verbose the verbose
	 * @return the compiled script
	 */
	public static CompiledScript compilePyScript(String script, boolean sandbox, boolean verbose) {
		return compileScript(PY, script, sandbox, verbose);
	}

	/**
	 * Creates a new JsEngine object.
	 *
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @return the js engine expression
	 */
	public static JsEngineExpression createJsEngineExpression(String expr, boolean sandbox) {
		return createJsEngineExpression(expr, sandbox, null);
	}

	/**
	 * Creates a new JsEngine object.
	 *
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @param config the config
	 * @return the js engine expression
	 */
	public static JsEngineExpression createJsEngineExpression(String expr, boolean sandbox, @Nullable OutputExpression config) {
		CompiledScript compiled;
		if ((config != null) && config.testCompile()) {
			compiled = compileJsScript(expr, sandbox, true);
		} else {
			compiled = null;
		}
		return new JsEngineExpression(expr, sandbox, compiled, config == null ? null : config.getGlobalVariablesResolved());
	}

	/**
	 * Creates the js engine expression with exception.
	 *
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @param config the config
	 * @return the js engine expression
	 * @throws ScriptException the script exception
	 */
	public static JsEngineExpression createJsEngineExpressionWithException(String expr, boolean sandbox, @Nullable OutputExpression config) throws ScriptException {
		CompiledScript compiled;
		if ((config != null) && config.testCompile()) {
			compiled = compileScriptWithException(JS, expr, sandbox, true);
		} else {
			compiled = null;
		}
		return new JsEngineExpression(expr, sandbox, compiled, config == null ? null : config.getGlobalVariablesResolved());
	}

	/**
	 * Creates a new JsEngine object.
	 *
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @return the py engine expression
	 */
	public static PyEngineExpression createPyEngineExpression(String expr, boolean sandbox) {
		return createPyEngineExpression(expr, sandbox, null);
	}

	/**
	 * Creates a new JsEngine object.
	 *
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @param config the config
	 * @return the py engine expression
	 */
	public static PyEngineExpression createPyEngineExpression(String expr, boolean sandbox, @Nullable OutputExpression config) {
		CompiledScript compiled;
		if ((config != null) && config.testCompile()) {
			compiled = compilePyScript(expr, sandbox, true);
		} else {
			compiled = null;
		}
		return new PyEngineExpression(expr, sandbox, compiled, config == null ? null : config.getGlobalVariablesResolved());
	}

	/**
	 * Creates the py engine expression with exception.
	 *
	 * @param expr the expr
	 * @param sandbox the sandbox
	 * @param config the config
	 * @return the py engine expression
	 * @throws ScriptException the script exception
	 */
	public static PyEngineExpression createPyEngineExpressionWithException(String expr, boolean sandbox, @Nullable OutputExpression config) throws ScriptException {
		CompiledScript compiled;
		if ((config != null) && config.testCompile()) {
			compiled = compileScriptWithException(PY, expr, sandbox, true);
		} else {
			compiled = null;
		}
		return new PyEngineExpression(expr, sandbox, compiled, config == null ? null : config.getGlobalVariablesResolved());
	}

	/**
	 * Eval js.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @param verbose the verbose
	 * @return the t
	 */
	public static <T extends Object> T evalJs(Object val, JsEngineExpression expr, boolean verbose) {
		return evalExpression(JS, val, expr, verbose);
	}

	/**
	 * Eval js.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @param verbose the verbose
	 * @return the t
	 */
	public static <T extends Object> T evalJs(Object val, String expr, boolean verbose) {
		return evalJs(val, createJsEngineExpression(expr, true), verbose);
	}

	/**
	 * Eval js with exception.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @param verbose the verbose
	 * @return the t
	 * @throws ExpressionEvalException the expression eval exception
	 */
	public static <T extends Object> T evalJsWithException(Object val, JsEngineExpression expr, boolean verbose) throws ExpressionEvalException {
		return evalExpressionWithException(JS, val, expr, verbose, null);
	}

	/**
	 * Eval py.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @param verbose the verbose
	 * @return the t
	 */
	public static <T extends Object> T evalPy(Object val, PyEngineExpression expr, boolean verbose) {
		return evalExpression(PY, val, expr, verbose, "result");
	}

	/**
	 * Eval py.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @param verbose the verbose
	 * @return the t
	 */
	public static <T extends Object> T evalPy(Object val, String expr, boolean verbose) {
		return evalPy(val, createPyEngineExpression(expr, true), verbose);
	}

	/**
	 * Eval py with exception.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param expr the expr
	 * @param verbose the verbose
	 * @return the t
	 * @throws ExpressionEvalException the expression eval exception
	 */
	public static <T extends Object> T evalPyWithException(Object val, PyEngineExpression expr, boolean verbose) throws ExpressionEvalException {
		return evalExpressionWithException(PY, val, expr, verbose, "result");
	}

	/**
	 * Gets the max engines.
	 *
	 * @return the maxEngines
	 */
	public static int getMaxEngines() {
		return maxEngines;
	}

	/**
	 * @return the allowSandboxed
	 */
	public static boolean isAllowSandboxed() {
		return allowSandboxed;
	}

	/**
	 * Reset engines.
	 */
	public static void resetEngines() {
		SHARED_ENGINES_SET.values().forEach(Set::clear);
		for (Entry<ExprParser, ConcurrentLinkedQueue<ScriptEngine>> enginesEntry : SCRIPT_ENGINES.entrySet()) {
			enginesEntry.getValue().clear();
			CURRENT_NUM_ENGINES.get(enginesEntry.getKey()).set(0);
		}
	}

	/**
	 * @param allowSandboxed the allowSandboxed to set
	 */
	public static void setAllowSandboxed(boolean allowSandboxed) {
		ScriptEngines.allowSandboxed = allowSandboxed;
	}

	/**
	 * Sets the max engines.
	 *
	 * @param maxEngines the maxEngines to set
	 */
	public static void setMaxEngines(int maxEngines) {
		ScriptEngines.maxEngines = maxEngines;
	}

	/**
	 * Compile script.
	 *
	 * @param type the type
	 * @param script the script
	 * @param sandbox the sandbox
	 * @param verbose the verbose
	 * @return the compiled script
	 */
	private static CompiledScript compileScript(ExprParser type, String script, boolean sandbox, boolean verbose) {
		try {
			return compileScriptWithException(type, script, sandbox, verbose);
		} catch (Throwable e) {
			Logs.logLevel(LOG, verbose ? Level.ERROR : Level.WARN, e, "Error compiling script: type=%s %s", type, Logs.getSplunkKeyValueString("script", script));
			return null;
		}
	}

	/**
	 * Compile script with exception.
	 *
	 * @param type the type
	 * @param script the script
	 * @param sandbox the sandbox
	 * @param verbose the verbose
	 * @return the compiled script
	 * @throws ScriptException the script exception
	 */
	private static CompiledScript compileScriptWithException(ExprParser type, String script, boolean sandbox, boolean verbose) throws ScriptException {
		ScriptEngine engine = null;
		try (Timer.Context watch = getCompileTimer(type).time()) {
			if (sandbox && !allowSandboxed) {
				throw new SecurityException("Sandboxed script engine expressions not allowed.");
			}
			engine = getInitializedEngine(type);
			if (engine instanceof Compilable) {
				CompiledScript compiledScript = ((Compilable) engine).compile(script);
				return compiledScript;
			}
			return null;
		} finally {
			releaseEngine(type, engine);
		}
	}

	/**
	 * Eval expression.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param val the val
	 * @param expr the expr
	 * @param verbose the verbose
	 * @return the t
	 */
	private static <T extends Object> T evalExpression(ExprParser type, Object val, ScriptEngineExpression expr, boolean verbose) {
		return evalExpression(type, val, expr, verbose, null);
	}

	/**
	 * Eval expression.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param val the val
	 * @param expr the expr
	 * @param verbose the verbose
	 * @param resultVariable the result variable
	 * @return the t
	 */
	private static <T extends Object> T evalExpression(ExprParser type, Object val, ScriptEngineExpression expr, boolean verbose, String resultVariable) {
		try {
			return evalExpressionWithException(type, val, expr, verbose, resultVariable);
		} catch (ExpressionEvalException e) {
			Logs.logLevel(LOG, verbose ? Level.ERROR : Level.WARN, e, "Error evaluating expression: type=%s %s", type, Logs.getSplunkKeyValueString("expr", expr.getExpression()));
			if (verbose) {
				Logs.logDebug(LOG, "Object to eval for expression was: %s", val);
			}
			return null;
		}
	}

	/**
	 * Eval js.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param val the val
	 * @param expr the expr
	 * @param verbose the verbose
	 * @param resultVariable the result variable
	 * @return the t
	 * @throws ExpressionEvalException the expression eval exception
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Object> T evalExpressionWithException(ExprParser type, Object val, ScriptEngineExpression expr, boolean verbose, String resultVariable)
		throws ExpressionEvalException {
		ScriptEngine engine = null;
		try (Timer.Context watch = getEvalTimer(type).time()) {
			if (expr.isSandbox() && !allowSandboxed) {
				throw new SecurityException("Sandboxed script engine expressions not allowed.");
			}

			engine = getInitializedEngine(type);
			Bindings bindings = engine.createBindings();
			if (expr.getGlobalVariables() != null) {
				bindings.putAll(expr.getGlobalVariables());
			}
			bindings.put("root", val);
			if (expr.getCompiled() != null) {
				return (T) expr.getCompiled().eval(bindings);
			}

			if (resultVariable == null) {
				return (T) engine.eval(expr.getExpression(), bindings);
			}
			engine.eval(expr.getExpression(), bindings);
			return (T) bindings.get(resultVariable);
		} catch (Throwable e) {
			throw new ExpressionEvalException(type, e);
		} finally {
			releaseEngine(type, engine);
		}
	}

	/**
	 * Gets the compile timer.
	 *
	 * @param type the type
	 * @return the compile timer
	 */
	private static Timer getCompileTimer(ExprParser type) {
		switch (type) {
		case JS:
			return JS_COMPILE_TIMER;
		case PY:
			return PY_COMPILE_TIMER;
		default:
			throw new IllegalArgumentException(String.format("Expression type %s is not supported.", type));
		}
	}

	/**
	 * Gets the eval timer.
	 *
	 * @param type the type
	 * @return the eval timer
	 */
	private static Timer getEvalTimer(ExprParser type) {
		switch (type) {
		case JS:
			return JS_EVAL_TIMER;
		case PY:
			return PY_EVAL_TIMER;
		default:
			throw new IllegalArgumentException(String.format("Expression type %s is not supported.", type));
		}
	}

	/**
	 * Gets the initialized engine.
	 *
	 * @param type the type
	 * @return the initialized engine
	 */
	private static ScriptEngine getInitializedEngine(ExprParser type) {
		ScriptEngine engine = SCRIPT_ENGINES.get(type).poll();
		if (engine == null) {
			AtomicInteger currentNumEngines = CURRENT_NUM_ENGINES.get(type);
			if (currentNumEngines.get() >= maxEngines) {
				return getNewScriptEngine(type);
			}
			engine = getNewScriptEngine(type);
			SHARED_ENGINES_SET.get(type).add(engine);
			currentNumEngines.incrementAndGet();
		}
		return engine;
	}

	/**
	 * Inits the script engine.
	 *
	 * @param type the type
	 * @return the script engine
	 */
	private static ScriptEngine getNewScriptEngine(ExprParser type) {
		if (engineManager == null) {
			engineManager = initScriptEngineManager(new ScriptEngineManager());
		}
		Stopwatch watch = Stopwatch.createAndStart();
		try {
			ScriptEngine engine;
			if (CUSTOM_ENGINE_INITIALIZERS.containsKey(type)) {
				engine = CUSTOM_ENGINE_INITIALIZERS.get(type).apply(engineManager);
			} else {
				engine = engineManager.getEngineByName(type.getEngineName());
			}
			return engine;
		} finally {
			Logs.logTimer(LOG, watch, "ScriptEngine Init");
		}
	}

	/**
	 * Inits the script engine manager.
	 *
	 * @param engineManager the engine manager
	 * @return the script engine manager
	 */
	private static ScriptEngineManager initScriptEngineManager(ScriptEngineManager engineManager) {
		Stopwatch watch = Stopwatch.createAndStart();
		try {
			Bindings global = engineManager.getBindings();
			global.put("average", (Function<Collection<Number>, Double>) ExprFunctions::average);
			global.put("max", (Function<Collection<Number>, Double>) ExprFunctions::max);
			global.put("min", (Function<Collection<Number>, Double>) ExprFunctions::min);
			global.put("sum", (Function<Collection<Number>, Double>) ExprFunctions::sum);
			global.put("WITHIN_JAVA", true);
			return engineManager;
		} finally {
			Logs.logTimer(LOG, watch, "ScriptEngineManager Init");
		}
	}

	/**
	 * Release engine.
	 *
	 * @param type the type
	 * @param engine the engine
	 */
	private static void releaseEngine(ExprParser type, ScriptEngine engine) {
		if (engine != null) {
			if (SHARED_ENGINES_SET.get(type).contains(engine)) {
				SCRIPT_ENGINES.get(type).add(engine);
			}
		}
	}

}
