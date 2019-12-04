package com.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class TemporalAccessorKeySerializer<T extends TemporalAccessor> extends StdSerializer<T> {

  private static final long serialVersionUID = 4788059320957807596L;

  private final Function<T, String> function;

  public TemporalAccessorKeySerializer(Class<T> type, Function<T, String> function) {
    super(type);
    this.function = function;
    GenericKeySerializer.addKeySerializerFunction(type, (val, gen, prov) -> function.apply(val));
  }

  @Override
  public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
    visitStringFormat(visitor, typeHint);
  }

  public Function<T, String> getFunction() {
    return function;
  }

  @Override
  public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
    return createSchemaNode("string");
  }

  @Override
  public void serialize(T value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    jgen.writeFieldName(function.apply(value));
  }

}
