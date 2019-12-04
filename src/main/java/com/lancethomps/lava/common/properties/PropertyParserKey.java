package com.lancethomps.lava.common.properties;

public class PropertyParserKey {

  private String bundle;

  private String defaultValue;

  private String propKey;

  private String type;

  public String getBundle() {
    return bundle;
  }

  public PropertyParserKey setBundle(String bundle) {
    this.bundle = bundle;
    return this;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public PropertyParserKey setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  public String getPropKey() {
    return propKey;
  }

  public PropertyParserKey setPropKey(String propKey) {
    this.propKey = propKey;
    return this;
  }

  public String getType() {
    return type;
  }

  public PropertyParserKey setType(String type) {
    this.type = type;
    return this;
  }

}
