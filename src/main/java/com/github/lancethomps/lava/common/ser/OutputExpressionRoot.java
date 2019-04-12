package com.github.lancethomps.lava.common.ser;

import java.util.Map;

import com.github.lancethomps.lava.common.expr.ExprContextRootWithResult;

public class OutputExpressionRoot extends ExprContextRootWithResult {

  private Object context;

  private Object data;

  public OutputExpressionRoot() {
    this(null, null);
  }

  public OutputExpressionRoot(Object data, Map<String, Object> result) {
    this(data, result, null);
  }

  public OutputExpressionRoot(Object data, Map<String, Object> result, Object context) {
    super(result);
    this.data = data;
    this.context = context;
  }

  public Object getContext() {
    return context;
  }

  public OutputExpressionRoot setContext(Object context) {
    this.context = context;
    return this;
  }

  public Object getData() {
    return data;
  }

  public OutputExpressionRoot setData(Object data) {
    this.data = data;
    return this;
  }

}
