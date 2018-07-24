package com.github.lancethomps.lava.common.ser.jackson.naming;

import com.github.lancethomps.lava.common.string.StringUtil;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.PropertyNamingStrategyBase;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;

/**
 * The Class FixCamelCaseNamingStrategy.
 */
public class FixCamelCaseNamingStrategy extends PropertyNamingStrategyBase {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7060943266369044926L;

	@Override
	public String translate(String propertyName) {
		return StringUtil.fixCamelCase(propertyName);
	}

	@Override
	public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
		return translate(defaultName);
	}

}
