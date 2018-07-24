package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.date.Dates;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

/**
 * The Class TemporalAccessorDeserializer.
 *
 * @param <T> the generic type
 */
public class TemporalAccessorDeserializer<T extends TemporalAccessor> extends StdScalarDeserializer<T> {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(TemporalAccessorDeserializer.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8299359543429836350L;

	/** The supplier. */
	private final Function<LocalDateTime, T> supplier;

	/** The default as int. */
	private final boolean defaultAsInt;

	/**
	 * Instantiates a new temporal accessor deserializer.
	 *
	 * @param type the type
	 * @param supplier the supplier
	 */
	public TemporalAccessorDeserializer(Class<T> type, Function<LocalDateTime, T> supplier) {
		this(type, supplier, false);
	}

	/**
	 * Instantiates a new temporal accessor deserializer.
	 *
	 * @param type the type
	 * @param supplier the supplier
	 * @param defaultAsInt the default as int
	 */
	public TemporalAccessorDeserializer(Class<T> type, Function<LocalDateTime, T> supplier, boolean defaultAsInt) {
		super(type);
		this.supplier = supplier;
		this.defaultAsInt = defaultAsInt;
	}

	@Override
	public T deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		LocalDateTime date = null;
		if (parser.getCurrentToken() == JsonToken.VALUE_STRING) {
			date = Dates.parseDateTime(parser.getValueAsString());
		} else if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
			long val = parser.getValueAsLong();
			date = defaultAsInt && (val < LocalDateDeserializer.MAX_NON_MILLIS_DATE) && (val > 0) ? Dates.fromInt((int) val).atStartOfDay() : Dates.fromMillis(val);
		}
		return date != null ? supplier.apply(date) : null;
	}

	/**
	 * @return the supplier
	 */
	public Function<LocalDateTime, T> getSupplier() {
		return supplier;
	}
}
