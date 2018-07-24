package com.github.lancethomps.lava.common.collections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * The Class MapUtilTest.
 */
public class MapUtilTest {

	/**
	 * Test map stringify values.
	 */
	@Test
	public void testMapStringifyValues() {
		final Map<String, Object> m = new HashMap<String, Object>() {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			{
				put("keyNull", null);
				put("keyInteger", 1);
				put("keyString", "string");
				put("keyList", Arrays.asList("s1", "s2"));
				put(null, "should be removed");
				put(null, null);
			}
		};

		// Note that null key value pairs are removed
		final Map<String, String> expected = new HashMap<String, String>() {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			{
				put("keyInteger", "1");
				put("keyString", "string");
				put("keyList", "[s1, s2]");
			}
		};
		final Map<String, String> actual = MapUtil.mapStringifyValues(m);
		Assert.assertEquals("Values in map are not stringified correctly!", expected, actual);
	}

}
