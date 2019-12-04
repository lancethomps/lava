package com.lancethomps.lava.common.properties;

import static com.lancethomps.lava.common.properties.PropertyParser.parseAndReplaceWithProps;
import static java.lang.System.setProperty;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.lancethomps.lava.common.BaseTest;

public class PropertyParserTest extends BaseTest {

  private static final String SYS_PROP_KEY;
  private static final String SYS_PROP_REPLACE_KEY;
  private static final String SYS_PROP_VALUE;

  static {
    SYS_PROP_KEY = "wtp.tests.propParser";
    SYS_PROP_VALUE = "Testing prop with chars %$?*@!.9";
    SYS_PROP_REPLACE_KEY = "${system:" + SYS_PROP_KEY + '}';
    setProperty(SYS_PROP_KEY, SYS_PROP_VALUE);
  }

  @Test
  public void testEscaping() throws Exception {
    assertEquals(SYS_PROP_REPLACE_KEY, parseAndReplaceWithProps("\\" + SYS_PROP_REPLACE_KEY));
  }

  @Test
  public void testSystemProp() throws Exception {
    assertEquals(SYS_PROP_VALUE, parseAndReplaceWithProps(SYS_PROP_REPLACE_KEY));
  }

}
