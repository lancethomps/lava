package com.github.lancethomps.lava.common.ser.jackson.types;

import java.util.Collection;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;

/**
 * The Class NoopTypeResolver.
 */
public class NoopTypeResolver extends DefaultTypeResolverBuilder {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6486011674616018279L;

	/**
	 * Instantiates a new noop type resolver.
	 */
	public NoopTypeResolver() {
		super(DefaultTyping.NON_FINAL);
	}

	@Override
	protected TypeIdResolver idResolver(MapperConfig<?> config, JavaType baseType, Collection<NamedType> subtypes, boolean forSer, boolean forDeser) {
		return new NoopTypeIdResolver(baseType, config.getTypeFactory());
	}

	@Override
	public boolean useForType(JavaType t) {
		return false;
	}

}
