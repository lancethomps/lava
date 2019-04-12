package com.github.lancethomps.lava.common.expr;

import java.util.List;

import com.github.lancethomps.lava.common.merge.MergeConfig;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.github.lancethomps.lava.common.ser.OutputExpression;

public class ExpressionsDataMapping<T> extends ExternalizableBean {

  private String id;
  private Boolean breakAfterMatch;
  private List<OutputExpression> expressions;
  private T mapping;

  private MergeConfig mergeConfig;

  private List<OutputExpression> outputExpressions;

  @Override
  public void afterDeserialization() {
    if (expressions != null) {
      ExprFactory.compileCreateExpressions(expressions, false, false);
    }
    if (outputExpressions != null) {
      ExprFactory.compileCreateExpressions(outputExpressions, false, false);
    }
  }

  public Boolean getBreakAfterMatch() {
    return breakAfterMatch;
  }

  public ExpressionsDataMapping<T> setBreakAfterMatch(Boolean breakAfterMatch) {
    this.breakAfterMatch = breakAfterMatch;
    return this;
  }

  public List<OutputExpression> getExpressions() {
    return expressions;
  }

  public ExpressionsDataMapping<T> setExpressions(List<OutputExpression> expressions) {
    this.expressions = expressions;
    return this;
  }

  public String getId() {
    return id;
  }

  public ExpressionsDataMapping<T> setId(String id) {
    this.id = id;
    return this;
  }

  public T getMapping() {
    return mapping;
  }

  public ExpressionsDataMapping<T> setMapping(T mapping) {
    this.mapping = mapping;
    return this;
  }

  public MergeConfig getMergeConfig() {
    return mergeConfig;
  }

  public ExpressionsDataMapping<T> setMergeConfig(MergeConfig mergeConfig) {
    this.mergeConfig = mergeConfig;
    return this;
  }

  public List<OutputExpression> getOutputExpressions() {
    return outputExpressions;
  }

  public ExpressionsDataMapping<T> setOutputExpressions(List<OutputExpression> outputExpressions) {
    this.outputExpressions = outputExpressions;
    return this;
  }

  public boolean testBreakAfterMatch() {
    return (breakAfterMatch != null) && breakAfterMatch.booleanValue();
  }

}
