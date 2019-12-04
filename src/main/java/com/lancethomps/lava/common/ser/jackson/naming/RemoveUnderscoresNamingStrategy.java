package com.lancethomps.lava.common.ser.jackson.naming;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.PropertyNamingStrategyBase;

public class RemoveUnderscoresNamingStrategy extends PropertyNamingStrategyBase {

  private static final long serialVersionUID = 9151296712740028938L;

  @Override
  public String translate(String propertyName) {
    return StringUtils.remove(propertyName, '_');
  }

}
