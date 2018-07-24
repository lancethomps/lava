package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;
import java.time.LocalDateTime;

import com.github.lancethomps.lava.common.date.Dates;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

/**
 * The Class LocalDateTimeDeserializer.
 */
public class LocalDateTimeDeserializer extends StdScalarDeserializer<LocalDateTime> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8690420690371304037L;

	/**
	 * Instantiates a new local date time deserializer.
	 */
	public LocalDateTimeDeserializer() {
		super(LocalDateTime.class);
	}

	@Override
	public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
		if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
			return Dates.parseDateTime(jsonParser.getValueAsString());
		} else if (jsonParser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
			long val = jsonParser.getValueAsLong();
			return Dates.fromMillis(val);
		}
		return null;
	}
}
