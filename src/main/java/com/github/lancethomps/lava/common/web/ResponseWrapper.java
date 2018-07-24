package com.github.lancethomps.lava.common.web;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * The Class ResponseWrapper.
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

	/** The ignore headers. */
	private Set<String> ignoreHeaders;

	/**
	 * Instantiates a new response wrapper.
	 *
	 * @param response the response
	 */
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

	/**
	 * @return the ignoreHeaders
	 */
	public Set<String> getIgnoreHeaders() {
		return ignoreHeaders;
	}

	/**
	 * Ignore header.
	 *
	 * @param name the name
	 * @return true, if successful
	 */
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

	/**
	 * @param ignoreHeaders the ignoreHeaders to set
	 */
	public void setIgnoreHeaders(Set<String> ignoreHeaders) {
		this.ignoreHeaders = ignoreHeaders;
	}

	@Override
	public void setIntHeader(String name, int value) {
		if (shouldAllowHeader(name)) {
			super.setIntHeader(name, value);
		}
	}

	/**
	 * Should allow header.
	 *
	 * @param name the name
	 * @return true, if successful
	 */
	private boolean shouldAllowHeader(String name) {
		return (ignoreHeaders == null) || !ignoreHeaders.contains(name.toLowerCase());
	}

}
