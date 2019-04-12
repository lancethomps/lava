package com.github.lancethomps.lava.common.collections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class MapUtilTest {

  @Test
  public void testMapStringifyValues() {
    final Map<String, Object> m = new HashMap<String, Object>() {

      {
        put("keyNull", null);
        put("keyInteger", 1);
        put("keyString", "string");
        put("keyList", Arrays.asList("s1", "s2"));
        put(null, "should be removed");
        put(null, null);
      }

      private static final long serialVersionUID = 1L;
    };

    final Map<String, String> expected = new HashMap<String, String>() {

      {
        put("keyInteger", "1");
        put("keyString", "string");
        put("keyList", "[s1, s2]");
      }

      private static final long serialVersionUID = 1L;
    };
    final Map<String, String> actual = MapUtil.mapStringifyValues(m);
    Assert.assertEquals("Values in map are not stringified correctly!", expected, actual);
  }

}
