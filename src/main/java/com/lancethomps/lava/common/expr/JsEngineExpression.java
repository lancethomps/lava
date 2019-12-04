package com.lancethomps.lava.common.expr;

import java.util.Map;

import javax.script.CompiledScript;

public class JsEngineExpression extends ScriptEngineExpression {

  public JsEngineExpression() {
    super();
  }

  public JsEngineExpression(String expression, boolean sandbox, CompiledScript compiled, Map<String, Object> globalVariables) {
    super(expression, sandbox, compiled, globalVariables);
  }

}
