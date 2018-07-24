package com.github.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.lambda.ThrowingFunction;
import com.github.lancethomps.lava.common.lambda.ThrowingTriFunction;
import com.github.lancethomps.lava.common.logging.Logs;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * The Class GenericKeySerializer.
 */
public class GenericKeySerializer extends StdSerializer<Object> {

	/** The Constant FUNCTIONS. */
	private static final Map<Class<?>, List<ThrowingTriFunction<Object, JsonGenerator, SerializerProvider, String>>> FUNCTIONS = new HashMap<>();

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(GenericKeySerializer.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8654920808877349523L;

	/**
	 * Instantiates a new generic key serializer.
	 */
	public GenericKeySerializer() {
		super(Object.class);
	}

	/**
	 * Adds the key serializer function.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param function the function
	 */
	public static <T> void addKeySerializerFunction(Class<T> type, ThrowingFunction<T, String> function) {
		addKeySerializerFunction(type, (val, gen, prov) -> function.apply(val));
	}

	/**
	 * Adds the key serializer function.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param function the function
	 */
	@SuppressWarnings("unchecked")
	public static <T> void addKeySerializerFunction(Class<T> type, ThrowingTriFunction<T, JsonGenerator, SerializerProvider, String> function) {
		synchronized (FUNCTIONS) {
			FUNCTIONS.computeIfAbsent(type, k -> new ArrayList<>()).add((ThrowingTriFunction<Object, JsonGenerator, SerializerProvider, String>) function);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.ser.std.StdKeySerializer#serialize(java.lang.Object,
	 * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
	 */
	@Override
	public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		String str = value == null ? "null" : FUNCTIONS.getOrDefault(value.getClass(), Arrays.asList(this::getDefaultValue)).stream().map(func -> {
			try {
				return func.apply(value, jgen, provider);
			} catch (Exception e) {
				Logs.logError(LOG, e, "Issue applying function for value [%s]!", value);
				return null;
			}
		}).filter(Objects::nonNull).findFirst().orElse(null);
		if (str != null) {
			jgen.writeFieldName(str);
		}
	}

	/**
	 * Gets the default value.
	 *
	 * @param value the value
	 * @param jgen the jgen
	 * @param provider the provider
	 * @return the default value
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String getDefaultValue(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		String str = null;
		Class<?> cls = value.getClass();
		if (cls == String.class) {
			str = (String) value;
		} else if (Date.class.isAssignableFrom(cls)) {
			provider.defaultSerializeDateKey((Date) value, jgen);
			return null;
		} else if (cls == Class.class) {
			str = ((Class<?>) value).getName();
		} else {
			str = value.toString();
		}
		return str;
	}
}
