package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;

public class CustomSingleValueBeanDeserializer extends BeanDeserializer {

  private static final long serialVersionUID = 8952958753951093572L;

  private Function<Double, Object> doubleCreator;

  private Function<Number, Object> numberCreator;

  private Function<String, Object> stringCreator;

  public CustomSingleValueBeanDeserializer(JsonDeserializer<?> defaultDeserializer) {
    this(defaultDeserializer, null);
  }

  public CustomSingleValueBeanDeserializer(JsonDeserializer<?> defaultDeserializer, Function<String, Object> stringCreator) {
    super((BeanDeserializerBase) defaultDeserializer);
    this.stringCreator = stringCreator;
  }

  @Override
  public Object deserializeFromDouble(JsonParser p, DeserializationContext ctxt) throws IOException {
    return doubleCreator != null ? doubleCreator.apply(p.getDoubleValue()) : super.deserializeFromDouble(p, ctxt);
  }

  @Override
  public Object deserializeFromNumber(JsonParser p, DeserializationContext ctxt) throws IOException {
    return numberCreator != null ? numberCreator.apply(p.getNumberValue()) : super.deserializeFromNumber(p, ctxt);
  }

  @Override
  public Object deserializeFromString(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (_objectIdReader != null) {
      return deserializeFromObjectId(p, ctxt);
    }
    return stringCreator != null ? stringCreator.apply(p.getText()) : super.deserializeFromString(p, ctxt);
  }

  public Function<Double, Object> getDoubleCreator() {
    return doubleCreator;
  }

  public CustomSingleValueBeanDeserializer setDoubleCreator(Function<Double, Object> doubleCreator) {
    this.doubleCreator = doubleCreator;
    return this;
  }

  public Function<Number, Object> getNumberCreator() {
    return numberCreator;
  }

  public CustomSingleValueBeanDeserializer setNumberCreator(Function<Number, Object> numberCreator) {
    this.numberCreator = numberCreator;
    return this;
  }

  public Function<String, Object> getStringCreator() {
    return stringCreator;
  }

  public CustomSingleValueBeanDeserializer setStringCreator(Function<String, Object> stringCreator) {
    this.stringCreator = stringCreator;
    return this;
  }

}
