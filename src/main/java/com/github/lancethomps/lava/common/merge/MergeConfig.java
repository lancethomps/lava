package com.github.lancethomps.lava.common.merge;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.Collect;
import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.fasterxml.jackson.databind.JavaType;

/**
 * The Class MergeConfig.
 *
 * @author lathomps
 */
@SuppressWarnings({ "unchecked", "serial" })
public class MergeConfig extends SimpleDomainObject {

	/** The add to target array. */
	private Boolean addToTargetArray;

	/** The create new bean. */
	private Boolean createNewBean;

	/** The field expressions. */
	private List<MergeFieldConfig> fieldExpressions;

	/** The fields. */
	private Map<String, MergeFieldConfig> fields;

	/** The ignore fields. */
	private Set<String> ignoreFields;

	/** The ignore fields patterns. */
	private Set<Pattern> ignoreFieldsPatterns;

	/** The merge array elements. */
	private Boolean mergeArrayElements;

	/** The merge array elements match field. */
	private String mergeArrayElementsMatchField;

	/** The merge array elements skip non matching. */
	private Boolean mergeArrayElementsSkipNonMatching;

	/** The modifications disabled. */
	private Boolean modificationsDisabled;

	/** The overwrite array nodes. */
	private Boolean overwriteArrayNodes;

	/** The ignore existing. */
	private Boolean overwriteExisting;

	/** The overwrite with non matching node type. */
	private Boolean overwriteWithNonMatchingNodeType;

	/** The ignore null. */
	private Boolean overwriteWithNull;

	/** The remove duplicates from target array. */
	private Boolean removeDuplicatesFromTargetArray;

	/** The remove fields. */
	private Set<String> removeFields;

	/** The remove from target array. */
	private Boolean removeFromTargetArray;

	/** The root type. */
	private JavaType rootType;

	/**
	 * Adds the field.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @param field the field
	 * @return the merge config
	 */
	public <T extends MergeConfig> T addField(String name, MergeFieldConfig field) {
		checkModificationsDisabled();
		if ((name != null) && (field != null)) {
			if (fields == null) {
				fields = new HashMap<>();
			}
			if (field.getName() == null) {
				field.setName(name);
			}
			fields.put(name, field);
		}
		return (T) this;
	}

	/**
	 * Adds the ignore fields.
	 *
	 * @param <T> the generic type
	 * @param fields the fields
	 * @return the t
	 */
	public <T extends MergeConfig> T addIgnoreFields(String... fields) {
		checkModificationsDisabled();
		if (Checks.isNotEmpty(fields)) {
			if (ignoreFields == null) {
				ignoreFields = new HashSet<>();
			}
			ignoreFields.addAll(Arrays.asList(fields));
		}
		return (T) this;
	}

	/**
	 * Adds the ignore fields patterns.
	 *
	 * @param <T> the generic type
	 * @param patterns the patterns
	 * @return the t
	 */
	public <T extends MergeConfig> T addIgnoreFieldsPatterns(Pattern... patterns) {
		checkModificationsDisabled();
		if (Checks.isNotEmpty(patterns)) {
			if (ignoreFieldsPatterns == null) {
				ignoreFieldsPatterns = new HashSet<>();
			}
			ignoreFieldsPatterns.addAll(Arrays.asList(patterns));
		}
		return (T) this;
	}

	/**
	 * Adds the ignore fields patterns.
	 *
	 * @param <T> the generic type
	 * @param patterns the patterns
	 * @return the t
	 */
	public <T extends MergeConfig> T addIgnoreFieldsPatterns(String... patterns) {
		checkModificationsDisabled();
		if (Checks.isNotEmpty(patterns)) {
			if (ignoreFieldsPatterns == null) {
				ignoreFieldsPatterns = new HashSet<>();
			}
			Stream.of(patterns).map(Pattern::compile).filter(Checks::nonNull).forEach(ignoreFieldsPatterns::add);
		}
		return (T) this;
	}

	/**
	 * Disable modifications.
	 *
	 * @return the merge config
	 */
	public MergeConfig disableModifications() {
		modificationsDisabled = true;
		return this;
	}

	/**
	 * Gets the adds the to target array.
	 *
	 * @return the addToTargetArray
	 */
	public Boolean getAddToTargetArray() {
		return addToTargetArray;
	}

	/**
	 * Gets the creates the new bean.
	 *
	 * @return the createNewBean
	 */
	public Boolean getCreateNewBean() {
		return createNewBean;
	}

	/**
	 * Gets the field.
	 *
	 * @param fieldName the field name
	 * @param wildcardMatch the wildcard match
	 * @return the field
	 */
	public MergeFieldConfig getField(@Nonnull String fieldName, boolean wildcardMatch) {
		return fields == null ? null : wildcardMatch ? Collect.wildcardGet(fields, fieldName) : fields.get(fieldName);
	}

	/**
	 * Gets the field expressions.
	 *
	 * @return the fieldExpressions
	 */
	public List<MergeFieldConfig> getFieldExpressions() {
		return fieldExpressions;
	}

	/**
	 * Gets the fields.
	 *
	 * @return the fields
	 */
	public Map<String, MergeFieldConfig> getFields() {
		return fields;
	}

	/**
	 * @return the ignoreFields
	 */
	public Set<String> getIgnoreFields() {
		return ignoreFields;
	}

	/**
	 * @return the ignoreFieldsPatterns
	 */
	public Set<Pattern> getIgnoreFieldsPatterns() {
		return ignoreFieldsPatterns;
	}

	/**
	 * Gets the merge array elements.
	 *
	 * @return the mergeArrayElements
	 */
	public Boolean getMergeArrayElements() {
		return mergeArrayElements;
	}

	/**
	 * @return the mergeArrayElementsMatchField
	 */
	public String getMergeArrayElementsMatchField() {
		return mergeArrayElementsMatchField;
	}

	/**
	 * @return the mergeArrayElementsSkipNonMatching
	 */
	public Boolean getMergeArrayElementsSkipNonMatching() {
		return mergeArrayElementsSkipNonMatching;
	}

	/**
	 * @return the modificationsDisabled
	 */
	public Boolean getModificationsDisabled() {
		return modificationsDisabled;
	}

	/**
	 * Gets the overwrite array nodes.
	 *
	 * @return the overwriteArrayNodes
	 */
	public Boolean getOverwriteArrayNodes() {
		return overwriteArrayNodes;
	}

	/**
	 * Gets the overwrite existing.
	 *
	 * @return the ignoreExisting
	 */
	public Boolean getOverwriteExisting() {
		return overwriteExisting;
	}

	/**
	 * @return the overwriteWithNonMatchingNodeType
	 */
	public Boolean getOverwriteWithNonMatchingNodeType() {
		return overwriteWithNonMatchingNodeType;
	}

	/**
	 * Gets the overwrite with null.
	 *
	 * @return the ignoreNull
	 */
	public Boolean getOverwriteWithNull() {
		return overwriteWithNull;
	}

	/**
	 * @return the removeDuplicatesFromTargetArray
	 */
	public Boolean getRemoveDuplicatesFromTargetArray() {
		return removeDuplicatesFromTargetArray;
	}

	/**
	 * @return the removeFields
	 */
	public Set<String> getRemoveFields() {
		return removeFields;
	}

	/**
	 * @return the removeFromTargetArray
	 */
	public Boolean getRemoveFromTargetArray() {
		return removeFromTargetArray;
	}

	/**
	 * @return the rootType
	 */
	public JavaType getRootType() {
		return rootType;
	}

	/**
	 * Sets the add to target array.
	 *
	 * @param <T> the generic type
	 * @param addToTargetArray the addToTargetArray to set
	 * @return the merge config
	 */
	public <T extends MergeConfig> T setAddToTargetArray(Boolean addToTargetArray) {
		checkModificationsDisabled();
		this.addToTargetArray = addToTargetArray;
		return (T) this;
	}

	/**
	 * Sets the create new bean.
	 *
	 * @param <T> the generic type
	 * @param createNewBean the createNewBean to set
	 * @return the merge config
	 */
	public <T extends MergeConfig> T setCreateNewBean(Boolean createNewBean) {
		checkModificationsDisabled();
		this.createNewBean = createNewBean;
		return (T) this;
	}

	/**
	 * Sets the field expressions.
	 *
	 * @param <T> the generic type
	 * @param fieldExpressions the fieldExpressions to set
	 * @return the merge config
	 */
	public <T extends MergeConfig> T setFieldExpressions(List<MergeFieldConfig> fieldExpressions) {
		checkModificationsDisabled();
		this.fieldExpressions = fieldExpressions;
		return (T) this;
	}

	/**
	 * Sets the fields.
	 *
	 * @param <T> the generic type
	 * @param fields the fields to set
	 * @return the merge config
	 */
	public <T extends MergeConfig> T setFields(Map<String, MergeFieldConfig> fields) {
		checkModificationsDisabled();
		this.fields = fields;
		return (T) this;
	}

	/**
	 * Sets the ignore fields.
	 *
	 * @param <T> the generic type
	 * @param ignoreFields the ignoreFields to set
	 * @return the t
	 */
	public <T extends MergeConfig> T setIgnoreFields(Set<String> ignoreFields) {
		checkModificationsDisabled();
		this.ignoreFields = ignoreFields;
		return (T) this;
	}

	/**
	 * Sets the ignore fields regexes.
	 *
	 * @param <T> the generic type
	 * @param ignoreFieldsPatterns the ignore fields patterns
	 * @return the t
	 */
	public <T extends MergeConfig> T setIgnoreFieldsPatterns(Set<Pattern> ignoreFieldsPatterns) {
		checkModificationsDisabled();
		this.ignoreFieldsPatterns = ignoreFieldsPatterns;
		return (T) this;
	}

	/**
	 * Sets the merge array elements.
	 *
	 * @param <T> the generic type
	 * @param mergeArrayElements the mergeArrayElements to set
	 * @return the merge config
	 */
	public <T extends MergeConfig> T setMergeArrayElements(Boolean mergeArrayElements) {
		checkModificationsDisabled();
		this.mergeArrayElements = mergeArrayElements;
		return (T) this;
	}

	/**
	 * Sets the merge array elements match field.
	 *
	 * @param <T> the generic type
	 * @param mergeArrayElementsMatchField the mergeArrayElementsMatchField to set
	 * @return the t
	 */
	public <T extends MergeConfig> T setMergeArrayElementsMatchField(String mergeArrayElementsMatchField) {
		checkModificationsDisabled();
		this.mergeArrayElementsMatchField = mergeArrayElementsMatchField;
		return (T) this;
	}

	/**
	 * Sets the merge array elements skip non matching.
	 *
	 * @param <T> the generic type
	 * @param mergeArrayElementsSkipNonMatching the mergeArrayElementsSkipNonMatching to set
	 * @return the t
	 */
	public <T extends MergeConfig> T setMergeArrayElementsSkipNonMatching(Boolean mergeArrayElementsSkipNonMatching) {
		checkModificationsDisabled();
		this.mergeArrayElementsSkipNonMatching = mergeArrayElementsSkipNonMatching;
		return (T) this;
	}

	/**
	 * Sets the overwrite array nodes.
	 *
	 * @param <T> the generic type
	 * @param overwriteArrayNodes the overwriteArrayNodes to set
	 * @return the merge config
	 */
	public <T extends MergeConfig> T setOverwriteArrayNodes(Boolean overwriteArrayNodes) {
		checkModificationsDisabled();
		this.overwriteArrayNodes = overwriteArrayNodes;
		return (T) this;
	}

	/**
	 * Sets the overwrite existing.
	 *
	 * @param <T> the generic type
	 * @param overwriteExisting the overwrite existing
	 * @return the merge config
	 */
	public <T extends MergeConfig> T setOverwriteExisting(Boolean overwriteExisting) {
		checkModificationsDisabled();
		this.overwriteExisting = overwriteExisting;
		return (T) this;
	}

	/**
	 * Sets the overwrite with non matching node type.
	 *
	 * @param overwriteWithNonMatchingNodeType the overwriteWithNonMatchingNodeType to set
	 * @return the merge config
	 */
	public MergeConfig setOverwriteWithNonMatchingNodeType(Boolean overwriteWithNonMatchingNodeType) {
		checkModificationsDisabled();
		this.overwriteWithNonMatchingNodeType = overwriteWithNonMatchingNodeType;
		return this;
	}

	/**
	 * Sets the overwrite with null.
	 *
	 * @param <T> the generic type
	 * @param overwriteWithNull the overwrite with null
	 * @return the merge config
	 */
	public <T extends MergeConfig> T setOverwriteWithNull(Boolean overwriteWithNull) {
		checkModificationsDisabled();
		this.overwriteWithNull = overwriteWithNull;
		return (T) this;
	}

	/**
	 * Sets the remove duplicates from target array.
	 *
	 * @param <T> the generic type
	 * @param removeDuplicatesFromTargetArray the removeDuplicatesFromTargetArray to set
	 * @return the t
	 */
	public <T extends MergeConfig> T setRemoveDuplicatesFromTargetArray(Boolean removeDuplicatesFromTargetArray) {
		checkModificationsDisabled();
		this.removeDuplicatesFromTargetArray = removeDuplicatesFromTargetArray;
		return (T) this;
	}

	/**
	 * Sets the remove fields.
	 *
	 * @param <T> the generic type
	 * @param removeFields the removeFields to set
	 * @return the t
	 */
	public <T extends MergeConfig> T setRemoveFields(Set<String> removeFields) {
		checkModificationsDisabled();
		this.removeFields = removeFields;
		return (T) this;
	}

	/**
	 * Sets the remove from target array.
	 *
	 * @param <T> the generic type
	 * @param removeFromTargetArray the removeFromTargetArray to set
	 * @return the merge config
	 */
	public <T extends MergeConfig> T setRemoveFromTargetArray(Boolean removeFromTargetArray) {
		checkModificationsDisabled();
		this.removeFromTargetArray = removeFromTargetArray;
		return (T) this;
	}

	/**
	 * Sets the root type.
	 *
	 * @param <T> the generic type
	 * @param rootType the rootType to set
	 * @return the t
	 */
	public <T extends MergeConfig> T setRootType(JavaType rootType) {
		checkModificationsDisabled();
		this.rootType = rootType;
		return (T) this;
	}

	/**
	 * Test add to target array.
	 *
	 * @return true, if successful
	 */
	public boolean testAddToTargetArray() {
		return (addToTargetArray == null) || addToTargetArray;
	}

	/**
	 * Test create new bean.
	 *
	 * @return true, if successful
	 */
	public boolean testCreateNewBean() {
		return (createNewBean != null) && createNewBean;
	}

	/**
	 * Test merge array elements.
	 *
	 * @return true, if successful
	 */
	public boolean testMergeArrayElements() {
		return (mergeArrayElements != null) && mergeArrayElements;
	}

	/**
	 * Test merge array elements skip non matching. The default is false.
	 *
	 * @return true, if successful
	 */
	public boolean testMergeArrayElementsSkipNonMatching() {
		return (mergeArrayElementsSkipNonMatching != null) && mergeArrayElementsSkipNonMatching.booleanValue();
	}

	/**
	 * Test modifications disabled.
	 *
	 * @return true, if successful
	 */
	public boolean testModificationsDisabled() {
		return (modificationsDisabled != null) && modificationsDisabled.booleanValue();
	}

	/**
	 * Test overwrite array nodes.
	 *
	 * @return true, if successful
	 */
	public boolean testOverwriteArrayNodes() {
		return (overwriteArrayNodes != null) && overwriteArrayNodes;
	}

	/**
	 * Test ignore existing.
	 *
	 * @return true, if successful
	 */
	public boolean testOverwriteExisting() {
		return (overwriteExisting != null) && overwriteExisting;
	}

	/**
	 * Test overwrite with non matching node type.
	 *
	 * @return true, if successful
	 */
	public boolean testOverwriteWithNonMatchingNodeType() {
		return (overwriteWithNonMatchingNodeType == null) || overwriteWithNonMatchingNodeType;
	}

	/**
	 * Test ignore null.
	 *
	 * @return true, if successful
	 */
	public boolean testOverwriteWithNull() {
		return (overwriteWithNull != null) && overwriteWithNull;
	}

	/**
	 * Test remove duplicates from target array.
	 *
	 * @return true, if successful
	 */
	public boolean testRemoveDuplicatesFromTargetArray() {
		return (removeDuplicatesFromTargetArray != null) && removeDuplicatesFromTargetArray;
	}

	/**
	 * Test remove from target array.
	 *
	 * @return true, if successful
	 */
	public boolean testRemoveFromTargetArray() {
		return (removeFromTargetArray != null) && removeFromTargetArray;
	}

	/**
	 * Check modifications disabled.
	 */
	protected void checkModificationsDisabled() {
		if (testModificationsDisabled()) {
			throw new UnsupportedOperationException("Modifications have been disabled for this instance!");
		}
	}

}
