package com.lancethomps.lava.common.ser.jackson.types;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.google.common.collect.Sets;
import com.lancethomps.lava.common.collections.MapUtil;

public class CustomTypeIdResolver extends TypeIdResolverBase {

  public static final String FIX_PACKAGE_PREFIX = "com.lancethomps.server.api.domain";

  public static final String OLD_ROOT = "com.lancethomps.server.";

  public static final String REMOVE_PACKAGE_PREFIX = "com.lancethomps.lava.api.domain";

  public static final String REPLACE_WITH_PACKAGE_PREFIX = "_bws";

  public static final String ROOT_PACKAGE = "com.lancethomps.lava.";

  public static final Set<String> SHARED_PACKAGE_PREFIXES = Sets.newHashSet("api", "factory", "utils");

  private static boolean jsonShortenedType;

  private static Map<String, String> typeIdFixes;

  private static Map<String, String> typeIdPackageFixes = MapUtil.createFrom("com.lancethomps.server.", "com.lancethomps.lava.");

  private boolean shortenedTypeOverride;

  public CustomTypeIdResolver(JavaType baseType, TypeFactory typeFactory) {
    this(baseType, typeFactory, false);
  }

  public CustomTypeIdResolver(JavaType baseType, TypeFactory typeFactory, boolean shortenedTypeOverride) {
    super(baseType, typeFactory);
    this.shortenedTypeOverride = shortenedTypeOverride;
  }

  public static String getCorrectClassName(String id) {
    String resolvedId;
    if ((id != null)) {
      Entry<String, String> replacePackage;
      if (id.startsWith(REPLACE_WITH_PACKAGE_PREFIX)) {
        resolvedId = REMOVE_PACKAGE_PREFIX + StringUtils.removeStart(id, REPLACE_WITH_PACKAGE_PREFIX);
      } else if ((typeIdPackageFixes != null) &&
        ((replacePackage = typeIdPackageFixes.entrySet().stream().filter(e -> id.startsWith(e.getKey())).findAny().orElse(null)) != null)) {
        final String idSuffix = StringUtils.removeStart(id, replacePackage.getKey());
        resolvedId = replacePackage.getValue() + (SHARED_PACKAGE_PREFIXES.stream().anyMatch(idSuffix::startsWith) ? "" : "dtts.") + idSuffix;
      } else {
        resolvedId = id;
      }
      if (typeIdFixes != null) {
        return typeIdFixes.getOrDefault(resolvedId, resolvedId);
      }
    } else {
      resolvedId = null;
    }
    return resolvedId;
  }

  public static Map<String, String> getTypeIdFixes() {
    return typeIdFixes;
  }

  public static void setTypeIdFixes(Map<String, String> typeIdFixes) {
    CustomTypeIdResolver.typeIdFixes = typeIdFixes;
  }

  public static Map<String, String> getTypeIdPackageFixes() {
    return typeIdPackageFixes;
  }

  public static void setTypeIdPackageFixes(Map<String, String> typeIdPackageFixes) {
    CustomTypeIdResolver.typeIdPackageFixes = typeIdPackageFixes;
  }

  public static boolean isJsonShortenedType() {
    return jsonShortenedType;
  }

  public static void setJsonShortenedType(boolean jsonShortenedType) {
    CustomTypeIdResolver.jsonShortenedType = jsonShortenedType;
  }

  protected final String idFrom(Object value, Class<?> cls) {
    if (Enum.class.isAssignableFrom(cls)) {
      if (!cls.isEnum()) {
        cls = cls.getSuperclass();
      }
    }
    String str = cls.getName();
    if (str.startsWith("java.util")) {
      if (value instanceof EnumSet<?>) {
        Class<?> enumClass = ClassUtil.findEnumType((EnumSet<?>) value);
        str = TypeFactory.defaultInstance().constructCollectionType(EnumSet.class, enumClass).toCanonical();
      } else if (value instanceof EnumMap<?, ?>) {
        Class<?> enumClass = ClassUtil.findEnumType((EnumMap<?, ?>) value);
        Class<?> valueClass = Object.class;
        str = TypeFactory.defaultInstance().constructMapType(EnumMap.class, enumClass, valueClass).toCanonical();
      } else {
        String end = str.substring(9);
        if ((end.startsWith(".Arrays$") || end.startsWith(".Collections$"))
          && (str.indexOf("List") >= 0)) {
          str = "java.util.ArrayList";
        }
      }
    } else if (str.indexOf('$') >= 0) {
      Class<?> outer = ClassUtil.getOuterClass(cls);
      if (outer != null) {
        Class<?> staticType = _baseType.getRawClass();
        if (ClassUtil.getOuterClass(staticType) == null) {
          cls = _baseType.getRawClass();
          str = cls.getName();
        }
      }
      if (str.startsWith(REMOVE_PACKAGE_PREFIX)) {
        str = (jsonShortenedType || shortenedTypeOverride ? REPLACE_WITH_PACKAGE_PREFIX : FIX_PACKAGE_PREFIX) +
          StringUtils.removeStart(str, REMOVE_PACKAGE_PREFIX);
      }
    } else if (str.startsWith(REMOVE_PACKAGE_PREFIX)) {
      str = (jsonShortenedType || shortenedTypeOverride ? REPLACE_WITH_PACKAGE_PREFIX : FIX_PACKAGE_PREFIX) +
        StringUtils.removeStart(str, REMOVE_PACKAGE_PREFIX);
    }
    return str;
  }

  @Override
  public String getDescForKnownTypeIds() {
    return "class name used as type id";
  }

  @Override
  public JsonTypeInfo.Id getMechanism() {
    return JsonTypeInfo.Id.CLASS;
  }

  @Override
  public String idFromValue(Object value) {
    return idFrom(value, value.getClass());
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> type) {
    return idFrom(value, type);
  }

  public boolean isShortenedTypeOverride() {
    return shortenedTypeOverride;
  }

  public void setShortenedTypeOverride(boolean shortenedTypeOverride) {
    this.shortenedTypeOverride = shortenedTypeOverride;
  }

  public void registerSubtype(Class<?> type, String name) {

  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) {
    return resolveTypeFromId(id, context.getTypeFactory());
  }

  protected JavaType resolveTypeFromId(String id, TypeFactory typeFactory) {
    if (id.indexOf('<') > 0) {
      JavaType t = typeFactory.constructFromCanonical(id);
      return t;
    }
    id = getCorrectClassName(id);
    try {
      Class<?> cls = typeFactory.findClass(id);
      return typeFactory.constructSpecializedType(_baseType, cls);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Invalid type id '" + id + "' (for id type 'Id.class'): no such class found");
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid type id '" + id + "' (for id type 'Id.class'): " + e.getMessage(), e);
    }
  }

}
