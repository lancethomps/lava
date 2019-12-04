package com.lancethomps.lava.common.expr;

import static com.lancethomps.lava.common.expr.ExprParser.JS;
import static com.lancethomps.lava.common.expr.ExprParser.PY;

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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.metrics.StatusMonitor;
import com.lancethomps.lava.common.ser.OutputExpression;
import com.lancethomps.lava.common.time.Stopwatch;

import io.dropwizard.metrics5.Timer;

public class ScriptEngines {

  private static final Map<ExprParser, AtomicInteger> CURRENT_NUM_ENGINES = new HashMap<>(ImmutableMap.of(
    JS, new AtomicInteger(0),
    PY, new AtomicInteger(0)
  ));
  private static final Map<ExprParser, Function<ScriptEngineManager, ScriptEngine>> CUSTOM_ENGINE_INITIALIZERS = new ConcurrentHashMap<>();
  private static final Timer JS_COMPILE_TIMER =
    StatusMonitor.timer(ExprFactory.METRIC_REGISTRY, ExprParser.JS.name(), ExprFactory.METRIC_REGISTRY_COMPILE);
  private static final Timer JS_EVAL_TIMER = StatusMonitor.timer(ExprFactory.METRIC_REGISTRY, ExprParser.JS.name(), ExprFactory.METRIC_REGISTRY_EVAL);
  private static final Logger LOG = Logger.getLogger(ScriptEngines.class);
  private static final Timer PY_COMPILE_TIMER =
    StatusMonitor.timer(ExprFactory.METRIC_REGISTRY, ExprParser.PY.name(), ExprFactory.METRIC_REGISTRY_COMPILE);
  private static final Timer PY_EVAL_TIMER = StatusMonitor.timer(ExprFactory.METRIC_REGISTRY, ExprParser.PY.name(), ExprFactory.METRIC_REGISTRY_EVAL);
  private static final Map<ExprParser, ConcurrentLinkedQueue<ScriptEngine>> SCRIPT_ENGINES = new HashMap<>(ImmutableMap.of(
    JS, new ConcurrentLinkedQueue<>(),
    PY, new ConcurrentLinkedQueue<>()
  ));
  private static final Map<ExprParser, Set<ScriptEngine>> SHARED_ENGINES_SET = new HashMap<>(ImmutableMap.of(
    JS, Sets.newConcurrentHashSet(),
    PY, Sets.newConcurrentHashSet()
  ));
  private static boolean allowSandboxed;
  private static ScriptEngineManager engineManager;
  private static int maxEngines = 4;

  static {
    if (Checks.isBlank(System.getProperty("nashorn.args"))) {
      System.setProperty("nashorn.args", "-scripting --language=es6");
    }
  }

  public static void addCustomEngineInitializer(ExprParser type, Function<ScriptEngineManager, ScriptEngine> function) {
    CUSTOM_ENGINE_INITIALIZERS.put(type, function);
    SHARED_ENGINES_SET.get(type).clear();
    SCRIPT_ENGINES.get(type).clear();
    CURRENT_NUM_ENGINES.get(type).set(0);
  }

  public static CompiledScript compileJsScript(String script, boolean sandbox, boolean verbose) {
    return compileScript(JS, script, sandbox, verbose);
  }

  public static CompiledScript compilePyScript(String script, boolean sandbox, boolean verbose) {
    return compileScript(PY, script, sandbox, verbose);
  }

  public static JsEngineExpression createJsEngineExpression(String expr, boolean sandbox) {
    return createJsEngineExpression(expr, sandbox, null);
  }

  public static JsEngineExpression createJsEngineExpression(String expr, boolean sandbox, @Nullable OutputExpression config) {
    CompiledScript compiled;
    if ((config != null) && config.testCompile()) {
      compiled = compileJsScript(expr, sandbox, true);
    } else {
      compiled = null;
    }
    return new JsEngineExpression(expr, sandbox, compiled, config == null ? null : config.getGlobalVariablesResolved());
  }

  public static JsEngineExpression createJsEngineExpressionWithException(String expr, boolean sandbox, @Nullable OutputExpression config)
    throws ScriptException {
    CompiledScript compiled;
    if ((config != null) && config.testCompile()) {
      compiled = compileScriptWithException(JS, expr, sandbox, true);
    } else {
      compiled = null;
    }
    return new JsEngineExpression(expr, sandbox, compiled, config == null ? null : config.getGlobalVariablesResolved());
  }

  public static PyEngineExpression createPyEngineExpression(String expr, boolean sandbox) {
    return createPyEngineExpression(expr, sandbox, null);
  }

  public static PyEngineExpression createPyEngineExpression(String expr, boolean sandbox, @Nullable OutputExpression config) {
    CompiledScript compiled;
    if ((config != null) && config.testCompile()) {
      compiled = compilePyScript(expr, sandbox, true);
    } else {
      compiled = null;
    }
    return new PyEngineExpression(expr, sandbox, compiled, config == null ? null : config.getGlobalVariablesResolved());
  }

  public static PyEngineExpression createPyEngineExpressionWithException(String expr, boolean sandbox, @Nullable OutputExpression config)
    throws ScriptException {
    CompiledScript compiled;
    if ((config != null) && config.testCompile()) {
      compiled = compileScriptWithException(PY, expr, sandbox, true);
    } else {
      compiled = null;
    }
    return new PyEngineExpression(expr, sandbox, compiled, config == null ? null : config.getGlobalVariablesResolved());
  }

  public static <T extends Object> T evalJs(Object val, JsEngineExpression expr, boolean verbose) {
    return evalExpression(JS, val, expr, verbose);
  }

  public static <T extends Object> T evalJs(Object val, String expr, boolean verbose) {
    return evalJs(val, createJsEngineExpression(expr, true), verbose);
  }

  public static <T extends Object> T evalJsWithException(Object val, JsEngineExpression expr, boolean verbose) throws ExpressionEvalException {
    return evalExpressionWithException(JS, val, expr, verbose, null);
  }

  public static <T extends Object> T evalPy(Object val, PyEngineExpression expr, boolean verbose) {
    return evalExpression(PY, val, expr, verbose, "result");
  }

  public static <T extends Object> T evalPy(Object val, String expr, boolean verbose) {
    return evalPy(val, createPyEngineExpression(expr, true), verbose);
  }

  public static <T extends Object> T evalPyWithException(Object val, PyEngineExpression expr, boolean verbose) throws ExpressionEvalException {
    return evalExpressionWithException(PY, val, expr, verbose, "result");
  }

  public static int getMaxEngines() {
    return maxEngines;
  }

  public static void setMaxEngines(int maxEngines) {
    ScriptEngines.maxEngines = maxEngines;
  }

  public static boolean isAllowSandboxed() {
    return allowSandboxed;
  }

  public static void setAllowSandboxed(boolean allowSandboxed) {
    ScriptEngines.allowSandboxed = allowSandboxed;
  }

  public static void resetEngines() {
    SHARED_ENGINES_SET.values().forEach(Set::clear);
    for (Entry<ExprParser, ConcurrentLinkedQueue<ScriptEngine>> enginesEntry : SCRIPT_ENGINES.entrySet()) {
      enginesEntry.getValue().clear();
      CURRENT_NUM_ENGINES.get(enginesEntry.getKey()).set(0);
    }
  }

  private static CompiledScript compileScript(ExprParser type, String script, boolean sandbox, boolean verbose) {
    try {
      return compileScriptWithException(type, script, sandbox, verbose);
    } catch (Throwable e) {
      Logs.logLevel(
        LOG,
        verbose ? Level.ERROR : Level.WARN,
        e,
        "Error compiling script: type=%s %s",
        type,
        Logs.getSplunkKeyValueString("script", script)
      );
      return null;
    }
  }

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

  private static <T extends Object> T evalExpression(ExprParser type, Object val, ScriptEngineExpression expr, boolean verbose) {
    return evalExpression(type, val, expr, verbose, null);
  }

  private static <T extends Object> T evalExpression(
    ExprParser type,
    Object val,
    ScriptEngineExpression expr,
    boolean verbose,
    String resultVariable
  ) {
    try {
      return evalExpressionWithException(type, val, expr, verbose, resultVariable);
    } catch (ExpressionEvalException e) {
      Logs.logLevel(
        LOG,
        verbose ? Level.ERROR : Level.WARN,
        e,
        "Error evaluating expression: type=%s %s",
        type,
        Logs.getSplunkKeyValueString("expr", expr.getExpression())
      );
      if (verbose) {
        Logs.logDebug(LOG, "Object to eval for expression was: %s", val);
      }
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private static <T extends Object> T evalExpressionWithException(
    ExprParser type,
    Object val,
    ScriptEngineExpression expr,
    boolean verbose,
    String resultVariable
  )
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

  private static void releaseEngine(ExprParser type, ScriptEngine engine) {
    if (engine != null) {
      if (SHARED_ENGINES_SET.get(type).contains(engine)) {
        SCRIPT_ENGINES.get(type).add(engine);
      }
    }
  }

}
