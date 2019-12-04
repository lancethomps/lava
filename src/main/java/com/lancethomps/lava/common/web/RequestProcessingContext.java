package com.lancethomps.lava.common.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestProcessingContext {

  private WebRequestContext context;

  private HttpServletRequest request;

  private HttpServletResponse response;

  private Object user;

  private Boolean valid;

  public RequestProcessingContext() {
    this(null, null, null, null);
  }

  public RequestProcessingContext(HttpServletRequest request, HttpServletResponse response, WebRequestContext context, Object user) {
    super();
    this.request = request;
    this.response = response;
    this.context = context;
    this.user = user;
  }

  public WebRequestContext getContext() {
    return context;
  }

  public void setContext(WebRequestContext context) {
    this.context = context;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public void setResponse(HttpServletResponse response) {
    this.response = response;
  }

  public Object getUser() {
    return user;
  }

  public void setUser(Object user) {
    this.user = user;
  }

  public Boolean getValid() {
    return valid;
  }

  public RequestProcessingContext setValid(Boolean valid) {
    this.valid = valid;
    return this;
  }

  public boolean testValid() {
    return (valid == null) || valid.booleanValue();
  }

}
