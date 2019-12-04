package com.lancethomps.lava.common.ser;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lancethomps.lava.common.SimpleDomainObject;
import com.lancethomps.lava.common.expr.ExprParser;

@SuppressWarnings("serial")
public class OutputExpression extends SimpleDomainObject {

  private Boolean compile;

  @JsonIgnore
  private Object compiledExpression;

  private String description;

  private String expression;

  private List<OutputExpression> globalVariables;

  @JsonIgnore
  private Map<String, Object> globalVariablesResolved;

  private String path;

  private Boolean returnsPathKeyMap;

  private ExprParser type;

  public Boolean getCompile() {
    return compile;
  }

  public OutputExpression setCompile(Boolean compile) {
    this.compile = compile;
    return this;
  }

  public Object getCompiledExpression() {
    return compiledExpression;
  }

  public OutputExpression setCompiledExpression(Object compiledExpression) {
    this.compiledExpression = compiledExpression;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public OutputExpression setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getExpression() {
    return expression;
  }

  public OutputExpression setExpression(String expression) {
    this.expression = expression;
    return this;
  }

  public List<OutputExpression> getGlobalVariables() {
    return globalVariables;
  }

  public OutputExpression setGlobalVariables(List<OutputExpression> globalVariables) {
    this.globalVariables = globalVariables;
    return this;
  }

  public Map<String, Object> getGlobalVariablesResolved() {
    return globalVariablesResolved;
  }

  public OutputExpression setGlobalVariablesResolved(Map<String, Object> globalVariablesResolved) {
    this.globalVariablesResolved = globalVariablesResolved;
    return this;
  }

  public String getPath() {
    return path;
  }

  public OutputExpression setPath(String path) {
    this.path = path;
    return this;
  }

  public Boolean getReturnsPathKeyMap() {
    return returnsPathKeyMap;
  }

  public OutputExpression setReturnsPathKeyMap(Boolean returnsPathKeyMap) {
    this.returnsPathKeyMap = returnsPathKeyMap;
    return this;
  }

  public ExprParser getType() {
    return type;
  }

  public OutputExpression setType(ExprParser type) {
    this.type = type;
    return this;
  }

  public boolean testCompile() {
    return (compile != null) && compile.booleanValue();
  }

  public boolean testReturnsPathKeyMap() {
    return (returnsPathKeyMap != null) && returnsPathKeyMap.booleanValue();
  }

}
