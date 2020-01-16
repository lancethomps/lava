package com.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.lancethomps.lava.common.lambda.ThrowingTriConsumer;
import com.lancethomps.lava.common.logging.Logs;

public class NumberWithLimitSerializer<T> extends StdScalarSerializer<T> implements ContextualSerializer {

  private static final Logger LOG = LogManager.getLogger(NumberWithLimitSerializer.class);

  private static final long serialVersionUID = -9123969819834800797L;

  private final JsonParser.NumberType numberType;

  private final Integer sigFigs;

  private final ThrowingTriConsumer<JsonGenerator, Integer, T> writer;

  public NumberWithLimitSerializer(
    final Class<T> type,
    final JsonParser.NumberType numberType,
    final ThrowingTriConsumer<JsonGenerator, Integer, T> writer,
    final int sigFigs
  ) {
    super(type, false);
    this.numberType = numberType;
    this.writer = writer;
    this.sigFigs = sigFigs;
  }

  @Override
  public void acceptJsonFormatVisitor(
    JsonFormatVisitorWrapper visitor,
    JavaType typeHint
  ) throws JsonMappingException {
    visitFloatFormat(visitor, typeHint, numberType);
  }

  @Override
  public JsonSerializer<?> createContextual(
    SerializerProvider prov,
    BeanProperty property
  ) throws JsonMappingException {
    if (property != null) {
      AnnotatedMember m = property.getMember();
      if (m != null) {
        JsonFormat.Value format = prov.getAnnotationIntrospector()
          .findFormat(m);
        if (format != null) {
          switch (format.getShape()) {
            case STRING:
              return ToStringSerializer.instance;
            default:
          }
        }
      }
    }
    return this;
  }

  @Override
  public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
    return createSchemaNode("number", true);
  }

  @Override
  public void serialize(T val, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    if (val != null) {
      try {
        writer.accept(jsonGenerator, sigFigs, val);
      } catch (Exception e) {
        Logs.logError(LOG, e, "Error writing number [%s] for class [%s] with sigFigs limit [%s]", val, _handledType, sigFigs);
      }
    } else {
      jsonGenerator.writeNull();
    }
  }

}
