package com.lancethomps.lava.common.ser.jackson.deserialize;

import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;

public class TemporalAccessorKeyDeserializer<T extends TemporalAccessor> extends StdKeyDeserializer {

  private static final long serialVersionUID = -6381946928012107571L;

  private final Function<String, T> function;

  public TemporalAccessorKeyDeserializer(Class<T> type, Function<String, T> function) {
    super(0, type);
    this.function = function;
  }

  public Function<String, T> getFunction() {
    return function;
  }

  @Override
  protected Object _parse(String key, DeserializationContext context) throws Exception {
    return function.apply(key);
  }

}
