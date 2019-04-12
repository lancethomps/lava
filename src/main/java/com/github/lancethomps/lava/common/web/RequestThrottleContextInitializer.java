package com.github.lancethomps.lava.common.web;

import javax.servlet.http.HttpServletRequest;

import com.github.lancethomps.lava.common.web.throttle.RequestThrottleContext;

public interface RequestThrottleContextInitializer {

  default RequestThrottleContext getRequestContext(HttpServletRequest request) {
    return WebRequestContext.getRequestContext(request);
  }

  String getUserId(RequestThrottleContext context);

}
