package com.lancethomps.lava.common;

import java.util.Arrays;

import org.junit.Test;

import com.google.common.collect.Sets;

public class PatternsTest extends BaseTest {

  @Test
  public void testSimpleAcronymAndPossibleAcronymsMatcher() {
    TestingCommon.assertEqualsViaJsonDiff(
      "Simple & Possible Acronym Matcher Test Failed",
      Sets.newLinkedHashSet(Arrays.asList("BRS", "DW", "PST")),
      Patterns.findSimpleAcronymsAndPossibleAcronyms(2, "BRS-Digital Wealth-Platform & Sales Technology")
    );
  }

  @Test
  public void testSimpleAcronymMatcher() {
    TestingCommon.assertEqualsViaJsonDiff(
      "Simple Acronym Matcher Test Failed",
      Sets.newLinkedHashSet(Arrays.asList("BRS", "PST")),
      Patterns.findSimpleAcronyms("BRS-Digital Wealth-PST-Platform Infrastructure")
    );
  }

}
