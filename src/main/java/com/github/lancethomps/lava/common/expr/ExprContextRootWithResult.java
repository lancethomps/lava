package com.github.lancethomps.lava.common.expr;

import java.util.Map;

/**
 * The Class ExprContextRootWithResult.
 */
public abstract class ExprContextRootWithResult {

	/** The created. */
	private Map<String, Object> result;

	/**
	 * Instantiates a new expr context root with result.
	 */
	public ExprContextRootWithResult() {
		this(null);
	}

	/**
	 * Instantiates a new expr context root with result.
	 *
	 * @param result the result
	 */
	public ExprContextRootWithResult(Map<String, Object> result) {
		super();
		this.result = result;
	}

	/**
	 * @return the result
	 */
	public Map<String, Object> getResult() {
		return result;
	}

	/**
	 * Sets the result.
	 *
	 * @param <T> the generic type
	 * @param result the result to set
	 * @return the t
	 */
	public <T extends ExprContextRootWithResult> T setResult(Map<String, Object> result) {
		this.result = result;
		return (T) this;
	}

}
