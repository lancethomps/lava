package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.util.function.Function;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;

/**
 * The Class GenericKeyDeserializer.
 *
 * @param <T> the generic type
 */
public class GenericKeyDeserializer<T> extends StdKeyDeserializer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3012361414052680751L;

	/** The function. */
	private final Function<String, T> function;

	/**
	 * Instantiates a new generic key deserializer.
	 *
	 * @param type the type
	 * @param function the function
	 */
	public GenericKeyDeserializer(Class<T> type, Function<String, T> function) {
		super(-1, type);
		this.function = function;
	}

	@Override
	protected Object _parse(String key, DeserializationContext ctxt) throws Exception {
		return function.apply(key);
	}

}
