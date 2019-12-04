package com.lancethomps.lava.common.web.requests;

import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lancethomps.lava.common.ser.ExternalizableBean;
import com.lancethomps.lava.common.string.WordUtil;

public class MissingRequestParameter extends ExternalizableBean {

  private String name;

  private String type;

  public MissingRequestParameter() {
    super();
  }

  public MissingRequestParameter(String name, String type) {
    this();
    this.name = name;
    this.type = type;
  }

  @JsonCreator
  public static MissingRequestParameter fromString(String param) {
    String name = null;
    if (contains(param, ':')) {
      name = trimToNull(substringBefore(param, ":"));
    } else {
      name = param;
    }
    return new MissingRequestParameter(name, trimToNull(substringAfter(param, ":")));
  }

  public String getMessage() {
    String message = "Required " + type + " parameter '" + name + "' is not present";
    if ((name != null) && (WordUtil.getSingularVersionOfWord(name) != null)) {
      String singleParam = WordUtil.getSingularVersionOfWord(name);
      message += " - it may also be specified as a singular version using the '" + singleParam + "' parameter";
    }
    return message;
  }

  public String getName() {
    return name;
  }

  public MissingRequestParameter setName(String name) {
    this.name = name;
    return this;
  }

  public String getType() {
    return type;
  }

  public MissingRequestParameter setType(String type) {
    this.type = type;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder param = new StringBuilder();
    if (getName() != null) {
      param.append(getName());
    }
    if (getType() != null) {
      param.append(':').append(getType());
    }
    return param.toString();
  }

}
