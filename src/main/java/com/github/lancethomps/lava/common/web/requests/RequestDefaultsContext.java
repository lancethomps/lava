package com.github.lancethomps.lava.common.web.requests;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.expr.ExprContextRootWithResult;
import com.github.lancethomps.lava.common.web.WebRequestContext;

/**
 * The Class RequestDefaultsContext.
 *
 * @author lathomps
 */
public class RequestDefaultsContext extends ExprContextRootWithResult {

	/** The context. */
	private WebRequestContext context;

	/** The parameters. */
	private Map<String, String[]> parameters;

	/** The request. */
	private HttpServletRequest request;

	/** The user. */
	private Object user;

	/**
	 * Instantiates a new request defaults context.
	 */
	public RequestDefaultsContext() {
		this(null, null, null);
	}

	/**
	 * Instantiates a new request defaults context.
	 *
	 * @param context the context
	 * @param request the request
	 * @param user the user
	 */
	public RequestDefaultsContext(WebRequestContext context, HttpServletRequest request, Object user) {
		this(context, request, user, request == null ? null : request.getParameterMap());
	}

	/**
	 * Instantiates a new request defaults context.
	 *
	 * @param context the context
	 * @param request the request
	 * @param user the user
	 * @param parameters the parameters
	 */
	public RequestDefaultsContext(WebRequestContext context, HttpServletRequest request, Object user, Map<String, String[]> parameters) {
		super();
		this.context = context;
		this.request = request;
		this.user = user;
		this.parameters = parameters;
	}

	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	public WebRequestContext getContext() {
		return context;
	}

	/**
	 * Gets the parameter.
	 *
	 * @param name the name
	 * @return the parameter
	 */
	public String getParameter(String name) {
		if (parameters != null) {
			return Optional.ofNullable(parameters.get(name)).filter(Checks::isNotEmpty).map(vals -> vals[0]).orElse(null);
		}
		return request == null ? null : request.getParameter(name);
	}

	/**
	 * @return the parameters
	 */
	public Map<String, String[]> getParameters() {
		return parameters;
	}

	/**
	 * Gets the request.
	 *
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * Gets the user.
	 *
	 * @return the user
	 */
	public Object getUser() {
		return user;
	}

	/**
	 * Sets the context.
	 *
	 * @param context the context to set
	 * @return the request defaults context
	 */
	public RequestDefaultsContext setContext(WebRequestContext context) {
		this.context = context;
		return this;
	}

	/**
	 * Sets the parameters.
	 *
	 * @param parameters the parameters to set
	 * @return the request defaults context
	 */
	public RequestDefaultsContext setParameters(Map<String, String[]> parameters) {
		this.parameters = parameters;
		return this;
	}

	/**
	 * Sets the request.
	 *
	 * @param request the request to set
	 * @return the request defaults context
	 */
	public RequestDefaultsContext setRequest(HttpServletRequest request) {
		this.request = request;
		return this;
	}

	/**
	 * Sets the user.
	 *
	 * @param user the user to set
	 * @return the request defaults context
	 */
	public RequestDefaultsContext setUser(Object user) {
		this.user = user;
		return this;
	}

}
