package com.lancethomps.lava.common.ser.jackson;

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.lancethomps.lava.common.lambda.ThrowingFunction;

public class CollectionDeserializerWithNonArray extends CollectionDeserializer {

  private static final long serialVersionUID = -7276603414402975221L;

  private final ThrowingFunction<JsonParser, Collection<Object>> function;

  public CollectionDeserializerWithNonArray(CollectionDeserializer src, ThrowingFunction<JsonParser, Collection<Object>> function) {
    super(src);
    this.function = function;
  }

  public CollectionDeserializerWithNonArray(
    JavaType collectionType, JsonDeserializer<Object> valueDeser, TypeDeserializer valueTypeDeser, ValueInstantiator valueInstantiator,
    JsonDeserializer<Object> delegateDeser, NullValueProvider nuller, Boolean unwrapSingle, ThrowingFunction<JsonParser, Collection<Object>> function
  ) {
    super(collectionType, valueDeser, valueTypeDeser, valueInstantiator, delegateDeser, nuller, unwrapSingle);
    this.function = function;
  }

  @Override
  public Collection<Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (!p.isExpectedStartArrayToken() && (function != null)) {
      try {
        return function.apply(p);
      } catch (Exception e) {
        throw new IOException(e);
      }
    }
    return super.deserialize(p, ctxt);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected CollectionDeserializer withResolved(
    JsonDeserializer<?> dd,
    JsonDeserializer<?> vd,
    TypeDeserializer vtd,
    NullValueProvider nuller,
    Boolean unwrapSingle
  ) {
    if ((dd == _delegateDeserializer) && (vd == _valueDeserializer) && (vtd == _valueTypeDeserializer) && (_unwrapSingle == unwrapSingle)) {
      return this;
    }
    return new CollectionDeserializerWithNonArray(
      _containerType,
      (JsonDeserializer<Object>) vd,
      vtd,
      _valueInstantiator,
      (JsonDeserializer<Object>) dd,
      nuller,
      unwrapSingle,
      function
    );
  }

}
