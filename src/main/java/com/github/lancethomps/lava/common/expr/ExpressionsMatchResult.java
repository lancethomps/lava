package com.github.lancethomps.lava.common.expr;

import java.util.ArrayList;
import java.util.List;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.github.lancethomps.lava.common.ser.OutputExpression;

public class ExpressionsMatchResult extends SimpleDomainObject {

  private static final long serialVersionUID = 1L;

  private List<String> errors;

  private Boolean matched;

  private OutputExpression matchedExpression;

  public ExpressionsMatchResult addError(String error) {
    if (error != null) {
      if (errors == null) {
        errors = new ArrayList<>();
      }
      errors.add(error);
    }
    return this;
  }

  public List<String> getErrors() {
    return errors;
  }

  public ExpressionsMatchResult setErrors(List<String> errors) {
    this.errors = errors;
    return this;
  }

  public Boolean getMatched() {
    return matched;
  }

  public ExpressionsMatchResult setMatched(Boolean matched) {
    this.matched = matched;
    return this;
  }

  public OutputExpression getMatchedExpression() {
    return matchedExpression;
  }

  public ExpressionsMatchResult setMatchedExpression(OutputExpression matchedExpression) {
    this.matchedExpression = matchedExpression;
    return this;
  }

  public boolean hasErrors() {
    return Checks.isNotEmpty(errors);
  }

  public boolean testMatched() {
    return (matched != null) && matched.booleanValue();
  }

  public boolean testMatchedOrHasErrors() {
    return testMatched() || hasErrors();
  }

}
