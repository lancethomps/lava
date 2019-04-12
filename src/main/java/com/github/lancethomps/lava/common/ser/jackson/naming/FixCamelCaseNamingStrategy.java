package com.github.lancethomps.lava.common.ser.jackson.naming;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.PropertyNamingStrategyBase;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.github.lancethomps.lava.common.string.StringUtil;

public class FixCamelCaseNamingStrategy extends PropertyNamingStrategyBase {

  private static final long serialVersionUID = 7060943266369044926L;

  @Override
  public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
    return translate(defaultName);
  }

  @Override
  public String translate(String propertyName) {
    return StringUtil.fixCamelCase(propertyName);
  }

}
