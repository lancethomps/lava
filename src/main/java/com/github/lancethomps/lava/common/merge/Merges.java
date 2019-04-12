package com.github.lancethomps.lava.common.merge;

import static com.github.lancethomps.lava.common.expr.ExprFactory.evalOgnl;
import static com.github.lancethomps.lava.common.lambda.Lambdas.functionIfNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.lambda.Lambdas;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.ser.SerializerFactory;

public class Merges {

  public static final ObjectMapper MERGE_MAPPER = SerializerFactory.getJsonMapper(true, false).configure(MapperFeature.USE_ANNOTATIONS, false);
  public static final MergeConfig NO_OVERWRITE_MERGE_CONFIG = getNoOverwriteMergeConfig().disableModifications();
  public static final MergeConfig NO_OVERWRITE_MERGE_CONFIG_WITH_ARRAY_SKIPPING = getNoOverwriteMergeConfigWithArraySkipping().disableModifications();
  public static final MergeConfig OVERWRITE_MERGE_CONFIG = getOverwriteMergeConfig().disableModifications();
  private static final MergeConfig DEFAULT_MERGE_CONFIG = getDefaultMergeConfig().disableModifications();
  private static final Logger LOG = Logger.getLogger(Merges.class);

  static {
    ignoreTypesWhenMerging(ObjectMapper.class);
  }

  public static <T> Boolean combineBooleans(@Nonnull T src, @Nonnull T target, @Nonnull Function<T, Boolean> getter, Boolean priorityValue) {
    Boolean srcVal = getter.apply(src);
    Boolean targetVal = getter.apply(target);
    if (srcVal == null) {
      return targetVal;
    }
    if (targetVal == null) {
      return srcVal;
    }
    if (priorityValue != null) {
      if (priorityValue.equals(srcVal) || priorityValue.equals(targetVal)) {
        return priorityValue;
      }
    }
    return targetVal;
  }

  public static <C extends Collection<V>, V> C combineCollections(@Nullable C srcVal, @Nullable C targetVal) {
    if (srcVal == null) {
      return targetVal;
    }
    if (targetVal == null) {
      return srcVal;
    }
    targetVal.addAll(srcVal);
    return targetVal;
  }

  public static <T, C extends Collection<V>, V> C combineCollections(@Nonnull T src, @Nonnull T target, @Nonnull Function<T, C> getter) {
    C srcVal = getter.apply(src);
    C targetVal = getter.apply(target);
    if (srcVal == null) {
      return targetVal;
    }
    if (targetVal == null) {
      return srcVal;
    }
    targetVal.addAll(srcVal);
    return targetVal;
  }

  public static <T, M extends Map<K, V>, K, V> M combineMaps(@Nonnull T src, @Nonnull T target, @Nonnull Function<T, M> getter) {
    M srcVal = getter.apply(src);
    M targetVal = getter.apply(target);
    if (srcVal == null) {
      return targetVal;
    }
    if (targetVal == null) {
      return srcVal;
    }
    return deepMerge(srcVal, targetVal);
  }

  // TODO: this needs to be updated to properly handle different src & target types, as well as all

  @SuppressWarnings("unchecked")
  public static <S extends Number, T extends Number> T combineNumericalValues(
    @Nonnull MergeConfig config,
    @Nullable S srcVal,
    @Nullable T targetVal
  ) {
    if (srcVal != null) {
      if (targetVal == null) {
        return (T) srcVal;
      }
      if (srcVal instanceof Long) {
        Long combined = ((Long) srcVal) + ((Long) targetVal);
        return (T) combined;
      } else if (srcVal instanceof Integer) {
        Integer combined = ((Integer) srcVal) + ((Integer) targetVal);
        return (T) combined;
      } else if (srcVal instanceof Double) {
        Double combined = ((Double) srcVal) + ((Double) targetVal);
        return (T) combined;
      } else if (srcVal instanceof Float) {
        Float combined = ((Float) srcVal) + ((Float) targetVal);
        return (T) combined;
      }
    }
    return targetVal;
  }

  public static <T, N extends Number> N combineNumericalValues(@Nonnull T src, @Nonnull T target, @Nonnull Function<T, N> getter) {
    return combineNumericalValues(DEFAULT_MERGE_CONFIG, getter.apply(src), getter.apply(target));
  }

  public static <T> String combineStrings(@Nonnull T src, @Nonnull T target, @Nonnull Function<T, String> getter) {
    return combineStrings(src, target, getter, "\n");
  }

  public static <T> String combineStrings(@Nonnull T src, @Nonnull T target, @Nonnull Function<T, String> getter, @Nonnull String separator) {
    String srcVal = getter.apply(src);
    String targetVal = getter.apply(target);
    if (srcVal == null) {
      return targetVal;
    }
    if (targetVal == null) {
      return srcVal;
    }
    return targetVal + separator + srcVal;
  }

  public static ArrayNode createNewArrayNode() {
    return new ArrayNode(MERGE_MAPPER.getNodeFactory());
  }

  public static ObjectNode createNewObjectNode() {
    return new ObjectNode(MERGE_MAPPER.getNodeFactory());
  }

  public static <T> T deepMerge(T src, T target) {
    return deepMerge(src, target, DEFAULT_MERGE_CONFIG);
  }

  public static <T> T deepMerge(T src, T target, MergeConfig config) {
    return deepMerge(src, target, config, true);
  }

  public static MergeConfig getDefaultMergeConfig() {
    return getNoOverwriteMergeConfig();
  }

  public static MergeConfig getNoOverwriteMergeConfig() {
    return new MergeConfig().setCreateNewBean(false).setMergeArrayElements(false).setOverwriteExisting(false).setOverwriteWithNull(false);
  }

  public static MergeConfig getNoOverwriteMergeConfigWithArraySkipping() {
    return getNoOverwriteMergeConfig().setAddToTargetArray(false);
  }

  public static MergeConfig getOverwriteMergeConfig() {
    return new MergeConfig()
      .setCreateNewBean(false)
      .setMergeArrayElements(false)
      .setOverwriteExisting(true)
      .setOverwriteWithNull(false)
      .setOverwriteArrayNodes(true);
  }

  public static void ignoreTypesWhenMerging(Class<?>... types) {
    synchronized (MERGE_MAPPER) {
      for (Class<?> type : types) {
        MERGE_MAPPER.configOverride(type).setIsIgnoredType(true);
      }
    }
  }

  public static void mergeArrayNodes(ArrayNode srcVal, ArrayNode targetVal, MergeConfig config, MergeConfig fieldConfig) {
    if (fieldConfig.testRemoveFromTargetArray() || fieldConfig.testRemoveDuplicatesFromTargetArray()) {
      List<Integer> removePositions = new ArrayList<>();
      srcVal.forEach(srcNode -> {
        Function<JsonNode, Object> nodeValueFunc;
        if (srcNode.isNull()) {
          return;
        } else if (!srcNode.isValueNode()) {
          return;
        } else if (srcNode.isNumber()) {
          nodeValueFunc = (node) -> node.numberValue();
        } else if (srcNode.isTextual()) {
          nodeValueFunc = (node) -> node.textValue();
        } else {
          return;
        }
        JsonNodeType nodeType = srcNode.getNodeType();
        Object srcNodeVal = nodeValueFunc.apply(srcNode);
        for (int pos = 0; pos < targetVal.size(); pos++) {
          JsonNode targetNode = targetVal.get(pos);
          if (!targetNode.isValueNode() || targetNode.isNull() || !(targetNode.getNodeType() == nodeType)) {
            continue;
          }
          if (Objects.equals(srcNodeVal, nodeValueFunc.apply(targetNode))) {
            removePositions.add(pos);
          }
        }
      });
      if (!removePositions.isEmpty()) {
        int subtractAmount = 0;
        for (Integer pos : removePositions) {
          int removePos = pos - subtractAmount;
          subtractAmount++;
          targetVal.remove(removePos);
        }
      }
      if (fieldConfig.testRemoveFromTargetArray()) {
        return;
      }
    }

    final boolean mergeArrayElements = fieldConfig.getMergeArrayElements() != null ? fieldConfig.getMergeArrayElements()
      : Checks.isNotBlank(fieldConfig.getMergeArrayElementsMatchField());
    if (!mergeArrayElements) {
      if (!fieldConfig.testAddToTargetArray()) {
        return;
      }
      targetVal.addAll(srcVal);
      return;
    }
    if (Checks.isNotBlank(fieldConfig.getMergeArrayElementsMatchField())) {
      Map<String, ObjectNode> srcMap = createArrayNodeMapByField(fieldConfig, srcVal);
      Map<String, ObjectNode> targetMap = createArrayNodeMapByField(fieldConfig, targetVal);
      srcMap.forEach((id, srcNode) -> {
        ObjectNode targetNode = targetMap.get(id);
        if (targetNode != null) {
          deepMerge(srcNode, targetNode, fieldConfig, false);
        } else if (!fieldConfig.testMergeArrayElementsSkipNonMatching()) {
          targetVal.add(srcNode);
        }
      });
      return;
    }
    for (int pos = 0; pos < srcVal.size(); pos++) {
      JsonNode srcNode = srcVal.get(pos);
      if (srcNode.isNull()) {
        continue;
      }

      if (targetVal.size() <= pos) {
        targetVal.add(srcNode);
        continue;
      }
      JsonNode targetNode = targetVal.get(pos);
      if (targetNode.isNull()) {
        targetVal.set(pos, srcNode);
        continue;
      }
      if (srcNode.isObject() && targetNode.isObject()) {
        deepMerge(srcNode, targetNode, fieldConfig, false);
      } else if (fieldConfig.testOverwriteExisting()) {
        targetVal.set(pos, srcNode);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends Object> T mergeIntoResult(MergeConfig config, Map<String, Object> data, Map<String, Object> current, Class<T> resultType) {
    final Map<String, Object> result = current == null ? new HashMap<>() : new HashMap<>(current);
    config.getFieldExpressions().stream().filter(field -> {
      return (field.getEnabled() == null) || field.getEnabled().stream().anyMatch(
        expr -> functionIfNonNull(evalOgnl(data, expr, false), res -> (res instanceof Boolean) && ((Boolean) res)).orElse(false)
      );
    }).forEach(field -> {
      Object val = field
        .getValue()
        .stream()
        .map(expr -> evalOgnl(data, expr, false))
        .filter(Objects::nonNull)
        .findFirst()
        .map(v -> functionIfNonNull(field.getPostProcess(), pp -> evalOgnl(v, pp, false)).orElse(null))
        .orElse(null);
      if ((val != null) || field.testOverwriteWithNull()) {
        Lambdas.consumeIfNonNull(field.getResultKeys(), keys -> keys.forEach(key -> Serializer.addPathKeyToMap(result, key, val)));
        Lambdas.consumeIfNonNull(field.getResult(), expressions -> expressions.forEach(expr -> ExprFactory.evalOgnl(result, expr, false)));
      }
    });
    return (resultType == null) || Map.class.isAssignableFrom(resultType) ? (T) result : Serializer.fromMap(result, resultType);
  }

  public static <T> List<T> mergeLists(List<T> src, List<T> target, Function<T, Object> fieldMatcher, MergeConfig config) {
    if ((src == null) || src.isEmpty()) {
      return target;
    } else if ((target == null) || target.isEmpty()) {
      return src;
    }
    Map<Object, List<T>> addMap = src.stream().filter(obj -> fieldMatcher.apply(obj) != null).collect(Collectors.groupingBy(fieldMatcher));
    for (T obj : target) {
      Object key = fieldMatcher.apply(obj);
      if (key != null) {
        List<T> srcObjs = addMap.remove(key);
        if ((srcObjs != null) && !srcObjs.isEmpty()) {
          for (T srcObj : srcObjs) {
            deepMerge(srcObj, obj, config, true);
          }
        }
      }
    }
    if (!Checks.isEmpty(addMap)) {
      addMap.values().forEach(target::addAll);
    }
    return target;
  }

  private static Map<String, ObjectNode> createArrayNodeMapByField(MergeConfig fieldConfig, ArrayNode node) {
    Map<String, ObjectNode> map = new HashMap<>();
    Iterator<JsonNode> iter = node.elements();
    while (iter.hasNext()) {
      JsonNode innerNode = iter.next();
      if (innerNode.isObject()) {
        JsonNode matchVal = innerNode.get(fieldConfig.getMergeArrayElementsMatchField());
        if ((matchVal != null) && !matchVal.isNull() && matchVal.isValueNode()) {
          map.put(matchVal.toString(), (ObjectNode) innerNode);
        }
      }
    }
    return map;
  }

  private static <T> T deepMerge(T src, T target, MergeConfig config, boolean firstObject) {
    if (src == null) {
      return target;
    }
    if (target == null) {
      return src;
    }
    if (config == null) {
      config = DEFAULT_MERGE_CONFIG;
    }
    if (firstObject && config.testCreateNewBean()) {
      target = Serializer.clone(MERGE_MAPPER, target);
    }
    boolean updatedTarget = false;
    ObjectNode srcTree = src instanceof ObjectNode ? (ObjectNode) src : MERGE_MAPPER.valueToTree(src);
    ObjectNode targetTree = target instanceof ObjectNode ? (ObjectNode) target : MERGE_MAPPER.valueToTree(target);
    ObjectNode updateVals = target instanceof ObjectNode ? targetTree : targetTree.objectNode();
    if (config.getRemoveFields() != null) {
      for (String fieldName : config.getRemoveFields()) {
        if (targetTree.hasNonNull(fieldName)) {
          updatedTarget = true;
          updateVals.set(fieldName, null);
        }
      }
    }
    Iterator<Map.Entry<String, JsonNode>> entries = srcTree.fields();
    while (entries.hasNext()) {
      Map.Entry<String, JsonNode> entry = entries.next();
      String fieldName = entry.getKey();
      if ((config.getIgnoreFields() != null) && config.getIgnoreFields().contains(fieldName)) {
        Logs.logTrace(LOG, "Ignoring field matched by ignoreFields in config: field=%s ignoreFields=%s", fieldName, config.getIgnoreFields());
        continue;
      } else if ((config.getIgnoreFieldsPatterns() != null) && Checks.regexMatch(fieldName, config.getIgnoreFieldsPatterns())) {
        Logs.logTrace(
          LOG,
          "Ignoring field matched by ignoreFieldsPatterns in config: field=%s ignoreFieldsPatterns=%s",
          fieldName,
          config.getIgnoreFieldsPatterns()
        );
        continue;
      }
      final MergeConfig fieldConfig = Optional.ofNullable(config.getField(fieldName, true)).map(MergeConfig.class::cast).orElse(config);
      JsonNode srcVal = entry.getValue();
      if ((srcVal == null) || srcVal.isNull()) {
        if (fieldConfig.testOverwriteExisting() && fieldConfig.testOverwriteWithNull()) {
          updatedTarget = true;
          updateVals.set(fieldName, srcVal);
        }
        continue;
      }
      JsonNode targetVal = targetTree.get(fieldName);
      if ((targetVal == null) || targetVal.isNull()) {
        updatedTarget = true;
        updateVals.set(fieldName, srcVal);
        continue;
      }
      boolean removeTargetField = false;
      if (srcVal.isObject()) {
        if (targetVal.isObject()) {
          updatedTarget = true;
          removeTargetField = true;
          updateVals.set(fieldName, deepMerge(srcVal, targetVal, fieldConfig, false));
        } else if (fieldConfig.testOverwriteWithNonMatchingNodeType()) {
          updatedTarget = true;
          removeTargetField = true;
          updateVals.set(fieldName, srcVal);
        }
      } else if (srcVal.isArray()) {
        if (targetVal.isArray()) {
          updatedTarget = true;
          removeTargetField = true;
          if (fieldConfig.testOverwriteArrayNodes()) {
            updateVals.set(fieldName, srcVal);
          } else {
            mergeArrayNodes((ArrayNode) srcVal, (ArrayNode) targetVal, config, fieldConfig);
            updateVals.set(fieldName, targetVal);
          }
        } else if (fieldConfig.testOverwriteWithNonMatchingNodeType()) {
          updatedTarget = true;
          removeTargetField = true;
          updateVals.set(fieldName, srcVal);
        }
      } else if (fieldConfig.testOverwriteExisting()) {
        updatedTarget = true;
        updateVals.set(fieldName, srcVal);
      }

      if (removeTargetField && (target instanceof Map)) {
        ((Map<?, ?>) target).remove(fieldName);
      }
    }
    T returnVal;
    try {
      if (!updatedTarget || (target == targetTree)) {
        returnVal = target;
      } else {
        ObjectReader reader = MERGE_MAPPER.readerForUpdating(target);
        if (firstObject && (config.getRootType() != null)) {
          reader = reader.forType(config.getRootType());
        }
        returnVal = reader.readValue(updateVals);

      }
    } catch (Throwable e) {
      returnVal = null;
      Logs.logError(LOG, e, "Issue converting target tree to value of type [%s]", target.getClass());
    }
    return returnVal;
  }

}
