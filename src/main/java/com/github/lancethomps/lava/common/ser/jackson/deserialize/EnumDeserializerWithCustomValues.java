package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;

import com.github.lancethomps.lava.common.Enums;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;

/**
 * The Class EnumDeserializer.
 */
@SuppressWarnings("serial")
public class EnumDeserializerWithCustomValues extends EnumDeserializer {

	/** The delegate. */
	private final EnumDeserializer delegate;

	/**
	 * Instantiates a new enum deserializer.
	 *
	 * @param delegate the delegate
	 * @param caseInsensitive the case insensitive
	 */
	public EnumDeserializerWithCustomValues(EnumDeserializer delegate, Boolean caseInsensitive) {
		super(delegate, caseInsensitive);
		this.delegate = delegate;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.fasterxml.jackson.databind.deser.std.EnumDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
	 */
	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonToken curr = p.currentToken();
		if (curr == JsonToken.VALUE_STRING) {
			@SuppressWarnings("unchecked")
			Object result = Enums.fromString((Class<Enum<?>>) _enumClass(), p.getText());
			if (result != null) {
				return result;
			}
		}
		return super.deserialize(p, ctxt);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.fasterxml.jackson.databind.deser.std.EnumDeserializer#withResolved(java.lang.Boolean)
	 */
	@Override
	public EnumDeserializer withResolved(Boolean caseInsensitive) {
		if (_caseInsensitive == caseInsensitive) {
			return this;
		}
		return new EnumDeserializerWithCustomValues(delegate, caseInsensitive);
	}
}
