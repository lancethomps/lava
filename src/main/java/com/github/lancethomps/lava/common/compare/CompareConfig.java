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

@SuppressWarnings("serial")
public class CompareConfig extends SimpleDomainObject {

  private Boolean calculateNumericValueChange;

  private Boolean deep;

  private Boolean deepWhenNullContainer;

  private OutputParams diffOutputParams;

  private Map<String, CompareConfig> fields;

  private Set<Pattern> fieldsBlackList;

  private Set<Pattern> fieldsWhiteList;

  private Set<String> ignoreFields;

  private Boolean numericNullsEqualZero;

  private String objectFieldNotation;

  public CompareConfig addFieldConfig(String field, CompareConfig config) {
    if (fields == null) {
      fields = new HashMap<>(5);
    }
    fields.put(field, config);
    return this;
  }

  public CompareConfig addIgnoreFields(String... fields) {
    if (Checks.isNotEmpty(fields)) {
      if (ignoreFields == null) {
        ignoreFields = new HashSet<>(fields.length);
      }
      ignoreFields.addAll(Arrays.asList(fields));
    }
    return this;
  }

  public Boolean getCalculateNumericValueChange() {
    return calculateNumericValueChange;
  }

  public CompareConfig setCalculateNumericValueChange(Boolean calculateNumericValueChange) {
    this.calculateNumericValueChange = calculateNumericValueChange;
    return this;
  }

  public Boolean getDeep() {
    return deep;
  }

  public CompareConfig setDeep(Boolean deep) {
    this.deep = deep;
    return this;
  }

  public Boolean getDeepWhenNullContainer() {
    return deepWhenNullContainer;
  }

  public CompareConfig setDeepWhenNullContainer(Boolean deepWhenNullContainer) {
    this.deepWhenNullContainer = deepWhenNullContainer;
    return this;
  }

  public OutputParams getDiffOutputParams() {
    return diffOutputParams;
  }

  public CompareConfig setDiffOutputParams(OutputParams diffOutputParams) {
    this.diffOutputParams = diffOutputParams;
    return this;
  }

  public Map<String, CompareConfig> getFields() {
    return fields;
  }

  public CompareConfig setFields(Map<String, CompareConfig> fields) {
    this.fields = fields;
    return this;
  }

  public Set<Pattern> getFieldsBlackList() {
    return fieldsBlackList;
  }

  public CompareConfig setFieldsBlackList(Set<Pattern> fieldsBlackList) {
    this.fieldsBlackList = fieldsBlackList;
    return this;
  }

  public Set<Pattern> getFieldsWhiteList() {
    return fieldsWhiteList;
  }

  public CompareConfig setFieldsWhiteList(Set<Pattern> fieldsWhiteList) {
    this.fieldsWhiteList = fieldsWhiteList;
    return this;
  }

  public Set<String> getIgnoreFields() {
    return ignoreFields;
  }

  public CompareConfig setIgnoreFields(Set<String> ignoreFields) {
    this.ignoreFields = ignoreFields;
    return this;
  }

  public Boolean getNumericNullsEqualZero() {
    return numericNullsEqualZero;
  }

  public CompareConfig setNumericNullsEqualZero(Boolean numericNullsEqualZero) {
    this.numericNullsEqualZero = numericNullsEqualZero;
    return this;
  }

  public String getObjectFieldNotation() {
    return objectFieldNotation;
  }

  public CompareConfig setObjectFieldNotation(String objectFieldNotation) {
    this.objectFieldNotation = objectFieldNotation;
    return this;
  }

  public boolean testCalculateNumericValueChange() {
    return (calculateNumericValueChange != null) && calculateNumericValueChange.booleanValue();
  }

  public boolean testDeep() {
    return (deep == null) || deep;
  }

  public boolean testDeepWhenNullContainer() {
    return (deepWhenNullContainer != null) && deepWhenNullContainer;
  }

  public boolean testNumericNullsEqualZero() {
    return (numericNullsEqualZero != null) && numericNullsEqualZero.booleanValue();
  }

}
