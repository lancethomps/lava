package com.github.lancethomps.lava.common.web.config;

import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.lancethomps.lava.common.SimpleDomainObject;

@SuppressWarnings("serial")
public class RequestProcessingLocation extends SimpleDomainObject {

  private RequestProcessingConfig config;

  private String location;

  @JsonIgnore
  private Pattern regex;

  private RequestProcessingLocationType type = RequestProcessingLocationType.PREFIX_MATCH;

  @Override
  public void afterDeserialization() {
    if (type.isRegex()) {
      switch (type) {
        case CASE_INSENSITIVE_REGEX:
          regex = Pattern.compile(location, Pattern.CASE_INSENSITIVE);
          break;
        case CASE_SENSITIVE_REGEX:
          regex = Pattern.compile(location);
          break;
        default:
          break;
      }
    }
  }

  public RequestProcessingConfig getConfig() {
    return config;
  }

  public void setConfig(RequestProcessingConfig config) {
    this.config = config;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Pattern getRegex() {
    return regex;
  }

  public void setRegex(Pattern regex) {
    this.regex = regex;
  }

  public RequestProcessingLocationType getType() {
    return type;
  }

  public void setType(RequestProcessingLocationType type) {
    this.type = type;
  }

}
