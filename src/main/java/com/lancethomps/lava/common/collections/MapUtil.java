// CHECKSTYLE.OFF: OpenCSV
package com.lancethomps.lava.common.collections;

import static com.lancethomps.lava.common.Checks.isNotEmpty;
import static com.lancethomps.lava.common.logging.Logs.logError;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.splitPreserveAllTokens;
import static org.apache.commons.lang3.math.NumberUtils.isNumber;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.Collect;
import com.lancethomps.lava.common.Reflections;
import com.lancethomps.lava.common.file.FileUtil;
import com.lancethomps.lava.common.lambda.Lambdas;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.ser.Serializer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opencsv.CSVParser;

public class MapUtil {

  private static final Logger LOG = Logger.getLogger(MapUtil.class);

  public static <K, V> void addNullSafeToMap(Map<K, V> map, K key, V value) {
    if ((key != null) && (value != null)) {
      map.put(key, value);
    }
  }

  @SafeVarargs
  public static <K, V> Map<K, V> addToMap(Map<K, V> map, final Pair<K, V>... pairs) {
    Arrays.stream(pairs).forEach(pair -> map.put(pair.getLeft(), pair.getRight()));
    return map;
  }

  public static Map<String, Double> addToMapCombineDuplicates(Map<String, Double> map, String key, Double val) {
    if (map.containsKey(key)) {
      val = val + map.get(key);
    }
    map.put(key, val);
    return map;
  }

  public static <K, V> LinkedHashMap<K, V> reverseMap(Map<K, V> map) {
    LinkedHashMap<K, V> reversed = new LinkedHashMap<>();
    for (K key : Lists.reverse(new ArrayList<>(map.keySet()))) {
      reversed.put(key, map.get(key));
    }
    return reversed;
  }

  public static Map<String, Object> addToMapIfUniqueObject(Map<String, Object> map, Set<Object> objects) {
    Map<String, Object> result = Maps.newLinkedHashMap();
    for (Entry<String, Object> entry : map.entrySet()) {
      Object val = entry.getValue();
      if ((val == null) || !objects.add(val)) {
        continue;
      }
      result.put(entry.getKey(), val instanceof Map ? addToMapIfUniqueObject((Map<String, Object>) val, objects) : val);
    }
    return result;
  }

  public static Map<String, List<String>> convertParameterMap(Map<String, String[]> current) {
    return current == null ? null : current.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> Lists.newArrayList(e.getValue())));
  }

  public static Map<String, String[]> convertToParameterMap(Map<String, List<String>> current) {
    return current == null ? null :
      current.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().toArray(new String[e.getValue().size()])));
  }

  public static HashMap<String, BigDecimal> copyMap(Map<String, BigDecimal> original) {
    HashMap<String, BigDecimal> map = new HashMap<>();
    for (Entry<String, BigDecimal> entry : original.entrySet()) {
      map.put(entry.getKey(), entry.getValue());
    }
    return map;
  }

  public static <K, V> FastHashMap<K, V> createFastHashMap(boolean fast) {
    FastHashMap<K, V> map = new FastHashMap<>();
    map.setFast(fast);
    return map;
  }

  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> createFrom(Object... pairs) {
    Map<K, V> map = new LinkedHashMap<>();
    if (pairs != null) {
      if ((pairs.length % 2) != 0) {
        throw new IllegalArgumentException("Pairs var arg must contain an even number of elements (sets of 2 - key/val pairs).");
      }
      for (int pos = 0; pos < pairs.length; pos++) {
        K key = (K) pairs[pos];
        V val = (V) pairs[++pos];
        Lambdas.actionIfTrue((key != null) && (val != null), () -> map.put(key, val));
      }
    }
    return map;
  }

  public static Map<String, String[]> createFromQueryString(String queryString) {
    return createFromQueryString(queryString, null);
  }

  public static Map<String, String[]> createFromQueryString(String queryString, Map<String, String[]> current) {
    return createFromQueryString(queryString, current, false);
  }

  public static Map<String, String[]> createFromQueryString(String queryString, Map<String, String[]> current, boolean addToCurrent) {
    Map<String, String[]> requestParams = addToCurrent && (current != null) ? current : Maps.newHashMap();
    if (isBlank(queryString)) {
      return requestParams;
    }
    CSVParser entryParser = Collect.getCsvParser('&');
    try {
      Map<String, List<String>> map = Maps.newHashMap();
      List<String> entries = Collect.splitCsvAsList(queryString, entryParser);
      for (String entry : entries) {
        if (Checks.isNotBlank(entry)) {
          String key = StringUtils.substringBefore(entry, "=");
          String val = URLDecoder.decode(StringUtils.substringAfter(entry, "="), StandardCharsets.UTF_8.name());
          if (isNotBlank(key)) {
            map.computeIfAbsent(key, k -> Lists.newArrayList()).add(val);
          }
        }
      }
      if (isNotEmpty(map)) {
        map.forEach(
          (key, val) -> {
            requestParams.merge(key, val.toArray(new String[val.size()]), (a, b) -> ArrayUtils.addAll(a, b));
          }
        );
      }
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Issue getting map from query string [%s]!", queryString);
    }
    if (!addToCurrent && (current != null)) {
      requestParams.putAll(current);
    }
    return requestParams;
  }

  public static Map<String, Object> createFromRequestParameters(Map<String, String[]> request) throws Exception {
    Map<String, Object> map = new HashMap<>();
    for (Entry<String, String[]> entry : request.entrySet()) {
      map.put(entry.getKey(), entry.getValue()[0]);
    }
    return map;
  }

  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> createFromWithNulls(Object... pairs) {
    Map<K, V> map = new HashMap<>();
    if (pairs != null) {
      if ((pairs.length % 2) != 0) {
        throw new IllegalArgumentException("Pairs var arg must contain an even number of elements (sets of 2 - key/val pairs).");
      }
      for (int pos = 0; pos < pairs.length; pos++) {
        map.put((K) pairs[pos], (V) pairs[++pos]);
      }
    }
    return map;
  }

  public static <K, V> Map<K, V> createLruMap(final int maxEntries) {
    return new LinkedHashMap<K, V>((maxEntries * 3) / 2, 0.7f, true) {

      private static final long serialVersionUID = 5299181956701430990L;

      @Override
      protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxEntries;
      }
    };
  }

  public static Map<String, Object> expandJsonFields(Map<String, Object> map, boolean expandInner, Set<String> skipFields) {
    Set<Entry<String, Object>> entries = Sets.newHashSet(map.entrySet());
    boolean hasSkipFields = isNotEmpty(skipFields);
    for (Entry<String, Object> entry : entries) {
      Object val = entry.getValue();
      if ((val instanceof String) && Serializer.isJsonMap(val.toString())) {
        Map<String, Object> expanded = Serializer.readJsonAsMap(val.toString());
        if (expanded != null) {
          map.remove(entry.getKey());
          expanded.entrySet().stream().filter(e -> !hasSkipFields || !skipFields.contains(e.getKey())).forEach(
            expandedEntry -> {
              Object expandedVal = Lambdas.functionIfTrueGeneric(
                !expandInner && Reflections.isListType(expandedEntry.getValue()),
                expandedEntry.getValue(),
                Serializer::toJson
              );
              map.put(map.containsKey(expandedEntry.getKey()) ? entry.getKey() + '.' + expandedEntry.getKey() : expandedEntry.getKey(), expandedVal);
            }
          );
        }
      }
    }
    return map;
  }

  public static Map<String, Object> fromHttpParameters(HttpServletRequest request) {
    Map<String, Object> map = new TreeMap<>();
    for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
      map.put(entry.getKey(), entry.getValue()[0]);
    }
    return map;
  }

  public static String getAsString(Map<String, Object> map, String key) {
    return (String) map.get(key);
  }

  @SuppressWarnings("unchecked")
  public static <K, V, T> T getFirstAvailable(Map<K, V> map, K... keys) {
    return (T) Stream.of(keys).map(map::get).filter(Objects::nonNull).findFirst().orElse(null);
  }

  @SuppressWarnings("unchecked")
  public static <K, V, T> T getFromMap(Map<K, V> map, K key) {
    return (T) map.get(key);
  }

  public static Map<String, Integer> getHeaderPositionsAsLowerCase(BufferedReader reader) {
    Map<String, Integer> headers = new HashMap<>();
    try {
      String line = reader.readLine();
      char perfSepChar = FileUtil.getSeparationChar(line);
      String[] firstLine = splitPreserveAllTokens(line, perfSepChar);
      int i = 0;
      for (String header : firstLine) {
        headers.put(header.toLowerCase(), i);
        i++;
      }
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Error creating header positions.");
    }
    return headers;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getWithCaseInsensitiveKey(Map<String, Object> map, String casedKey) {
    return map.containsKey(casedKey) ? (T) map.get(casedKey) : (T) map.get(StringUtils.lowerCase(casedKey));
  }

  public static <V> V getWithFallback(final Map<String, V> map, final String sep, V defaultValue, final List<String> vars) {
    V result = null;
    while (!vars.isEmpty() && (result == null)) {
      result = map.get(StringUtils.join(vars, sep));
      vars.remove(vars.size() - 1);
    }
    return result == null ? defaultValue : result;
  }

  public static <V> V getWithFallback(final Map<String, V> map, final String sep, V defaultValue, final String... keyVars) {
    return getWithFallback(map, sep, defaultValue, Arrays.stream(keyVars).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
  }

  public static <K, V> V getWithFallbacks(final Map<K, V> map, @SuppressWarnings("unchecked") K... keys) {
    return getWithFallbacks(map, Arrays.asList(keys), null);
  }

  public static <K, V> V getWithFallbacks(final Map<K, V> map, final List<K> keys, V defaultValue) {
    return keys.stream().filter(k -> map.containsKey(k)).findFirst().map(k -> map.get(k)).orElse(defaultValue);
  }

  public static Map<String, AtomicInteger> incrementMap(Map<String, AtomicInteger> map, String key) {
    if (map.containsKey(key)) {
      map.get(key).incrementAndGet();
    } else {
      map.put(key, new AtomicInteger(1));
    }
    return map;
  }

  public static Map<String, String> mapFromString(String original, char keyValueSep, char entrySep) {
    return mapFromString(original, keyValueSep, entrySep, false);
  }

  public static Map<String, String> mapFromString(String original, char keyValueSep, char entrySep, boolean combineValues) {
    Map<String, String> map = new HashMap<>();
    return mapFromString(original, keyValueSep, entrySep, combineValues, map);
  }

  public static Map<String, String> mapFromString(String original, char keyValueSep, char entrySep, boolean combineValues, Map<String, String> map) {
    if (isBlank(original)) {
      return map;
    }
    CSVParser entryParser = Collect.getCsvParser(entrySep);
    CSVParser keyValueParser = Collect.getCsvParser(keyValueSep);
    try {
      List<String> entries = Collect.splitCsvAsList(original, entryParser);
      for (String entry : entries) {
        List<String> keyVals = Collect.splitCsvAsList(entry, keyValueParser);
        if (keyVals.size() > 0) {
          String key = keyVals.get(0);
          String val = keyVals.size() > 1 ? keyVals.get(1) : StringUtils.EMPTY;
          if (StringUtils.isNotBlank(key)) {
            if (combineValues) {
              if (map.containsKey(key)) {
                if (isNumber(val)) {
                  Double doubleVal = Double.parseDouble(val);
                  Double currentDoubleVal = Double.parseDouble(map.get(key));
                  Double combined = (doubleVal + currentDoubleVal);
                  val = combined.toString();
                } else {
                  val = map.get(key) + val;
                }
              }
            }
            map.put(key, val);
          }
        }
      }
    } catch (Throwable e) {
      Logs.logError(
        LOG,
        e,
        "Issue getting map from string [%s] with keyValueSep [%s], entrySep [%s], combineValues [%s], and original map [%s]!",
        original,
        keyValueSep,
        entrySep,
        combineValues,
        map
      );
    }
    return map;
  }

  public static Map<String, Object> mapFromString(String original, char keyValueSep, char entrySep, Map<String, Object> map) {
    CSVParser entryParser = Collect.getCsvParser(entrySep);
    CSVParser keyValueParser = Collect.getCsvParser(keyValueSep);
    try {
      List<String> entries = Collect.splitCsvAsList(original, entryParser);
      for (String entry : entries) {
        List<String> keyVals = Collect.splitCsvAsList(entry, keyValueParser);
        if (keyVals.size() > 1) {
          String key = keyVals.get(0);
          String val = keyVals.get(1);
          if (isNotBlank(key) && isNotBlank(val)) {
            map.put(key, Serializer.parseStringAsBestFit(val));
          }
        }
      }
    } catch (Throwable e) {
      Logs.logError(
        LOG,
        e,
        "Issue getting map from string [%s] with keyValueSep [%s], entrySep [%s], and original map [%s]!",
        original,
        keyValueSep,
        entrySep,
        map
      );
    }
    return map;
  }

  public static Map<String, String> mapFromString(String original, String keyValueSep, String entrySep) {
    return mapFromString(original, keyValueSep.charAt(0), entrySep.charAt(0));
  }

  public static Map<String, String> mapStringifyValues(Map<String, Object> map) {
    return map
      .entrySet()
      .stream()
      .filter(
        entry -> {
          if ((entry.getKey() == null) || (entry.getValue() == null)) {
            Logs.logWarn(LOG, "Removing map entry %s because it contains null, from original map: %s", entry, map);
            return false;
          }
          return true;
        }
      )
      .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().toString()));
  }

  public static <V> LinkedHashMap<String, LinkedHashMap<String, V>> partitionMapByRegex(
    @Nonnull Map<String, V> map,
    @Nonnull Map<String, Pattern> groups
  ) {
    return partitionMapByRegex(map, groups, "Others");
  }

  public static <V> LinkedHashMap<String, LinkedHashMap<String, V>> partitionMapByRegex(
    @Nonnull Map<String, V> map,
    @Nonnull Map<String, Pattern> groups,
    @Nullable String othersGroupKey
  ) {
    final LinkedHashMap<String, LinkedHashMap<String, V>> result = new LinkedHashMap<>();
    for (Entry<String, Pattern> group : groups.entrySet()) {
      result.put(group.getKey(), new LinkedHashMap<>());
    }
    final LinkedHashMap<String, V> others = new LinkedHashMap<>();
    if (othersGroupKey != null) {
      result.put(othersGroupKey, others);
    }
    for (Entry<String, V> entry : map.entrySet()) {
      boolean matched = false;
      for (Entry<String, Pattern> group : groups.entrySet()) {
        if (group.getValue().matcher(entry.getKey()).matches()) {
          matched = true;
          result.get(group.getKey()).put(entry.getKey(), entry.getValue());
          break;
        }
      }
      if (!matched) {
        others.put(entry.getKey(), entry.getValue());
      }
    }
    result.entrySet().stream().filter(e -> e.getValue().isEmpty()).map(Entry::getKey).collect(Collectors.toSet()).forEach(val -> result.remove(val));
    return result;
  }

  public static void putAll(Map<String, Object> map, Object[] keysAndValues) {
    if (keysAndValues != null) {
      if ((keysAndValues.length % 2) != 0) {
        String msg = format("Illegal number of arguments %d, must be divisible by 2", keysAndValues.length);
        throw new IllegalArgumentException(msg);
      }
      for (int i = 0; i < keysAndValues.length; i += 2) {
        map.put((String) keysAndValues[i], keysAndValues[i + 1]);
      }
    }
  }

  public static Map<String, String> sortedMapFromString(String original, char keyValueSep, char entrySep) {
    return mapFromString(original, keyValueSep, entrySep, false, new TreeMap<String, String>());
  }

  public static <V> String stringFromMap(Map<String, V> original, String keyValueSep, String entrySep) {
    if (MapUtils.isEmpty(original)) {
      return "";
    }
    StringBuilder string = new StringBuilder();
    for (String key : original.keySet()) {
      string.append(key).append(keyValueSep).append(original.get(key)).append(entrySep);
    }
    return string.toString();
  }

  public static Map<String, List<String>> stringListMapFromString(String original) {
    return stringListMapFromString(original, '~', '|');
  }

  public static Map<String, List<String>> stringListMapFromString(String original, char keyValueSep, char entrySep) {
    if (isBlank(original)) {
      return null;
    }
    Map<String, List<String>> map = new HashMap<>();
    CSVParser entryParser = Collect.getCsvParser(entrySep);
    CSVParser keyValueParser = Collect.getCsvParser(keyValueSep);
    try {
      List<String> entries = Collect.splitCsvAsList(original, entryParser);
      for (String entry : entries) {
        List<String> keyVals = Collect.splitCsvAsList(entry, keyValueParser);
        if (keyVals.size() > 0) {
          String key = keyVals.get(0);
          String val = keyVals.size() > 1 ? keyVals.get(1) : StringUtils.EMPTY;
          if (StringUtils.isNotBlank(key)) {
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(val);
          }
        }
      }
    } catch (Throwable e) {
      Logs.logError(
        LOG,
        e,
        "Issue getting map from string [%s] with keyValueSep [%s], entrySep [%s], and current map [%s]!",
        original,
        keyValueSep,
        entrySep,
        map
      );
    }
    return map;
  }

}
