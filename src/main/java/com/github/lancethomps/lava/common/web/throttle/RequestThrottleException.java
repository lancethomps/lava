package com.github.lancethomps.lava.common.web.throttle;

import static java.lang.String.format;

import com.github.lancethomps.lava.common.format.Formatting;

/**
 * The Class RequestThrottleException.
 */
public class RequestThrottleException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4598261915410167423L;

	/** The max requests. */
	private Integer maxRequests;

	/** The user. */
	private String user;

	/**
	 * Instantiates a new request throttle exception.
	 *
	 * @param message the message
	 */
	public RequestThrottleException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new request throttle exception.
	 *
	 * @param user the user
	 * @param maxRequests the max requests
	 */
	public RequestThrottleException(String user, Integer maxRequests) {
		super(format("Maximum number of requests - [%s] - for user [%s] reached!", maxRequests, user));
		this.user = user;
		this.maxRequests = maxRequests;
	}

	/**
	 * Instantiates a new request throttle exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public RequestThrottleException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new request throttle exception.
	 *
	 * @param cause the cause
	 */
	public RequestThrottleException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new request throttle exception.
	 *
	 * @param cause the cause
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public RequestThrottleException(Throwable cause, String message, Object... formatArgs) {
		super(Formatting.getMessage(message, formatArgs), cause);
	}

	/**
	 * Throw if greater.
	 *
	 * @param openRequests the open requests
	 * @param maxRequests the max requests
	 * @param user the user
	 * @throws RequestThrottleException the request throttle exception
	 */
	public static void throwIfGreater(int openRequests, int maxRequests, String user) throws RequestThrottleException {
		if (openRequests > maxRequests) {
			throw new RequestThrottleException(user, maxRequests);
		}
	}

	/**
	 * Gets the max requests.
	 *
	 * @return the maxRequests
	 */
	public Integer getMaxRequests() {
		return maxRequests;
	}

	/**
	 * Gets the user.
	 *
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the max requests.
	 *
	 * @param maxRequests the maxRequests to set
	 * @return the request throttle exception
	 */
	public RequestThrottleException setMaxRequests(Integer maxRequests) {
		this.maxRequests = maxRequests;
		return this;
	}

	/**
	 * Sets the user.
	 *
	 * @param user the user to set
	 * @return the request throttle exception
	 */
	public RequestThrottleException setUser(String user) {
		this.user = user;
		return this;
	}
}
