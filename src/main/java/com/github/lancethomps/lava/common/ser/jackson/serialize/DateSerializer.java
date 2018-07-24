package com.github.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.github.lancethomps.lava.common.date.Dates;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * The Class DateSerializer.
 */
public final class DateSerializer extends StdScalarSerializer<Date> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7520558870662116400L;

	/** The date formatter. */
	private final DateTimeFormatter dateFormatter;

	/**
	 * Instantiates a new date serializer.
	 */
	public DateSerializer() {
		this((String) null);
	}

	/**
	 * Instantiates a new date serializer.
	 *
	 * @param dateFormatter the date formatter
	 */
	public DateSerializer(final DateTimeFormatter dateFormatter) {
		super(Date.class);
		this.dateFormatter = dateFormatter;
	}

	/**
	 * Default Constructor.
	 *
	 * @param dateFormat the date format
	 */
	public DateSerializer(final String dateFormat) {
		this(dateFormat == null ? null : Dates.formatterFromPattern(dateFormat));
	}

	/**
	 * Serialize.
	 *
	 * @param date the date
	 * @param jsonGenerator the json generator
	 * @param serializerProvider the serializer provider
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws JsonGenerationException the json generation exception
	 */
	@Override
	public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException,
		JsonGenerationException {
		if (date == null) {
			jsonGenerator.writeNull();
		} else if (dateFormatter != null) {
			jsonGenerator.writeString(dateFormatter.format(Dates.toDateTime(date)));
		} else {
			jsonGenerator.writeNumber(date.getTime());
		}
	}
}
