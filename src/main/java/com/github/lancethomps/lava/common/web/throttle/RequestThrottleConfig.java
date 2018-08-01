package com.github.lancethomps.lava.common.web.throttle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * The Class RequestThrottleConfig.
 *
 * @author lancethomps
 */
public class RequestThrottleConfig {

	/** The black list. */
	private List<Pattern> blackList;

	/** The by user. */
	private boolean byUser = true;

	/** The default max requests. */
	private int defaultMaxRequests = 20;

	/** The max requests by user. */
	private Map<String, Integer> maxRequestsByUser = new HashMap<>();

	/** The white list. */
	private List<Pattern> whiteList;

	/**
	 * Gets the black list.
	 *
	 * @return the blackList
	 */
	public List<Pattern> getBlackList() {
		return blackList;
	}

	/**
	 * Gets the default max requests.
	 *
	 * @return the defaultMaxRequests
	 */
	public int getDefaultMaxRequests() {
		return defaultMaxRequests;
	}

	/**
	 * Gets the max requests by user.
	 *
	 * @return the maxRequestsByUser
	 */
	public Map<String, Integer> getMaxRequestsByUser() {
		return maxRequestsByUser;
	}

	/**
	 * Gets the white list.
	 *
	 * @return the whiteList
	 */
	public List<Pattern> getWhiteList() {
		return whiteList;
	}

	/**
	 * @return the byUser
	 */
	public boolean isByUser() {
		return byUser;
	}

	/**
	 * Sets the black list.
	 *
	 * @param blackList the blackList to set
	 * @return the request throttle config
	 */
	public RequestThrottleConfig setBlackList(List<Pattern> blackList) {
		this.blackList = blackList;
		return this;
	}

	/**
	 * @param byUser the byUser to set
	 */
	public void setByUser(boolean byUser) {
		this.byUser = byUser;
	}

	/**
	 * Sets the default max requests.
	 *
	 * @param defaultMaxRequests the defaultMaxRequests to set
	 * @return the request throttle config
	 */
	public RequestThrottleConfig setDefaultMaxRequests(int defaultMaxRequests) {
		this.defaultMaxRequests = defaultMaxRequests;
		return this;
	}

	/**
	 * Sets the max requests by user.
	 *
	 * @param maxRequestsByUser the maxRequestsByUser to set
	 * @return the request throttle config
	 */
	public RequestThrottleConfig setMaxRequestsByUser(@Nonnull Map<String, Integer> maxRequestsByUser) {
		this.maxRequestsByUser = maxRequestsByUser;
		return this;
	}

	/**
	 * Sets the white list.
	 *
	 * @param whiteList the whiteList to set
	 * @return the request throttle config
	 */
	public RequestThrottleConfig setWhiteList(List<Pattern> whiteList) {
		this.whiteList = whiteList;
		return this;
	}

}
