package com.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.lancethomps.lava.common.date.Dates;

public class LocalDateSerializer extends StdScalarSerializer<LocalDate> {

  private static final long serialVersionUID = 834875867312073591L;

  public LocalDateSerializer() {
    super(LocalDate.class);
  }

  @Override
  public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeNumber(Dates.toInt(value));
  }

}
