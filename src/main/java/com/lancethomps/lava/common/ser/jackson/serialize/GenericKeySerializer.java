package com.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.lancethomps.lava.common.lambda.ThrowingFunction;
import com.lancethomps.lava.common.lambda.ThrowingTriFunction;
import com.lancethomps.lava.common.logging.Logs;

public class GenericKeySerializer extends StdSerializer<Object> {

  private static final Map<Class<?>, List<ThrowingTriFunction<Object, JsonGenerator, SerializerProvider, String>>> FUNCTIONS = new HashMap<>();

  private static final Logger LOG = LogManager.getLogger(GenericKeySerializer.class);

  private static final long serialVersionUID = 8654920808877349523L;

  public GenericKeySerializer() {
    super(Object.class);
  }

  public static <T> void addKeySerializerFunction(Class<T> type, ThrowingFunction<T, String> function) {
    addKeySerializerFunction(type, (val, gen, prov) -> function.apply(val));
  }

  @SuppressWarnings("unchecked")
  public static <T> void addKeySerializerFunction(Class<T> type, ThrowingTriFunction<T, JsonGenerator, SerializerProvider, String> function) {
    synchronized (FUNCTIONS) {
      FUNCTIONS.computeIfAbsent(type, k -> new ArrayList<>()).add((ThrowingTriFunction<Object, JsonGenerator, SerializerProvider, String>) function);
    }
  }

  @Override
  public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    String str = value == null ? "null" : FUNCTIONS.getOrDefault(value.getClass(), Arrays.asList(this::getDefaultValue)).stream().map(func -> {
      try {
        return func.apply(value, jgen, provider);
      } catch (Exception e) {
        Logs.logError(LOG, e, "Issue applying function for value [%s]!", value);
        return null;
      }
    }).filter(Objects::nonNull).findFirst().orElse(null);
    if (str != null) {
      jgen.writeFieldName(str);
    }
  }

  private String getDefaultValue(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    String str = null;
    Class<?> cls = value.getClass();
    if (cls == String.class) {
      str = (String) value;
    } else if (Date.class.isAssignableFrom(cls)) {
      provider.defaultSerializeDateKey((Date) value, jgen);
      return null;
    } else if (cls == Class.class) {
      str = ((Class<?>) value).getName();
    } else {
      str = value.toString();
    }
    return str;
  }

}
