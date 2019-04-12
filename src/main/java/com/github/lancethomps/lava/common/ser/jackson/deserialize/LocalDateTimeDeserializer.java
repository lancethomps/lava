package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.github.lancethomps.lava.common.date.Dates;

public class LocalDateTimeDeserializer extends StdScalarDeserializer<LocalDateTime> {

  private static final long serialVersionUID = -8690420690371304037L;

  public LocalDateTimeDeserializer() {
    super(LocalDateTime.class);
  }

  @Override
  public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
    if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
      return Dates.parseDateTime(jsonParser.getValueAsString());
    } else if (jsonParser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
      long val = jsonParser.getValueAsLong();
      return Dates.fromMillis(val);
    }
    return null;
  }

}
