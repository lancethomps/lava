package com.github.lancethomps.lava.common.expr;

import java.util.Map;

public abstract class ExprContextRootWithResult {

  private Map<String, Object> result;

  public ExprContextRootWithResult() {
    this(null);
  }

  public ExprContextRootWithResult(Map<String, Object> result) {
    super();
    this.result = result;
  }

  public Map<String, Object> getResult() {
    return result;
  }

  public <T extends ExprContextRootWithResult> T setResult(Map<String, Object> result) {
    this.result = result;
    return (T) this;
  }

}
