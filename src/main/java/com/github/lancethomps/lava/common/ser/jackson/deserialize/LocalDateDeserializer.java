package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.date.Dates;
import com.github.lancethomps.lava.common.lambda.Lambdas;
import com.github.lancethomps.lava.common.logging.Logs;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

/**
 * The Class LocalDateDeserializer.
 */
public class LocalDateDeserializer extends StdScalarDeserializer<LocalDate> {

	/** The Constant MAX_NON_MILLIS_DATE. */
	public static final int MAX_NON_MILLIS_DATE = 22221231;

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(LocalDateDeserializer.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6356065359683945861L;

	/**
	 * Instantiates a new local date deserializer.
	 */
	public LocalDateDeserializer() {
		super(LocalDate.class);
	}

	@Override
	public LocalDate deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
		if (jsonParser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
			long val = jsonParser.getValueAsLong();
			if (val > MAX_NON_MILLIS_DATE) {
				return Lambdas.functionIfNonNull(Dates.fromMillis(val), LocalDateTime::toLocalDate).orElse(null);
			}
			return Dates.fromInt((int) val);
		} else if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
			return Dates.parseDate(jsonParser.getValueAsString());
		}

		Logs.logWarn(LOG, new JsonParseException(jsonParser, "Expected string in format yyyyMMdd"), "JSON deserialization issue!");
		return null;
	}
}
