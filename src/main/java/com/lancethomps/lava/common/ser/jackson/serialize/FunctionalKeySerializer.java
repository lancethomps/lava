package com.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.lancethomps.lava.common.lambda.ThrowingTriFunction;
import com.lancethomps.lava.common.logging.Logs;

public class FunctionalKeySerializer<T extends Object> extends StdScalarSerializer<T> {

  private static final Logger LOG = Logger.getLogger(FunctionalKeySerializer.class);

  private static final long serialVersionUID = 305655599047712413L;

  private final ThrowingTriFunction<T, JsonGenerator, SerializerProvider, String> function;

  public FunctionalKeySerializer(Class<T> type, ThrowingTriFunction<T, JsonGenerator, SerializerProvider, String> function, boolean addToGeneric) {
    super(type);
    this.function = function;
    if (addToGeneric) {
      GenericKeySerializer.addKeySerializerFunction(type, function);
    }
  }

  public static <T> SimpleModule createAndAdd(
    SimpleModule module,
    Class<T> type,
    ThrowingTriFunction<T, JsonGenerator, SerializerProvider, String> function
  ) {
    return module.addKeySerializer(type, new FunctionalKeySerializer<>(type, function, true));
  }

  @Override
  public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    String key = null;
    try {
      key = function.apply(value, gen, provider);
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Error serializing value [%s] of type [%s] to key!", value, _handledType);
    }
    gen.writeFieldName(StringUtils.defaultIfBlank(key, "null"));
  }

}
