package com.github.lancethomps.lava.common.web;

import java.util.List;

import javax.servlet.ServletException;

import org.owasp.esapi.errors.ValidationException;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.format.Formatting;

/**
 * The Class ValidationErrorsException.
 *
 * @author lathomps
 */
public class ValidationErrorsException extends ServletException {

	/** The Constant REQUEST_ATTRIBUTE. */
	public static final String REQUEST_ATTRIBUTE = "_zzValidationErrorsException";

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The validation errors. */
	private List<ValidationException> validationErrors;

	/**
	 * Instantiates a new validation errors exception.
	 *
	 * @param validationErrors the validation errors
	 */
	public ValidationErrorsException(List<ValidationException> validationErrors) {
		this(null, null, validationErrors);
	}

	/**
	 * Instantiates a new validation errors exception.
	 *
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public ValidationErrorsException(String message, Object... formatArgs) {
		this(Formatting.getMessage(message, formatArgs), (Throwable) null);
	}

	/**
	 * Instantiates a new validation errors exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public ValidationErrorsException(String message, Throwable cause) {
		this(message, cause, null);
	}

	/**
	 * Instantiates a new validation errors exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param validationErrors the validation errors
	 */
	public ValidationErrorsException(String message, Throwable cause, List<ValidationException> validationErrors) {
		super(message, cause);
		this.validationErrors = validationErrors;
	}

	/**
	 * Instantiates a new validation errors exception.
	 *
	 * @param cause the cause
	 */
	public ValidationErrorsException(Throwable cause) {
		this(null, cause);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return super.getMessage() + (Checks.isEmpty(validationErrors) ? "" : validationErrors.stream().map(err -> err.getLogMessage()).reduce(String::concat).orElse(""));
	}

	/**
	 * Gets the user message.
	 *
	 * @return the user message
	 */
	public String getUserMessage() {
		return Checks.isEmpty(validationErrors) ? "" : validationErrors.stream().map(err -> err.getMessage()).reduce(String::concat).orElse("");
	}

	/**
	 * Gets the validation errors.
	 *
	 * @return the validationErrors
	 */
	public List<ValidationException> getValidationErrors() {
		return validationErrors;
	}

	/**
	 * Sets the validation errors.
	 *
	 * @param validationErrors the validationErrors to set
	 */
	public void setValidationErrors(List<ValidationException> validationErrors) {
		this.validationErrors = validationErrors;
	}

}
