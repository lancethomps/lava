package com.lancethomps.lava.common.file;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.lancethomps.lava.common.expr.ExprFactory;
import com.lancethomps.lava.common.ser.ExternalizableBean;
import com.lancethomps.lava.common.ser.OutputExpression;
import com.lancethomps.lava.common.ser.Serializer;
import com.lancethomps.lava.common.web.requests.parsers.RequestField;

public class FileParserOptions extends ExternalizableBean {

  @RequestField
  private Boolean allowBlanks;

  @RequestField
  private Map<String, Set<String>> copyFields;

  private CsvFieldConverter fieldConverter;

  @RequestField
  private Set<Pattern> fieldValuesBlackList;

  @RequestField
  private Map<String, Set<Pattern>> fieldValuesBlackListByColumn;

  @RequestField
  private Set<Pattern> fieldValuesWhiteList;

  @RequestField
  private Map<String, Set<Pattern>> fieldValuesWhiteListByColumn;

  @RequestField
  private Integer firstRowNum;

  @RequestField
  private Map<String, Integer> firstRowNumBySheet;

  @RequestField
  private Map<String, String> headerConversions;

  private CsvFieldConverter headerConverter;

  private Map<String, Integer> headers;

  @RequestField
  private Set<Pattern> headersBlackList;

  @RequestField
  private Set<Pattern> headersWhiteList;

  @RequestField
  private Set<String> ignoreFields;

  @RequestField
  private Boolean keysAsPaths;

  @RequestField
  private Integer maxLines;

  private boolean modificationsDisabled;

  @RequestField
  private Set<String> onlySheets;

  private FileParserPostProcessor postProcessor;

  @RequestField
  private Set<String> removeLinesWithPrefixes;

  @RequestField
  private Map<String, Object> rowConstants;

  private List<OutputExpression> rowValidationExpressions;

  @RequestField
  private Character sepChar;

  @RequestField
  private Set<String> skipSheets;

  @RequestField
  private Boolean strict;

  @RequestField
  private Boolean transpose;

  @RequestField
  private Boolean trimValues;

  @RequestField
  private Class<?> type;

  @RequestField
  private Boolean useExcelFormats;

  @RequestField
  private Boolean usePathKeys;

  public FileParserOptions addIgnoreFields(@Nonnull Collection<String> ignoreFields) {
    checkModificationsDisabled();
    if (this.ignoreFields == null) {
      this.ignoreFields = new LinkedHashSet<>();
    }
    this.ignoreFields.addAll(ignoreFields);
    return this;
  }

  public FileParserOptions addIgnoreFields(@Nonnull String... ignoreFields) {
    return addIgnoreFields(Arrays.asList(ignoreFields));
  }

  @Override
  public void afterDeserialization() {
    if (rowValidationExpressions != null) {
      ExprFactory.compileCreateExpressions(rowValidationExpressions, false, false, true);
    }
  }

  public FileParserOptions copy() {
    FileParserOptions copied = Serializer.copy(this);
    copied.modificationsDisabled = false;
    return copied;
  }

  public FileParserOptions disableModifications() {
    modificationsDisabled = true;
    return this;
  }

  public Boolean getAllowBlanks() {
    return allowBlanks;
  }

  public FileParserOptions setAllowBlanks(Boolean allowBlanks) {
    checkModificationsDisabled();
    this.allowBlanks = allowBlanks;
    return this;
  }

  public Map<String, Set<String>> getCopyFields() {
    return copyFields;
  }

  public FileParserOptions setCopyFields(Map<String, Set<String>> copyFields) {
    checkModificationsDisabled();
    this.copyFields = copyFields;
    return this;
  }

  public CsvFieldConverter getFieldConverter() {
    return fieldConverter;
  }

  public FileParserOptions setFieldConverter(CsvFieldConverter fieldConverter) {
    checkModificationsDisabled();
    this.fieldConverter = fieldConverter;
    return this;
  }

  public Set<Pattern> getFieldValuesBlackList() {
    return fieldValuesBlackList;
  }

  public FileParserOptions setFieldValuesBlackList(Set<Pattern> fieldValuesBlackList) {
    checkModificationsDisabled();
    this.fieldValuesBlackList = fieldValuesBlackList;
    return this;
  }

  public Map<String, Set<Pattern>> getFieldValuesBlackListByColumn() {
    return fieldValuesBlackListByColumn;
  }

  public FileParserOptions setFieldValuesBlackListByColumn(Map<String, Set<Pattern>> fieldValuesBlackListByColumn) {
    checkModificationsDisabled();
    this.fieldValuesBlackListByColumn = fieldValuesBlackListByColumn;
    return this;
  }

  public Set<Pattern> getFieldValuesWhiteList() {
    return fieldValuesWhiteList;
  }

  public FileParserOptions setFieldValuesWhiteList(Set<Pattern> fieldValuesWhiteList) {
    checkModificationsDisabled();
    this.fieldValuesWhiteList = fieldValuesWhiteList;
    return this;
  }

  public Map<String, Set<Pattern>> getFieldValuesWhiteListByColumn() {
    return fieldValuesWhiteListByColumn;
  }

  public FileParserOptions setFieldValuesWhiteListByColumn(Map<String, Set<Pattern>> fieldValuesWhiteListByColumn) {
    checkModificationsDisabled();
    this.fieldValuesWhiteListByColumn = fieldValuesWhiteListByColumn;
    return this;
  }

  public Integer getFirstRowNum() {
    return firstRowNum;
  }

  public FileParserOptions setFirstRowNum(Integer firstRowNum) {
    checkModificationsDisabled();
    this.firstRowNum = firstRowNum;
    return this;
  }

  public Map<String, Integer> getFirstRowNumBySheet() {
    return firstRowNumBySheet;
  }

  public FileParserOptions setFirstRowNumBySheet(Map<String, Integer> firstRowNumBySheet) {
    checkModificationsDisabled();
    this.firstRowNumBySheet = firstRowNumBySheet;
    return this;
  }

  public Map<String, String> getHeaderConversions() {
    return headerConversions;
  }

  public FileParserOptions setHeaderConversions(Map<String, String> headerConversions) {
    checkModificationsDisabled();
    this.headerConversions = headerConversions;
    return this;
  }

  public CsvFieldConverter getHeaderConverter() {
    return headerConverter;
  }

  public FileParserOptions setHeaderConverter(CsvFieldConverter headerConverter) {
    checkModificationsDisabled();
    this.headerConverter = headerConverter;
    return this;
  }

  public Map<String, Integer> getHeaders() {
    return headers;
  }

  public FileParserOptions setHeaders(Map<String, Integer> headers) {
    checkModificationsDisabled();
    this.headers = headers;
    return this;
  }

  public Set<Pattern> getHeadersBlackList() {
    return headersBlackList;
  }

  public FileParserOptions setHeadersBlackList(Set<Pattern> headersBlackList) {
    checkModificationsDisabled();
    this.headersBlackList = headersBlackList;
    return this;
  }

  public Set<Pattern> getHeadersWhiteList() {
    return headersWhiteList;
  }

  public FileParserOptions setHeadersWhiteList(Set<Pattern> headersWhiteList) {
    checkModificationsDisabled();
    this.headersWhiteList = headersWhiteList;
    return this;
  }

  public Set<String> getIgnoreFields() {
    return ignoreFields;
  }

  public FileParserOptions setIgnoreFields(Set<String> ignoreFields) {
    checkModificationsDisabled();
    this.ignoreFields = ignoreFields;
    return this;
  }

  public Boolean getKeysAsPaths() {
    return keysAsPaths;
  }

  public FileParserOptions setKeysAsPaths(Boolean keysAsPaths) {
    checkModificationsDisabled();
    this.keysAsPaths = keysAsPaths;
    return this;
  }

  public Integer getMaxLines() {
    return maxLines;
  }

  public FileParserOptions setMaxLines(Integer maxLines) {
    checkModificationsDisabled();
    this.maxLines = maxLines;
    return this;
  }

  public Set<String> getOnlySheets() {
    return onlySheets;
  }

  public FileParserOptions setOnlySheets(Set<String> onlySheets) {
    checkModificationsDisabled();
    this.onlySheets = onlySheets;
    return this;
  }

  public FileParserPostProcessor getPostProcessor() {
    return postProcessor;
  }

  public FileParserOptions setPostProcessor(FileParserPostProcessor postProcessor) {
    checkModificationsDisabled();
    this.postProcessor = postProcessor;
    return this;
  }

  public Set<String> getRemoveLinesWithPrefixes() {
    return removeLinesWithPrefixes;
  }

  public FileParserOptions setRemoveLinesWithPrefixes(Set<String> removeLinesWithPrefixes) {
    checkModificationsDisabled();
    this.removeLinesWithPrefixes = removeLinesWithPrefixes;
    return this;
  }

  public Map<String, Object> getRowConstants() {
    return rowConstants;
  }

  public FileParserOptions setRowConstants(Map<String, Object> rowConstants) {
    checkModificationsDisabled();
    this.rowConstants = rowConstants;
    return this;
  }

  public List<OutputExpression> getRowValidationExpressions() {
    return rowValidationExpressions;
  }

  public FileParserOptions setRowValidationExpressions(List<OutputExpression> rowValidationExpressions) {
    checkModificationsDisabled();
    this.rowValidationExpressions = rowValidationExpressions;
    return this;
  }

  public Character getSepChar() {
    return sepChar;
  }

  public FileParserOptions setSepChar(Character sepChar) {
    checkModificationsDisabled();
    this.sepChar = sepChar;
    return this;
  }

  public Set<String> getSkipSheets() {
    return skipSheets;
  }

  public FileParserOptions setSkipSheets(Set<String> skipSheets) {
    checkModificationsDisabled();
    this.skipSheets = skipSheets;
    return this;
  }

  public Boolean getStrict() {
    return strict;
  }

  public FileParserOptions setStrict(Boolean strict) {
    checkModificationsDisabled();
    this.strict = strict;
    return this;
  }

  public Boolean getTranspose() {
    return transpose;
  }

  public FileParserOptions setTranspose(Boolean transpose) {
    checkModificationsDisabled();
    this.transpose = transpose;
    return this;
  }

  public Boolean getTrimValues() {
    return trimValues;
  }

  public FileParserOptions setTrimValues(Boolean trimValues) {
    checkModificationsDisabled();
    this.trimValues = trimValues;
    return this;
  }

  public Class<?> getType() {
    return type;
  }

  public FileParserOptions setType(Class<?> type) {
    checkModificationsDisabled();
    this.type = type;
    return this;
  }

  public Boolean getUseExcelFormats() {
    return useExcelFormats;
  }

  public FileParserOptions setUseExcelFormats(Boolean useExcelFormats) {
    checkModificationsDisabled();
    this.useExcelFormats = useExcelFormats;
    return this;
  }

  public Boolean getUsePathKeys() {
    return usePathKeys;
  }

  public FileParserOptions setUsePathKeys(Boolean usePathKeys) {
    checkModificationsDisabled();
    this.usePathKeys = usePathKeys;
    return this;
  }

  public boolean isModificationsDisabled() {
    return modificationsDisabled;
  }

  public boolean testAllowBlanks() {
    return (allowBlanks != null) && allowBlanks;
  }

  public boolean testKeysAsPaths() {
    return (keysAsPaths == null) || keysAsPaths;
  }

  public boolean testStrict() {
    return (strict != null) && strict.booleanValue();
  }

  public boolean testTranspose() {
    return (transpose != null) && transpose;
  }

  public boolean testTrimValues() {
    return (trimValues != null) && trimValues;
  }

  public boolean testUseExcelFormats() {
    return (useExcelFormats != null) && useExcelFormats;
  }

  public boolean testUsePathKeys() {
    return (usePathKeys != null) && usePathKeys.booleanValue();
  }

  private void checkModificationsDisabled() {
    if (modificationsDisabled) {
      throw new UnsupportedOperationException("Modifications have been disabled for this instance!");
    }
  }

}
