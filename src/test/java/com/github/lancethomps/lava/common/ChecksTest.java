package com.github.lancethomps.lava.common;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.github.lancethomps.lava.common.collections.MapUtil;
import com.github.lancethomps.lava.common.ser.OutputExpression;

/**
 * The Class ChecksTest.
 */
public class ChecksTest extends BaseTest {

	/**
	 * Test assertions.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testAssertions() throws Exception {
		Checks.assertFalse(false);
		try {
			Checks.assertFalse(true);
			throw new Exception("This should have thrown an exception.");
		} catch (AssertionError e) {
			;
		}
		Checks.assertTrue(true);
		try {
			Checks.assertTrue(false);
			throw new Exception("This should have thrown an exception.");
		} catch (AssertionError e) {
			;
		}
		Checks.assertFilesExist(new File(Testing.getProjRootPath(), "pom.xml"));
		try {
			Checks.assertFilesExist(new File("FILE_DOES_NOT_EXIST"));
			throw new Exception("This should have thrown an exception.");
		} catch (AssertionError e) {
			;
		}
	}

	/**
	 * Test do any expressions match.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testDoAnyExpressionsMatch() throws Exception {
		// TODO: add test for throwing error
		Object rootObject = MapUtil.createFrom("key", "value");
		Assert.assertTrue(Checks.doAnyExpressionsMatch(Arrays.asList(new OutputExpression().setExpression("['key'] != null")), rootObject).testMatched());
		Assert.assertFalse(Checks.doAnyExpressionsMatch(Arrays.asList(new OutputExpression().setExpression("['key'] == null")), rootObject).testMatched());

		Assert.assertTrue(
			Checks.doAnyExpressionsMatchUsingRootWrapper(Arrays.asList(new OutputExpression().setExpression("data['key'] != null")), rootObject, Collections.emptyMap()).testMatched()
		);
		Assert.assertFalse(
			Checks.doAnyExpressionsMatchUsingRootWrapper(Arrays.asList(new OutputExpression().setExpression("data['key'] == null")), rootObject, Collections.emptyMap()).testMatched()
		);
	}

	/**
	 * Test filtering.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFiltering() throws Exception {
		List<String> vals = Arrays.asList("good", "bad");
		List<Pattern> whiteList = Arrays.asList(Pattern.compile("^good$"));
		List<Pattern> blackList = Arrays.asList(Pattern.compile("^bad$"));

		Assert.assertArrayEquals(new String[] { "good" }, Checks.filterWithWhiteAndBlackList(vals, whiteList, blackList).toArray(new String[] {}));
		Assert.assertArrayEquals(new String[] { "good" }, Checks.filterWithWhiteAndBlackList(vals, null, blackList).toArray(new String[] {}));
		Assert.assertArrayEquals(new String[] { "good" }, Checks.filterWithWhiteAndBlackList(vals, whiteList, null).toArray(new String[] {}));
		Assert.assertArrayEquals(new String[] { "good", "bad" }, Checks.filterWithWhiteAndBlackList(vals, null, null).toArray(new String[] {}));
	}

	/**
	 * Test is empty.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testIsEmpty() throws Exception {
		assertFalse(Checks.isNotEmpty(new boolean[] {}));
		assertFalse(Checks.isNotEmpty(new byte[] {}));
		assertFalse(Checks.isNotEmpty(new char[] {}));
		assertFalse(Checks.isNotEmpty(new double[] {}));
		assertFalse(Checks.isNotEmpty(new float[] {}));
		assertFalse(Checks.isNotEmpty(new int[] {}));
		assertFalse(Checks.isNotEmpty(new long[] {}));
		assertFalse(Checks.isNotEmpty(new short[] {}));
		assertFalse(Checks.isNotEmpty(new String[] {}));
		assertFalse(Checks.isNotEmpty(new Object[] {}));
		assertFalse(Checks.isNotEmpty(new ArrayList<>()));
	}

	/**
	 * Test wildcards.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testWildcards() throws Exception {
		Assert.assertTrue(Checks.containsWithWildcards(Arrays.asList("prefix"), "prefix"));
		Assert.assertTrue(Checks.containsWithWildcards(Arrays.asList("prefix*"), "prefixsuffix"));
		Assert.assertFalse(Checks.containsWithWildcards(Arrays.asList("prefix*"), "suffixprefix"));
	}

}
