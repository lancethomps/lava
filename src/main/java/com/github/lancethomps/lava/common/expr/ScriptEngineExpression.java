package com.github.lancethomps.lava.common.expr;

import java.util.Map;

import javax.script.CompiledScript;

/**
 * The Class ScriptEngineExpression.
 */
public class ScriptEngineExpression {

	/** The compiled. */
	private CompiledScript compiled;

	/** The expression. */
	private String expression;

	/** The global variables. */
	private Map<String, Object> globalVariables;

	/** The sandbox. */
	private boolean sandbox = true;

	/**
	 * Instantiates a new js engine expression.
	 */
	public ScriptEngineExpression() {
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
	public ScriptEngineExpression(String expression, boolean sandbox, CompiledScript compiled, Map<String, Object> globalVariables) {
		super();
		this.expression = expression;
		this.sandbox = sandbox;
		this.compiled = compiled;
		this.globalVariables = globalVariables;
	}

	/**
	 * Gets the compiled.
	 *
	 * @return the compiled
	 */
	public CompiledScript getCompiled() {
		return compiled;
	}

	/**
	 * Gets the expression.
	 *
	 * @return the expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @return the globalVariables
	 */
	public Map<String, Object> getGlobalVariables() {
		return globalVariables;
	}

	/**
	 * Checks if is sandbox.
	 *
	 * @return the sandbox
	 */
	public boolean isSandbox() {
		return sandbox;
	}

	/**
	 * Sets the compiled.
	 *
	 * @param <T> the generic type
	 * @param compiled the compiled to set
	 * @return the js engine expression
	 */
	public <T extends ScriptEngineExpression> T setCompiled(CompiledScript compiled) {
		this.compiled = compiled;
		return (T) this;
	}

	/**
	 * Sets the expression.
	 *
	 * @param <T> the generic type
	 * @param expression the expression to set
	 * @return the t
	 */
	public <T extends ScriptEngineExpression> T setExpression(String expression) {
		this.expression = expression;
		return (T) this;
	}

	/**
	 * Sets the global variables.
	 *
	 * @param <T> the generic type
	 * @param globalVariables the globalVariables to set
	 * @return the t
	 */
	public <T extends ScriptEngineExpression> T setGlobalVariables(Map<String, Object> globalVariables) {
		this.globalVariables = globalVariables;
		return (T) this;
	}

	/**
	 * Sets the sandbox.
	 *
	 * @param <T> the generic type
	 * @param sandbox the sandbox to set
	 * @return the t
	 */
	public <T extends ScriptEngineExpression> T setSandbox(boolean sandbox) {
		this.sandbox = sandbox;
		return (T) this;
	}

}
