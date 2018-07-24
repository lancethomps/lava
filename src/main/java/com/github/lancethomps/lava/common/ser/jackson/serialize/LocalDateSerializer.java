package com.github.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.time.LocalDate;

import com.github.lancethomps.lava.common.date.Dates;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * The Class LocalDateSerializer.
 */
public class LocalDateSerializer extends StdScalarSerializer<LocalDate> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 834875867312073591L;

	/**
	 * Instantiates a new local date serializer.
	 */
	public LocalDateSerializer() {
		super(LocalDate.class);
	}

	@Override
	public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeNumber(Dates.toInt(value));
	}
}
