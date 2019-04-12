package com.github.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.DoubleSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class CustomDoubleSerializerWithFiniteCheck extends StdScalarSerializer<Object> implements ContextualSerializer {

  private static final long serialVersionUID = 1L;

  private final DoubleSerializer delegate;

  public CustomDoubleSerializerWithFiniteCheck(Class<?> type) {
    super(type, false);
    delegate = new DoubleSerializer(type);
  }

  @Override
  public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
    delegate.acceptJsonFormatVisitor(visitor, typeHint);
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
    JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
    if (format != null) {
      switch (format.getShape()) {
        case STRING:
          return ToStringSerializer.instance;
        default:
      }
    }
    return this;
  }

  @Override
  public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
    return delegate.getSchema(provider, typeHint);
  }

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    if ((value == null) || Double.isFinite((Double) value)) {
      delegate.serialize(value, gen, provider);
    } else {
      gen.writeNull();
    }
  }

  @Override
  public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
    if ((value == null) || Double.isFinite((Double) value)) {
      delegate.serialize(value, gen, provider);
    } else {
      gen.writeNull();
    }
  }

}
