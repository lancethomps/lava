package com.github.lancethomps.lava.common.web.requests;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.expr.ExprContextRootWithResult;
import com.github.lancethomps.lava.common.web.WebRequestContext;

public class RequestDefaultsContext extends ExprContextRootWithResult {

  private WebRequestContext context;

  private Map<String, String[]> parameters;

  private HttpServletRequest request;

  private Object user;

  public RequestDefaultsContext() {
    this(null, null, null);
  }

  public RequestDefaultsContext(WebRequestContext context, HttpServletRequest request, Object user) {
    this(context, request, user, request == null ? null : request.getParameterMap());
  }

  public RequestDefaultsContext(WebRequestContext context, HttpServletRequest request, Object user, Map<String, String[]> parameters) {
    super();
    this.context = context;
    this.request = request;
    this.user = user;
    this.parameters = parameters;
  }

  public WebRequestContext getContext() {
    return context;
  }

  public RequestDefaultsContext setContext(WebRequestContext context) {
    this.context = context;
    return this;
  }

  public String getParameter(String name) {
    if (parameters != null) {
      return Optional.ofNullable(parameters.get(name)).filter(Checks::isNotEmpty).map(vals -> vals[0]).orElse(null);
    }
    return request == null ? null : request.getParameter(name);
  }

  public Map<String, String[]> getParameters() {
    return parameters;
  }

  public RequestDefaultsContext setParameters(Map<String, String[]> parameters) {
    this.parameters = parameters;
    return this;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public RequestDefaultsContext setRequest(HttpServletRequest request) {
    this.request = request;
    return this;
  }

  public Object getUser() {
    return user;
  }

  public RequestDefaultsContext setUser(Object user) {
    this.user = user;
    return this;
  }

}
