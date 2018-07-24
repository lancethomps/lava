package com.github.lancethomps.lava.common.expr;

import java.util.Map;

import javax.script.CompiledScript;

/**
 * The Class PyEngineExpression.
 */
public class PyEngineExpression extends ScriptEngineExpression {

	/**
	 * Instantiates a new py engine expression.
	 */
	public PyEngineExpression() {
		super();
	}

	/**
	 * Instantiates a new py engine expression.
	 *
	 * @param expression the expression
	 * @param sandbox the sandbox
	 * @param compiled the compiled
	 * @param globalVariables the global variables
	 */
	public PyEngineExpression(String expression, boolean sandbox, CompiledScript compiled, Map<String, Object> globalVariables) {
		super(expression, sandbox, compiled, globalVariables);
	}

}
