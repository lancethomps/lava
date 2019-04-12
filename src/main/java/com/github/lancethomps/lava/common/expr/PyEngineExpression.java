package com.github.lancethomps.lava.common.expr;

import java.util.Map;

import javax.script.CompiledScript;

public class PyEngineExpression extends ScriptEngineExpression {

  public PyEngineExpression() {
    super();
  }

  public PyEngineExpression(String expression, boolean sandbox, CompiledScript compiled, Map<String, Object> globalVariables) {
    super(expression, sandbox, compiled, globalVariables);
  }

}
