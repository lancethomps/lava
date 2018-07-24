package com.github.lancethomps.lava.common.web;

import javax.servlet.http.HttpServletRequest;

import com.github.lancethomps.lava.common.web.throttle.RequestThrottleContext;

/**
 * The Interface RequestThrottleContextInitializer.
 *
 * @author lathomps
 */
public interface RequestThrottleContextInitializer {

	/**
	 * Gets the request context.
	 *
	 * @param request the request
	 * @return the request context
	 */
	default RequestThrottleContext getRequestContext(HttpServletRequest request) {
		return WebRequestContext.getRequestContext(request);
	}

	/**
	 * Gets the user id.
	 *
	 * @param context the context
	 * @return the user id
	 */
	String getUserId(RequestThrottleContext context);
}
