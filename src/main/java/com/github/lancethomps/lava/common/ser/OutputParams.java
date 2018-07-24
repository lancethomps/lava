package com.github.lancethomps.lava.common.ser;

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

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.apis.ApiDocConfig;
import com.github.lancethomps.lava.common.web.requests.parsers.RequestField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class OutputParams.
 */
public class OutputParams extends ExternalizableBean {

	/** The jsonp callback. */
	@RequestField
	private String callback;

	/** The create bean expressions. */
	@RequestField(additionalParameterNames = "out.createExprs")
	private List<OutputExpression> createExpressions;

	/** The csv as flat objs. */
	@RequestField
	private Boolean csvAsFlatObjs;

	/** The csv chaining sep. */
	@RequestField
	private String csvChainingSep;

	/** The csv delimiter. */
	@RequestField
	private String csvDelimiter;

	/** The csv expand json fields. */
	@RequestField
	private Boolean csvExpandJsonFields;

	/** The csv expand json fields inner. */
	@RequestField
	private Boolean csvExpandJsonFieldsInner;

	/** The csv headers. */
	@RequestField
	private List<String> csvHeaders;

	/** The csv include properties. */
	@RequestField
	private Set<String> csvIncludeProperties;

	/** The csv no file. */
	@RequestField
	private Boolean csvNoFile;

	/** The csv params. */
	// TODO: This isnt be merged correctly in Merges.deepMerge - need to figure out why
	@RequestField
	private CsvParams csvParams;

	/** The csv skip rows expression. */
	@RequestField
	private String csvSkipRowsExpression;

	/** The csv sort. */
	@RequestField(additionalParameterNames = { "out.sort" })
	private String csvSort;

	/** The csv sort by field. */
	@RequestField
	private String csvSortByField;

	/** The csv type. */
	@RequestField
	private String csvType;

	/** The csv use chaining list sep. */
	@RequestField
	private Boolean csvUseChainingListSep;

	/** The csv wrap fields in function. */
	@RequestField
	private Set<String> csvWrapFieldsInFunction;

	/** The dates as strings. */
	@RequestField
	@ApiDocConfig(initiallyDisplayed = true)
	private Boolean datesAsStrings;

	/** The dates as strings format. */
	@RequestField
	private String datesAsStringsFormat;

	/** The encode strings for html. */
	@RequestField
	private Boolean encodeStringsForHtml;

	/** The field inclusion predicate filter. */
	private Map<Class<?>, BiPredicate<Object, Object>> fieldInclusionPredicateFilter;

	/** The skip fields patterns. */
	@RequestField
	private Set<Pattern> fieldsBlackList;

	/** The only fields patterns. */
	@RequestField
	private Set<Pattern> fieldsWhiteList;

	/** The file name. */
	@RequestField
	private String fileName;

	/** The graph. */
	@RequestField
	@ApiDocConfig(initiallyDisplayed = true)
	private String graph;

	/** The json include type. */
	@RequestField(additionalParameterNames = { "outputIncludeType" })
	private Boolean jsonIncludeType;

	/** The json sig figs. */
	@RequestField(additionalParameterNames = { "out.sigFigs" })
	private Integer jsonSigFigs;

	/** The modifications disabled. */
	private boolean modificationsDisabled;

	/** The object mapper. */
	@JsonIgnore
	private ObjectMapper objectMapper;

	/** The object mapper custom cache key id. */
	private String objectMapperCustomCacheKeyId;

	/** The ognl. */
	@RequestField(additionalParameterNames = { "out.ognl" })
	private Boolean ognl;

	/** The only fields. */
	@RequestField
	private Set<String> onlyFields;

	/** The order keys. */
	@RequestField
	private Boolean orderKeys;

	/** The data expression. */
	@RequestField(additionalParameterNames = { "csvTypeField", "out.expr" })
	@ApiDocConfig(initiallyDisplayed = true)
	private String outputDataExpression;

	/** The output format. */
	@RequestField
	@ApiDocConfig(initiallyDisplayed = true)
	private OutputFormat outputFormat;

	/** The post process expressions. */
	@RequestField(additionalParameterNames = { "out.postProcessExprs" })
	private List<OutputExpression> postProcessExpressions;

	/** The prettify json. */
	@RequestField
	private Boolean prettifyJson;

	/** The quote field names. */
	@RequestField
	private Boolean quoteFieldNames;

	/** The rc. */
	@RequestField
	@ApiDocConfig(initiallyDisplayed = true)
	private List<String> rcs;

	/** The sanitize json. */
	@RequestField
	private Boolean sanitizeJson;

	/** The csv skip properties. */
	@RequestField(additionalParameterNames = { "csvSkipProperties" })
	private Set<String> skipFields;

	/** The skip fields by class. */
	private Map<Class<?>, Set<String>> skipFieldsByType;

	/** The skip strings. */
	@RequestField
	private Boolean skipStrings;

	/** The template id. */
	@RequestField
	private String templateId;

	/** The transpose. */
	@RequestField
	private Boolean transpose;

	/**
	 * Adds the csv include properties.
	 *
	 * @param csvIncludeProperties the csv include properties
	 * @return the output params
	 */
	public OutputParams addCsvIncludeProperties(@Nonnull Collection<String> csvIncludeProperties) {
		checkModificationsDisabled();
		if (this.csvIncludeProperties == null) {
			this.csvIncludeProperties = new LinkedHashSet<>();
		}
		this.csvIncludeProperties.addAll(csvIncludeProperties);
		return this;
	}

	/**
	 * Adds the csv include properties.
	 *
	 * @param csvIncludeProperties the csv include properties
	 * @return the output params
	 */
	public OutputParams addCsvIncludeProperties(@Nonnull String... csvIncludeProperties) {
		return addCsvIncludeProperties(Arrays.asList(csvIncludeProperties));
	}

	/**
	 * Adds the fields black list.
	 *
	 * @param fieldsBlackList the fields black list
	 * @return the output params
	 */
	public OutputParams addFieldsBlackList(@Nonnull Collection<Pattern> fieldsBlackList) {
		checkModificationsDisabled();
		if (this.fieldsBlackList == null) {
			this.fieldsBlackList = new LinkedHashSet<>();
		}
		this.fieldsBlackList.addAll(fieldsBlackList);
		return this;
	}

	/**
	 * Adds the fields black list.
	 *
	 * @param fieldsBlackList the fields black list
	 * @return the output params
	 */
	public OutputParams addFieldsBlackList(@Nonnull Pattern... fieldsBlackList) {
		return addFieldsBlackList(Arrays.asList(fieldsBlackList));
	}

	/**
	 * Adds the fields white list.
	 *
	 * @param fieldsWhiteList the fields white list
	 * @return the output params
	 */
	public OutputParams addFieldsWhiteList(@Nonnull Collection<Pattern> fieldsWhiteList) {
		checkModificationsDisabled();
		if (this.fieldsWhiteList == null) {
			this.fieldsWhiteList = new LinkedHashSet<>();
		}
		this.fieldsWhiteList.addAll(fieldsWhiteList);
		return this;
	}

	/**
	 * Adds the fields white list.
	 *
	 * @param fieldsWhiteList the fields white list
	 * @return the output params
	 */
	public OutputParams addFieldsWhiteList(@Nonnull Pattern... fieldsWhiteList) {
		return addFieldsWhiteList(Arrays.asList(fieldsWhiteList));
	}

	/**
	 * Adds the only fields.
	 *
	 * @param onlyFields the only fields
	 * @return the output params
	 */
	public OutputParams addOnlyFields(@Nonnull Collection<String> onlyFields) {
		checkModificationsDisabled();
		if (this.onlyFields == null) {
			this.onlyFields = new LinkedHashSet<>();
		}
		this.onlyFields.addAll(onlyFields);
		return this;
	}

	/**
	 * Adds the only fields.
	 *
	 * @param onlyFields the only fields
	 * @return the output params
	 */
	public OutputParams addOnlyFields(@Nonnull String... onlyFields) {
		return addOnlyFields(Arrays.asList(onlyFields));
	}

	/**
	 * Adds the skip fields.
	 *
	 * @param skipFields the skip fields
	 * @return the output params
	 */
	public OutputParams addSkipFields(@Nonnull Collection<String> skipFields) {
		checkModificationsDisabled();
		if (this.skipFields == null) {
			this.skipFields = new LinkedHashSet<>();
		}
		this.skipFields.addAll(skipFields);
		return this;
	}

	/**
	 * Adds the skip fields.
	 *
	 * @param skipFields the skip fields
	 * @return the output params
	 */
	public OutputParams addSkipFields(@Nonnull String... skipFields) {
		return addSkipFields(Arrays.asList(skipFields));
	}

	/**
	 * Can cache resolved object mapper.
	 *
	 * @return true, if successful
	 */
	public boolean canCacheResolvedObjectMapper() {
		return Checks.isEmpty(fieldInclusionPredicateFilter) && ((objectMapper == null) || (objectMapperCustomCacheKeyId != null));
	}

	/**
	 * Copy.
	 *
	 * @return the output params
	 */
	public OutputParams copy() {
		OutputParams copied = Serializer.copy(this);
		copied.modificationsDisabled = false;
		return copied;
	}

	/**
	 * Disable modifications.
	 *
	 * @return the output params
	 */
	public OutputParams disableModifications() {
		modificationsDisabled = true;
		return this;
	}

	/**
	 * Gets the callback.
	 *
	 * @return the callback
	 */
	public String getCallback() {
		return callback;
	}

	/**
	 * Gets the creates the expressions.
	 *
	 * @return the createBeanExpressions
	 */
	public List<OutputExpression> getCreateExpressions() {
		return createExpressions;
	}

	/**
	 * Gets the csv as flat objs.
	 *
	 * @return the csvAsFlatObjs
	 */
	public Boolean getCsvAsFlatObjs() {
		return csvAsFlatObjs;
	}

	/**
	 * Gets the csv chaining sep.
	 *
	 * @return the csvChainingSep
	 */
	public String getCsvChainingSep() {
		return csvChainingSep;
	}

	/**
	 * Gets the csv delimiter.
	 *
	 * @return the csvDelimiter
	 */
	public String getCsvDelimiter() {
		return csvDelimiter;
	}

	/**
	 * Gets the csv expand json fields.
	 *
	 * @return the csvExpandJsonFields
	 */
	public Boolean getCsvExpandJsonFields() {
		return csvExpandJsonFields;
	}

	/**
	 * Gets the csv expand json fields inner.
	 *
	 * @return the csvExpandJsonFieldsInner
	 */
	public Boolean getCsvExpandJsonFieldsInner() {
		return csvExpandJsonFieldsInner;
	}

	/**
	 * Gets the csv headers.
	 *
	 * @return the csvHeaders
	 */
	public List<String> getCsvHeaders() {
		return csvHeaders;
	}

	/**
	 * Gets the csv include properties.
	 *
	 * @return the csvIncludeProperties
	 */
	public Set<String> getCsvIncludeProperties() {
		return csvIncludeProperties;
	}

	/**
	 * Gets the csv no file.
	 *
	 * @return the csvNoFile
	 */
	public Boolean getCsvNoFile() {
		return csvNoFile;
	}

	/**
	 * Gets the csv params.
	 *
	 * @return the csvParams
	 */
	public CsvParams getCsvParams() {
		return csvParams;
	}

	/**
	 * Gets the csv skip rows expression.
	 *
	 * @return the csvSkipRowsExpression
	 */
	public String getCsvSkipRowsExpression() {
		return csvSkipRowsExpression;
	}

	/**
	 * Gets the csv sort.
	 *
	 * @return the csvSort
	 */
	public String getCsvSort() {
		return csvSort;
	}

	/**
	 * Gets the csv sort by field.
	 *
	 * @return the csvSortByField
	 */
	public String getCsvSortByField() {
		return csvSortByField;
	}

	/**
	 * Gets the csv type.
	 *
	 * @return the csvType
	 */
	public String getCsvType() {
		return csvType;
	}

	/**
	 * Gets the csv use chaining list sep.
	 *
	 * @return the csvUseChainingListSep
	 */
	public Boolean getCsvUseChainingListSep() {
		return csvUseChainingListSep;
	}

	/**
	 * Gets the csv wrap fields in function.
	 *
	 * @return the csvWrapFieldsInFunction
	 */
	public Set<String> getCsvWrapFieldsInFunction() {
		return csvWrapFieldsInFunction;
	}

	/**
	 * Gets the dates as strings.
	 *
	 * @return the datesAsStrings
	 */
	public Boolean getDatesAsStrings() {
		return datesAsStrings;
	}

	/**
	 * Gets the dates as strings format.
	 *
	 * @return the datesAsStringsFormat
	 */
	public String getDatesAsStringsFormat() {
		return datesAsStringsFormat;
	}

	/**
	 * Gets the encode strings for html.
	 *
	 * @return the encodeStringsForHtml
	 */
	public Boolean getEncodeStringsForHtml() {
		return encodeStringsForHtml;
	}

	/**
	 * Gets the field inclusion predicate filter.
	 *
	 * @return the fieldInclusionPredicateFilter
	 */
	public Map<Class<?>, BiPredicate<Object, Object>> getFieldInclusionPredicateFilter() {
		return fieldInclusionPredicateFilter;
	}

	/**
	 * Gets the fields black list.
	 *
	 * @return the skipFieldsPatterns
	 */
	public Set<Pattern> getFieldsBlackList() {
		return fieldsBlackList;
	}

	/**
	 * Gets the fields white list.
	 *
	 * @return the onlyFieldsPatterns
	 */
	public Set<Pattern> getFieldsWhiteList() {
		return fieldsWhiteList;
	}

	/**
	 * Gets the file name.
	 *
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Gets the graph.
	 *
	 * @return the graph
	 */
	public String getGraph() {
		return graph;
	}

	/**
	 * Gets the json include type.
	 *
	 * @return the jsonIncludeType
	 */
	public Boolean getJsonIncludeType() {
		return jsonIncludeType;
	}

	/**
	 * Gets the json sig figs.
	 *
	 * @return the jsonSigFigs
	 */
	public Integer getJsonSigFigs() {
		return jsonSigFigs;
	}

	/**
	 * Gets the object mapper.
	 *
	 * @return the objectMapper
	 */
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	/**
	 * Gets the object mapper custom cache key id.
	 *
	 * @return the object mapper custom cache key id
	 */
	public String getObjectMapperCustomCacheKeyId() {
		return objectMapperCustomCacheKeyId;
	}

	/**
	 * Gets the ognl.
	 *
	 * @return the ognl
	 */
	public Boolean getOgnl() {
		return ognl;
	}

	/**
	 * Gets the only fields.
	 *
	 * @return the onlyFields
	 */
	public Set<String> getOnlyFields() {
		return onlyFields;
	}

	/**
	 * Gets the order keys.
	 *
	 * @return the orderKeys
	 */
	public Boolean getOrderKeys() {
		return orderKeys;
	}

	/**
	 * Gets the output data expression.
	 *
	 * @return the output data expression
	 */
	public String getOutputDataExpression() {
		return outputDataExpression;
	}

	/**
	 * Gets the output format.
	 *
	 * @return the outputFormat
	 */
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}

	/**
	 * Gets the post process expressions.
	 *
	 * @return the post process expressions
	 */
	public List<OutputExpression> getPostProcessExpressions() {
		return postProcessExpressions;
	}

	/**
	 * Gets the prettify json.
	 *
	 * @return the prettifyJson
	 */
	public Boolean getPrettifyJson() {
		return prettifyJson;
	}

	/**
	 * Gets the quote field names.
	 *
	 * @return the quoteFieldNames
	 */
	public Boolean getQuoteFieldNames() {
		return quoteFieldNames;
	}

	/**
	 * Gets the rcs.
	 *
	 * @return the rcs
	 */
	public List<String> getRcs() {
		return rcs;
	}

	/**
	 * Gets the sanitize json.
	 *
	 * @return the sanitizeJson
	 */
	public Boolean getSanitizeJson() {
		return sanitizeJson;
	}

	/**
	 * Gets the skip fields.
	 *
	 * @return the skip fields
	 */
	public Set<String> getSkipFields() {
		return skipFields;
	}

	/**
	 * Gets the skip fields by type.
	 *
	 * @return the skipFieldsByType
	 */
	public Map<Class<?>, Set<String>> getSkipFieldsByType() {
		return skipFieldsByType;
	}

	/**
	 * Gets the skip strings.
	 *
	 * @return the skipStrings
	 */
	public Boolean getSkipStrings() {
		return skipStrings;
	}

	/**
	 * Gets the template id.
	 *
	 * @return the template id
	 */
	public String getTemplateId() {
		return templateId;
	}

	/**
	 * Gets the transpose.
	 *
	 * @return the transpose
	 */
	public Boolean getTranspose() {
		return transpose;
	}

	/**
	 * Checks if is modifications disabled.
	 *
	 * @return the modificationsDisabled
	 */
	public boolean isModificationsDisabled() {
		return modificationsDisabled;
	}

	/**
	 * Sets the callback.
	 *
	 * @param callback the callback to set
	 * @return the output params
	 */
	public OutputParams setCallback(String callback) {
		checkModificationsDisabled();
		this.callback = callback;
		return this;
	}

	/**
	 * Sets the create bean expressions.
	 *
	 * @param createExpressions the create expressions
	 * @return the output params
	 */
	public OutputParams setCreateExpressions(List<OutputExpression> createExpressions) {
		checkModificationsDisabled();
		this.createExpressions = createExpressions;
		return this;
	}

	/**
	 * Sets the csv as flat objs.
	 *
	 * @param csvAsFlatObjs the csvAsFlatObjs to set
	 * @return the output params
	 */
	public OutputParams setCsvAsFlatObjs(Boolean csvAsFlatObjs) {
		checkModificationsDisabled();
		this.csvAsFlatObjs = csvAsFlatObjs;
		return this;
	}

	/**
	 * Sets the csv chaining sep.
	 *
	 * @param csvChainingSep the csvChainingSep to set
	 * @return the output params
	 */
	public OutputParams setCsvChainingSep(String csvChainingSep) {
		checkModificationsDisabled();
		this.csvChainingSep = csvChainingSep;
		return this;
	}

	/**
	 * Sets the csv delimiter.
	 *
	 * @param csvDelimiter the csvDelimiter to set
	 * @return the output params
	 */
	public OutputParams setCsvDelimiter(String csvDelimiter) {
		checkModificationsDisabled();
		this.csvDelimiter = csvDelimiter;
		return this;
	}

	/**
	 * Sets the csv expand json fields.
	 *
	 * @param csvExpandJsonFields the csvExpandJsonFields to set
	 * @return the output params
	 */
	public OutputParams setCsvExpandJsonFields(Boolean csvExpandJsonFields) {
		checkModificationsDisabled();
		this.csvExpandJsonFields = csvExpandJsonFields;
		return this;
	}

	/**
	 * Sets the csv expand json fields inner.
	 *
	 * @param csvExpandJsonFieldsInner the csvExpandJsonFieldsInner to set
	 * @return the output params
	 */
	public OutputParams setCsvExpandJsonFieldsInner(Boolean csvExpandJsonFieldsInner) {
		checkModificationsDisabled();
		this.csvExpandJsonFieldsInner = csvExpandJsonFieldsInner;
		return this;
	}

	/**
	 * Sets the csv headers.
	 *
	 * @param csvHeaders the csvHeaders to set
	 * @return the output params
	 */
	public OutputParams setCsvHeaders(List<String> csvHeaders) {
		checkModificationsDisabled();
		this.csvHeaders = csvHeaders;
		return this;
	}

	/**
	 * Sets the csv include properties.
	 *
	 * @param csvIncludeProperties the csvIncludeProperties to set
	 * @return the output params
	 */
	public OutputParams setCsvIncludeProperties(Set<String> csvIncludeProperties) {
		checkModificationsDisabled();
		this.csvIncludeProperties = csvIncludeProperties;
		return this;
	}

	/**
	 * Sets the csv no file.
	 *
	 * @param csvNoFile the csvNoFile to set
	 * @return the output params
	 */
	public OutputParams setCsvNoFile(Boolean csvNoFile) {
		checkModificationsDisabled();
		this.csvNoFile = csvNoFile;
		return this;
	}

	/**
	 * Sets the csv params.
	 *
	 * @param csvParams the csvParams to set
	 * @return the output params
	 */
	public OutputParams setCsvParams(CsvParams csvParams) {
		checkModificationsDisabled();
		this.csvParams = csvParams;
		return this;
	}

	/**
	 * Sets the csv skip rows expression.
	 *
	 * @param csvSkipRowsExpression the csvSkipRowsExpression to set
	 * @return the output params
	 */
	public OutputParams setCsvSkipRowsExpression(String csvSkipRowsExpression) {
		checkModificationsDisabled();
		this.csvSkipRowsExpression = csvSkipRowsExpression;
		return this;
	}

	/**
	 * Sets the csv sort.
	 *
	 * @param csvSort the csvSort to set
	 * @return the output params
	 */
	public OutputParams setCsvSort(String csvSort) {
		checkModificationsDisabled();
		this.csvSort = csvSort;
		return this;
	}

	/**
	 * Sets the csv sort by field.
	 *
	 * @param csvSortByField the csvSortByField to set
	 * @return the output params
	 */
	public OutputParams setCsvSortByField(String csvSortByField) {
		checkModificationsDisabled();
		this.csvSortByField = csvSortByField;
		return this;
	}

	/**
	 * Sets the csv type.
	 *
	 * @param csvType the csvType to set
	 * @return the output params
	 */
	public OutputParams setCsvType(String csvType) {
		checkModificationsDisabled();
		this.csvType = csvType;
		return this;
	}

	/**
	 * Sets the csv use chaining list sep.
	 *
	 * @param csvUseChainingListSep the csvUseChainingListSep to set
	 * @return the output params
	 */
	public OutputParams setCsvUseChainingListSep(Boolean csvUseChainingListSep) {
		checkModificationsDisabled();
		this.csvUseChainingListSep = csvUseChainingListSep;
		return this;
	}

	/**
	 * Sets the csv wrap fields in function.
	 *
	 * @param csvWrapFieldsInFunction the csvWrapFieldsInFunction to set
	 * @return the output params
	 */
	public OutputParams setCsvWrapFieldsInFunction(Set<String> csvWrapFieldsInFunction) {
		checkModificationsDisabled();
		this.csvWrapFieldsInFunction = csvWrapFieldsInFunction;
		return this;
	}

	/**
	 * Sets the dates as strings.
	 *
	 * @param datesAsStrings the datesAsStrings to set
	 * @return the output params
	 */
	public OutputParams setDatesAsStrings(Boolean datesAsStrings) {
		checkModificationsDisabled();
		this.datesAsStrings = datesAsStrings;
		return this;
	}

	/**
	 * Sets the dates as strings format.
	 *
	 * @param datesAsStringsFormat the datesAsStringsFormat to set
	 * @return the output params
	 */
	public OutputParams setDatesAsStringsFormat(String datesAsStringsFormat) {
		checkModificationsDisabled();
		this.datesAsStringsFormat = datesAsStringsFormat;
		return this;
	}

	/**
	 * Sets the encode strings for html.
	 *
	 * @param encodeStringsForHtml the encodeStringsForHtml to set
	 * @return the output params
	 */
	public OutputParams setEncodeStringsForHtml(Boolean encodeStringsForHtml) {
		checkModificationsDisabled();
		this.encodeStringsForHtml = encodeStringsForHtml;
		return this;
	}

	/**
	 * Sets the field inclusion predicate filter.
	 *
	 * @param fieldInclusionPredicateFilter the fieldInclusionPredicateFilter to set
	 * @return the output params
	 */
	public OutputParams setFieldInclusionPredicateFilter(Map<Class<?>, BiPredicate<Object, Object>> fieldInclusionPredicateFilter) {
		checkModificationsDisabled();
		this.fieldInclusionPredicateFilter = fieldInclusionPredicateFilter;
		return this;
	}

	/**
	 * Sets the skip fields patterns.
	 *
	 * @param fieldsBlackList the fields black list
	 * @return the output params
	 */
	public OutputParams setFieldsBlackList(Set<Pattern> fieldsBlackList) {
		checkModificationsDisabled();
		this.fieldsBlackList = fieldsBlackList;
		return this;
	}

	/**
	 * Sets the only fields patterns.
	 *
	 * @param fieldsWhiteList the fields white list
	 * @return the output params
	 */
	public OutputParams setFieldsWhiteList(Set<Pattern> fieldsWhiteList) {
		checkModificationsDisabled();
		this.fieldsWhiteList = fieldsWhiteList;
		return this;
	}

	/**
	 * Sets the file name.
	 *
	 * @param fileName the fileName to set
	 * @return the output params
	 */
	public OutputParams setFileName(String fileName) {
		checkModificationsDisabled();
		this.fileName = fileName;
		return this;
	}

	/**
	 * Sets the graph.
	 *
	 * @param graph the graph to set
	 * @return the output params
	 */
	public OutputParams setGraph(String graph) {
		checkModificationsDisabled();
		this.graph = graph;
		return this;
	}

	/**
	 * Sets the json include type.
	 *
	 * @param jsonIncludeType the jsonIncludeType to set
	 * @return the output params
	 */
	public OutputParams setJsonIncludeType(Boolean jsonIncludeType) {
		checkModificationsDisabled();
		this.jsonIncludeType = jsonIncludeType;
		return this;
	}

	/**
	 * Sets the json sig figs.
	 *
	 * @param jsonSigFigs the jsonSigFigs to set
	 * @return the output params
	 */
	public OutputParams setJsonSigFigs(Integer jsonSigFigs) {
		checkModificationsDisabled();
		this.jsonSigFigs = jsonSigFigs;
		return this;
	}

	/**
	 * Sets the object mapper.
	 *
	 * @param objectMapper the objectMapper to set
	 * @return the output params
	 */
	public OutputParams setObjectMapper(ObjectMapper objectMapper) {
		checkModificationsDisabled();
		this.objectMapper = objectMapper;
		return this;
	}

	/**
	 * Sets the object mapper custom cache key id.
	 *
	 * @param objectMapperCustomCacheKeyId the object mapper custom cache key id
	 * @return the output params
	 */
	public OutputParams setObjectMapperCustomCacheKeyId(String objectMapperCustomCacheKeyId) {
		checkModificationsDisabled();
		this.objectMapperCustomCacheKeyId = objectMapperCustomCacheKeyId;
		return this;
	}

	/**
	 * Sets the ognl.
	 *
	 * @param ognl the ognl to set
	 * @return the output params
	 */
	public OutputParams setOgnl(Boolean ognl) {
		checkModificationsDisabled();
		this.ognl = ognl;
		return this;
	}

	/**
	 * Sets the only fields.
	 *
	 * @param onlyFields the onlyFields to set
	 * @return the output params
	 */
	public OutputParams setOnlyFields(Set<String> onlyFields) {
		checkModificationsDisabled();
		this.onlyFields = onlyFields;
		return this;
	}

	/**
	 * Sets the order keys.
	 *
	 * @param orderKeys the orderKeys to set
	 * @return the output params
	 */
	public OutputParams setOrderKeys(Boolean orderKeys) {
		checkModificationsDisabled();
		this.orderKeys = orderKeys;
		return this;
	}

	/**
	 * Sets the output data expression.
	 *
	 * @param outputDataExpression the output data expression
	 * @return the output params
	 */
	public OutputParams setOutputDataExpression(String outputDataExpression) {
		checkModificationsDisabled();
		this.outputDataExpression = outputDataExpression;
		return this;
	}

	/**
	 * Sets the output format.
	 *
	 * @param outputFormat the outputFormat to set
	 * @return the output params
	 */
	public OutputParams setOutputFormat(OutputFormat outputFormat) {
		checkModificationsDisabled();
		this.outputFormat = outputFormat;
		return this;
	}

	/**
	 * Sets the post create expressions.
	 *
	 * @param postProcessExpressions the post process expressions
	 * @return the output params
	 */
	public OutputParams setPostProcessExpressions(List<OutputExpression> postProcessExpressions) {
		checkModificationsDisabled();
		this.postProcessExpressions = postProcessExpressions;
		return this;
	}

	/**
	 * Sets the prettify json.
	 *
	 * @param prettifyJson the prettifyJson to set
	 * @return the output params
	 */
	public OutputParams setPrettifyJson(Boolean prettifyJson) {
		checkModificationsDisabled();
		this.prettifyJson = prettifyJson;
		return this;
	}

	/**
	 * Sets the quote field names.
	 *
	 * @param quoteFieldNames the quoteFieldNames to set
	 * @return the output params
	 */
	public OutputParams setQuoteFieldNames(Boolean quoteFieldNames) {
		this.quoteFieldNames = quoteFieldNames;
		return this;
	}

	/**
	 * Sets the rcs.
	 *
	 * @param rcs the rcs to set
	 * @return the output params
	 */
	public OutputParams setRcs(List<String> rcs) {
		checkModificationsDisabled();
		this.rcs = rcs;
		return this;
	}

	/**
	 * Sets the sanitize json.
	 *
	 * @param sanitizeJson the sanitizeJson to set
	 * @return the output params
	 */
	public OutputParams setSanitizeJson(Boolean sanitizeJson) {
		checkModificationsDisabled();
		this.sanitizeJson = sanitizeJson;
		return this;
	}

	/**
	 * Sets the skip fields.
	 *
	 * @param skipFields the skip fields
	 * @return the output params
	 */
	public OutputParams setSkipFields(Set<String> skipFields) {
		checkModificationsDisabled();
		this.skipFields = skipFields;
		return this;
	}

	/**
	 * Sets the skip fields by type.
	 *
	 * @param skipFieldsByType the skipFieldsByType to set
	 * @return the output params
	 */
	public OutputParams setSkipFieldsByType(Map<Class<?>, Set<String>> skipFieldsByType) {
		checkModificationsDisabled();
		this.skipFieldsByType = skipFieldsByType;
		return this;
	}

	/**
	 * Sets the skip strings.
	 *
	 * @param skipStrings the skipStrings to set
	 * @return the output params
	 */
	public OutputParams setSkipStrings(Boolean skipStrings) {
		checkModificationsDisabled();
		this.skipStrings = skipStrings;
		return this;
	}

	/**
	 * Sets the template id.
	 *
	 * @param templateId the new template id
	 * @return the output params
	 */
	public OutputParams setTemplateId(String templateId) {
		checkModificationsDisabled();
		this.templateId = templateId;
		return this;
	}

	/**
	 * Sets the transpose.
	 *
	 * @param transpose the transpose to set
	 * @return the output params
	 */
	public OutputParams setTranspose(Boolean transpose) {
		checkModificationsDisabled();
		this.transpose = transpose;
		return this;
	}

	/**
	 * Test csv as flat objs.
	 *
	 * @return true, if successful
	 */
	public boolean testCsvAsFlatObjs() {
		return BooleanUtils.toBoolean(csvAsFlatObjs);
	}

	/**
	 * Test csv no file.
	 *
	 * @return true, if successful
	 */
	public boolean testCsvNoFile() {
		return BooleanUtils.toBoolean(csvNoFile);
	}

	/**
	 * Test csv use chaining list sep.
	 *
	 * @return true, if successful
	 */
	public boolean testCsvUseChainingListSep() {
		return BooleanUtils.toBoolean(csvUseChainingListSep);
	}

	/**
	 * Test dates as strings.
	 *
	 * @return true, if successful
	 */
	public boolean testDatesAsStrings() {
		return (datesAsStrings != null) && datesAsStrings;
	}

	/**
	 * Test encode strings for html.
	 *
	 * @return true, if successful
	 */
	public boolean testEncodeStringsForHtml() {
		return (encodeStringsForHtml != null) && encodeStringsForHtml.booleanValue();
	}

	/**
	 * Test json include type.
	 *
	 * @return true, if successful
	 */
	public boolean testJsonIncludeType() {
		return jsonIncludeType == null ? true : jsonIncludeType;
	}

	/**
	 * Test ognl.
	 *
	 * @return true, if successful
	 */
	public boolean testOgnl() {
		return (getOgnl() != null) && getOgnl().booleanValue();
	}

	/**
	 * Test order keys.
	 *
	 * @return true, if successful
	 */
	public boolean testOrderKeys() {
		return (orderKeys != null) && orderKeys;
	}

	/**
	 * Test prettify json.
	 *
	 * @return true, if successful
	 */
	public boolean testPrettifyJson() {
		return toBoolean(prettifyJson);
	}

	/**
	 * Test sanitize json.
	 *
	 * @return true, if successful
	 */
	public boolean testSanitizeJson() {
		return (sanitizeJson != null) && sanitizeJson.booleanValue();
	}

	/**
	 * Test skip strings.
	 *
	 * @return true, if successful
	 */
	public boolean testSkipStrings() {
		return (skipStrings == null) || skipStrings;
	}

	/**
	 * Test transpose.
	 *
	 * @return true, if successful
	 */
	public boolean testTranspose() {
		return BooleanUtils.toBoolean(transpose);
	}

	/**
	 * To object mapper cache key.
	 *
	 * @return the string
	 */
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

	/**
	 * Update output format if allowed.
	 *
	 * @param outputFormat the output format
	 * @return the output params
	 */
	public OutputParams updateOutputFormatIfAllowed(OutputFormat outputFormat) {
		if (modificationsDisabled) {
			return this;
		}
		this.outputFormat = outputFormat;
		return this;
	}

	/**
	 * Check modifications disabled.
	 */
	private void checkModificationsDisabled() {
		if (modificationsDisabled) {
			throw new UnsupportedOperationException("Modifications have been disabled for this instance!");
		}
	}
}
