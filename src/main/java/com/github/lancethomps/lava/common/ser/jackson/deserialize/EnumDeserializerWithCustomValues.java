package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;
import com.github.lancethomps.lava.common.Enums;

@SuppressWarnings("serial")
public class EnumDeserializerWithCustomValues extends EnumDeserializer {

  private final EnumDeserializer delegate;

  public EnumDeserializerWithCustomValues(EnumDeserializer delegate, Boolean caseInsensitive) {
    super(delegate, caseInsensitive);
    this.delegate = delegate;
  }

  @Override
  public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    JsonToken curr = p.currentToken();
    if (curr == JsonToken.VALUE_STRING) {
      @SuppressWarnings("unchecked")
      Object result = Enums.fromString((Class<Enum<?>>) _enumClass(), p.getText());
      if (result != null) {
        return result;
      }
    }
    return super.deserialize(p, ctxt);
  }

  @Override
  public EnumDeserializer withResolved(Boolean caseInsensitive) {
    if (_caseInsensitive == caseInsensitive) {
      return this;
    }
    return new EnumDeserializerWithCustomValues(delegate, caseInsensitive);
  }

}
