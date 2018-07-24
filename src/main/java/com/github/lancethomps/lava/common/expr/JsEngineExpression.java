package com.github.lancethomps.lava.common.expr;

import java.util.Map;

import javax.script.CompiledScript;

/**
 * The Class JsEngineExpression.
 *
 * @author lathomps
 */
public class JsEngineExpression extends ScriptEngineExpression {

	/**
	 * Instantiates a new js engine expression.
	 */
	public JsEngineExpression() {
		super();
	}

	/**
	 * Instantiates a new js engine expression.
	 *
	 * @param expression the expression
	 * @param sandbox the sandbox
	 * @param compiled the compiled
	 * @param globalVariables the global variables
	 */
	public JsEngineExpression(String expression, boolean sandbox, CompiledScript compiled, Map<String, Object> globalVariables) {
		super(expression, sandbox, compiled, globalVariables);
	}

}
