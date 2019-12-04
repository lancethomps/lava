package com.lancethomps.lava.common.web.requests.parsers;

import java.time.LocalDateTime;
import java.util.List;

public class MockRequestWithAllowedJsonParam {

  @RequestField
  private LocalDateTime fromDate;

  @RequestField
  private List<String> groupByFields;

  @RequestField
  private String requestKey;

  @RequestField
  private List<String> responseFields;

  @RequestField
  private LocalDateTime toDate;

  public LocalDateTime getFromDate() {
    return fromDate;
  }

  public MockRequestWithAllowedJsonParam setFromDate(LocalDateTime fromDate) {
    this.fromDate = fromDate;
    return this;
  }

  public List<String> getGroupByFields() {
    return groupByFields;
  }

  public MockRequestWithAllowedJsonParam setGroupByFields(List<String> groupByFields) {
    this.groupByFields = groupByFields;
    return this;
  }

  public String getRequestKey() {
    return requestKey;
  }

  public MockRequestWithAllowedJsonParam setRequestKey(String requestKey) {
    this.requestKey = requestKey;
    return this;
  }

  public List<String> getResponseFields() {
    return responseFields;
  }

  public MockRequestWithAllowedJsonParam setResponseFields(List<String> responseFields) {
    this.responseFields = responseFields;
    return this;
  }

  public LocalDateTime getToDate() {
    return toDate;
  }

  public MockRequestWithAllowedJsonParam setToDate(LocalDateTime toDate) {
    this.toDate = toDate;
    return this;
  }

}
