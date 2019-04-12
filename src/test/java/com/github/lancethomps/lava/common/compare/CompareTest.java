package com.github.lancethomps.lava.common.compare;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.lancethomps.lava.common.BaseTest;
import com.github.lancethomps.lava.common.TestingCommon;
import com.github.lancethomps.lava.common.merge.Merges;

public class CompareTest extends BaseTest {

  @Test
  public void testGenerateDiffObject() throws Exception {
    final ObjectNode diffs = Compare.generateDifferences(
      getDiffFieldsOriginal(),
      getDiffFieldsRevised(),
      new CompareConfig()
        .setCalculateNumericValueChange(true)
        .setDeep(true)
        .setNumericNullsEqualZero(true)
    );
    TestingCommon.assertEqualsViaJsonDiff("Compare.generateDifferences", Merges.MERGE_MAPPER.readTree(getResultFileStream("object")), diffs);
  }

  @Test
  public void testGetFieldsWithDiffs() throws Exception {
    List<String> fields = new ArrayList<>(Compare.getFieldsWithDifferences(getDiffFieldsOriginal(), getDiffFieldsRevised()));
    List<String> correctFields = getCorrectResultFields("fields");
    Collections.sort(fields);
    Collections.sort(correctFields);
    TestingCommon.assertEqualsViaJsonDiff("Compare.getFieldsWithDifferences", correctFields, fields);
  }

  @Test
  public void testGetFieldsWithDiffsWithIgnoreFields() throws Exception {
    CompareConfig config = new CompareConfig().setDeep(true).addIgnoreFields("diff_number", "diff_number_array");
    List<String> fields = new ArrayList<>(Compare.getFieldsWithDifferences(getDiffFieldsOriginal(), getDiffFieldsRevised(), config));
    List<String> correctFields = getCorrectResultFields("fields", "ignorefields");
    Collections.sort(fields);
    Collections.sort(correctFields);
    TestingCommon.assertEqualsViaJsonDiff("Compare.getFieldsWithDifferences", correctFields, fields);
  }

  private List<String> getCorrectResultFields(final @Nonnull String... ids) throws IOException {
    List<String> correctFields = Merges.MERGE_MAPPER.readValue(
      getResultFileStream(ids),
      Merges.MERGE_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, String.class)
    );
    Collections.sort(correctFields);
    return correctFields;
  }

  private Object getDiffFieldsOriginal() throws IOException {
    return Merges.MERGE_MAPPER.readValue(getClass().getResourceAsStream("/compare/diff-fields-original.json"), Object.class);
  }

  private Object getDiffFieldsRevised() throws IOException {
    return Merges.MERGE_MAPPER.readValue(getClass().getResourceAsStream("/compare/diff-fields-revised.json"), Object.class);
  }

  private InputStream getResultFileStream(final @Nonnull String... ids) {
    String fileName = "/compare/diff-result" + Stream.of(ids).collect(Collectors.joining("-", "-", ".json"));
    return getClass().getResourceAsStream(fileName);
  }

}
