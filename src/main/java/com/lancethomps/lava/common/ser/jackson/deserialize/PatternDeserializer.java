package com.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.lancethomps.lava.common.logging.Logs;

@SuppressWarnings("serial")
public class PatternDeserializer extends StdScalarDeserializer<Pattern> {

  private static final Logger LOG = Logger.getLogger(PatternDeserializer.class);

  public PatternDeserializer() {
    super(Pattern.class);
  }

  @Override
  public Pattern deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
    String regex = jsonParser.getValueAsString();
    if (regex != null) {
      try {
        return Pattern.compile(regex);
      } catch (Throwable e) {
        Logs.logWarn(LOG, e, "Pattern deserialization issue for [%s].", regex);
      }
    }
    return null;
  }

}
