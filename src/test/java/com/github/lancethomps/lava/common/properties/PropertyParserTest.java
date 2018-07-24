package com.github.lancethomps.lava.common.properties;

import static com.github.lancethomps.lava.common.properties.PropertyParser.parseAndReplaceWithProps;
import static java.lang.System.setProperty;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * The Class PropertyParserTest.
 *
 * @author lathomps
 */
public class PropertyParserTest extends com.github.lancethomps.lava.common.BaseTest {

	/** The Constant SYS_PROP_KEY. */
	private static final String SYS_PROP_KEY;

	/** The Constant SYS_PROP_VALUE. */
	private static final String SYS_PROP_VALUE;

	/** The Constant SYS_PROP_REPLACE_KEY. */
	private static final String SYS_PROP_REPLACE_KEY;

	static {
		SYS_PROP_KEY = "wtp.tests.propParser";
		SYS_PROP_VALUE = "Testing prop with chars %$?*@!.9";
		SYS_PROP_REPLACE_KEY = "${system:" + SYS_PROP_KEY + '}';
		setProperty(SYS_PROP_KEY, SYS_PROP_VALUE);
	}

	/**
	 * Test system prop.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSystemProp() throws Exception {
		assertEquals(SYS_PROP_VALUE, parseAndReplaceWithProps(SYS_PROP_REPLACE_KEY));
	}

	/**
	 * Test escaping.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testEscaping() throws Exception {
		assertEquals(SYS_PROP_REPLACE_KEY, parseAndReplaceWithProps("\\" + SYS_PROP_REPLACE_KEY));
	}
}
