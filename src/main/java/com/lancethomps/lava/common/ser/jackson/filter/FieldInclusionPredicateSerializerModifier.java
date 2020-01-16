package com.lancethomps.lava.common.ser.jackson.filter;

import java.util.Map;
import java.util.function.BiPredicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.CollectionSerializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.lancethomps.lava.common.logging.Logs;

public class FieldInclusionPredicateSerializerModifier extends BeanSerializerModifier {

  private static final Logger LOG = LogManager.getLogger(FieldInclusionPredicateSerializerModifier.class);

  private final Map<Class<?>, BiPredicate<Object, Object>> predicateByType;

  public FieldInclusionPredicateSerializerModifier(Map<Class<?>, BiPredicate<Object, Object>> predicateByType) {
    super();
    this.predicateByType = predicateByType;
  }

  public Map<Class<?>, BiPredicate<Object, Object>> getPredicateByType() {
    return predicateByType;
  }

  @Override
  public JsonSerializer<?> modifyArraySerializer(
    SerializationConfig config,
    ArrayType valueType,
    BeanDescription beanDesc,
    JsonSerializer<?> serializer
  ) {
    return modifySerializerForContentType(config, valueType, beanDesc, serializer);
  }

  @Override
  public JsonSerializer<?> modifyCollectionLikeSerializer(
    SerializationConfig config,
    CollectionLikeType valueType,
    BeanDescription beanDesc,
    JsonSerializer<?> serializer
  ) {
    return modifySerializerForContentType(config, valueType, beanDesc, serializer);
  }

  @Override
  public JsonSerializer<?> modifyCollectionSerializer(
    SerializationConfig config,
    CollectionType valueType,
    BeanDescription beanDesc,
    JsonSerializer<?> serializer
  ) {
    return modifySerializerForContentType(config, valueType, beanDesc, serializer);
  }

  private JsonSerializer<?> modifySerializerForContentType(
    SerializationConfig config,
    JavaType valueType,
    BeanDescription beanDesc,
    JsonSerializer<?> serializer
  ) {
    BiPredicate<Object, Object> predicate;
    JavaType contentType = valueType.getContentType();
    if ((contentType != null) && (contentType.getRawClass() != null)
      && ((predicate = FieldInclusionPredicateFilter.getIncludePredicateForType(predicateByType, contentType.getRawClass())) != null)
      && (serializer instanceof CollectionSerializer)) {
      Logs.logTrace(
        LOG,
        "Modified serializer for config [%s], value type [%s], bean description [%s] and original serializer [%s]",
        config,
        valueType,
        beanDesc,
        serializer
      );
      return new FieldInclusionPredicateCollectionSerializer(config, valueType, beanDesc, (CollectionSerializer) serializer, predicate);
    }
    return serializer;
  }

}
