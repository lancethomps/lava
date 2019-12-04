package com.lancethomps.lava.common.web.ua;

import java.util.regex.Pattern;

import com.lancethomps.lava.common.Patterns;
import com.lancethomps.lava.common.ser.ExternalizableBean;

public abstract class AbstractUserAgentParserRegex extends ExternalizableBean {

  private Pattern parsedRegex;

  private String regex;

  private String regexFlag;

  @Override
  public void afterDeserialization() {
    super.afterDeserialization();
    if (regex != null) {
      parsedRegex = Pattern.compile(regex, Patterns.asOptions(regexFlag));
    }
  }

  public Pattern getParsedRegex() {
    return parsedRegex;
  }

  public void setParsedRegex(Pattern parsedRegex) {
    this.parsedRegex = parsedRegex;
  }

  public String getRegex() {
    return regex;
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }

  public String getRegexFlag() {
    return regexFlag;
  }

  public void setRegexFlag(String regexFlag) {
    this.regexFlag = regexFlag;
  }

}
