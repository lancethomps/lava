package com.lancethomps.lava.common.ser.jackson.naming;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.PropertyNamingStrategyBase;

public class CapFirstNamingStrategy extends PropertyNamingStrategyBase {

  private static final long serialVersionUID = 7060943266369044926L;

  @Override
  public String translate(String propertyName) {
    return StringUtils.capitalize(propertyName);
  }

}
