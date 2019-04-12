package com.github.lancethomps.lava.common.web.config;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.SimpleDomainObject;

@SuppressWarnings("serial")
public class RequestProcessingConfig extends SimpleDomainObject {

  private String id;
  private List<ResponseHeaderConfig> addHeaders;
  private List<ResponseHeaderConfig> clearHeaders;
  private List<RequestProcessingRule> contextRules;
  private Map<String, List<RequestProcessingRule>> cookieRules;
  private String failStatusMessage;
  private Integer failWith;
  private Map<String, List<RequestProcessingRule>> headerRules;
  @JsonIgnore
  private String info;

  private List<RequestProcessingLocation> locations;

  private Map<String, List<RequestProcessingRule>> parameterRules;

  private List<ResponseHeaderConfig> setHeaders;

  @Override
  public void afterDeserialization() {
  }

  public RequestProcessingConfig deriveInfoRecursively() {
    return deriveInfo(null, id);
  }

  public List<ResponseHeaderConfig> getAddHeaders() {
    return addHeaders;
  }

  public RequestProcessingConfig setAddHeaders(List<ResponseHeaderConfig> addHeaders) {
    this.addHeaders = addHeaders;
    return this;
  }

  public List<ResponseHeaderConfig> getClearHeaders() {
    return clearHeaders;
  }

  public RequestProcessingConfig setClearHeaders(List<ResponseHeaderConfig> clearHeaders) {
    this.clearHeaders = clearHeaders;
    return this;
  }

  public List<RequestProcessingRule> getContextRules() {
    return contextRules;
  }

  public RequestProcessingConfig setContextRules(List<RequestProcessingRule> contextRules) {
    this.contextRules = contextRules;
    return this;
  }

  public Map<String, List<RequestProcessingRule>> getCookieRules() {
    return cookieRules;
  }

  public RequestProcessingConfig setCookieRules(Map<String, List<RequestProcessingRule>> cookieRules) {
    this.cookieRules = cookieRules;
    return this;
  }

  public String getFailStatusMessage() {
    return failStatusMessage;
  }

  public RequestProcessingConfig setFailStatusMessage(String failStatusMessage) {
    this.failStatusMessage = failStatusMessage;
    return this;
  }

  public Integer getFailWith() {
    return failWith;
  }

  public RequestProcessingConfig setFailWith(Integer failWith) {
    this.failWith = failWith;
    return this;
  }

  public Map<String, List<RequestProcessingRule>> getHeaderRules() {
    return headerRules;
  }

  public RequestProcessingConfig setHeaderRules(Map<String, List<RequestProcessingRule>> headerRules) {
    this.headerRules = headerRules;
    return this;
  }

  public String getId() {
    return id;
  }

  public RequestProcessingConfig setId(String id) {
    this.id = id;
    return this;
  }

  public String getInfo() {
    return info;
  }

  public RequestProcessingConfig setInfo(String info) {
    this.info = info;
    return this;
  }

  public List<RequestProcessingLocation> getLocations() {
    return locations;
  }

  public RequestProcessingConfig setLocations(List<RequestProcessingLocation> locations) {
    this.locations = locations;
    return this;
  }

  public Map<String, List<RequestProcessingRule>> getParameterRules() {
    return parameterRules;
  }

  public RequestProcessingConfig setParameterRules(Map<String, List<RequestProcessingRule>> parameterRules) {
    this.parameterRules = parameterRules;
    return this;
  }

  public List<ResponseHeaderConfig> getSetHeaders() {
    return setHeaders;
  }

  public RequestProcessingConfig setSetHeaders(List<ResponseHeaderConfig> setHeaders) {
    this.setHeaders = setHeaders;
    return this;
  }

  protected RequestProcessingConfig deriveInfo(RequestProcessingConfig parent, String info) {
    this.info = ((parent == null) || Checks.isBlank(parent.getInfo()) ? "" : (parent.getInfo() + '.')) + info;
    if (contextRules != null) {
      for (int index = 0; index < contextRules.size(); index++) {
        RequestProcessingRule rule = contextRules.get(index);
        if (rule.getConfig() != null) {
          rule.getConfig().deriveInfo(this, "contextRules[" + index + ']');
        }
      }
    }
    addRulesMapInfo("cookieRules", cookieRules);
    addRulesMapInfo("headerRules", headerRules);
    addRulesMapInfo("parameterRules", parameterRules);
    if (locations != null) {
      for (int index = 0; index < locations.size(); index++) {
        RequestProcessingLocation location = locations.get(index);
        if (location.getConfig() != null) {
          String locationInfo = location.getLocation() == null ? ("locations[" + index + ']') : ("locations['" + location.getLocation() + "']");
          location.getConfig().deriveInfo(this, locationInfo);
        }
      }
    }
    return this;
  }

  private void addRulesMapInfo(String type, Map<String, List<RequestProcessingRule>> rules) {
    if (rules != null) {
      for (Entry<String, List<RequestProcessingRule>> entry : rules.entrySet()) {
        for (int index = 0; index < entry.getValue().size(); index++) {
          RequestProcessingRule rule = entry.getValue().get(index);
          if (rule.getConfig() != null) {
            rule.getConfig().deriveInfo(this, type + '.' + entry.getKey() + '[' + index + ']');
          }
        }
      }
    }
  }

}
