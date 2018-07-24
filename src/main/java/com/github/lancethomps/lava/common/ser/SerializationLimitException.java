package com.github.lancethomps.lava.common.ser;

import com.github.lancethomps.lava.common.ser.jackson.SerializationException;

/**
 * The Class SerializationLimitException.
 */
public class SerializationLimitException extends SerializationException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7693589741611316819L;

	/** The limit. */
	private Long limit;

	/**
	 * Instantiates a new serialization limit exception.
	 */
	public SerializationLimitException() {
		this(null);
	}

	/**
	 * Instantiates a new serialization limit exception.
	 *
	 * @param limit the limit
	 */
	public SerializationLimitException(Long limit) {
		this(limit, null);
	}

	/**
	 * Instantiates a new serialization limit exception.
	 *
	 * @param limit the limit
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public SerializationLimitException(Long limit, String message, Object... formatArgs) {
		this(limit, null, message, formatArgs);
	}

	/**
	 * Instantiates a new serialization limit exception.
	 *
	 * @param limit the limit
	 * @param cause the cause
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public SerializationLimitException(Long limit, Throwable cause, String message, Object... formatArgs) {
		super(cause, message, formatArgs);
		this.limit = limit;
	}

	/**
	 * Gets the limit.
	 *
	 * @return the limit
	 */
	public Long getLimit() {
		return limit;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return String.format("Serialization limit exceeded (%,d). ", limit) + super.getMessage();
	}

	/**
	 * Sets the limit.
	 *
	 * @param limit the limit to set
	 * @return the serialization limit exception
	 */
	public SerializationLimitException setLimit(Long limit) {
		this.limit = limit;
		return this;
	}

}
