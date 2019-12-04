package com.lancethomps.lava.common;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.lancethomps.lava.common.date.Dates;
import com.lancethomps.lava.common.ser.Serializer;
import com.lancethomps.lava.common.string.StringUtil;
import com.google.common.collect.ImmutableMap;
import com.lancethomps.lava.common.merge.Merges;

public class Randoms {

  public static final SecureRandom RANDOM_GEN = new SecureRandom();

  private static final Map<JavaType, Supplier> RANDOM_VALUE_SUPPLIERS = new ConcurrentHashMap<>(ImmutableMap.<JavaType, Supplier>builder()
    .put(Merges.MERGE_MAPPER.constructType(Boolean.class), () -> Randoms.RANDOM_GEN.nextBoolean())
    .put(Merges.MERGE_MAPPER.constructType(Boolean.TYPE), () -> Randoms.RANDOM_GEN.nextBoolean())
    .put(Merges.MERGE_MAPPER.constructType(String.class), () -> StringUtil.generateUniqueId(20))
    .put(Merges.MERGE_MAPPER.constructType(Date.class), () -> Dates.toOldDate(createRandomDateTime()))
    .put(Merges.MERGE_MAPPER.constructType(Byte.class), () -> {
      byte[] bytes = new byte[1];
      Randoms.RANDOM_GEN.nextBytes(bytes);
      return bytes[0];
    })
    .put(Merges.MERGE_MAPPER.constructType(Byte.TYPE), () -> {
      byte[] bytes = new byte[1];
      Randoms.RANDOM_GEN.nextBytes(bytes);
      return bytes[0];
    })
    .build()
  );

  public static <T> void addRandomValueSupplier(@Nonnull Class<T> type, @Nonnull Supplier<T> supplier) {
    addRandomValueSupplier(Merges.MERGE_MAPPER.constructType(type), supplier);
  }

  public static <T> void addRandomValueSupplier(@Nonnull JavaType type, @Nonnull Supplier<T> supplier) {
    RANDOM_VALUE_SUPPLIERS.put(type, supplier);
  }

  public static LocalDateTime createRandomDateTime() {
    return createRandomDateTime(20);
  }

  public static LocalDateTime createRandomDateTime(int minYear) {
    int year = randomInt(LocalDateTime.now().getYear() - minYear, LocalDateTime.now().getYear() + 1);
    Month month = createRandomEnum(Month.class);
    LocalDate date = LocalDate.of(year, month, randomInt(1, month.length(Year.isLeap(year))));
    return LocalDateTime.of(date, LocalTime.of(randomInt(0, 23), randomInt(0, 59), randomInt(0, 59)));
  }

  public static <T extends Enum<?>> T createRandomEnum(Class<T> type) {
    T[] vals = type.getEnumConstants();
    return vals[RANDOM_GEN.nextInt(vals.length)];
  }

  public static <T> T createRandomValue(@Nonnull Class<T> type) {
    return createRandomValue(Merges.MERGE_MAPPER.constructType(type));
  }

  public static <T> T createRandomValue(@Nonnull Class<T> type, @Nullable Collection<String> fields) {
    return createRandomValue(type, fields, null);
  }

  public static <T> T createRandomValue(@Nonnull Class<T> type, @Nullable Collection<String> fields, @Nullable Collection<String> skipFields) {
    return createRandomValue(type, fields, skipFields, true);
  }

  public static <T> T createRandomValue(
    @Nonnull Class<T> type,
    @Nullable Collection<String> fields,
    @Nullable Collection<String> skipFields,
    boolean recurseSameClass
  ) {
    return createRandomValue(Merges.MERGE_MAPPER.constructType(type), fields, skipFields, recurseSameClass);
  }

  public static <T> T createRandomValue(
    @Nonnull Class<T> type,
    @Nullable Collection<String> fields,
    @Nullable Collection<String> skipFields,
    @Nullable Set<JavaType> skipTypes
  ) {
    return createRandomValue(Merges.MERGE_MAPPER.constructType(type), fields, skipFields, skipTypes);
  }

  public static <T> T createRandomValue(@Nonnull JavaType type) {
    return createRandomValue(type, null, new HashMap<>(), null, null, null);
  }

  public static <T> T createRandomValue(@Nonnull JavaType type, @Nullable Collection<String> fields) {
    return createRandomValue(type, fields, null);
  }

  public static <T> T createRandomValue(@Nonnull JavaType type, @Nullable Collection<String> fields, @Nullable Collection<String> skipFields) {
    return createRandomValue(type, fields, skipFields, true);
  }

  public static <T> T createRandomValue(
    @Nonnull JavaType type,
    @Nullable Collection<String> fields,
    @Nullable Collection<String> skipFields,
    boolean recurseSameClass
  ) {
    Set<JavaType> skipTypes = new HashSet<>();
    if (!recurseSameClass) {
      skipTypes.add(type);
    }
    return createRandomValue(type, fields, skipFields, skipTypes);
  }

  public static <T> T createRandomValue(
    @Nonnull JavaType type,
    @Nullable Collection<String> fields,
    @Nullable Collection<String> skipFields,
    @Nullable Set<JavaType> skipTypes
  ) {
    return createRandomValue(type, null, new HashMap<>(), fields, skipFields, skipTypes);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getRandomFromCollection(@Nonnull Collection<T> vals) {
    return (T) vals.toArray()[RANDOM_GEN.nextInt(vals.size())];
  }

  public static <T> List<T> getRandomSubList(@Nonnull List<T> vals, int size) {
    return IntStream
      .iterate(0, i -> i + 1)
      .limit(Math.min(size, vals.size()))
      .mapToObj(i -> vals.get(RANDOM_GEN.nextInt(vals.size())))
      .collect(Collectors.toList());
  }

  public static int randomInt(int lowerBound, int upperBound) {
    return Randoms.RANDOM_GEN.nextInt((upperBound + 1) - lowerBound) + lowerBound;
  }

  @SuppressWarnings("unchecked")
  private static <T> T createRandomValue(
    @Nonnull JavaType type,
    @Nullable String fieldName,
    @Nonnull Map<JavaType, Set<String>> recursedFieldsByType,
    @Nullable Collection<String> fields,
    @Nullable Collection<String> skipFields,
    @Nullable Set<JavaType> skipTypes
  ) {
    if ((fieldName != null) && (skipTypes != null) && skipTypes.contains(type)) {
      return null;
    }
    if (!type.isConcrete() && !type.isContainerType()) {
      return null;
    }
    if (type.isTypeOrSubTypeOf(Number.class)) {
      return Serializer.convert(randomInt(-100, 100) + Math.random(), type);
    } else if (type.isEnumType()) {
      return (T) createRandomEnum((Class<? extends Enum<?>>) type.getRawClass());
    } else if (Temporal.class.isAssignableFrom(type.getRawClass())) {
      if (type.getRawClass().getSimpleName().startsWith("Local")) {
        LocalDate date = LocalDate.of(
          randomInt(LocalDateTime.now().getYear() - 20, LocalDateTime.now().getYear() + 1),
          createRandomEnum(Month.class),
          randomInt(1, 28)
        );
        if (type.getRawClass() == LocalDate.class) {
          return (T) date;
        } else if (type.getRawClass() == LocalDateTime.class) {
          return (T) LocalDateTime.of(date, LocalTime.of(randomInt(0, 23), randomInt(0, 59), randomInt(0, 59)));
        }
      }
      return null;
    } else if (RANDOM_VALUE_SUPPLIERS.containsKey(type)) {
      return (T) RANDOM_VALUE_SUPPLIERS.get(type).get();
    } else if (type.isCollectionLikeType() || type.isArrayType()) {
      JavaType innerType = type.getContentType();
      if ((innerType == null) || ((skipTypes != null) && skipTypes.contains(innerType))) {
        return null;
      }
      Object innerVal = createRandomValue(innerType, fieldName, recursedFieldsByType, fields, skipFields, skipTypes);
      if (innerVal == null) {
        return null;
      }
      int randomSize = randomInt(2, 20);
      List<Object> randomList = new ArrayList<>();
      randomList.add(innerVal);
      int count = 1;
      while (count < randomSize) {
        count++;
        randomList.add(createRandomValue(innerType, fieldName, recursedFieldsByType, fields, skipFields, skipTypes));
      }
      return Serializer.convert(randomList, type);
    } else if (type.isMapLikeType()) {
      JavaType keyType = type.getKeyType();
      JavaType valueType = type.getContentType();
      if ((keyType == null) || (valueType == null) || ((skipTypes != null) && (skipTypes.contains(keyType) || skipTypes.contains(valueType)))) {
        return null;
      }
      Object keyVal = createRandomValue(keyType, fieldName, recursedFieldsByType, fields, skipFields, skipTypes);
      Object valueVal = createRandomValue(valueType, fieldName, recursedFieldsByType, fields, skipFields, skipTypes);
      if ((keyVal != null) && (valueVal != null)) {
        int randomSize = randomInt(2, 20);
        Map<Object, Object> randomMap = new HashMap<>();
        randomMap.put(keyVal, valueVal);
        int count = 1;
        while (count < randomSize) {
          count++;
          randomMap.put(
            createRandomValue(keyType, fieldName, recursedFieldsByType, fields, skipFields, skipTypes),
            createRandomValue(valueType, fieldName, recursedFieldsByType, fields, skipFields, skipTypes)
          );
        }
        return Serializer.convert(randomMap, type);
      }
    } else if (type.isPrimitive()) {
      return null;
    } else if (type.isJavaLangObject()) {
      return (T) RANDOM_VALUE_SUPPLIERS
        .get(new ArrayList<>(RANDOM_VALUE_SUPPLIERS.keySet()).get(randomInt(0, RANDOM_VALUE_SUPPLIERS.keySet().size() - 1)))
        .get();
    } else {
      return Serializer.fromMap(
        Stream
          .of(BeanUtils.getPropertyDescriptors(type.getRawClass()))
          .filter(pd -> (pd.getReadMethod() != null) && (pd.getWriteMethod() != null))
          .filter(
            pd -> (fields == null) || fields.isEmpty() || fields.contains(pd.getName())
          )
          .filter(pd -> (skipFields == null) || !skipFields.contains(pd.getName()))
          .filter(pd -> {
            Field field = Reflections.getField(type.getRawClass(), pd.getName());
            JavaType fieldType = Merges.MERGE_MAPPER.constructType(field.getGenericType());
            if (isNonRecursiveRandomValueGenerator(fieldType)) {
              return true;
            }
            return recursedFieldsByType.computeIfAbsent(type, k -> new HashSet<>()).add(pd.getName());
          })
          .collect(Collectors.toList())
          .stream()
          .map(pd -> {
            Field field = Reflections.getField(type.getRawClass(), pd.getName());
            JavaType fieldType = Merges.MERGE_MAPPER.constructType(field.getGenericType());
            Object val = createRandomValue(fieldType, pd.getName(), recursedFieldsByType, fields, skipFields, skipTypes);
            return val == null ? null : Pair.of(pd.getName(), val);
          })
          .filter(Objects::nonNull)
          .collect(Collect.pairsToMap()),
        type
      );
    }
    return null;
  }

  private static boolean isNonRecursiveRandomValueGenerator(@Nonnull JavaType type) {
    if (Serializer.isSimpleOutputClass(type.getRawClass())) {
      return true;
    }
    if (RANDOM_VALUE_SUPPLIERS.containsKey(type)) {
      return true;
    }
    if (type.isCollectionLikeType() || type.isArrayType()) {
      JavaType innerType = type.getContentType();
      return (innerType != null) && isNonRecursiveRandomValueGenerator(innerType);
    }
    if (type.isMapLikeType()) {
      JavaType keyType = type.getKeyType();
      JavaType valueType = type.getContentType();
      return (keyType != null) && (valueType != null) && isNonRecursiveRandomValueGenerator(keyType) && isNonRecursiveRandomValueGenerator(valueType);
    }
    return false;
  }

}
