package com.github.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;

import com.github.lancethomps.lava.common.lambda.ThrowingFunction;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * The Class CustomFunctionalSerializer.
 *
 * @author lancethomps
 * @param <T> the generic type
 */
public class CustomFunctionalSerializer<T> extends StdScalarSerializer<T> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2736286770181862914L;

	/** The function. */
	private final ThrowingFunction<T, Object> function;

	/** The type. */
	private final Class<T> type;

	/**
	 * Default Constructor.
	 *
	 * @param type the type
	 * @param function the function
	 */
	public CustomFunctionalSerializer(Class<T> type, ThrowingFunction<T, Object> function) {
		super(type);
		this.type = type;
		this.function = function;
	}

	/**
	 * Gets the function.
	 *
	 * @return the function
	 */
	public ThrowingFunction<T, Object> getFunction() {
		return function;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public Class<T> getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator,
	 * com.fasterxml.jackson.databind.SerializerProvider)
	 */
	@Override
	public void serialize(T value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException,
		JsonGenerationException {
		try {
			Object val = function.apply(value);
			if (val == null) {
				jsonGenerator.writeNull();
			} else {
				jsonGenerator.writeObject(val);
			}
		} catch (Exception e) {
			throw new JsonGenerationException(e, jsonGenerator);
		}
	}
}
