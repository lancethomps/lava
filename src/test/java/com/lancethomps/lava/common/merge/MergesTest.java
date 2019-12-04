package com.lancethomps.lava.common.merge;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.lancethomps.lava.common.BaseTest;
import com.lancethomps.lava.common.Randoms;
import com.lancethomps.lava.common.TestingCommon;
import com.lancethomps.lava.common.collections.MapUtil;
import com.google.common.collect.Lists;

public class MergesTest extends BaseTest {

  public static final MergeConfig OVERWRITE_CONFIG = Merges.getDefaultMergeConfig().setOverwriteExisting(true).setOverwriteArrayNodes(true);

  private MergesTestData baseData;

  @Test
  public void testIgnoreFieldsPatterns() throws Exception {
    Map<String, Object> src = MapUtil.createFrom("dontignore", "dontignore", "ignore", "ignore");
    Map<String, Object> target = new HashMap<>();
    Merges.deepMerge(src, target, Merges.getOverwriteMergeConfig().addIgnoreFieldsPatterns("^ignore.*"));
    TestingCommon.assertEqualsViaJsonDiff("ignoreFieldsPatterns not processed correctly.", MapUtil.createFrom("dontignore", "dontignore"), target);
  }

  @Test
  public void testMergeArrayWithMatchField() throws Exception {
    MergesTestData src = new MergesTestData().setChildrenVal(
      Lists.newArrayList(
        new MergesTestData().setId("1").setStrVal("src").setIntVal(1),
        new MergesTestData().setId("2").setStrVal("src").setIntVal(2),
        new MergesTestData().setId("3").setStrVal("src").setIntVal(3)
      )
    );
    MergesTestData target = new MergesTestData().setChildrenVal(
      Lists.newArrayList(new MergesTestData().setId("1").setStrVal("target"), new MergesTestData().setId("2").setStrVal("target"))
    );
    MergeConfig mergeConfig = Merges.getDefaultMergeConfig().setCreateNewBean(true).addField(
      "childrenVal",
      new MergeFieldConfig()
        .setOverwriteExisting(false)
        .setMergeArrayElements(true)
        .setMergeArrayElementsMatchField("id")
        .setMergeArrayElementsSkipNonMatching(true)
    );
    MergesTestData merged = Merges.deepMerge(src, target, mergeConfig);
    MergesTestData expected = new MergesTestData()
      .setChildrenVal(Lists.newArrayList(
        new MergesTestData().setId("1").setStrVal("target").setIntVal(1),
        new MergesTestData().setId("2").setStrVal("target").setIntVal(2)
      ));
    TestingCommon.assertEqualsViaJsonDiff("MergesTest.testMergeArrayWithMatchField mergeArrayElementsSkipNonMatching:true", expected, merged);
    MergesTestData expectedWithoutSkip = new MergesTestData()
      .setChildrenVal(
        Lists.newArrayList(
          new MergesTestData().setId("1").setStrVal("target").setIntVal(1),
          new MergesTestData().setId("2").setStrVal("target").setIntVal(2),
          new MergesTestData().setId("3").setStrVal("src").setIntVal(3)
        )
      );
    TestingCommon.assertEqualsViaJsonDiff(
      "MergesTest.testMergeArrayWithMatchField mergeArrayElementsSkipNonMatching:false",
      expectedWithoutSkip,
      Merges.deepMerge(src, target, mergeConfig.getFields().get("childrenVal").setMergeArrayElementsSkipNonMatching(false))
    );
  }

  @Test
  public void testMergeArraysWithoutJacksonMergeConfig() throws Exception {
    Map<String, String[]> src = MapUtil.createFromQueryString("x=1&x=2");
    Map<String, String[]> target = MapUtil.createFromQueryString("x=3");
    Map<String, String[]> merged = Merges.deepMerge(src, target, Merges.getOverwriteMergeConfig());
    TestingCommon.assertEqualsViaJsonDiff("MergesTest.testMergeArraysWithoutJacksonMergeConfig", src, merged);
  }

  @Test
  public void testMergeWithOverwrite() throws Exception {
    MergesTestData target = getBaseDataCopy();
    MergesTestData src = getBaseDataCopy();
    String newStrVal = Randoms.createRandomValue(String.class);
    src.setStrVal(newStrVal);
    Merges.deepMerge(src, target, OVERWRITE_CONFIG);
    Assert.assertEquals("Field 'strVal' on target did not contain the new value!", newStrVal, target.getStrVal());
  }

  private MergesTestData getBaseDataCopy() throws IOException {
    synchronized (this) {
      if (baseData == null) {
        baseData = Randoms.createRandomValue(MergesTestData.class);
      }
    }
    return Merges.MERGE_MAPPER.readValue(Merges.MERGE_MAPPER.writeValueAsBytes(baseData), MergesTestData.class);
  }

}
