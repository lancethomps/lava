package com.github.lancethomps.lava.common.file;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.github.lancethomps.lava.common.ser.OutputExpression;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.web.requests.parsers.RequestField;

/**
 * The Class FileParserOptions.
 */
public class FileParserOptions extends ExternalizableBean {

	/** The allow blanks. */
	@RequestField
	private Boolean allowBlanks;

	/** The copy fields. */
	@RequestField
	private Map<String, Set<String>> copyFields;

	/** The field converter. */
	private CsvFieldConverter fieldConverter;

	/** The field values black list. */
	@RequestField
	private Set<Pattern> fieldValuesBlackList;

	/** The field values black list. */
	@RequestField
	private Map<String, Set<Pattern>> fieldValuesBlackListByColumn;

	/** The field values white list. */
	@RequestField
	private Set<Pattern> fieldValuesWhiteList;

	/** The field values white list. */
	@RequestField
	private Map<String, Set<Pattern>> fieldValuesWhiteListByColumn;

	/** The first row num. */
	@RequestField
	private Integer firstRowNum;

	/** The first row num by sheet. */
	@RequestField
	private Map<String, Integer> firstRowNumBySheet;

	/** The header conversions. */
	@RequestField
	private Map<String, String> headerConversions;

	/** The header converter. */
	private CsvFieldConverter headerConverter;

	/** The headers. */
	private Map<String, Integer> headers;

	/** The headers black list. */
	@RequestField
	private Set<Pattern> headersBlackList;

	/** The headers white list. */
	@RequestField
	private Set<Pattern> headersWhiteList;

	/** The ignore fields. */
	@RequestField
	private Set<String> ignoreFields;

	/** The keys as paths. */
	@RequestField
	private Boolean keysAsPaths;

	/** The max lines. */
	@RequestField
	private Integer maxLines;

	/** The modifications disabled. */
	private boolean modificationsDisabled;

	/** The only sheets. */
	@RequestField
	private Set<String> onlySheets;

	/** The post processor. */
	private FileParserPostProcessor postProcessor;

	/** The remove lines with prefixes. */
	@RequestField
	private Set<String> removeLinesWithPrefixes;

	/** The row constants. */
	@RequestField
	private Map<String, Object> rowConstants;

	/** The row validation expressions. */
	private List<OutputExpression> rowValidationExpressions;

	/** The sep char. */
	@RequestField
	private Character sepChar;

	/** The skip sheets. */
	@RequestField
	private Set<String> skipSheets;

	/** The strict. */
	@RequestField
	private Boolean strict;

	/** The transpose. */
	@RequestField
	private Boolean transpose;

	/** The type. */
	@RequestField
	private Class<?> type;

	/** The use excel formats. */
	@RequestField
	private Boolean useExcelFormats;

	/** The use path keys. */
	@RequestField
	private Boolean usePathKeys;

	/**
	 * Adds the ignore fields.
	 *
	 * @param ignoreFields the ignore fields
	 * @return the file parser options
	 */
	public FileParserOptions addIgnoreFields(@Nonnull Collection<String> ignoreFields) {
		checkModificationsDisabled();
		if (this.ignoreFields == null) {
			this.ignoreFields = new LinkedHashSet<>();
		}
		this.ignoreFields.addAll(ignoreFields);
		return this;
	}

	/**
	 * Adds the ignore fields.
	 *
	 * @param ignoreFields the ignore fields
	 * @return the file parser options
	 */
	public FileParserOptions addIgnoreFields(@Nonnull String... ignoreFields) {
		return addIgnoreFields(Arrays.asList(ignoreFields));
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.ser.PostConstructor#afterDeserialization()
	 */
	@Override
	public void afterDeserialization() {
		if (rowValidationExpressions != null) {
			ExprFactory.compileCreateExpressions(rowValidationExpressions, false, false, true);
		}
	}

	/**
	 * Copy.
	 *
	 * @return the file parser options
	 */
	public FileParserOptions copy() {
		FileParserOptions copied = Serializer.copy(this);
		copied.modificationsDisabled = false;
		return copied;
	}

	/**
	 * Disable modifications.
	 *
	 * @return the file parser options
	 */
	public FileParserOptions disableModifications() {
		modificationsDisabled = true;
		return this;
	}

	/**
	 * Gets the allow blanks.
	 *
	 * @return the allowBlanks
	 */
	public Boolean getAllowBlanks() {
		return allowBlanks;
	}

	/**
	 * @return the copyFields
	 */
	public Map<String, Set<String>> getCopyFields() {
		return copyFields;
	}

	/**
	 * Gets the field converter.
	 *
	 * @return the fieldConverter
	 */
	public CsvFieldConverter getFieldConverter() {
		return fieldConverter;
	}

	/**
	 * Gets the field values black list.
	 *
	 * @return the fieldValuesBlackList
	 */
	public Set<Pattern> getFieldValuesBlackList() {
		return fieldValuesBlackList;
	}

	/**
	 * Gets the field values black list by column.
	 *
	 * @return the fieldValuesBlackList
	 */
	public Map<String, Set<Pattern>> getFieldValuesBlackListByColumn() {
		return fieldValuesBlackListByColumn;
	}

	/**
	 * Gets the field values white list.
	 *
	 * @return the fieldValuesWhiteList
	 */
	public Set<Pattern> getFieldValuesWhiteList() {
		return fieldValuesWhiteList;
	}

	/**
	 * Gets the field values white list by column.
	 *
	 * @return the fieldValuesWhiteList
	 */
	public Map<String, Set<Pattern>> getFieldValuesWhiteListByColumn() {
		return fieldValuesWhiteListByColumn;
	}

	/**
	 * Gets the first row num.
	 *
	 * @return the firstRowNum
	 */
	public Integer getFirstRowNum() {
		return firstRowNum;
	}

	/**
	 * Gets the first row num by sheet.
	 *
	 * @return the firstRowNumBySheet
	 */
	public Map<String, Integer> getFirstRowNumBySheet() {
		return firstRowNumBySheet;
	}

	/**
	 * Gets the header conversions.
	 *
	 * @return the headerConversions
	 */
	public Map<String, String> getHeaderConversions() {
		return headerConversions;
	}

	/**
	 * Gets the header converter.
	 *
	 * @return the headerConverter
	 */
	public CsvFieldConverter getHeaderConverter() {
		return headerConverter;
	}

	/**
	 * Gets the headers.
	 *
	 * @return the headers
	 */
	public Map<String, Integer> getHeaders() {
		return headers;
	}

	/**
	 * Gets the headers black list.
	 *
	 * @return the headersBlackList
	 */
	public Set<Pattern> getHeadersBlackList() {
		return headersBlackList;
	}

	/**
	 * Gets the headers white list.
	 *
	 * @return the headersWhiteList
	 */
	public Set<Pattern> getHeadersWhiteList() {
		return headersWhiteList;
	}

	/**
	 * @return the ignoreFields
	 */
	public Set<String> getIgnoreFields() {
		return ignoreFields;
	}

	/**
	 * Gets the keys as paths.
	 *
	 * @return the keysAsPaths
	 */
	public Boolean getKeysAsPaths() {
		return keysAsPaths;
	}

	/**
	 * Gets the max lines.
	 *
	 * @return the maxLines
	 */
	public Integer getMaxLines() {
		return maxLines;
	}

	/**
	 * Gets the only sheets.
	 *
	 * @return the onlySheets
	 */
	public Set<String> getOnlySheets() {
		return onlySheets;
	}

	/**
	 * Gets the post processor.
	 *
	 * @return the postProcessor
	 */
	public FileParserPostProcessor getPostProcessor() {
		return postProcessor;
	}

	/**
	 * Gets the removes the lines with prefixes.
	 *
	 * @return the removeLinesWithPrefixes
	 */
	public Set<String> getRemoveLinesWithPrefixes() {
		return removeLinesWithPrefixes;
	}

	/**
	 * @return the rowConstants
	 */
	public Map<String, Object> getRowConstants() {
		return rowConstants;
	}

	/**
	 * @return the rowValidationExpressions
	 */
	public List<OutputExpression> getRowValidationExpressions() {
		return rowValidationExpressions;
	}

	/**
	 * Gets the sep char.
	 *
	 * @return the sepChar
	 */
	public Character getSepChar() {
		return sepChar;
	}

	/**
	 * Gets the skip sheets.
	 *
	 * @return the skipSheets
	 */
	public Set<String> getSkipSheets() {
		return skipSheets;
	}

	/**
	 * @return the strict
	 */
	public Boolean getStrict() {
		return strict;
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
	 * Gets the type.
	 *
	 * @return the type
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Gets the use excel formats.
	 *
	 * @return the useExcelFormats
	 */
	public Boolean getUseExcelFormats() {
		return useExcelFormats;
	}

	/**
	 * Gets the use path keys.
	 *
	 * @return the usePathKeys
	 */
	public Boolean getUsePathKeys() {
		return usePathKeys;
	}

	/**
	 * @return the modificationsDisabled
	 */
	public boolean isModificationsDisabled() {
		return modificationsDisabled;
	}

	/**
	 * Sets the allow blanks.
	 *
	 * @param allowBlanks the allowBlanks to set
	 * @return the file parser options
	 */
	public FileParserOptions setAllowBlanks(Boolean allowBlanks) {
		checkModificationsDisabled();
		this.allowBlanks = allowBlanks;
		return this;
	}

	/**
	 * Sets the copy fields.
	 *
	 * @param copyFields the copyFields to set
	 * @return the file parser options
	 */
	public FileParserOptions setCopyFields(Map<String, Set<String>> copyFields) {
		checkModificationsDisabled();
		this.copyFields = copyFields;
		return this;
	}

	/**
	 * Sets the field converter.
	 *
	 * @param fieldConverter the fieldConverter to set
	 * @return the file parser options
	 */
	public FileParserOptions setFieldConverter(CsvFieldConverter fieldConverter) {
		checkModificationsDisabled();
		this.fieldConverter = fieldConverter;
		return this;
	}

	/**
	 * Sets the field values black list.
	 *
	 * @param fieldValuesBlackList the fieldValuesBlackList to set
	 * @return the file parser options
	 */
	public FileParserOptions setFieldValuesBlackList(Set<Pattern> fieldValuesBlackList) {
		checkModificationsDisabled();
		this.fieldValuesBlackList = fieldValuesBlackList;
		return this;
	}

	/**
	 * Sets the field values black list by column.
	 *
	 * @param fieldValuesBlackListByColumn the field values black list by column
	 * @return the file parser options
	 */
	public FileParserOptions setFieldValuesBlackListByColumn(Map<String, Set<Pattern>> fieldValuesBlackListByColumn) {
		checkModificationsDisabled();
		this.fieldValuesBlackListByColumn = fieldValuesBlackListByColumn;
		return this;
	}

	/**
	 * Sets the field values white list.
	 *
	 * @param fieldValuesWhiteList the fieldValuesWhiteList to set
	 * @return the file parser options
	 */
	public FileParserOptions setFieldValuesWhiteList(Set<Pattern> fieldValuesWhiteList) {
		checkModificationsDisabled();
		this.fieldValuesWhiteList = fieldValuesWhiteList;
		return this;
	}

	/**
	 * Sets the field values white list by column.
	 *
	 * @param fieldValuesWhiteListByColumn the field values white list by column
	 * @return the file parser options
	 */
	public FileParserOptions setFieldValuesWhiteListByColumn(Map<String, Set<Pattern>> fieldValuesWhiteListByColumn) {
		checkModificationsDisabled();
		this.fieldValuesWhiteListByColumn = fieldValuesWhiteListByColumn;
		return this;
	}

	/**
	 * Sets the first row num.
	 *
	 * @param firstRowNum the firstRowNum to set
	 * @return the file parser options
	 */
	public FileParserOptions setFirstRowNum(Integer firstRowNum) {
		checkModificationsDisabled();
		this.firstRowNum = firstRowNum;
		return this;
	}

	/**
	 * Sets the first row num by sheet.
	 *
	 * @param firstRowNumBySheet the firstRowNumBySheet to set
	 * @return the first row num by sheet
	 */
	public FileParserOptions setFirstRowNumBySheet(Map<String, Integer> firstRowNumBySheet) {
		checkModificationsDisabled();
		this.firstRowNumBySheet = firstRowNumBySheet;
		return this;
	}

	/**
	 * Sets the header conversions.
	 *
	 * @param headerConversions the headerConversions to set
	 * @return the file parser options
	 */
	public FileParserOptions setHeaderConversions(Map<String, String> headerConversions) {
		checkModificationsDisabled();
		this.headerConversions = headerConversions;
		return this;
	}

	/**
	 * Sets the header converter.
	 *
	 * @param headerConverter the headerConverter to set
	 * @return the file parser options
	 */
	public FileParserOptions setHeaderConverter(CsvFieldConverter headerConverter) {
		checkModificationsDisabled();
		this.headerConverter = headerConverter;
		return this;
	}

	/**
	 * Sets the headers.
	 *
	 * @param headers the headers to set
	 * @return the file parser options
	 */
	public FileParserOptions setHeaders(Map<String, Integer> headers) {
		checkModificationsDisabled();
		this.headers = headers;
		return this;
	}

	/**
	 * Sets the headers black list.
	 *
	 * @param headersBlackList the headersBlackList to set
	 * @return the file parser options
	 */
	public FileParserOptions setHeadersBlackList(Set<Pattern> headersBlackList) {
		checkModificationsDisabled();
		this.headersBlackList = headersBlackList;
		return this;
	}

	/**
	 * Sets the headers white list.
	 *
	 * @param headersWhiteList the headersWhiteList to set
	 * @return the file parser options
	 */
	public FileParserOptions setHeadersWhiteList(Set<Pattern> headersWhiteList) {
		checkModificationsDisabled();
		this.headersWhiteList = headersWhiteList;
		return this;
	}

	/**
	 * Sets the ignore fields.
	 *
	 * @param ignoreFields the ignoreFields to set
	 * @return the file parser options
	 */
	public FileParserOptions setIgnoreFields(Set<String> ignoreFields) {
		checkModificationsDisabled();
		this.ignoreFields = ignoreFields;
		return this;
	}

	/**
	 * Sets the keys as paths.
	 *
	 * @param keysAsPaths the keysAsPaths to set
	 * @return the file parser options
	 */
	public FileParserOptions setKeysAsPaths(Boolean keysAsPaths) {
		checkModificationsDisabled();
		this.keysAsPaths = keysAsPaths;
		return this;
	}

	/**
	 * Sets the max lines.
	 *
	 * @param maxLines the maxLines to set
	 * @return the file parser options
	 */
	public FileParserOptions setMaxLines(Integer maxLines) {
		checkModificationsDisabled();
		this.maxLines = maxLines;
		return this;
	}

	/**
	 * Sets the only sheets.
	 *
	 * @param onlySheets the onlySheets to set
	 * @return the file parser options
	 */
	public FileParserOptions setOnlySheets(Set<String> onlySheets) {
		checkModificationsDisabled();
		this.onlySheets = onlySheets;
		return this;
	}

	/**
	 * Sets the post processor.
	 *
	 * @param postProcessor the postProcessor to set
	 * @return the file parser options
	 */
	public FileParserOptions setPostProcessor(FileParserPostProcessor postProcessor) {
		checkModificationsDisabled();
		this.postProcessor = postProcessor;
		return this;
	}

	/**
	 * Sets the remove lines with prefixes.
	 *
	 * @param removeLinesWithPrefixes the remove lines with prefixes
	 * @return the file parser options
	 */
	public FileParserOptions setRemoveLinesWithPrefixes(Set<String> removeLinesWithPrefixes) {
		checkModificationsDisabled();
		this.removeLinesWithPrefixes = removeLinesWithPrefixes;
		return this;
	}

	/**
	 * Sets the row constants.
	 *
	 * @param rowConstants the rowConstants to set
	 * @return the file parser options
	 */
	public FileParserOptions setRowConstants(Map<String, Object> rowConstants) {
		checkModificationsDisabled();
		this.rowConstants = rowConstants;
		return this;
	}

	/**
	 * Sets the row validation expressions.
	 *
	 * @param rowValidationExpressions the rowValidationExpressions to set
	 * @return the file parser options
	 */
	public FileParserOptions setRowValidationExpressions(List<OutputExpression> rowValidationExpressions) {
		checkModificationsDisabled();
		this.rowValidationExpressions = rowValidationExpressions;
		return this;
	}

	/**
	 * Sets the sep char.
	 *
	 * @param sepChar the sepChar to set
	 * @return the file parser options
	 */
	public FileParserOptions setSepChar(Character sepChar) {
		checkModificationsDisabled();
		this.sepChar = sepChar;
		return this;
	}

	/**
	 * Sets the skip sheets.
	 *
	 * @param skipSheets the skipSheets to set
	 * @return the file parser options
	 */
	public FileParserOptions setSkipSheets(Set<String> skipSheets) {
		checkModificationsDisabled();
		this.skipSheets = skipSheets;
		return this;
	}

	/**
	 * Sets the strict.
	 *
	 * @param strict the strict to set
	 * @return the file parser options
	 */
	public FileParserOptions setStrict(Boolean strict) {
		checkModificationsDisabled();
		this.strict = strict;
		return this;
	}

	/**
	 * Sets the transpose.
	 *
	 * @param transpose the transpose to set
	 * @return the file parser options
	 */
	public FileParserOptions setTranspose(Boolean transpose) {
		checkModificationsDisabled();
		this.transpose = transpose;
		return this;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type to set
	 * @return the file parser options
	 */
	public FileParserOptions setType(Class<?> type) {
		checkModificationsDisabled();
		this.type = type;
		return this;
	}

	/**
	 * Sets the use excel formats.
	 *
	 * @param useExcelFormats the useExcelFormats to set
	 * @return the file parser options
	 */
	public FileParserOptions setUseExcelFormats(Boolean useExcelFormats) {
		checkModificationsDisabled();
		this.useExcelFormats = useExcelFormats;
		return this;
	}

	/**
	 * Sets the use path keys.
	 *
	 * @param usePathKeys the usePathKeys to set
	 * @return the file parser options
	 */
	public FileParserOptions setUsePathKeys(Boolean usePathKeys) {
		checkModificationsDisabled();
		this.usePathKeys = usePathKeys;
		return this;
	}

	/**
	 * Test allow blanks.
	 *
	 * @return true, if successful
	 */
	public boolean testAllowBlanks() {
		return (allowBlanks != null) && allowBlanks;
	}

	/**
	 * Test keys as paths.
	 *
	 * @return true, if successful
	 */
	public boolean testKeysAsPaths() {
		return (keysAsPaths == null) || keysAsPaths;
	}

	/**
	 * Test strict.
	 *
	 * @return true, if successful
	 */
	public boolean testStrict() {
		return (strict != null) && strict.booleanValue();
	}

	/**
	 * Test transpose.
	 *
	 * @return true, if successful
	 */
	public boolean testTranspose() {
		return (transpose != null) && transpose;
	}

	/**
	 * Test use excel formats.
	 *
	 * @return true, if successful
	 */
	public boolean testUseExcelFormats() {
		return (useExcelFormats != null) && useExcelFormats;
	}

	/**
	 * Test use path keys.
	 *
	 * @return true, if successful
	 */
	public boolean testUsePathKeys() {
		return (usePathKeys != null) && usePathKeys.booleanValue();
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
