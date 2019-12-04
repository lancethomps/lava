package com.lancethomps.lava.common.web.config;

import java.util.List;
import java.util.regex.Pattern;

import com.lancethomps.lava.common.SimpleDomainObject;
import com.lancethomps.lava.common.expr.ExprFactory;
import com.lancethomps.lava.common.ser.OutputExpression;

@SuppressWarnings("serial")
public class RequestProcessingRule extends SimpleDomainObject {

  private List<Pattern> blackList;

  private RequestProcessingConfig config;

  private OutputExpression matchExpression;

  private List<Pattern> whiteList;

  @Override
  public void afterDeserialization() {
    if (matchExpression != null) {
      ExprFactory.compileCreateExpressions(false, false, true, matchExpression);
    }
  }

  public List<Pattern> getBlackList() {
    return blackList;
  }

  public RequestProcessingRule setBlackList(List<Pattern> blackList) {
    this.blackList = blackList;
    return this;
  }

  public RequestProcessingConfig getConfig() {
    return config;
  }

  public RequestProcessingRule setConfig(RequestProcessingConfig config) {
    this.config = config;
    return this;
  }

  public OutputExpression getMatchExpression() {
    return matchExpression;
  }

  public RequestProcessingRule setMatchExpression(OutputExpression matchExpression) {
    this.matchExpression = matchExpression;
    return this;
  }

  public List<Pattern> getWhiteList() {
    return whiteList;
  }

  public RequestProcessingRule setWhiteList(List<Pattern> whiteList) {
    this.whiteList = whiteList;
    return this;
  }

}
