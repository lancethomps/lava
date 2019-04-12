package com.github.lancethomps.lava.common.web.ua;

public class DeviceParserRegex extends AbstractUserAgentParserRegex {

  private String brandReplacement;

  private String deviceReplacement;

  private String modelReplacement;

  public String getBrandReplacement() {
    return brandReplacement;
  }

  public void setBrandReplacement(String brandReplacement) {
    this.brandReplacement = brandReplacement;
  }

  public String getDeviceReplacement() {
    return deviceReplacement;
  }

  public void setDeviceReplacement(String deviceReplacement) {
    this.deviceReplacement = deviceReplacement;
  }

  public String getModelReplacement() {
    return modelReplacement;
  }

  public void setModelReplacement(String modelReplacement) {
    this.modelReplacement = modelReplacement;
  }

}
