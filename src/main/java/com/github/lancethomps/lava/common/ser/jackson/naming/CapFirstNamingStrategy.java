package com.github.lancethomps.lava.common.ser.jackson.naming;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.PropertyNamingStrategyBase;

/**
 * The Class CamelCaseNamingStrategy.
 */
public class CapFirstNamingStrategy extends PropertyNamingStrategyBase {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7060943266369044926L;

	@Override
	public String translate(String propertyName) {
		return StringUtils.capitalize(propertyName);
	}

}
