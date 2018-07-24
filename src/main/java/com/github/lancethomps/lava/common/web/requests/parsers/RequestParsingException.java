package com.github.lancethomps.lava.common.web.requests.parsers;

/**
 * The Class RequestParsingException.
 */
public class RequestParsingException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7716936144520697255L;

	/**
	 * Instantiates a new request parsing exception.
	 */
	public RequestParsingException() {
		super();
	}

	/**
	 * Instantiates a new request parsing exception.
	 *
	 * @param message the message
	 */
	public RequestParsingException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new request parsing exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public RequestParsingException(String message, Throwable cause) {
		super(message, cause);
	}

}
