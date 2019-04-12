package com.github.lancethomps.lava.common.expr;

import java.util.Map;

import javax.script.CompiledScript;

public class ScriptEngineExpression {

  private CompiledScript compiled;

  private String expression;

  private Map<String, Object> globalVariables;

  private boolean sandbox = true;

  public ScriptEngineExpression() {
    super();
  }

  public ScriptEngineExpression(String expression, boolean sandbox, CompiledScript compiled, Map<String, Object> globalVariables) {
    super();
    this.expression = expression;
    this.sandbox = sandbox;
    this.compiled = compiled;
    this.globalVariables = globalVariables;
  }

  public CompiledScript getCompiled() {
    return compiled;
  }

  public <T extends ScriptEngineExpression> T setCompiled(CompiledScript compiled) {
    this.compiled = compiled;
    return (T) this;
  }

  public String getExpression() {
    return expression;
  }

  public <T extends ScriptEngineExpression> T setExpression(String expression) {
    this.expression = expression;
    return (T) this;
  }

  public Map<String, Object> getGlobalVariables() {
    return globalVariables;
  }

  public <T extends ScriptEngineExpression> T setGlobalVariables(Map<String, Object> globalVariables) {
    this.globalVariables = globalVariables;
    return (T) this;
  }

  public boolean isSandbox() {
    return sandbox;
  }

  public <T extends ScriptEngineExpression> T setSandbox(boolean sandbox) {
    this.sandbox = sandbox;
    return (T) this;
  }

}
