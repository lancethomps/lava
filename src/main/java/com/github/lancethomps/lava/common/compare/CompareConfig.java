package com.github.lancethomps.lava.common.compare;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.github.lancethomps.lava.common.ser.OutputParams;

/**
 * The Class CompareConfig.
 */
@SuppressWarnings("serial")
public class CompareConfig extends SimpleDomainObject {

	/** The calculate numeric value change. */
	private Boolean calculateNumericValueChange;

	/** The deep. */
	private Boolean deep;

	/** The deep when null containers. */
	private Boolean deepWhenNullContainer;

	/** The diff output params. */
	private OutputParams diffOutputParams;

	/** The fields. */
	private Map<String, CompareConfig> fields;

	/** The fields black list. */
	private Set<Pattern> fieldsBlackList;

	/** The only fields patterns. */
	private Set<Pattern> fieldsWhiteList;

	/** The ignore fields. */
	private Set<String> ignoreFields;

	/** The numeric nulls equal zero. */
	private Boolean numericNullsEqualZero;

	/** The object field notation. */
	private String objectFieldNotation;

	/**
	 * Adds the field config.
	 *
	 * @param field the field
	 * @param config the config
	 * @return the compare config
	 */
	public CompareConfig addFieldConfig(String field, CompareConfig config) {
		if (fields == null) {
			fields = new HashMap<>(5);
		}
		fields.put(field, config);
		return this;
	}

	/**
	 * Adds the ignore fields.
	 *
	 * @param fields the fields
	 * @return the compare config
	 */
	public CompareConfig addIgnoreFields(String... fields) {
		if (Checks.isNotEmpty(fields)) {
			if (ignoreFields == null) {
				ignoreFields = new HashSet<>(fields.length);
			}
			ignoreFields.addAll(Arrays.asList(fields));
		}
		return this;
	}

	/**
	 * @return the calculateNumericValueChange
	 */
	public Boolean getCalculateNumericValueChange() {
		return calculateNumericValueChange;
	}

	/**
	 * Gets the deep.
	 *
	 * @return the deep
	 */
	public Boolean getDeep() {
		return deep;
	}

	/**
	 * Gets the deep when null container.
	 *
	 * @return the deepWhenNullContainers
	 */
	public Boolean getDeepWhenNullContainer() {
		return deepWhenNullContainer;
	}

	/**
	 * @return the diffOutputParams
	 */
	public OutputParams getDiffOutputParams() {
		return diffOutputParams;
	}

	/**
	 * Gets the fields.
	 *
	 * @return the fields
	 */
	public Map<String, CompareConfig> getFields() {
		return fields;
	}

	/**
	 * @return the fieldsBlackList
	 */
	public Set<Pattern> getFieldsBlackList() {
		return fieldsBlackList;
	}

	/**
	 * @return the fieldsWhiteList
	 */
	public Set<Pattern> getFieldsWhiteList() {
		return fieldsWhiteList;
	}

	/**
	 * @return the ignoreFields
	 */
	public Set<String> getIgnoreFields() {
		return ignoreFields;
	}

	/**
	 * @return the numericNullsEqualZero
	 */
	public Boolean getNumericNullsEqualZero() {
		return numericNullsEqualZero;
	}

	/**
	 * @return the objectFieldNotation
	 */
	public String getObjectFieldNotation() {
		return objectFieldNotation;
	}

	/**
	 * Sets the calculate numeric value change.
	 *
	 * @param calculateNumericValueChange the calculateNumericValueChange to set
	 * @return the compare config
	 */
	public CompareConfig setCalculateNumericValueChange(Boolean calculateNumericValueChange) {
		this.calculateNumericValueChange = calculateNumericValueChange;
		return this;
	}

	/**
	 * Sets the deep.
	 *
	 * @param deep the deep to set
	 * @return the compare config
	 */
	public CompareConfig setDeep(Boolean deep) {
		this.deep = deep;
		return this;
	}

	/**
	 * Sets the deep when null containers.
	 *
	 * @param deepWhenNullContainer the deep when null container
	 * @return the compare config
	 */
	public CompareConfig setDeepWhenNullContainer(Boolean deepWhenNullContainer) {
		this.deepWhenNullContainer = deepWhenNullContainer;
		return this;
	}

	/**
	 * Sets the diff output params.
	 *
	 * @param diffOutputParams the diff output params
	 * @return the compare config
	 */
	public CompareConfig setDiffOutputParams(OutputParams diffOutputParams) {
		this.diffOutputParams = diffOutputParams;
		return this;
	}

	/**
	 * Sets the fields.
	 *
	 * @param fields the fields to set
	 * @return the compare config
	 */
	public CompareConfig setFields(Map<String, CompareConfig> fields) {
		this.fields = fields;
		return this;
	}

	/**
	 * Sets the fields black list.
	 *
	 * @param fieldsBlackList the fieldsBlackList to set
	 * @return the compare config
	 */
	public CompareConfig setFieldsBlackList(Set<Pattern> fieldsBlackList) {
		this.fieldsBlackList = fieldsBlackList;
		return this;
	}

	/**
	 * Sets the fields white list.
	 *
	 * @param fieldsWhiteList the fieldsWhiteList to set
	 * @return the compare config
	 */
	public CompareConfig setFieldsWhiteList(Set<Pattern> fieldsWhiteList) {
		this.fieldsWhiteList = fieldsWhiteList;
		return this;
	}

	/**
	 * Sets the ignore fields.
	 *
	 * @param ignoreFields the ignoreFields to set
	 * @return the compare config
	 */
	public CompareConfig setIgnoreFields(Set<String> ignoreFields) {
		this.ignoreFields = ignoreFields;
		return this;
	}

	/**
	 * Sets the numeric nulls equal zero.
	 *
	 * @param numericNullsEqualZero the numericNullsEqualZero to set
	 * @return the compare config
	 */
	public CompareConfig setNumericNullsEqualZero(Boolean numericNullsEqualZero) {
		this.numericNullsEqualZero = numericNullsEqualZero;
		return this;
	}

	/**
	 * Sets the object field notation.
	 *
	 * @param objectFieldNotation the objectFieldNotation to set
	 * @return the compare config
	 */
	public CompareConfig setObjectFieldNotation(String objectFieldNotation) {
		this.objectFieldNotation = objectFieldNotation;
		return this;
	}

	/**
	 * Test calculate numeric value change.
	 *
	 * @return true, if successful
	 */
	public boolean testCalculateNumericValueChange() {
		return (calculateNumericValueChange != null) && calculateNumericValueChange.booleanValue();
	}

	/**
	 * Test deep.
	 *
	 * @return true, if successful
	 */
	public boolean testDeep() {
		return (deep == null) || deep;
	}

	/**
	 * Test deep when null container.
	 *
	 * @return true, if successful
	 */
	public boolean testDeepWhenNullContainer() {
		return (deepWhenNullContainer != null) && deepWhenNullContainer;
	}

	/**
	 * Test numeric nulls equal zero.
	 *
	 * @return true, if successful
	 */
	public boolean testNumericNullsEqualZero() {
		return (numericNullsEqualZero != null) && numericNullsEqualZero.booleanValue();
	}

}
