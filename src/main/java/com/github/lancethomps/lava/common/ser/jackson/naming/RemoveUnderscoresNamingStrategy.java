package com.github.lancethomps.lava.common.ser.jackson.naming;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.PropertyNamingStrategyBase;

/**
 * The Class RemoveUnderscoresNamingStrategy.
 */
public class RemoveUnderscoresNamingStrategy extends PropertyNamingStrategyBase {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9151296712740028938L;

	@Override
	public String translate(String propertyName) {
		return StringUtils.remove(propertyName, '_');
	}

}
