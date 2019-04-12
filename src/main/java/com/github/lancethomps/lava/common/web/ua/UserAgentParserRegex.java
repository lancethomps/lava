package com.github.lancethomps.lava.common.web.ua;

public class UserAgentParserRegex extends AbstractUserAgentParserRegex {

  private String familyReplacement;

  private String v1Replacement;

  private String v2Replacement;

  private String v3Replacement;

  public String getFamilyReplacement() {
    return familyReplacement;
  }

  public void setFamilyReplacement(String familyReplacement) {
    this.familyReplacement = familyReplacement;
  }

  public String getV1Replacement() {
    return v1Replacement;
  }

  public void setV1Replacement(String v1Replacement) {
    this.v1Replacement = v1Replacement;
  }

  public String getV2Replacement() {
    return v2Replacement;
  }

  public void setV2Replacement(String v2Replacement) {
    this.v2Replacement = v2Replacement;
  }

  public String getV3Replacement() {
    return v3Replacement;
  }

  public void setV3Replacement(String v3Replacement) {
    this.v3Replacement = v3Replacement;
  }

}
