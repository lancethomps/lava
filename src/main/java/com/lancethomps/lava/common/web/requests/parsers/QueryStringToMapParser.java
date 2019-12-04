package com.lancethomps.lava.common.web.requests.parsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.lancethomps.lava.common.collections.MapUtil;
import com.lancethomps.lava.common.ser.Serializer;

public class QueryStringToMapParser implements RequestParameterParser<Map<String, List<String>>> {

  public static Map<String, List<String>> getStringListMapParam(Map<String, String[]> request, String paramName) {
    Map<String, List<String>> map = new HashMap<>();
    if (paramName.endsWith("*")) {
      String prefix = StringUtils.substringBeforeLast(paramName, "*");
      request.forEach((key, vals) -> {
        if (key.startsWith(prefix)) {
          map.computeIfAbsent(StringUtils.substringAfter(key, prefix), k -> new ArrayList<>()).addAll(Arrays.asList(vals));
        }
      });
    } else if (request.containsKey(paramName)) {
      for (String param : request.get(paramName)) {
        Map<String, List<String>> subMap = Serializer.isJsonObject(param)
          ? Serializer.fromJson(
          param,
          Serializer.constructMapType(HashMap.class, String.class, Serializer.constructCollectionType(ArrayList.class, String.class))
        )
          : MapUtil.stringListMapFromString(param, '~', '|');
        if (map.isEmpty()) {
          map.putAll(subMap);
        } else {
          subMap.forEach((key, val) -> map.computeIfAbsent(key, k -> new ArrayList<>()).addAll(val));
        }
      }
    }
    return map.isEmpty() ? null : map;
  }

  @Override
  public Map<String, List<String>> process(@Nullable RequestFieldInfo<Map<String, List<String>>> info, Map<String, String[]> params, String name) {
    Map<String, List<String>> val = Optional.ofNullable(getStringListMapParam(params, name)).orElseGet(HashMap::new);
    if (!name.endsWith("*")) {
      return val.isEmpty() ? null : val;
    }
    String key = name.substring(0, name.length() - 2);
    if (params.containsKey(key)) {
      Map<String, String[]> current = val.isEmpty() ? new HashMap<>() : MapUtil.convertToParameterMap(val);
      for (String paramVal : params.get(key)) {
        MapUtil.createFromQueryString(paramVal, current, true);
      }
      val = MapUtil.convertParameterMap(current);
    }
    return val.isEmpty() ? null : val;
  }

}
