package com.github.lancethomps.lava.common.ser.jackson;

import com.github.lancethomps.lava.common.format.Formatting;

/**
 * The Class SerializationException.
 */
public class SerializationException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2606633865857608126L;

	/**
	 * Instantiates a new serialization exception.
	 */
	public SerializationException() {
		this(null);
	}

	/**
	 * Instantiates a new serialization exception.
	 *
	 * @param message the message
	 */
	public SerializationException(String message) {
		this(null, message);
	}

	/**
	 * Instantiates a new serialization exception.
	 *
	 * @param cause the cause
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public SerializationException(Throwable cause, String message, Object... formatArgs) {
		super(Formatting.getMessage(message, formatArgs), cause);
	}
}
