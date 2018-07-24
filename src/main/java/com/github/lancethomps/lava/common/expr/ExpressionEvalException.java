package com.github.lancethomps.lava.common.expr;

import com.github.lancethomps.lava.common.format.Formatting;
import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class ExpressionEvalException.
 */
public class ExpressionEvalException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The type. */
	private final ExprParser type;

	/**
	 * Instantiates a new expression eval exception.
	 */
	public ExpressionEvalException() {
		this((ExprParser) null);
	}

	/**
	 * Instantiates a new expression eval exception.
	 *
	 * @param type the type
	 */
	public ExpressionEvalException(ExprParser type) {
		this(type, (String) null);
	}

	/**
	 * Instantiates a new expression eval exception.
	 *
	 * @param type the type
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public ExpressionEvalException(ExprParser type, String message, Object... formatArgs) {
		this(type, (Throwable) null, Formatting.getMessage(message, formatArgs));
	}

	/**
	 * Instantiates a new expression eval exception.
	 *
	 * @param type the type
	 * @param cause the cause
	 */
	public ExpressionEvalException(ExprParser type, Throwable cause) {
		this(type, cause, (String) null);
	}

	/**
	 * Instantiates a new expression eval exception.
	 *
	 * @param type the type
	 * @param cause the cause
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public ExpressionEvalException(ExprParser type, Throwable cause, String message, Object... formatArgs) {
		super(Formatting.getMessage(message, formatArgs), cause);
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		final StringBuilder msg = new StringBuilder(String.format("Expression evaluation error: exprType=%s", type));
		if (super.getMessage() != null) {
			msg.append(' ').append(super.getMessage());
		}
		if (getCause() != null) {
			msg.append(" cause=").append(Logs.getSplunkValueString(getCause().getMessage()));
		}
		return msg.toString();
	}

	/**
	 * @return the type
	 */
	public ExprParser getType() {
		return type;
	}

}
