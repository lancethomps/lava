package com.github.lancethomps.lava.common.web.requests.parsers;

/**
 * The Class RequestValidationException.
 */
public class RequestValidationException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6585970547612832425L;

	/**
	 * Instantiates a new request parsing exception.
	 */
	public RequestValidationException() {
		super();
	}

	/**
	 * Instantiates a new request parsing exception.
	 *
	 * @param message the message
	 */
	public RequestValidationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new request parsing exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public RequestValidationException(String message, Throwable cause) {
		super(message, cause);
	}

}