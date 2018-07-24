package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/**
 * The Class BooleanDeserializer.
 */
// TODO: is this the best way to deserialize booleans or should we check for more values?
public class BooleanDeserializer extends PrimitiveOrWrapperDeserializer<Boolean> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7409828322344324932L;

	/**
	 * Instantiates a new boolean deserializer.
	 *
	 * @param cls the cls
	 * @param nvl the nvl
	 */
	public BooleanDeserializer(Class<Boolean> cls, Boolean nvl) {
		super(cls, nvl, Boolean.FALSE);
	}

	@Override
	public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonToken t = p.getCurrentToken();
		if (t == JsonToken.VALUE_TRUE) {
			return Boolean.TRUE;
		}
		if (t == JsonToken.VALUE_FALSE) {
			return Boolean.FALSE;
		}
		return parseBoolean(p, ctxt);
	}

	@Override
	public Boolean deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
		JsonToken t = p.getCurrentToken();
		if (t == JsonToken.VALUE_TRUE) {
			return Boolean.TRUE;
		}
		if (t == JsonToken.VALUE_FALSE) {
			return Boolean.FALSE;
		}
		return parseBoolean(p, ctxt);
	}

	/**
	 * Parses the boolean.
	 *
	 * @param p the p
	 * @param ctxt the ctxt
	 * @return the boolean
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Boolean parseBoolean(JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonToken t = p.getCurrentToken();
		if (t == JsonToken.VALUE_NULL) {
			return (Boolean) _coerceNullToken(ctxt, primitive);
		}
		if (t == JsonToken.START_ARRAY) { // unwrapping?
			return _deserializeFromArray(p, ctxt);
		}
		// should accept ints too, (0 == false, otherwise true)
		if (t == JsonToken.VALUE_NUMBER_INT) {
			return Boolean.valueOf(_parseBooleanFromInt(p, ctxt));
		}
		// And finally, let's allow Strings to be converted too
		if (t == JsonToken.VALUE_STRING) {
			String text = p.getText().trim();
			// [databind#422]: Allow aliases
			if ("true".equalsIgnoreCase(text)) {
				_verifyStringForScalarCoercion(ctxt, text);
				return Boolean.TRUE;
			}
			if ("false".equalsIgnoreCase(text)) {
				_verifyStringForScalarCoercion(ctxt, text);
				return Boolean.FALSE;
			}
			if ("y".equalsIgnoreCase(text)) {
				return Boolean.TRUE;
			}
			if ("n".equalsIgnoreCase(text)) {
				return Boolean.FALSE;
			}
			if (text.length() == 0) {
				return (Boolean) _coerceEmptyString(ctxt, primitive);
			}
			if (_hasTextualNull(text)) {
				return (Boolean) _coerceTextualNull(ctxt, primitive);
			}
			if ("1".equals(text)) {
				return Boolean.TRUE;
			}
			if ("0".equals(text)) {
				return Boolean.FALSE;
			}
			return (Boolean) ctxt.handleWeirdStringValue(
				_valueClass,
				text,
				"only \"true\" or \"false\" recognized"
			);
		}
		// usually caller should have handled but:
		if (t == JsonToken.VALUE_TRUE) {
			return Boolean.TRUE;
		}
		if (t == JsonToken.VALUE_FALSE) {
			return Boolean.FALSE;
		}
		// Otherwise, no can do:
		return (Boolean) ctxt.handleUnexpectedToken(_valueClass, p);
	}
}
