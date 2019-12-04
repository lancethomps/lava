package com.lancethomps.lava.common.web.config;

import com.lancethomps.lava.common.Enums;

public enum RequestProcessingLocationType {

  CASE_INSENSITIVE_REGEX("~*", true),

  CASE_SENSITIVE_REGEX("~", true),

  EXACT_MATCH("=", false),

  PREFIX_MATCH("", false),

  PREFIX_MATCH_SKIP_REGEX("^~", false);

  static {
    Enums.createStringToTypeMap(
      RequestProcessingLocationType.class,
      RequestProcessingLocationType.PREFIX_MATCH,
      RequestProcessingLocationType::getValue
    );
  }

  private final boolean regex;
  private final String value;

  RequestProcessingLocationType(String value, boolean regex) {
    this.value = value;
    this.regex = regex;
  }

  public static RequestProcessingLocationType fromString(String val) {
    return Enums.fromString(RequestProcessingLocationType.class, val);
  }

  public String getValue() {
    return value;
  }

  public boolean isRegex() {
    return regex;
  }
}
