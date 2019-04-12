package com.github.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.github.lancethomps.lava.common.lambda.ThrowingFunction;

public class CustomFunctionalSerializer<T> extends StdScalarSerializer<T> {

  private static final long serialVersionUID = -2736286770181862914L;

  private final ThrowingFunction<T, Object> function;

  private final Class<T> type;

  public CustomFunctionalSerializer(Class<T> type, ThrowingFunction<T, Object> function) {
    super(type);
    this.type = type;
    this.function = function;
  }

  public ThrowingFunction<T, Object> getFunction() {
    return function;
  }

  public Class<T> getType() {
    return type;
  }

  @Override
  public void serialize(T value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    try {
      Object val = function.apply(value);
      if (val == null) {
        jsonGenerator.writeNull();
      } else {
        jsonGenerator.writeObject(val);
      }
    } catch (Exception e) {
      throw new JsonGenerationException(e, jsonGenerator);
    }
  }

}
