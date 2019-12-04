package com.lancethomps.lava.common.web.requests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.lancethomps.lava.common.SimpleDomainObject;
import com.lancethomps.lava.common.expr.ExprFactory;
import com.lancethomps.lava.common.merge.MergeConfig;
import com.lancethomps.lava.common.ser.OutputExpression;
import com.lancethomps.lava.common.ser.OutputParams;
import com.lancethomps.lava.common.ser.Serializer;

@SuppressWarnings("serial")
public class RequestDefaultsConfig extends SimpleDomainObject {

  private String id;
  private Set<String> appliesToUris;
  private List<String> dependsOn;
  private Boolean failOnDisallowedParameters;
  private List<String> loadAfter;

  private OutputParams outputParams;

  private MergeConfig outputParamsMergeConfig;

  private List<Pattern> parameterBlackList;

  private MergeConfig parameterMergeConfig;

  private List<Pattern> parameterWhiteList;

  private List<OutputExpression> preProcessExpressions;

  private MergeConfig preProcessMergeConfig;

  @JsonIgnore
  private Map<String, String[]> requestMap;

  private Set<String> requiredParamsNonBlank;

  private Map<String, Object> values;

  public static Map<String, String[]> convertGenericMapToRequestParameters(Map<String, Object> values) {
    return values == null ? null : values.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> {
      List<String> vals = new ArrayList<>();
      JsonNode val = Serializer.toTree(e.getValue());
      if (val.isArray()) {
        val.elements().forEachRemaining(node -> {
          if (node.isValueNode()) {
            vals.add(node.asText());
          } else {
            vals.add(Serializer.toJson(node));
          }
        });
      } else if (val.isObject()) {
        vals.add(Serializer.toJson(val));
      } else {
        vals.add(val.asText());
      }
      return vals.toArray(new String[]{});
    }));
  }

  @Override
  public void afterDeserialization() {
    if (values != null) {
      requestMap = convertGenericMapToRequestParameters(values);
    }
    if (preProcessExpressions != null) {
      preProcessExpressions = ExprFactory.compileCreateExpressions(preProcessExpressions, false);
    }
    if ((outputParams != null) && (outputParams.getCreateExpressions() != null)) {
      outputParams.setCreateExpressions(ExprFactory.compileCreateExpressions(outputParams.getCreateExpressions(), false));
    }
    if ((outputParams != null) && (outputParams.getPostProcessExpressions() != null)) {
      outputParams.setPostProcessExpressions(ExprFactory.compileCreateExpressions(outputParams.getPostProcessExpressions(), false));
    }
  }

  public Set<String> getAppliesToUris() {
    return appliesToUris;
  }

  public RequestDefaultsConfig setAppliesToUris(Set<String> appliesToUris) {
    this.appliesToUris = appliesToUris;
    return this;
  }

  public List<String> getDependsOn() {
    return dependsOn;
  }

  public RequestDefaultsConfig setDependsOn(List<String> dependsOn) {
    this.dependsOn = dependsOn;
    return this;
  }

  public Boolean getFailOnDisallowedParameters() {
    return failOnDisallowedParameters;
  }

  public RequestDefaultsConfig setFailOnDisallowedParameters(Boolean failOnDisallowedParameters) {
    this.failOnDisallowedParameters = failOnDisallowedParameters;
    return this;
  }

  public String getId() {
    return id;
  }

  public RequestDefaultsConfig setId(String id) {
    this.id = id;
    return this;
  }

  public List<String> getLoadAfter() {
    return loadAfter;
  }

  public RequestDefaultsConfig setLoadAfter(List<String> loadAfter) {
    this.loadAfter = loadAfter;
    return this;
  }

  public OutputParams getOutputParams() {
    return outputParams;
  }

  public RequestDefaultsConfig setOutputParams(OutputParams outputParams) {
    this.outputParams = outputParams;
    return this;
  }

  public MergeConfig getOutputParamsMergeConfig() {
    return outputParamsMergeConfig;
  }

  public RequestDefaultsConfig setOutputParamsMergeConfig(MergeConfig outputParamsMergeConfig) {
    this.outputParamsMergeConfig = outputParamsMergeConfig;
    return this;
  }

  public List<Pattern> getParameterBlackList() {
    return parameterBlackList;
  }

  public RequestDefaultsConfig setParameterBlackList(List<Pattern> parameterBlackList) {
    this.parameterBlackList = parameterBlackList;
    return this;
  }

  public MergeConfig getParameterMergeConfig() {
    return parameterMergeConfig;
  }

  public RequestDefaultsConfig setParameterMergeConfig(MergeConfig parameterMergeConfig) {
    this.parameterMergeConfig = parameterMergeConfig;
    return this;
  }

  public List<Pattern> getParameterWhiteList() {
    return parameterWhiteList;
  }

  public RequestDefaultsConfig setParameterWhiteList(List<Pattern> parameterWhiteList) {
    this.parameterWhiteList = parameterWhiteList;
    return this;
  }

  public List<OutputExpression> getPreProcessExpressions() {
    return preProcessExpressions;
  }

  public RequestDefaultsConfig setPreProcessExpressions(List<OutputExpression> preProcessExpressions) {
    this.preProcessExpressions = preProcessExpressions;
    return this;
  }

  public MergeConfig getPreProcessMergeConfig() {
    return preProcessMergeConfig;
  }

  public RequestDefaultsConfig setPreProcessMergeConfig(MergeConfig preProcessMergeConfig) {
    this.preProcessMergeConfig = preProcessMergeConfig;
    return this;
  }

  public Map<String, String[]> getRequestMap() {
    return requestMap;
  }

  public RequestDefaultsConfig setRequestMap(Map<String, String[]> requestMap) {
    this.requestMap = requestMap;
    return this;
  }

  public Set<String> getRequiredParamsNonBlank() {
    return requiredParamsNonBlank;
  }

  public RequestDefaultsConfig setRequiredParamsNonBlank(Set<String> requiredParamsNonBlank) {
    this.requiredParamsNonBlank = requiredParamsNonBlank;
    return this;
  }

  public Map<String, Object> getValues() {
    return values;
  }

  public RequestDefaultsConfig setValues(Map<String, Object> values) {
    this.values = values;
    return this;
  }

  public boolean testFailOnDisallowedParameters() {
    return (failOnDisallowedParameters != null) && failOnDisallowedParameters.booleanValue();
  }

}
