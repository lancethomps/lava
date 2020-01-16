package com.lancethomps.lava.common.compare;

import static com.lancethomps.lava.common.ser.OutputFormat.json;
import static com.lancethomps.lava.common.string.StringUtil.removeAllLineBreaks;
import static org.apache.commons.lang3.StringUtils.abbreviate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.Collect;
import com.lancethomps.lava.common.diff.DiffToHtml;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.merge.Merges;
import com.lancethomps.lava.common.ser.OutputParams;
import com.lancethomps.lava.common.ser.Serializer;
import com.lancethomps.lava.common.ser.SerializerFactory;
import com.lancethomps.lava.common.string.StringUtil;

import difflib.DiffUtils;

public class Compare {

  public static final OutputParams DEFAULT_DIFF_AS_JSON_PARAMS =
    new OutputParams().setOutputFormat(json).setPrettifyJson(true).setOrderKeys(true).disableModifications();

  private static final CompareConfig DEFAULT_COMPARE_CONFIG = new CompareConfig().setDeep(true);

  private static final Logger LOG = LogManager.getLogger(Compare.class);

  public static CompareResult compare(@Nullable final Object original, @Nullable final Object revised) {
    return compare(original, revised, null);
  }

  public static CompareResult compare(@Nullable final Object original, @Nullable final Object revised, @Nullable final CompareConfig config) {
    return compare(original, revised, config, false);
  }

  public static CompareResult compare(
    @Nullable final Object original,
    @Nullable final Object revised,
    @Nullable final CompareConfig config,
    final boolean generateDifferences
  ) {
    final CompareResult result = getFieldsWithDifferences(
      original,
      revised,
      config == null ? DEFAULT_COMPARE_CONFIG : config,
      CompareResult.createAndInitialize(),
      "",
      generateDifferences,
      getOrCreateDiffOutputParams(config),
      null
    );
    if (original == null) {
      result.setAllNewData(true);
    }
    return result;
  }

  public static String diff(String original, String revised) {
    List<String> originalLines = StringUtil.splitLines(original);
    List<String> revisedLines = StringUtil.splitLines(revised);
    List<String> diff = DiffUtils.generateUnifiedDiff("original", "revised", originalLines, DiffUtils.diff(originalLines, revisedLines), 3);
    return StringUtils.join(diff, System.lineSeparator());
  }

  public static String diffAsJson(Object obj1, Object obj2) {
    return diffSerialized(obj1, obj2, DEFAULT_DIFF_AS_JSON_PARAMS);
  }

  public static String diffAsJsonToHtml(Object obj1, Object obj2) {
    try {
      return new DiffToHtml().setDiffInput(Compare.diffAsJson(obj1, obj2)).generateHtml().getHtml();
    } catch (Exception e) {
      Logs.logError(LOG, e, "Issue diffing [%s] and [%s]", obj1, obj2);
      return null;
    }
  }

  public static String diffSerialized(final Object obj1, final Object obj2, final OutputParams params) {
    return diffSerialized(obj1, obj2, params, 3);
  }

  public static String diffSerialized(final Object obj1, final Object obj2, final OutputParams params, int contextSize) {
    String original = Serializer.output(obj1, params);
    String revised = Serializer.output(obj2, params);
    List<String> originalLines = StringUtil.splitLines(original);
    List<String> revisedLines = StringUtil.splitLines(revised);
    List<String> diff = DiffUtils.generateUnifiedDiff(
      abbreviate(obj1 == null ? "null" : obj1.toString(), 25),
      abbreviate(obj2 == null ? "null" : obj2.toString(), 25),
      originalLines,
      DiffUtils.diff(originalLines, revisedLines),
      contextSize
    );
    List<String> revisedDiff =
      diff.isEmpty() ? new ArrayList<>() : Lists.newArrayList(removeAllLineBreaks(diff.remove(0)), removeAllLineBreaks(diff.remove(0)));
    revisedDiff.addAll(diff);
    return StringUtils.join(revisedDiff, System.lineSeparator());
  }

  public static String diffSerializedToHtml(final Object obj1, final Object obj2, final OutputParams params) {
    try {
      return new DiffToHtml().setDiffInput(Compare.diffSerialized(obj1, obj2, params)).generateHtml().getHtml();
    } catch (Exception e) {
      Logs.logError(LOG, e, "Issue diffing [%s] and [%s]", obj1, obj2);
      return null;
    }
  }

  public static boolean equalsSimple(final Object lhs, final Object rhs) {
    return lhs == null ? rhs == null : lhs.equals(rhs);
  }

  public static ObjectNode generateDifferences(final Object original, final Object revised) {
    return generateDifferences(original, revised, DEFAULT_COMPARE_CONFIG);
  }

  public static ObjectNode generateDifferences(final Object original, final Object revised, final CompareConfig config) {
    return getFieldsWithDifferences(
      original,
      revised,
      config == null ? DEFAULT_COMPARE_CONFIG : config,
      CompareResult.createAndInitialize(),
      "",
      true,
      getOrCreateDiffOutputParams(config),
      null
    )
      .getDifferences();
  }

  public static Set<String> getFieldsWithDifferences(final Object original, final Object revised) {
    return getFieldsWithDifferences(original, revised, null);
  }

  public static Set<String> getFieldsWithDifferences(final Object original, final Object revised, final CompareConfig config) {
    return getFieldsWithDifferences(
      original,
      revised,
      config == null ? DEFAULT_COMPARE_CONFIG : config,
      CompareResult.createAndInitialize(),
      "",
      false,
      getOrCreateDiffOutputParams(config),
      null
    ).getFieldsWithDifferences();
  }

  public static NumericNode getNumericValueDifference(@Nonnull final NumericNode originalVal, @Nonnull final NumericNode revisedVal) {
    return getNumericValueDifference(originalVal, revisedVal, originalVal.numberType());
  }

  public static NumericNode getNumericValueDifference(
    @Nonnull final NumericNode originalVal,
    @Nonnull final NumericNode revisedVal,
    @Nonnull final NumberType type
  ) {
    switch (type) {
      case BIG_DECIMAL:
        return DecimalNode.valueOf(revisedVal.decimalValue().subtract(originalVal.decimalValue()));
      case BIG_INTEGER:
        return BigIntegerNode.valueOf(revisedVal.bigIntegerValue().subtract(originalVal.bigIntegerValue()));
      case DOUBLE:
        return DoubleNode.valueOf(revisedVal.doubleValue() - originalVal.doubleValue());
      case FLOAT:
        return FloatNode.valueOf(revisedVal.floatValue() - originalVal.floatValue());
      case INT:
        return IntNode.valueOf(revisedVal.intValue() - originalVal.intValue());
      case LONG:
        return LongNode.valueOf(revisedVal.longValue() - originalVal.longValue());
      default:
        throw new IllegalArgumentException("NumberType " + type + " not supported.");
    }
  }

  public static JsonNode getValueDifference(final JsonNode originalVal, final JsonNode revisedVal, final CompareConfig config) {
    if ((revisedVal == null) || revisedVal.isNull()) {
      if ((originalVal == null) || originalVal.isNull()) {
        return null;
      }
      if (config.testNumericNullsEqualZero() && originalVal.isNumber()) {
        return getNumericValueDifference((NumericNode) originalVal, DecimalNode.ZERO, originalVal.numberType());
      }
      return NullNode.getInstance();
    } else if ((originalVal == null) || originalVal.isNull()) {
      return revisedVal;
    }
    if (originalVal.isNumber() && config.testCalculateNumericValueChange()) {
      return getNumericValueDifference(
        (NumericNode) originalVal,
        revisedVal.isNumber() ? (NumericNode) revisedVal : DecimalNode.valueOf(new BigDecimal(revisedVal.asText())),
        originalVal.numberType()
      );
    }
    return revisedVal;
  }

  public static boolean hasDifferencesAsJson(Object original, Object revised) {
    return hasDifferencesAsJson(original, revised, null);
  }

  public static boolean hasDifferencesAsJson(Object original, Object revised, @Nullable OutputParams diffOutputParams) {
    if (original == null) {
      return !(revised == null);
    } else if (revised == null) {
      return true;
    }
    String originalJson = Serializer.output(original, Checks.defaultIfNull(diffOutputParams, DEFAULT_DIFF_AS_JSON_PARAMS));
    String revisedJson = Serializer.output(revised, Checks.defaultIfNull(diffOutputParams, DEFAULT_DIFF_AS_JSON_PARAMS));
    return originalJson == null ? !(revisedJson == null) : !originalJson.equals(revisedJson);
  }

  private static CompareResult addFieldWithDifference(
    final @Nonnull CompareResult current,
    final @Nullable JsonNode originalVal,
    final @Nullable JsonNode revisedVal,
    final @Nonnull String fieldKey
  ) {
    if ((originalVal == null) || originalVal.isNull()) {
      current.addAddedFields(fieldKey);
    } else if ((revisedVal == null) || revisedVal.isNull()) {
      current.addRemovedFields(fieldKey);
    } else {
      current.addUpdatedFields(fieldKey);
    }
    return current;
  }

  @Nonnull
  private static CompareResult getFieldsWithDifferences(
    final Object original,
    final Object revised,
    final @Nonnull CompareConfig config,
    final @Nonnull CompareResult current,
    final @Nonnull String fieldPrefix,
    final boolean generateDiffObject,
    final OutputParams diffOutputParams,
    @Nullable final ObjectMapper valueToTreeMapperOpt
  ) {
    final ObjectMapper valueToTreeMapper;
    if (valueToTreeMapperOpt != null) {
      valueToTreeMapper = valueToTreeMapperOpt;
    } else if (diffOutputParams == null) {
      valueToTreeMapper = Merges.MERGE_MAPPER;
    } else if ((diffOutputParams.getJsonSigFigs() != null)) {
      valueToTreeMapper = SerializerFactory.resolveObjectMapper(
        new OutputParams()
          .setObjectMapper(Merges.MERGE_MAPPER)
          .setObjectMapperCustomCacheKeyId("merge_mapper")
          .setJsonSigFigs(diffOutputParams.getJsonSigFigs()),
        true
      );
    } else {
      valueToTreeMapper = Merges.MERGE_MAPPER;
    }
    final ObjectNode originalNode = original == null ? Merges.createNewObjectNode()
      : original instanceof ObjectNode ? (ObjectNode) original : valueToTreeMapper.valueToTree(original);
    final ObjectNode revisedNode = revised == null ? Merges.createNewObjectNode()
      : revised instanceof ObjectNode ? (ObjectNode) revised : valueToTreeMapper.valueToTree(revised);
    final Set<String> fields = new TreeSet<>();
    CollectionUtils.addAll(fields, originalNode.fieldNames());
    CollectionUtils.addAll(fields, revisedNode.fieldNames());
    if (config.getIgnoreFields() != null) {
      fields.removeAll(config.getIgnoreFields());
    }
    if ((config.getFieldsBlackList() != null) || (config.getFieldsWhiteList() != null)) {
      fields.removeIf(field -> !Checks.passesWhiteAndBlackListCheck(field, config.getFieldsWhiteList(), config.getFieldsBlackList()).getLeft());
    }
    for (String field : fields) {
      final CompareConfig fieldConfig =
        Optional.ofNullable(config.getFields()).map(fieldConfigs -> Collect.wildcardGet(fieldConfigs, field)).orElse(config);
      final OutputParams fieldDiffOutputParams = (fieldConfig == config) || (fieldConfig.getDiffOutputParams() == null) ? diffOutputParams
        : Merges.deepMerge(fieldConfig.getDiffOutputParams(), DEFAULT_DIFF_AS_JSON_PARAMS.copy(), Merges.OVERWRITE_MERGE_CONFIG);
      final JsonNode originalVal = originalNode.get(field);
      final JsonNode revisedVal = revisedNode.get(field);
      boolean equal = nodesAreEqual(
        originalVal,
        revisedVal,
        fieldConfig,
        current,
        field,
        fieldPrefix,
        generateDiffObject,
        fieldDiffOutputParams,
        valueToTreeMapper
      );
      if (!equal) {
        addFieldWithDifference(current, originalVal, revisedVal, fieldPrefix + field);
        if (generateDiffObject) {
          final JsonNode valDiff = getValueDifference(originalVal, revisedVal, fieldConfig);
          if (valDiff != null) {
            current.getDifferences().set(field, valDiff);
          }
        }
      }
    }
    return current;
  }

  private static OutputParams getOrCreateDiffOutputParams(@Nullable CompareConfig config) {
    return (config == null) || (config.getDiffOutputParams() == null) ? DEFAULT_DIFF_AS_JSON_PARAMS
      : Merges.deepMerge(config.getDiffOutputParams(), DEFAULT_DIFF_AS_JSON_PARAMS.copy(), Merges.OVERWRITE_MERGE_CONFIG);
  }

  private static boolean nodesAreEqual(
    final JsonNode originalVal,
    final JsonNode revisedVal,
    final CompareConfig config,
    final @Nonnull CompareResult current,
    final String field,
    final String fullPrefix,
    final boolean generateDiffObject,
    final OutputParams diffOutputParams,
    final ObjectMapper valueToTreeMapper
  ) {

    String objectFieldNotation = Checks.defaultIfBlank(config.getObjectFieldNotation(), ".");
    if ((originalVal == null) || originalVal.isNull()) {
      if ((revisedVal == null) || revisedVal.isNull()) {
        return true;
      }
      if (config.testDeepWhenNullContainer() && revisedVal.isObject()) {
        ObjectNode fieldNode = Merges.createNewObjectNode();
        current.getDifferences().set(field, fieldNode);
        getFieldsWithDifferences(
          originalVal,
          revisedVal,
          config,
          CompareResult.createWithNewNode(current, fieldNode),
          fullPrefix + field + objectFieldNotation,
          generateDiffObject,
          diffOutputParams,
          valueToTreeMapper
        );
        if (fieldNode.isEmpty(null)) {
          current.getDifferences().remove(field);
        }
        return true;
      }
      return false;
    } else if ((revisedVal == null) || revisedVal.isNull()) {
      if (config.testDeepWhenNullContainer() && originalVal.isObject()) {
        ObjectNode fieldNode = Merges.createNewObjectNode();
        current.getDifferences().set(field, fieldNode);
        getFieldsWithDifferences(
          originalVal,
          revisedVal,
          config,
          CompareResult.createWithNewNode(current, fieldNode),
          fullPrefix + field + objectFieldNotation,
          generateDiffObject,
          diffOutputParams,
          valueToTreeMapper
        );
        if (fieldNode.isEmpty(null)) {
          current.getDifferences().remove(field);
        }
        return true;
      }
      return false;
    }

    if (config.testDeep()) {

      if (originalVal.isObject()) {
        if (revisedVal.isObject()) {
          ObjectNode fieldNode = Merges.createNewObjectNode();
          current.getDifferences().set(field, fieldNode);
          getFieldsWithDifferences(
            originalVal,
            revisedVal,
            config,
            CompareResult.createWithNewNode(current, fieldNode),
            fullPrefix + field + objectFieldNotation,
            generateDiffObject,
            diffOutputParams,
            valueToTreeMapper
          );
          if (fieldNode.isEmpty(null)) {
            current.getDifferences().remove(field);
          }
          return true;
        }
        return false;
      } else if (revisedVal.isObject()) {
        return false;
      }

      if (originalVal.isArray()) {
        if (!revisedVal.isArray()) {
          return false;
        }
        final ArrayNode fieldNode = Merges.createNewArrayNode();
        current.getDifferences().set(field, fieldNode);
        final ArrayNode originalArray = (ArrayNode) originalVal;
        final ArrayNode revisedArray = (ArrayNode) revisedVal;
        int maxArraySize = Math.max(originalArray.size(), revisedArray.size());
        for (int index = 0; index < maxArraySize; index++) {
          final String fullPrefixWithPos = fullPrefix + field + '[' + index + ']';
          final boolean revisedIsSmaller = revisedArray.size() <= index;
          if (revisedIsSmaller || (originalArray.size() <= index)) {
            if (generateDiffObject) {
              if (revisedIsSmaller) {
                fieldNode.add(NullNode.getInstance());
              } else {
                fieldNode.add(revisedArray.get(index));
              }
            }
            current.addUpdatedFields(fullPrefixWithPos);
            continue;
          }
          final JsonNode originalArrayVal = originalArray.get(index);
          final JsonNode revisedArrayVal = revisedArray.get(index);
          if (originalArrayVal.isObject() && revisedArrayVal.isObject()) {
            ObjectNode arrayNodeObj = Merges.createNewObjectNode();
            getFieldsWithDifferences(
              originalArrayVal,
              revisedArrayVal,
              config,
              CompareResult.createWithNewNode(current, arrayNodeObj),
              fullPrefixWithPos + objectFieldNotation,
              generateDiffObject,
              diffOutputParams,
              valueToTreeMapper
            );
            if (!arrayNodeObj.isEmpty(null)) {
              fieldNode.add(arrayNodeObj);
            }
          } else if (!nodesAreEqual(
            originalArrayVal,
            revisedArrayVal,
            config,
            current,
            "",
            fullPrefixWithPos,
            generateDiffObject,
            diffOutputParams,
            valueToTreeMapper
          )) {
            if (generateDiffObject) {
              final JsonNode valDiff = getValueDifference(originalArrayVal, revisedArrayVal, config);
              if (valDiff != null) {
                fieldNode.add(valDiff);
              }
            }
            current.addUpdatedFields(fullPrefixWithPos);
          }
        }
        if (fieldNode.isEmpty(null)) {
          current.getDifferences().remove(field);
        }
        return true;
      } else if (revisedVal.isArray()) {
        return false;
      }
    } else if (originalVal.isContainerNode() && revisedVal.isContainerNode() && (diffOutputParams.getSkipFields() != null)) {
      removeFields(originalVal, diffOutputParams.getSkipFields());
      removeFields(revisedVal, diffOutputParams.getSkipFields());
    }

    return !hasDifferencesAsJson(originalVal, revisedVal, diffOutputParams);
  }

  private static JsonNode removeFields(@Nonnull JsonNode node, @Nonnull Collection<String> fields) {
    if (node.isArray()) {
      node.forEach(child -> removeFields(child, fields));
    } else if (node.isObject()) {
      ((ObjectNode) node).remove(fields);
    }
    return node;
  }

}
