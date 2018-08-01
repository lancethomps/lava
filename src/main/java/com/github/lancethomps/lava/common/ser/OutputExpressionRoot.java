package com.github.lancethomps.lava.common.ser;

import java.util.Map;

import com.github.lancethomps.lava.common.expr.ExprContextRootWithResult;

/**
 * The Class OutputExpressionRoot.
 *
 * @author lancethomps
 */
public class OutputExpressionRoot extends ExprContextRootWithResult {

	/** The context. */
	private Object context;

	/** The original. */
	private Object data;

	/**
	 * Instantiates a new output expression root.
	 */
	public OutputExpressionRoot() {
		this(null, null);
	}

	/**
	 * Instantiates a new output expression root.
	 *
	 * @param data the data
	 * @param result the result
	 */
	public OutputExpressionRoot(Object data, Map<String, Object> result) {
		this(data, result, null);
	}

	/**
	 * Instantiates a new output expression root.
	 *
	 * @param data the data
	 * @param result the result
	 * @param context the context
	 */
	public OutputExpressionRoot(Object data, Map<String, Object> result, Object context) {
		super(result);
		this.data = data;
		this.context = context;
	}

	/**
	 * @return the context
	 */
	public Object getContext() {
		return context;
	}

	/**
	 * Gets the original.
	 *
	 * @return the original
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Sets the context.
	 *
	 * @param context the context to set
	 * @return the output expression root
	 */
	public OutputExpressionRoot setContext(Object context) {
		this.context = context;
		return this;
	}

	/**
	 * Sets the original.
	 *
	 * @param data the new data
	 * @return the output expression root
	 */
	public OutputExpressionRoot setData(Object data) {
		this.data = data;
		return this;
	}

}
