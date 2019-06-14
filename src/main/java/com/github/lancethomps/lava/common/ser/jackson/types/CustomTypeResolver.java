package com.github.lancethomps.lava.common.ser.jackson.types;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Set;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.google.common.collect.Sets;

public class CustomTypeResolver extends DefaultTypeResolverBuilder {

  public static final Set<String> IGNORE_PACKAGES = Sets.newHashSet();

  public static final Set<Class<?>> IGNORE_TYPES = Sets.newHashSet(LocalDate.class, LocalDateTime.class, ZonedDateTime.class);

  public static final Set<String> INCLUDE_PACKAGES = Sets.newHashSet(
    "com.github.lancethomps.lava",
    "graphql",
    "org.apache.commons.math3.linear"
  );

  private static final long serialVersionUID = 7025088197372998890L;

  private boolean shortenedTypeOverride;

  public CustomTypeResolver() {
    this(false);
  }

  public CustomTypeResolver(boolean shortenedTypeOverride) {
    super(DefaultTyping.NON_FINAL);
    this.shortenedTypeOverride = shortenedTypeOverride;
  }

  public TypeDeserializer buildTypeDes(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
    TypeIdResolver idRes = idResolver(config, baseType, subtypes, false, true);
    JavaType defaultImpl;

    if (_defaultImpl == null) {
      defaultImpl = null;
    } else {
      if ((_defaultImpl == Void.class) || (_defaultImpl == NoClass.class)) {
        defaultImpl = config.getTypeFactory().constructType(_defaultImpl);
      } else {
        defaultImpl = config.getTypeFactory().constructSpecializedType(baseType, _defaultImpl);
      }
    }
    return new CustomTypeDeserializer(baseType, idRes, _typeProperty, _typeIdVisible, defaultImpl, _includeAs);
  }

  @Override
  public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
    return useForType(baseType) ? buildTypeDes(config, baseType, subtypes) : null;
  }

  @Override
  public boolean useForType(JavaType t) {
    while (t.isArrayType()) {
      t = t.getContentType();
    }
    String className = t.getRawClass().getName();
    if (Temporal.class.isAssignableFrom(t.getRawClass()) || IGNORE_TYPES.contains(t.getRawClass())) {
      return false;
    }
    if ((!t.isFinal() || (!t.isEnumType() && className.startsWith("com.github.lancethomps.lava"))) &&
      !TreeNode.class.isAssignableFrom(t.getRawClass())) {
      for (String pack : INCLUDE_PACKAGES) {
        if (className.startsWith(pack) && !IGNORE_PACKAGES.stream().anyMatch(className::startsWith)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected TypeIdResolver idResolver(MapperConfig<?> config, JavaType baseType, Collection<NamedType> subtypes, boolean forSer, boolean forDeser) {
    return new CustomTypeIdResolver(baseType, config.getTypeFactory(), shortenedTypeOverride);
  }

}
