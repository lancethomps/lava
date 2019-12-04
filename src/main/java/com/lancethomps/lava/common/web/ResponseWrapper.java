package com.lancethomps.lava.common.web;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ResponseWrapper extends HttpServletResponseWrapper {

  private Set<String> ignoreHeaders;

  public ResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  @Override
  public void addDateHeader(String name, long date) {
    if (shouldAllowHeader(name)) {
      super.addDateHeader(name, date);
    }
  }

  @Override
  public void addHeader(String name, String value) {
    if (shouldAllowHeader(name)) {
      super.addHeader(name, value);
    }
  }

  @Override
  public void addIntHeader(String name, int value) {
    if (shouldAllowHeader(name)) {
      super.addIntHeader(name, value);
    }
  }

  public Set<String> getIgnoreHeaders() {
    return ignoreHeaders;
  }

  public void setIgnoreHeaders(Set<String> ignoreHeaders) {
    this.ignoreHeaders = ignoreHeaders;
  }

  public boolean ignoreHeader(@Nonnull String name) {
    if (ignoreHeaders == null) {
      ignoreHeaders = new HashSet<>();
    }
    return ignoreHeaders.add(name.toLowerCase());
  }

  @Override
  public void setDateHeader(String name, long date) {
    if (shouldAllowHeader(name)) {
      super.setDateHeader(name, date);
    }
  }

  @Override
  public void setHeader(String name, String value) {
    if (shouldAllowHeader(name)) {
      super.setHeader(name, value);
    }
  }

  @Override
  public void setIntHeader(String name, int value) {
    if (shouldAllowHeader(name)) {
      super.setIntHeader(name, value);
    }
  }

  private boolean shouldAllowHeader(String name) {
    return (ignoreHeaders == null) || !ignoreHeaders.contains(name.toLowerCase());
  }

}
