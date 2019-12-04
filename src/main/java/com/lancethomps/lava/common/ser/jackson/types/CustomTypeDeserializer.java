package com.lancethomps.lava.common.ser.jackson.types;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserSequence;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class CustomTypeDeserializer extends AsPropertyTypeDeserializer {

  private static final Logger LOG = Logger.getLogger(CustomTypeDeserializer.class);

  private static final long serialVersionUID = 1L;

  public CustomTypeDeserializer(JavaType bt, TypeIdResolver idRes, String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
    this(bt, idRes, typePropertyName, typeIdVisible, defaultImpl, As.PROPERTY);
  }

  public CustomTypeDeserializer(
    JavaType bt,
    TypeIdResolver idRes,
    String typePropertyName,
    boolean typeIdVisible,
    JavaType defaultImpl,
    As inclusion
  ) {
    super(bt, idRes, typePropertyName, typeIdVisible, defaultImpl, inclusion);
  }

  public CustomTypeDeserializer(CustomTypeDeserializer src, BeanProperty property) {
    super(src, property);
  }

  @Override
  public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt) throws IOException {

    if (jp.canReadTypeId()) {
      Object typeId = jp.getTypeId();
      if (typeId != null) {
        return _deserializeWithNativeTypeId(jp, ctxt, typeId);
      }
    }

    JsonToken t = jp.getCurrentToken();
    if (t == JsonToken.START_OBJECT) {
      t = jp.nextToken();
    } else if (t == JsonToken.START_ARRAY) {
      return _deserializeTypedUsingDefaultImpl(jp, ctxt, null);
    } else if (t != JsonToken.FIELD_NAME) {
      return _deserializeTypedUsingDefaultImpl(jp, ctxt, null);
    }
    TokenBuffer tb = null;

    for (; t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
      String name = jp.getCurrentName();
      jp.nextToken();
      if (_typePropertyName.equals(name)) {
        return deserializeTypedForIdText(jp, ctxt, tb, jp.getText());
      }
      if (tb == null) {
        tb = new TokenBuffer(null, false);
      }
      tb.writeFieldName(name);
      tb.copyCurrentStructure(jp);
    }
    return _deserializeTypedUsingDefaultImpl(jp, ctxt, tb);
  }

  @Override
  public TypeDeserializer forProperty(BeanProperty prop) {
    return (prop == _property) ? this : new CustomTypeDeserializer(this, prop);
  }

  @Override
  protected Object _deserializeTypedUsingDefaultImpl(JsonParser jp, DeserializationContext ctxt, TokenBuffer tb) throws IOException {
    String typeId = _baseType.getRawClass().getName();
    JsonDeserializer<Object> deser = _findDeserializer(ctxt, typeId);
    if (deser != null) {
      if (tb != null) {
        tb.writeEndObject();
        jp = tb.asParser(jp);

        jp.nextToken();
      }
      return deser.deserialize(jp, ctxt);
    }
    Object result = TypeDeserializer.deserializeIfNatural(jp, ctxt, _baseType);
    if (result != null) {
      return result;
    }
    if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
      return super.deserializeTypedFromAny(jp, ctxt);
    }
    throw ctxt.wrongTokenException(
      jp,
      JsonToken.FIELD_NAME,
      "missing property '" + _typePropertyName + "' that is to contain type id  (for class " + baseTypeName() + ')'
    );
  }

  private Object deserializeTypedForIdText(JsonParser jp, DeserializationContext ctxt, TokenBuffer tb, String typeId) throws IOException {
    if (!typeId.contains(".")) {
      typeId = _baseType.getRawClass().getName();
    }
    JsonDeserializer<Object> deser = _findDeserializer(ctxt, typeId);
    if (_typeIdVisible) {
      if (tb == null) {
        tb = new TokenBuffer(null, false);
      }
      tb.writeFieldName(jp.getCurrentName());
      tb.writeString(typeId);
    }
    if (tb != null) {
      jp = JsonParserSequence.createFlattened(tb.asParser(jp), jp);
    }
    jp.nextToken();
    return deser.deserialize(jp, ctxt);
  }

}
