package com.github.lancethomps.lava.common.web.config;

import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.ser.OutputExpression;

/**
 * The Class ResponseHeaderConfig.
 */
@SuppressWarnings("serial")
public class ResponseHeaderConfig extends SimpleDomainObject {

	/** The name. */
	private String name;

	/** The value. */
	private String value;

	/** The value expression. */
	private OutputExpression valueExpression;

	@Override
	public void afterDeserialization() {
		if (valueExpression != null) {
			ExprFactory.compileCreateExpressions(false, false, true, valueExpression);
		}
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets the value expression.
	 *
	 * @return the valueExpression
	 */
	public OutputExpression getValueExpression() {
		return valueExpression;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Sets the value expression.
	 *
	 * @param valueExpression the valueExpression to set
	 */
	public void setValueExpression(OutputExpression valueExpression) {
		this.valueExpression = valueExpression;
	}

}
