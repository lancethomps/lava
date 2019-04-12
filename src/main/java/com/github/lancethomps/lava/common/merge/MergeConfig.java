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

import com.fasterxml.jackson.databind.JavaType;
import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.Collect;
import com.github.lancethomps.lava.common.SimpleDomainObject;

@SuppressWarnings({"unchecked", "serial"})
public class MergeConfig extends SimpleDomainObject {

  private Boolean addToTargetArray;

  private Boolean createNewBean;

  private List<MergeFieldConfig> fieldExpressions;

  private Map<String, MergeFieldConfig> fields;

  private Set<String> ignoreFields;

  private Set<Pattern> ignoreFieldsPatterns;

  private Boolean mergeArrayElements;

  private String mergeArrayElementsMatchField;

  private Boolean mergeArrayElementsSkipNonMatching;

  private Boolean modificationsDisabled;

  private Boolean overwriteArrayNodes;

  private Boolean overwriteExisting;

  private Boolean overwriteWithNonMatchingNodeType;

  private Boolean overwriteWithNull;

  private Boolean removeDuplicatesFromTargetArray;

  private Set<String> removeFields;

  private Boolean removeFromTargetArray;

  private JavaType rootType;

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

  public MergeConfig disableModifications() {
    modificationsDisabled = true;
    return this;
  }

  public Boolean getAddToTargetArray() {
    return addToTargetArray;
  }

  public <T extends MergeConfig> T setAddToTargetArray(Boolean addToTargetArray) {
    checkModificationsDisabled();
    this.addToTargetArray = addToTargetArray;
    return (T) this;
  }

  public Boolean getCreateNewBean() {
    return createNewBean;
  }

  public <T extends MergeConfig> T setCreateNewBean(Boolean createNewBean) {
    checkModificationsDisabled();
    this.createNewBean = createNewBean;
    return (T) this;
  }

  public MergeFieldConfig getField(@Nonnull String fieldName, boolean wildcardMatch) {
    return fields == null ? null : wildcardMatch ? Collect.wildcardGet(fields, fieldName) : fields.get(fieldName);
  }

  public List<MergeFieldConfig> getFieldExpressions() {
    return fieldExpressions;
  }

  public <T extends MergeConfig> T setFieldExpressions(List<MergeFieldConfig> fieldExpressions) {
    checkModificationsDisabled();
    this.fieldExpressions = fieldExpressions;
    return (T) this;
  }

  public Map<String, MergeFieldConfig> getFields() {
    return fields;
  }

  public <T extends MergeConfig> T setFields(Map<String, MergeFieldConfig> fields) {
    checkModificationsDisabled();
    this.fields = fields;
    return (T) this;
  }

  public Set<String> getIgnoreFields() {
    return ignoreFields;
  }

  public <T extends MergeConfig> T setIgnoreFields(Set<String> ignoreFields) {
    checkModificationsDisabled();
    this.ignoreFields = ignoreFields;
    return (T) this;
  }

  public Set<Pattern> getIgnoreFieldsPatterns() {
    return ignoreFieldsPatterns;
  }

  public <T extends MergeConfig> T setIgnoreFieldsPatterns(Set<Pattern> ignoreFieldsPatterns) {
    checkModificationsDisabled();
    this.ignoreFieldsPatterns = ignoreFieldsPatterns;
    return (T) this;
  }

  public Boolean getMergeArrayElements() {
    return mergeArrayElements;
  }

  public <T extends MergeConfig> T setMergeArrayElements(Boolean mergeArrayElements) {
    checkModificationsDisabled();
    this.mergeArrayElements = mergeArrayElements;
    return (T) this;
  }

  public String getMergeArrayElementsMatchField() {
    return mergeArrayElementsMatchField;
  }

  public <T extends MergeConfig> T setMergeArrayElementsMatchField(String mergeArrayElementsMatchField) {
    checkModificationsDisabled();
    this.mergeArrayElementsMatchField = mergeArrayElementsMatchField;
    return (T) this;
  }

  public Boolean getMergeArrayElementsSkipNonMatching() {
    return mergeArrayElementsSkipNonMatching;
  }

  public <T extends MergeConfig> T setMergeArrayElementsSkipNonMatching(Boolean mergeArrayElementsSkipNonMatching) {
    checkModificationsDisabled();
    this.mergeArrayElementsSkipNonMatching = mergeArrayElementsSkipNonMatching;
    return (T) this;
  }

  public Boolean getModificationsDisabled() {
    return modificationsDisabled;
  }

  public Boolean getOverwriteArrayNodes() {
    return overwriteArrayNodes;
  }

  public <T extends MergeConfig> T setOverwriteArrayNodes(Boolean overwriteArrayNodes) {
    checkModificationsDisabled();
    this.overwriteArrayNodes = overwriteArrayNodes;
    return (T) this;
  }

  public Boolean getOverwriteExisting() {
    return overwriteExisting;
  }

  public <T extends MergeConfig> T setOverwriteExisting(Boolean overwriteExisting) {
    checkModificationsDisabled();
    this.overwriteExisting = overwriteExisting;
    return (T) this;
  }

  public Boolean getOverwriteWithNonMatchingNodeType() {
    return overwriteWithNonMatchingNodeType;
  }

  public MergeConfig setOverwriteWithNonMatchingNodeType(Boolean overwriteWithNonMatchingNodeType) {
    checkModificationsDisabled();
    this.overwriteWithNonMatchingNodeType = overwriteWithNonMatchingNodeType;
    return this;
  }

  public Boolean getOverwriteWithNull() {
    return overwriteWithNull;
  }

  public <T extends MergeConfig> T setOverwriteWithNull(Boolean overwriteWithNull) {
    checkModificationsDisabled();
    this.overwriteWithNull = overwriteWithNull;
    return (T) this;
  }

  public Boolean getRemoveDuplicatesFromTargetArray() {
    return removeDuplicatesFromTargetArray;
  }

  public <T extends MergeConfig> T setRemoveDuplicatesFromTargetArray(Boolean removeDuplicatesFromTargetArray) {
    checkModificationsDisabled();
    this.removeDuplicatesFromTargetArray = removeDuplicatesFromTargetArray;
    return (T) this;
  }

  public Set<String> getRemoveFields() {
    return removeFields;
  }

  public <T extends MergeConfig> T setRemoveFields(Set<String> removeFields) {
    checkModificationsDisabled();
    this.removeFields = removeFields;
    return (T) this;
  }

  public Boolean getRemoveFromTargetArray() {
    return removeFromTargetArray;
  }

  public <T extends MergeConfig> T setRemoveFromTargetArray(Boolean removeFromTargetArray) {
    checkModificationsDisabled();
    this.removeFromTargetArray = removeFromTargetArray;
    return (T) this;
  }

  public JavaType getRootType() {
    return rootType;
  }

  public <T extends MergeConfig> T setRootType(JavaType rootType) {
    checkModificationsDisabled();
    this.rootType = rootType;
    return (T) this;
  }

  public boolean testAddToTargetArray() {
    return (addToTargetArray == null) || addToTargetArray;
  }

  public boolean testCreateNewBean() {
    return (createNewBean != null) && createNewBean;
  }

  public boolean testMergeArrayElements() {
    return (mergeArrayElements != null) && mergeArrayElements;
  }

  public boolean testMergeArrayElementsSkipNonMatching() {
    return (mergeArrayElementsSkipNonMatching != null) && mergeArrayElementsSkipNonMatching.booleanValue();
  }

  public boolean testModificationsDisabled() {
    return (modificationsDisabled != null) && modificationsDisabled.booleanValue();
  }

  public boolean testOverwriteArrayNodes() {
    return (overwriteArrayNodes != null) && overwriteArrayNodes;
  }

  public boolean testOverwriteExisting() {
    return (overwriteExisting != null) && overwriteExisting;
  }

  public boolean testOverwriteWithNonMatchingNodeType() {
    return (overwriteWithNonMatchingNodeType == null) || overwriteWithNonMatchingNodeType;
  }

  public boolean testOverwriteWithNull() {
    return (overwriteWithNull != null) && overwriteWithNull;
  }

  public boolean testRemoveDuplicatesFromTargetArray() {
    return (removeDuplicatesFromTargetArray != null) && removeDuplicatesFromTargetArray;
  }

  public boolean testRemoveFromTargetArray() {
    return (removeFromTargetArray != null) && removeFromTargetArray;
  }

  protected void checkModificationsDisabled() {
    if (testModificationsDisabled()) {
      throw new UnsupportedOperationException("Modifications have been disabled for this instance!");
    }
  }

}
