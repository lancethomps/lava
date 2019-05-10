package com.github.lancethomps.lava.common.ser.jackson;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.std.MapProperty;
import com.github.lancethomps.lava.common.lambda.ThrowingFunction;
import com.github.lancethomps.lava.common.lambda.ThrowingTriFunction;
import com.github.lancethomps.lava.common.merge.Merges;

public class JacksonUtils {

  public static TypeSerializer createTypeSerializer(SerializationConfig config, JavaType baseType) {
    BeanDescription bean = config.introspectClassAnnotations(baseType.getRawClass());
    AnnotatedClass ac = bean.getClassInfo();
    AnnotationIntrospector ai = config.getAnnotationIntrospector();
    TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);

    Collection<NamedType> subtypes = null;
    if (b == null) {
      b = config.getDefaultTyper(baseType);
    } else {
      subtypes = config.getSubtypeResolver().collectAndResolveSubtypesByClass(config, ac);
    }
    if (b == null) {
      return null;
    }

    return b.buildTypeSerializer(config, baseType, subtypes);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Object> T getProperty(@Nonnull Object pojo, @Nonnull PropertyWriter writer) throws Exception {
    if (writer instanceof BeanPropertyWriter) {
      return (T) ((BeanPropertyWriter) writer).get(pojo);
    } else if (writer instanceof MapProperty) {
      return (T) pojo;
    }
    return null;
  }

  public static void transformFieldValues(
      JsonNode node,
      String fieldName,
      ThrowingFunction<JsonNode, Object> transform
  ) {
    transformFieldValues(node, Collections.singleton(fieldName), (field, parent, fieldNode) -> transform.apply(fieldNode));
  }

  public static void transformFieldValues(
      JsonNode node,
      String fieldName,
      ThrowingTriFunction<String, ObjectNode, JsonNode, Object> transform
  ) {
    transformFieldValues(node, Collections.singleton(fieldName), transform);
  }

  public static void transformFieldValues(
      JsonNode node,
      Collection<String> fieldNames,
      ThrowingTriFunction<String, ObjectNode, JsonNode, Object> transform
  ) {
    for (String fieldName : fieldNames) {
      for (JsonNode parentTemp : node.findParents(fieldName)) {
        ObjectNode parent = (ObjectNode) parentTemp;
        Object converted = transform.applyWithSneakyThrow(fieldName, parent, parent.get(fieldName));
        parent.set(fieldName, Merges.MERGE_MAPPER.convertValue(converted, JsonNode.class));
      }
    }
  }

}
