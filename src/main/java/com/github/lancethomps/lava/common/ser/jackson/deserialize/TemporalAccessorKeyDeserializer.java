package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;

/**
 * The Class TemporalAccessorKeyDeserializer.
 *
 * @param <T> the generic type
 */
public class TemporalAccessorKeyDeserializer<T extends TemporalAccessor> extends StdKeyDeserializer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6381946928012107571L;

	/** The function. */
	private final Function<String, T> function;

	/**
	 * Instantiates a new temporal accessor key deserializer.
	 *
	 * @param type the type
	 * @param function the function
	 */
	public TemporalAccessorKeyDeserializer(Class<T> type, Function<String, T> function) {
		super(0, type);
		this.function = function;
	}

	/**
	 * @return the function
	 */
	public Function<String, T> getFunction() {
		return function;
	}

	@Override
	protected Object _parse(String key, DeserializationContext context) throws Exception {
		return function.apply(key);
	}

}
