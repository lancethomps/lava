package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.github.lancethomps.lava.common.date.Dates;

public final class DateDeserializer extends StdScalarDeserializer<Date> {

  private static final Logger LOG = Logger.getLogger(DateDeserializer.class);

  private static final long serialVersionUID = 7520558870662116400L;

  public DateDeserializer() {
    super(Date.class);
  }

  @Override
  public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

    if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
      return Dates.parseDateString(jsonParser.getValueAsString());
    } else if (jsonParser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
      long val = jsonParser.getValueAsLong();
      return new Date(val);
    }
    return null;
  }

}
