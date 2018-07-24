package com.github.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.DoubleSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * The Class CustomDoubleSerializerWithFiniteCheck.
 */
public class CustomDoubleSerializerWithFiniteCheck extends StdScalarSerializer<Object> implements ContextualSerializer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The delegate. */
	private final DoubleSerializer delegate;

	/**
	 * Instantiates a new custom double serializer with nan skipping.
	 *
	 * @param type the type
	 */
	public CustomDoubleSerializerWithFiniteCheck(Class<?> type) {
		super(type, false);
		delegate = new DoubleSerializer(type);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.fasterxml.jackson.databind.ser.std.StdScalarSerializer#acceptJsonFormatVisitor(com.fasterxml.
	 * jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper,
	 * com.fasterxml.jackson.databind.JavaType)
	 */
	@Override
	public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
		delegate.acceptJsonFormatVisitor(visitor, typeHint);
	}

	/**
	 * Creates the contextual.
	 *
	 * @param prov the prov
	 * @param property the property
	 * @return the json serializer
	 * @throws JsonMappingException the json mapping exception
	 */
	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
		if (format != null) {
			switch (format.getShape()) {
			case STRING:
				return ToStringSerializer.instance;
			default:
			}
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.ser.std.StdScalarSerializer#getSchema(com.fasterxml.jackson.
	 * databind.SerializerProvider, java.lang.reflect.Type)
	 */
	@Override
	public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
		return delegate.getSchema(provider, typeHint);
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object,
	 * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
	 */
	@Override
	public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		if ((value == null) || Double.isFinite((Double) value)) {
			delegate.serialize(value, gen, provider);
		} else {
			gen.writeNull();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.fasterxml.jackson.databind.ser.std.StdScalarSerializer#serializeWithType(java.lang.Object,
	 * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider,
	 * com.fasterxml.jackson.databind.jsontype.TypeSerializer)
	 */
	@Override
	public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
		if ((value == null) || Double.isFinite((Double) value)) {
			delegate.serialize(value, gen, provider);
		} else {
			gen.writeNull();
		}
	}

}
