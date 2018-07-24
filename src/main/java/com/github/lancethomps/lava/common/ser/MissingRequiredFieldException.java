package com.github.lancethomps.lava.common.ser;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.format.Formatting;

/**
 * The Class MissingRequiredFieldException.
 */
public class MissingRequiredFieldException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The field name. */
	private final String fieldName;

	/**
	 * Instantiates a new missing required field exception.
	 */
	public MissingRequiredFieldException() {
		this(null);
	}

	/**
	 * Instantiates a new missing required field exception.
	 *
	 * @param fieldName the field name
	 */
	public MissingRequiredFieldException(String fieldName) {
		this(null, fieldName);
	}

	/**
	 * Instantiates a new missing required field exception.
	 *
	 * @param cause the cause
	 * @param fieldName the field name
	 */
	public MissingRequiredFieldException(Throwable cause, String fieldName) {
		this(cause, fieldName, null);
	}

	/**
	 * Instantiates a new missing required field exception.
	 *
	 * @param cause the cause
	 * @param fieldName the field name
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public MissingRequiredFieldException(Throwable cause, String fieldName, String message, Object... formatArgs) {
		super(Formatting.getMessage(message, formatArgs), cause);
		this.fieldName = fieldName;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return (fieldName == null ? "" : String.format("The `%s` field is required.", fieldName)) + Checks.defaultIfNull(super.getMessage(), "");
	}
}
