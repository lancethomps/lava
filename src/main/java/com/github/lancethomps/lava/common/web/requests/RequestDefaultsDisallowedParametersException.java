package com.github.lancethomps.lava.common.web.requests;

import java.util.List;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.format.Formatting;

/**
 * The Class RequestDefaultsDisallowedParametersException.
 */
public class RequestDefaultsDisallowedParametersException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7015802387098897856L;

	/** The additional message. */
	private final String additionalMessage;

	/** The parameters. */
	private final List<String> parameters;

	/**
	 * Instantiates a new request defaults disallowed parameters exception.
	 */
	public RequestDefaultsDisallowedParametersException() {
		this(null);
	}

	/**
	 * Instantiates a new request defaults disallowed parameters exception.
	 *
	 * @param parameters the parameters
	 */
	public RequestDefaultsDisallowedParametersException(List<String> parameters) {
		this(parameters, null);
	}

	/**
	 * Instantiates a new request defaults disallowed parameters exception.
	 *
	 * @param parameters the parameters
	 * @param additionalMessage the additional message
	 * @param formatArgs the format args
	 */
	public RequestDefaultsDisallowedParametersException(List<String> parameters, String additionalMessage, Object... formatArgs) {
		super(additionalMessage == null ? "" : Formatting.getMessage(additionalMessage, formatArgs));
		this.additionalMessage = Formatting.getMessage(additionalMessage, formatArgs);
		this.parameters = parameters;
	}

	/**
	 * Instantiates a new request defaults disallowed parameters exception.
	 *
	 * @param additionalMessage the additional message
	 * @param formatArgs the format args
	 */
	public RequestDefaultsDisallowedParametersException(String additionalMessage, Object... formatArgs) {
		this(null, additionalMessage, formatArgs);
	}

	/**
	 * @return the additionalMessage
	 */
	public String getAdditionalMessage() {
		return additionalMessage;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return (Checks.isBlank(additionalMessage) ? "" : (additionalMessage + ' ')) + "Found disallowed parameters in request" + ((parameters == null) || parameters.isEmpty() ? '.'
			: (": " + parameters.toString()));
	}

	/**
	 * @return the parameters
	 */
	public List<String> getParameters() {
		return parameters;
	}

}
