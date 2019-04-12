package com.github.lancethomps.lava.common.ser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.github.lancethomps.lava.common.BaseTest;
import com.github.lancethomps.lava.common.FailedTestDataLogger;
import com.github.lancethomps.lava.common.TestingCommon;

public class SerializerTest extends BaseTest {

  @Rule
  public final FailedTestDataLogger dataLogger = new FailedTestDataLogger();

  @Test
  public void testDeserializeCsv() throws Exception {
    final List<Map<String, Object>> csvData = getCsvTestData();

    final List<Map<String, Object>> deserialized = Serializer.fromCsv(getClass().getResourceAsStream("/serializer/deserialize-csv-data.csv"));
    dataLogger.addData("deserialized", deserialized);
    TestingCommon.assertEqualsViaJsonDiff("Serializer.fromCsv", csvData, deserialized);
  }

  @Test
  public void testSerializeCsv() throws Exception {
    final String serialized = Serializer.toCsv(getCsvTestData());

    final String csv =
      StringUtils.replace(
        IOUtils.toString(getClass().getResourceAsStream("/serializer/serialize-csv-data.csv"), StandardCharsets.UTF_8),
        "\n",
        System.lineSeparator()
      );
    Assert.assertEquals("Serializer.toCsv", csv, serialized);
  }

  public void testXlsDeserialization() throws Exception {
    // TODO: add test
  }

  private List<Map<String, Object>> getCsvTestData() {
    final List<Map<String, Object>> csvData = new ArrayList<>();
    final Map<String, Object> data = new HashMap<>();
    csvData.add(data);
    data.put("field", "value");
    final Map<String, Object> innerData = new HashMap<>();
    innerData.put("field", "innerValue");
    data.put("innerData", innerData);
    final List<String> list = Arrays.asList("listVal1", "listVal2");
    data.put("list", list);
    dataLogger.addData("csvData", csvData);
    return csvData;
  }

}
