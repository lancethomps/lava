package com.github.lancethomps.lava.common.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class RequestProcessingContext.
 */
public class RequestProcessingContext {

	/** The context. */
	private WebRequestContext context;

	/** The request. */
	private HttpServletRequest request;

	/** The response. */
	private HttpServletResponse response;

	/** The user. */
	private Object user;

	/** The valid. */
	private Boolean valid;

	/**
	 * Instantiates a new request processing context.
	 */
	public RequestProcessingContext() {
		this(null, null, null, null);
	}

	/**
	 * Instantiates a new request processing context.
	 *
	 * @param request the request
	 * @param response the response
	 * @param context the context
	 * @param user the user
	 */
	public RequestProcessingContext(HttpServletRequest request, HttpServletResponse response, WebRequestContext context, Object user) {
		super();
		this.request = request;
		this.response = response;
		this.context = context;
		this.user = user;
	}

	/**
	 * @return the context
	 */
	public WebRequestContext getContext() {
		return context;
	}

	/**
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * @return the response
	 */
	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * @return the user
	 */
	public Object getUser() {
		return user;
	}

	/**
	 * @return the valid
	 */
	public Boolean getValid() {
		return valid;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(WebRequestContext context) {
		this.context = context;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(Object user) {
		this.user = user;
	}

	/**
	 * Sets the valid.
	 *
	 * @param valid the valid to set
	 * @return the request processing context
	 */
	public RequestProcessingContext setValid(Boolean valid) {
		this.valid = valid;
		return this;
	}

	/**
	 * Test valid.
	 *
	 * @return true, if successful
	 */
	public boolean testValid() {
		return (valid == null) || valid.booleanValue();
	}

}
