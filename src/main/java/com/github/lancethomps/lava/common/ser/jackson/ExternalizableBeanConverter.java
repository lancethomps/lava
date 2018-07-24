package com.github.lancethomps.lava.common.ser.jackson;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

/**
 * The Class ExternalizableBeanConverter.
 */
public class ExternalizableBeanConverter implements Converter<ExternalizableBean, ExternalizableBean> {

	/** The sub classtype. */
	private final JavaType subClassType;

	/**
	 * Instantiates a new externalizable bean converter.
	 *
	 * @param config the config
	 * @param annotated the annotated
	 */
	public ExternalizableBeanConverter(MapperConfig<?> config, Annotated annotated) {
		subClassType = annotated.getType();
	}

	@Override
	public ExternalizableBean convert(ExternalizableBean value) {
		value.afterDeserialization();
		return value;
	}

	@Override
	public JavaType getInputType(TypeFactory typeFactory) {
		return subClassType;
	}

	@Override
	public JavaType getOutputType(TypeFactory typeFactory) {
		return subClassType;
	}
}
