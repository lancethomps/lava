package com.github.lancethomps.lava.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;

// TODO: Update all enums to use this
public class Enums {

  public static final Map<Class<?>, Enum<?>> DEFAULT_VALUE_MAP = new HashMap<>();

  public static final Map<Class<?>, Map<String, Enum<?>>> STRING_TO_TYPE_MAPS = new HashMap<>();

  public static final Set<Class<?>> THROWING_ENUMS = new HashSet<>();

  private static final Logger LOG = Logger.getLogger(Enums.class);

  public static <T extends Enum<?>> Map<String, Enum<?>> createStringToTypeMap(Class<T> type) {
    return createStringToTypeMap(type, null);
  }

  public static <T extends Enum<?>> Map<String, Enum<?>> createStringToTypeMap(Class<T> type, T defaultValue) {
    return createStringToTypeMap(type, defaultValue, (List<Function<T, Object>>) null);
  }

  @SafeVarargs
  public static <T extends Enum<?>> Map<String, Enum<?>> createStringToTypeMap(
    Class<T> type,
    T defaultValue,
    boolean throwException,
    Function<T, Object>... additionalSupplier
  ) {
    List<Function<T, Object>> additionalSuppliers =
      (additionalSupplier == null) || (additionalSupplier.length == 0) ? null : Arrays.asList(additionalSupplier);
    return createStringToTypeMap(type, defaultValue, throwException, additionalSuppliers);
  }

  public static <T extends Enum<?>> Map<String, Enum<?>> createStringToTypeMap(
    Class<T> type,
    T defaultValue,
    boolean throwException,
    List<Function<T, Object>> additionalSuppliers
  ) {
    return createStringToTypeMap(type, defaultValue, throwException, additionalSuppliers, null);
  }

  public static <T extends Enum<?>> Map<String, Enum<?>> createStringToTypeMap(
    Class<T> type,
    T defaultValue,
    boolean throwException,
    List<Function<T, Object>> additionalSuppliers,
    List<Function<T, Collection<?>>> additionalMultiSuppliers
  ) {
    synchronized (STRING_TO_TYPE_MAPS) {
      Map<String, Enum<?>> map = STRING_TO_TYPE_MAPS.computeIfAbsent(type, k -> new HashMap<>());
      try {
        for (T val : type.getEnumConstants()) {
          map.put(val.name(), val);
          map.put(val.name().toLowerCase(), val);
          if ((additionalSuppliers != null) && !additionalSuppliers.isEmpty()) {
            additionalSuppliers
              .stream()
              .map(func -> func.apply(val))
              .filter(Objects::nonNull)
              .filter(key -> !map.containsKey(key.toString()))
              .forEach(key -> map.put(key.toString(), val));
          }
          if ((additionalMultiSuppliers != null) && !additionalMultiSuppliers.isEmpty()) {
            additionalMultiSuppliers
              .stream()
              .map(func -> func.apply(val))
              .filter(Objects::nonNull)
              .flatMap(Collection::stream)
              .filter(Objects::nonNull)
              .filter(key -> !map.containsKey(key.toString()))
              .forEach(key -> map.put(key.toString(), val));
          }
        }
        if (defaultValue != null) {
          DEFAULT_VALUE_MAP.put(type, defaultValue);
        }
        if (throwException) {
          THROWING_ENUMS.add(type);
        }
      } catch (Throwable e) {
        Logs.logError(
          LOG,
          e,
          "Issue creating string to type map for [%s] with default value [%s] and throw exception set to [%s]",
          type,
          defaultValue,
          throwException
        );
      }
      return map;
    }
  }

  @SafeVarargs
  public static <T extends Enum<?>> Map<String, Enum<?>> createStringToTypeMap(
    Class<T> type,
    T defaultValue,
    Function<T, Object>... additionalSupplier
  ) {
    return createStringToTypeMap(type, defaultValue, false, additionalSupplier);
  }

  public static <T extends Enum<?>> Map<String, Enum<?>> createStringToTypeMap(
    Class<T> type,
    T defaultValue,
    List<Function<T, Object>> additionalSuppliers
  ) {
    return createStringToTypeMap(type, defaultValue, false, additionalSuppliers);
  }

  public static <T extends Enum<?>> T fromString(Class<T> type, String val) {
    return fromString(type, val, null);
  }

  public static <T extends Enum<?>> T fromString(Class<T> type, String val, T defaultValue) {
    return fromString(type, val, defaultValue, true);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Enum<?>> T fromString(Class<T> type, String val, T defaultValue, boolean allowGlobalDefault) {
    if ((type == null) || (val == null)) {
      return defaultValue;
    }
    Map<String, Enum<?>> map = STRING_TO_TYPE_MAPS.get(type);
    if (map == null) {
      map = createStringToTypeMap(type);
    }
    if (map == null) {
      T enumVal = (defaultValue == null) && allowGlobalDefault ? (T) DEFAULT_VALUE_MAP.get(type) : defaultValue;
      if ((enumVal == null) && THROWING_ENUMS.contains(type)) {
        throw new IllegalArgumentException(String.format("No enum of type [%s] matching string [%s]!", type, val));
      }
      return enumVal;
    }
    T enumVal = (T) map.get(val);
    if (enumVal == null) {
      enumVal =
        (T) map.getOrDefault(val.toLowerCase(), (defaultValue == null) && allowGlobalDefault ? (T) DEFAULT_VALUE_MAP.get(type) : defaultValue);
    }
    return enumVal;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Enum<?>> T getDefaultValue(Class<T> type) {
    return (T) DEFAULT_VALUE_MAP.get(type);
  }

}
