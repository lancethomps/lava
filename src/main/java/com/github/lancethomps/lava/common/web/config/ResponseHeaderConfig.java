package com.github.lancethomps.lava.common.web.config;

import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.ser.OutputExpression;

@SuppressWarnings("serial")
public class ResponseHeaderConfig extends SimpleDomainObject {

  private String name;

  private String value;

  private OutputExpression valueExpression;

  @Override
  public void afterDeserialization() {
    if (valueExpression != null) {
      ExprFactory.compileCreateExpressions(false, false, true, valueExpression);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public OutputExpression getValueExpression() {
    return valueExpression;
  }

  public void setValueExpression(OutputExpression valueExpression) {
    this.valueExpression = valueExpression;
  }

}
