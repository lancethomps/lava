package com.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.lancethomps.lava.common.date.Dates;
import com.lancethomps.lava.common.lambda.Lambdas;
import com.lancethomps.lava.common.logging.Logs;

public class LocalDateDeserializer extends StdScalarDeserializer<LocalDate> {

  public static final int MAX_NON_MILLIS_DATE = 22221231;

  private static final Logger LOG = LogManager.getLogger(LocalDateDeserializer.class);

  private static final long serialVersionUID = -6356065359683945861L;

  public LocalDateDeserializer() {
    super(LocalDate.class);
  }

  @Override
  public LocalDate deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
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
