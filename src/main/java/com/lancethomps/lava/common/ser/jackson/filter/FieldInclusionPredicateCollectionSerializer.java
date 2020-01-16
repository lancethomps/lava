package com.lancethomps.lava.common.ser.jackson.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiPredicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.std.CollectionSerializer;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.ser.jackson.JacksonUtils;

public class FieldInclusionPredicateCollectionSerializer extends CollectionSerializer {

  private static final Logger LOG = LogManager.getLogger(FieldInclusionPredicateCollectionSerializer.class);

  private static final long serialVersionUID = 5951376033544930044L;

  private final CollectionSerializer delegate;

  private final BiPredicate<Object, Object> predicate;

  public FieldInclusionPredicateCollectionSerializer(
    FieldInclusionPredicateCollectionSerializer src, BeanProperty property, TypeSerializer vts, JsonSerializer<?> valueSerializer,
    Boolean unwrapSingle
  ) {
    super(src, property, vts, valueSerializer, unwrapSingle);
    delegate = src.delegate;
    predicate = src.predicate;
  }

  public FieldInclusionPredicateCollectionSerializer(
    SerializationConfig config, JavaType valueType, BeanDescription beanDesc, CollectionSerializer delegate,
    BiPredicate<Object, Object> predicate
  ) {
    super(delegate, null, JacksonUtils.createTypeSerializer(config, valueType.getContentType()), delegate.getContentSerializer(), null);
    this.delegate = delegate;
    this.predicate = predicate;
  }

  @Override
  public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
    return new FieldInclusionPredicateCollectionSerializer(this, _property, vts, _elementSerializer, _unwrapSingle);
  }

  @Override
  public void serializeContents(Collection<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    if (_elementSerializer != null) {
      serializeContentsUsing(value, jgen, provider, _elementSerializer);
      return;
    }
    Iterator<?> it = value.iterator();
    if (!it.hasNext()) {
      return;
    }
    PropertySerializerMap serializers = _dynamicSerializers;
    final TypeSerializer typeSer = _valueTypeSerializer;

    int i = 0;
    try {
      do {
        Object elem = it.next();
        if (elem == null) {
          provider.defaultSerializeNull(jgen);
        } else if (shouldFilter(elem)) {
          Logs.logTrace(LOG, "Filtered collection element: %s", elem);
        } else {
          Class<?> cc = elem.getClass();
          JsonSerializer<Object> serializer = serializers.serializerFor(cc);
          if (serializer == null) {
            if (_elementType.hasGenericTypes()) {
              serializer = _findAndAddDynamic(serializers,
                provider.constructSpecializedType(_elementType, cc), provider
              );
            } else {
              serializer = _findAndAddDynamic(serializers, cc, provider);
            }
            serializers = _dynamicSerializers;
          }
          if (typeSer == null) {
            serializer.serialize(elem, jgen, provider);
          } else {
            serializer.serializeWithType(elem, jgen, provider, typeSer);
          }
        }
        ++i;
      } while (it.hasNext());
    } catch (Exception e) {
      wrapAndThrow(provider, e, value, i);
    }
  }

  @Override
  public void serializeContentsUsing(
    Collection<?> value, JsonGenerator jgen, SerializerProvider provider,
    JsonSerializer<Object> ser
  )
    throws IOException {
    Iterator<?> it = value.iterator();
    if (it.hasNext()) {
      TypeSerializer typeSer = _valueTypeSerializer;
      int i = 0;
      do {
        Object elem = it.next();
        try {
          if (elem == null) {
            provider.defaultSerializeNull(jgen);
          } else if (shouldFilter(elem)) {
            Logs.logTrace(LOG, "Filtered collection element: %s", elem);
          } else {
            if (typeSer == null) {
              ser.serialize(elem, jgen, provider);
            } else {
              ser.serializeWithType(elem, jgen, provider, typeSer);
            }
          }
          ++i;
        } catch (Exception e) {
          wrapAndThrow(provider, e, value, i);
        }
      } while (it.hasNext());
    }
  }

  @Override
  public FieldInclusionPredicateCollectionSerializer withResolved(
    BeanProperty property,
    TypeSerializer vts,
    JsonSerializer<?> elementSerializer,
    Boolean unwrapSingle
  ) {
    return new FieldInclusionPredicateCollectionSerializer(this, property, vts, elementSerializer, unwrapSingle);
  }

  private boolean shouldFilter(Object elem) {
    return (elem == null) || !predicate.test(null, elem);
  }

}
