package com.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.lancethomps.lava.common.date.Dates;

public final class DateSerializer extends StdScalarSerializer<Date> {

  private static final long serialVersionUID = 7520558870662116400L;

  private final DateTimeFormatter dateFormatter;

  public DateSerializer() {
    this((String) null);
  }

  public DateSerializer(final DateTimeFormatter dateFormatter) {
    super(Date.class);
    this.dateFormatter = dateFormatter;
  }

  public DateSerializer(final String dateFormat) {
    this(dateFormat == null ? null : Dates.formatterFromPattern(dateFormat));
  }

  @Override
  public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    if (date == null) {
      jsonGenerator.writeNull();
    } else if (dateFormatter != null) {
      jsonGenerator.writeString(dateFormatter.format(Dates.toDateTime(date)));
    } else {
      jsonGenerator.writeNumber(date.getTime());
    }
  }

}
