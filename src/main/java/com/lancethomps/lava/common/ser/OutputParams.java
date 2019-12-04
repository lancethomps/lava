package com.lancethomps.lava.common.ser;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.BooleanUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.web.requests.parsers.RequestField;

public class OutputParams extends ExternalizableBean {

  @RequestField
  private String callback;

  @RequestField(additionalParameterNames = "out.createExprs")
  private List<OutputExpression> createExpressions;

  @RequestField
  private Boolean csvAlwaysQuote;

  @RequestField
  private Boolean csvNeverQuote;

  @RequestField
  private Boolean csvAsFlatObjs;

  @RequestField
  private String csvChainingSep;

  @RequestField
  private String csvDelimiter;

  @RequestField
  private Boolean csvExpandJsonFields;

  @RequestField
  private Boolean csvExpandJsonFieldsInner;

  @RequestField
  private List<String> csvHeaders;

  @RequestField
  private Set<String> csvIncludeProperties;

  @RequestField
  private Boolean csvNoFile;

  // TODO: This isnt be merged correctly in Merges.deepMerge - need to figure out why
  @RequestField
  private CsvParams csvParams;

  @RequestField
  private String csvSkipRowsExpression;

  @RequestField(additionalParameterNames = {"out.sort"})
  private String csvSort;

  @RequestField
  private String csvSortByField;

  @RequestField
  private String csvType;

  @RequestField
  private Boolean csvUseChainingListSep;

  @RequestField
  private Set<String> csvWrapFieldsInFunction;

  @RequestField
  private Boolean datesAsStrings;

  @RequestField
  private String datesAsStringsFormat;

  @RequestField
  private Boolean encodeStringsForHtml;

  private Map<Class<?>, BiPredicate<Object, Object>> fieldInclusionPredicateFilter;

  @RequestField
  private Set<Pattern> fieldsBlackList;

  @RequestField
  private Set<Pattern> fieldsWhiteList;

  @RequestField
  private String fileName;

  @RequestField
  private String graph;

  @RequestField(additionalParameterNames = {"outputIncludeType"})
  private Boolean jsonIncludeType;

  @RequestField(additionalParameterNames = {"out.sigFigs"})
  private Integer jsonSigFigs;

  private boolean modificationsDisabled;

  @JsonIgnore
  private ObjectMapper objectMapper;

  private String objectMapperCustomCacheKeyId;

  @RequestField(additionalParameterNames = {"out.ognl"})
  private Boolean ognl;

  @RequestField
  private Set<String> onlyFields;

  @RequestField
  private Boolean orderKeys;

  @RequestField(additionalParameterNames = {"csvTypeField", "out.expr"})
  private String outputDataExpression;

  @RequestField
  private OutputFormat outputFormat;

  @RequestField(additionalParameterNames = {"out.postProcessExprs"})
  private List<OutputExpression> postProcessExpressions;

  @RequestField
  private Boolean prettifyJson;

  @RequestField
  private Boolean quoteFieldNames;

  @RequestField
  private List<String> rcs;

  @RequestField
  private Boolean sanitizeJson;

  @RequestField(additionalParameterNames = {"csvSkipProperties"})
  private Set<String> skipFields;

  private Map<Class<?>, Set<String>> skipFieldsByType;

  @RequestField
  private Boolean skipStrings;

  @RequestField
  private String templateId;

  @RequestField
  private Boolean transpose;

  public OutputParams addCsvIncludeProperties(@Nonnull Collection<String> csvIncludeProperties) {
    checkModificationsDisabled();
    if (this.csvIncludeProperties == null) {
      this.csvIncludeProperties = new LinkedHashSet<>();
    }
    this.csvIncludeProperties.addAll(csvIncludeProperties);
    return this;
  }

  public OutputParams addCsvIncludeProperties(@Nonnull String... csvIncludeProperties) {
    return addCsvIncludeProperties(Arrays.asList(csvIncludeProperties));
  }

  public OutputParams addFieldsBlackList(@Nonnull Collection<Pattern> fieldsBlackList) {
    checkModificationsDisabled();
    if (this.fieldsBlackList == null) {
      this.fieldsBlackList = new LinkedHashSet<>();
    }
    this.fieldsBlackList.addAll(fieldsBlackList);
    return this;
  }

  public OutputParams addFieldsBlackList(@Nonnull Pattern... fieldsBlackList) {
    return addFieldsBlackList(Arrays.asList(fieldsBlackList));
  }

  public OutputParams addFieldsWhiteList(@Nonnull Collection<Pattern> fieldsWhiteList) {
    checkModificationsDisabled();
    if (this.fieldsWhiteList == null) {
      this.fieldsWhiteList = new LinkedHashSet<>();
    }
    this.fieldsWhiteList.addAll(fieldsWhiteList);
    return this;
  }

  public OutputParams addFieldsWhiteList(@Nonnull Pattern... fieldsWhiteList) {
    return addFieldsWhiteList(Arrays.asList(fieldsWhiteList));
  }

  public OutputParams addOnlyFields(@Nonnull Collection<String> onlyFields) {
    checkModificationsDisabled();
    if (this.onlyFields == null) {
      this.onlyFields = new LinkedHashSet<>();
    }
    this.onlyFields.addAll(onlyFields);
    return this;
  }

  public OutputParams addOnlyFields(@Nonnull String... onlyFields) {
    return addOnlyFields(Arrays.asList(onlyFields));
  }

  public OutputParams addSkipFields(@Nonnull Collection<String> skipFields) {
    checkModificationsDisabled();
    if (this.skipFields == null) {
      this.skipFields = new LinkedHashSet<>();
    }
    this.skipFields.addAll(skipFields);
    return this;
  }

  public OutputParams addSkipFields(@Nonnull String... skipFields) {
    return addSkipFields(Arrays.asList(skipFields));
  }

  public boolean canCacheResolvedObjectMapper() {
    return Checks.isEmpty(fieldInclusionPredicateFilter) && ((objectMapper == null) || (objectMapperCustomCacheKeyId != null));
  }

  public OutputParams copy() {
    OutputParams copied = Serializer.copy(this);
    copied.modificationsDisabled = false;
    return copied;
  }

  public OutputParams disableModifications() {
    modificationsDisabled = true;
    return this;
  }

  public String getCallback() {
    return callback;
  }

  public OutputParams setCallback(String callback) {
    checkModificationsDisabled();
    this.callback = callback;
    return this;
  }

  public List<OutputExpression> getCreateExpressions() {
    return createExpressions;
  }

  public OutputParams setCreateExpressions(List<OutputExpression> createExpressions) {
    checkModificationsDisabled();
    this.createExpressions = createExpressions;
    return this;
  }

  public Boolean getCsvAlwaysQuote() {
    return csvAlwaysQuote;
  }

  public OutputParams setCsvAlwaysQuote(Boolean csvAlwaysQuote) {
    checkModificationsDisabled();
    this.csvAlwaysQuote = csvAlwaysQuote;
    return this;
  }

  public Boolean getCsvNeverQuote() {
    return csvNeverQuote;
  }

  public OutputParams setCsvNeverQuote(Boolean csvNeverQuote) {
    checkModificationsDisabled();
    this.csvNeverQuote = csvNeverQuote;
    return this;
  }

  public Boolean getCsvAsFlatObjs() {
    return csvAsFlatObjs;
  }

  public OutputParams setCsvAsFlatObjs(Boolean csvAsFlatObjs) {
    checkModificationsDisabled();
    this.csvAsFlatObjs = csvAsFlatObjs;
    return this;
  }

  public String getCsvChainingSep() {
    return csvChainingSep;
  }

  public OutputParams setCsvChainingSep(String csvChainingSep) {
    checkModificationsDisabled();
    this.csvChainingSep = csvChainingSep;
    return this;
  }

  public String getCsvDelimiter() {
    return csvDelimiter;
  }

  public OutputParams setCsvDelimiter(String csvDelimiter) {
    checkModificationsDisabled();
    this.csvDelimiter = csvDelimiter;
    return this;
  }

  public Boolean getCsvExpandJsonFields() {
    return csvExpandJsonFields;
  }

  public OutputParams setCsvExpandJsonFields(Boolean csvExpandJsonFields) {
    checkModificationsDisabled();
    this.csvExpandJsonFields = csvExpandJsonFields;
    return this;
  }

  public Boolean getCsvExpandJsonFieldsInner() {
    return csvExpandJsonFieldsInner;
  }

  public OutputParams setCsvExpandJsonFieldsInner(Boolean csvExpandJsonFieldsInner) {
    checkModificationsDisabled();
    this.csvExpandJsonFieldsInner = csvExpandJsonFieldsInner;
    return this;
  }

  public List<String> getCsvHeaders() {
    return csvHeaders;
  }

  public OutputParams setCsvHeaders(List<String> csvHeaders) {
    checkModificationsDisabled();
    this.csvHeaders = csvHeaders;
    return this;
  }

  public Set<String> getCsvIncludeProperties() {
    return csvIncludeProperties;
  }

  public OutputParams setCsvIncludeProperties(Set<String> csvIncludeProperties) {
    checkModificationsDisabled();
    this.csvIncludeProperties = csvIncludeProperties;
    return this;
  }

  public Boolean getCsvNoFile() {
    return csvNoFile;
  }

  public OutputParams setCsvNoFile(Boolean csvNoFile) {
    checkModificationsDisabled();
    this.csvNoFile = csvNoFile;
    return this;
  }

  public CsvParams getCsvParams() {
    return csvParams;
  }

  public OutputParams setCsvParams(CsvParams csvParams) {
    checkModificationsDisabled();
    this.csvParams = csvParams;
    return this;
  }

  public String getCsvSkipRowsExpression() {
    return csvSkipRowsExpression;
  }

  public OutputParams setCsvSkipRowsExpression(String csvSkipRowsExpression) {
    checkModificationsDisabled();
    this.csvSkipRowsExpression = csvSkipRowsExpression;
    return this;
  }

  public String getCsvSort() {
    return csvSort;
  }

  public OutputParams setCsvSort(String csvSort) {
    checkModificationsDisabled();
    this.csvSort = csvSort;
    return this;
  }

  public String getCsvSortByField() {
    return csvSortByField;
  }

  public OutputParams setCsvSortByField(String csvSortByField) {
    checkModificationsDisabled();
    this.csvSortByField = csvSortByField;
    return this;
  }

  public String getCsvType() {
    return csvType;
  }

  public OutputParams setCsvType(String csvType) {
    checkModificationsDisabled();
    this.csvType = csvType;
    return this;
  }

  public Boolean getCsvUseChainingListSep() {
    return csvUseChainingListSep;
  }

  public OutputParams setCsvUseChainingListSep(Boolean csvUseChainingListSep) {
    checkModificationsDisabled();
    this.csvUseChainingListSep = csvUseChainingListSep;
    return this;
  }

  public Set<String> getCsvWrapFieldsInFunction() {
    return csvWrapFieldsInFunction;
  }

  public OutputParams setCsvWrapFieldsInFunction(Set<String> csvWrapFieldsInFunction) {
    checkModificationsDisabled();
    this.csvWrapFieldsInFunction = csvWrapFieldsInFunction;
    return this;
  }

  public Boolean getDatesAsStrings() {
    return datesAsStrings;
  }

  public OutputParams setDatesAsStrings(Boolean datesAsStrings) {
    checkModificationsDisabled();
    this.datesAsStrings = datesAsStrings;
    return this;
  }

  public String getDatesAsStringsFormat() {
    return datesAsStringsFormat;
  }

  public OutputParams setDatesAsStringsFormat(String datesAsStringsFormat) {
    checkModificationsDisabled();
    this.datesAsStringsFormat = datesAsStringsFormat;
    return this;
  }

  public Boolean getEncodeStringsForHtml() {
    return encodeStringsForHtml;
  }

  public OutputParams setEncodeStringsForHtml(Boolean encodeStringsForHtml) {
    checkModificationsDisabled();
    this.encodeStringsForHtml = encodeStringsForHtml;
    return this;
  }

  public Map<Class<?>, BiPredicate<Object, Object>> getFieldInclusionPredicateFilter() {
    return fieldInclusionPredicateFilter;
  }

  public OutputParams setFieldInclusionPredicateFilter(Map<Class<?>, BiPredicate<Object, Object>> fieldInclusionPredicateFilter) {
    checkModificationsDisabled();
    this.fieldInclusionPredicateFilter = fieldInclusionPredicateFilter;
    return this;
  }

  public Set<Pattern> getFieldsBlackList() {
    return fieldsBlackList;
  }

  public OutputParams setFieldsBlackList(Set<Pattern> fieldsBlackList) {
    checkModificationsDisabled();
    this.fieldsBlackList = fieldsBlackList;
    return this;
  }

  public Set<Pattern> getFieldsWhiteList() {
    return fieldsWhiteList;
  }

  public OutputParams setFieldsWhiteList(Set<Pattern> fieldsWhiteList) {
    checkModificationsDisabled();
    this.fieldsWhiteList = fieldsWhiteList;
    return this;
  }

  public String getFileName() {
    return fileName;
  }

  public OutputParams setFileName(String fileName) {
    checkModificationsDisabled();
    this.fileName = fileName;
    return this;
  }

  public String getGraph() {
    return graph;
  }

  public OutputParams setGraph(String graph) {
    checkModificationsDisabled();
    this.graph = graph;
    return this;
  }

  public Boolean getJsonIncludeType() {
    return jsonIncludeType;
  }

  public OutputParams setJsonIncludeType(Boolean jsonIncludeType) {
    checkModificationsDisabled();
    this.jsonIncludeType = jsonIncludeType;
    return this;
  }

  public Integer getJsonSigFigs() {
    return jsonSigFigs;
  }

  public OutputParams setJsonSigFigs(Integer jsonSigFigs) {
    checkModificationsDisabled();
    this.jsonSigFigs = jsonSigFigs;
    return this;
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public OutputParams setObjectMapper(ObjectMapper objectMapper) {
    checkModificationsDisabled();
    this.objectMapper = objectMapper;
    return this;
  }

  public String getObjectMapperCustomCacheKeyId() {
    return objectMapperCustomCacheKeyId;
  }

  public OutputParams setObjectMapperCustomCacheKeyId(String objectMapperCustomCacheKeyId) {
    checkModificationsDisabled();
    this.objectMapperCustomCacheKeyId = objectMapperCustomCacheKeyId;
    return this;
  }

  public Boolean getOgnl() {
    return ognl;
  }

  public OutputParams setOgnl(Boolean ognl) {
    checkModificationsDisabled();
    this.ognl = ognl;
    return this;
  }

  public Set<String> getOnlyFields() {
    return onlyFields;
  }

  public OutputParams setOnlyFields(Set<String> onlyFields) {
    checkModificationsDisabled();
    this.onlyFields = onlyFields;
    return this;
  }

  public Boolean getOrderKeys() {
    return orderKeys;
  }

  public OutputParams setOrderKeys(Boolean orderKeys) {
    checkModificationsDisabled();
    this.orderKeys = orderKeys;
    return this;
  }

  public String getOutputDataExpression() {
    return outputDataExpression;
  }

  public OutputParams setOutputDataExpression(String outputDataExpression) {
    checkModificationsDisabled();
    this.outputDataExpression = outputDataExpression;
    return this;
  }

  public OutputFormat getOutputFormat() {
    return outputFormat;
  }

  public OutputParams setOutputFormat(OutputFormat outputFormat) {
    checkModificationsDisabled();
    this.outputFormat = outputFormat;
    return this;
  }

  public List<OutputExpression> getPostProcessExpressions() {
    return postProcessExpressions;
  }

  public OutputParams setPostProcessExpressions(List<OutputExpression> postProcessExpressions) {
    checkModificationsDisabled();
    this.postProcessExpressions = postProcessExpressions;
    return this;
  }

  public Boolean getPrettifyJson() {
    return prettifyJson;
  }

  public OutputParams setPrettifyJson(Boolean prettifyJson) {
    checkModificationsDisabled();
    this.prettifyJson = prettifyJson;
    return this;
  }

  public Boolean getQuoteFieldNames() {
    return quoteFieldNames;
  }

  public OutputParams setQuoteFieldNames(Boolean quoteFieldNames) {
    this.quoteFieldNames = quoteFieldNames;
    return this;
  }

  public List<String> getRcs() {
    return rcs;
  }

  public OutputParams setRcs(List<String> rcs) {
    checkModificationsDisabled();
    this.rcs = rcs;
    return this;
  }

  public Boolean getSanitizeJson() {
    return sanitizeJson;
  }

  public OutputParams setSanitizeJson(Boolean sanitizeJson) {
    checkModificationsDisabled();
    this.sanitizeJson = sanitizeJson;
    return this;
  }

  public Set<String> getSkipFields() {
    return skipFields;
  }

  public OutputParams setSkipFields(Set<String> skipFields) {
    checkModificationsDisabled();
    this.skipFields = skipFields;
    return this;
  }

  public Map<Class<?>, Set<String>> getSkipFieldsByType() {
    return skipFieldsByType;
  }

  public OutputParams setSkipFieldsByType(Map<Class<?>, Set<String>> skipFieldsByType) {
    checkModificationsDisabled();
    this.skipFieldsByType = skipFieldsByType;
    return this;
  }

  public Boolean getSkipStrings() {
    return skipStrings;
  }

  public OutputParams setSkipStrings(Boolean skipStrings) {
    checkModificationsDisabled();
    this.skipStrings = skipStrings;
    return this;
  }

  public String getTemplateId() {
    return templateId;
  }

  public OutputParams setTemplateId(String templateId) {
    checkModificationsDisabled();
    this.templateId = templateId;
    return this;
  }

  public Boolean getTranspose() {
    return transpose;
  }

  public OutputParams setTranspose(Boolean transpose) {
    checkModificationsDisabled();
    this.transpose = transpose;
    return this;
  }

  public boolean isModificationsDisabled() {
    return modificationsDisabled;
  }

  public boolean testCsvAlwaysQuote() {
    return (csvAlwaysQuote != null) && csvAlwaysQuote.booleanValue();
  }

  public boolean testCsvNeverQuote() {
    return (csvNeverQuote != null) && csvNeverQuote.booleanValue();
  }

  public boolean testCsvAsFlatObjs() {
    return BooleanUtils.toBoolean(csvAsFlatObjs);
  }

  public boolean testCsvNoFile() {
    return BooleanUtils.toBoolean(csvNoFile);
  }

  public boolean testCsvUseChainingListSep() {
    return BooleanUtils.toBoolean(csvUseChainingListSep);
  }

  public boolean testDatesAsStrings() {
    return (datesAsStrings != null) && datesAsStrings;
  }

  public boolean testEncodeStringsForHtml() {
    return (encodeStringsForHtml != null) && encodeStringsForHtml.booleanValue();
  }

  public boolean testJsonIncludeType() {
    return jsonIncludeType == null ? true : jsonIncludeType;
  }

  public boolean testOgnl() {
    return (getOgnl() != null) && getOgnl().booleanValue();
  }

  public boolean testOrderKeys() {
    return (orderKeys != null) && orderKeys;
  }

  public boolean testPrettifyJson() {
    return toBoolean(prettifyJson);
  }

  public boolean testSanitizeJson() {
    return (sanitizeJson != null) && sanitizeJson.booleanValue();
  }

  public boolean testSkipStrings() {
    return (skipStrings == null) || skipStrings;
  }

  public boolean testTranspose() {
    return BooleanUtils.toBoolean(transpose);
  }

  public String toObjectMapperCacheKey() {
    final StringBuilder builder = new StringBuilder()
      .append("csvAsFlatObjs=")
      .append(csvAsFlatObjs)
      .append(" csvChainingSep=")
      .append(csvChainingSep)
      .append(" csvDelimiter=")
      .append(csvDelimiter)
      .append(" csvExpandJsonFields=")
      .append(csvExpandJsonFields)
      .append(" csvExpandJsonFieldsInner=")
      .append(csvExpandJsonFieldsInner)
      .append(" csvHeaders=")
      .append(csvHeaders)
      .append(" csvIncludeProperties=")
      .append(csvIncludeProperties)
      .append(" csvNoFile=")
      .append(csvNoFile)
      .append(" csvSkipRowsExpression=")
      .append(csvSkipRowsExpression)
      .append(" csvSort=")
      .append(csvSort)
      .append(" csvSortByField=")
      .append(csvSortByField)
      .append(" csvType=")
      .append(csvType)
      .append(" csvUseChainingListSep=")
      .append(csvUseChainingListSep)
      .append(" csvWrapFieldsInFunction=")
      .append(csvWrapFieldsInFunction)
      .append(" datesAsStrings=")
      .append(datesAsStrings)
      .append(" datesAsStringsFormat=")
      .append(datesAsStringsFormat)
      .append(" encodeStringsForHtml=")
      .append(encodeStringsForHtml)
      .append(" fieldsBlackList=")
      .append(fieldsBlackList)
      .append(" fieldsWhiteList=")
      .append(fieldsWhiteList)
      .append(" graph=")
      .append(graph)
      .append(" jsonIncludeType=")
      .append(jsonIncludeType)
      .append(" jsonSigFigs=")
      .append(jsonSigFigs)
      .append(" onlyFields=")
      .append(onlyFields)
      .append(" orderKeys=")
      .append(orderKeys)
      .append(" outputFormat=")
      .append(outputFormat)
      .append(" prettifyJson=")
      .append(prettifyJson)
      .append(" sanitizeJson=")
      .append(sanitizeJson)
      .append(" quoteFieldNames=")
      .append(quoteFieldNames)
      .append(" skipFields=")
      .append(skipFields)
      .append(" skipFieldsByType=")
      .append(skipFieldsByType)
      .append(" skipStrings=")
      .append(skipStrings)
      .append(" templateId=")
      .append(templateId)
      .append(" transpose=")
      .append(transpose);
    if (getObjectMapper() != null) {
      builder
        .append(" objectMapper=")
        .append(objectMapper.hashCode());
    }
    if (getObjectMapperCustomCacheKeyId() != null) {
      builder
        .append(" objectMapperCustomCacheKeyId=")
        .append(objectMapperCustomCacheKeyId);
    }
    return builder.toString();
  }

  public OutputParams updateOutputFormatIfAllowed(OutputFormat outputFormat) {
    if (modificationsDisabled) {
      return this;
    }
    this.outputFormat = outputFormat;
    return this;
  }

  private void checkModificationsDisabled() {
    if (modificationsDisabled) {
      throw new UnsupportedOperationException("Modifications have been disabled for this instance!");
    }
  }

}
