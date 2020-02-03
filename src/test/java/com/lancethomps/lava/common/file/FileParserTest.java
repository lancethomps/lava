package com.lancethomps.lava.common.file;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.lancethomps.lava.common.TestingCommon;
import com.lancethomps.lava.common.ser.Serializer;

public class FileParserTest {

  @Test
  public void fromCsv_multiLineField() throws FileParsingException {
    FileParser<Map<String, Object>> parser = new FileParser<>(getClass().getResourceAsStream("/serializer/fromCsv_multiLineField.csv"), null, null);
    parser.parseFile();

    List<Map<String, Object>> deserializedCsv = parser.getResultList();
    List<Map<String, Object>> expected = Serializer.readJsonAsList(getClass().getResourceAsStream("/serializer/fromCsv_multiLineField.json"));

    TestingCommon.assertEqualsViaJsonDiff("fromCsv_multiLineField", expected, deserializedCsv);
  }

}