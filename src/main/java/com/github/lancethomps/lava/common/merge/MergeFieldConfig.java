package com.github.lancethomps.lava.common.merge;

import static com.github.lancethomps.lava.common.Exceptions.throwIfTrue;
import static com.github.lancethomps.lava.common.expr.ExprFactory.parseAndConsumeExpressions;

import java.util.List;

import org.apache.commons.lang3.SerializationException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.lambda.Lambdas;

import ognl.Node;

public class MergeFieldConfig extends MergeConfig {

  @JsonIgnore
  private List<Node> enabled;

  private List<String> enabledExpressions;

  private String name;

  @JsonIgnore
  private Node postProcess;

  private String postProcessExpression;

  @JsonIgnore
  private List<Node> result;

  private List<String> resultExpressions;

  private List<String> resultKeys;

  @JsonIgnore
  private List<Node> value;

  private List<String> valueExpressions;

  @Override
  public void afterDeserialization() {
    super.afterDeserialization();
    String msg = "Could not parse expressions!";
    throwIfTrue(!parseAndConsumeExpressions(enabledExpressions, this::setEnabled), SerializationException.class, msg);
    throwIfTrue(!parseAndConsumeExpressions(resultExpressions, this::setResult), SerializationException.class, msg);
    throwIfTrue(!parseAndConsumeExpressions(valueExpressions, this::setValue), SerializationException.class, msg);
    postProcess = Lambdas.functionIfNonNull(postProcessExpression, ExprFactory::createOgnlExpression).orElse(null);
  }

  public List<Node> getEnabled() {
    return enabled;
  }

  public MergeFieldConfig setEnabled(List<Node> enabled) {
    checkModificationsDisabled();
    this.enabled = enabled;
    return this;
  }

  public List<String> getEnabledExpressions() {
    return enabledExpressions;
  }

  public MergeFieldConfig setEnabledExpressions(List<String> enabledExpressions) {
    checkModificationsDisabled();
    this.enabledExpressions = enabledExpressions;
    return this;
  }

  public String getName() {
    return name;
  }

  public MergeFieldConfig setName(String name) {
    checkModificationsDisabled();
    this.name = name;
    return this;
  }

  public Node getPostProcess() {
    return postProcess;
  }

  public MergeFieldConfig setPostProcess(Node postProcess) {
    checkModificationsDisabled();
    this.postProcess = postProcess;
    return this;
  }

  public String getPostProcessExpression() {
    return postProcessExpression;
  }

  public MergeFieldConfig setPostProcessExpression(String postProcessExpression) {
    checkModificationsDisabled();
    this.postProcessExpression = postProcessExpression;
    return this;
  }

  public List<Node> getResult() {
    return result;
  }

  public MergeFieldConfig setResult(List<Node> result) {
    checkModificationsDisabled();
    this.result = result;
    return this;
  }

  public List<String> getResultExpressions() {
    return resultExpressions;
  }

  public MergeFieldConfig setResultExpressions(List<String> resultExpressions) {
    checkModificationsDisabled();
    this.resultExpressions = resultExpressions;
    return this;
  }

  public List<String> getResultKeys() {
    return resultKeys;
  }

  public MergeFieldConfig setResultKeys(List<String> resultKeys) {
    checkModificationsDisabled();
    this.resultKeys = resultKeys;
    return this;
  }

  public List<Node> getValue() {
    return value;
  }

  public MergeFieldConfig setValue(List<Node> value) {
    checkModificationsDisabled();
    this.value = value;
    return this;
  }

  public List<String> getValueExpressions() {
    return valueExpressions;
  }

  public MergeFieldConfig setValueExpressions(List<String> valueExpressions) {
    checkModificationsDisabled();
    this.valueExpressions = valueExpressions;
    return this;
  }

}
