package com.lancethomps.lava.common.web.throttle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public class RequestThrottleConfig {

  private List<Pattern> blackList;

  private boolean byUser = true;

  private int defaultMaxRequests = 20;

  private Map<String, Integer> maxRequestsByUser = new HashMap<>();

  private List<Pattern> whiteList;

  public List<Pattern> getBlackList() {
    return blackList;
  }

  public RequestThrottleConfig setBlackList(List<Pattern> blackList) {
    this.blackList = blackList;
    return this;
  }

  public int getDefaultMaxRequests() {
    return defaultMaxRequests;
  }

  public RequestThrottleConfig setDefaultMaxRequests(int defaultMaxRequests) {
    this.defaultMaxRequests = defaultMaxRequests;
    return this;
  }

  public Map<String, Integer> getMaxRequestsByUser() {
    return maxRequestsByUser;
  }

  public RequestThrottleConfig setMaxRequestsByUser(@Nonnull Map<String, Integer> maxRequestsByUser) {
    this.maxRequestsByUser = maxRequestsByUser;
    return this;
  }

  public List<Pattern> getWhiteList() {
    return whiteList;
  }

  public RequestThrottleConfig setWhiteList(List<Pattern> whiteList) {
    this.whiteList = whiteList;
    return this;
  }

  public boolean isByUser() {
    return byUser;
  }

  public void setByUser(boolean byUser) {
    this.byUser = byUser;
  }

}
