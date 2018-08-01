package com.github.lancethomps.lava.common.logging;

import static java.lang.Thread.currentThread;
import static java.time.ZoneId.SHORT_IDS;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.log4j.Logger;

/**
 * The Class SavedErrorMessage.
 *
 * @author lancethomps
 */
public class SavedErrorMessage {

	/** The default time zone. */
	private static ZoneId defaultTimeZone = ZonedDateTime.now(ZoneId.of(SHORT_IDS.get("PST"))).getZone();

	/** The class name. */
	private String className;

	/** The e. */
	private Throwable e;

	/** The logger. */
	private String logger;

	/** The message. */
	private String message;

	/** The severity. */
	private SavedErrorMessageSeverity severity;

	/** The thread. */
	private String thread;

	/** The time. */
	private ZonedDateTime time;

	/**
	 * Instantiates a new saved error message.
	 */
	public SavedErrorMessage() {
		super();
	}

	/**
	 * Instantiates a new saved error message.
	 *
	 * @param severity the severity
	 * @param e the e
	 * @param message the message
	 * @param logger the logger
	 * @param className the class name
	 * @param thread the thread
	 */
	public SavedErrorMessage(SavedErrorMessageSeverity severity, Throwable e, String message, String logger, String className, String thread) {
		super();
		this.severity = severity;
		this.e = e;
		this.message = message;
		this.logger = logger;
		this.className = className;
		this.thread = thread;
		time = ZonedDateTime.now(defaultTimeZone);
	}

	/**
	 * Instantiates a new saved error message.
	 *
	 * @param e the e
	 * @param message the message
	 * @param logger the logger
	 */
	public SavedErrorMessage(Throwable e, String message, Logger logger) {
		this(null, e, message, substringAfterLast(logger.getName(), "."), logger.getName(), currentThread().getName());
	}

	/**
	 * Instantiates a new saved error message.
	 *
	 * @param e the e
	 * @param message the message
	 * @param logger the logger
	 */
	public SavedErrorMessage(Throwable e, String message, String logger) {
		this(null, e, message, logger, null, currentThread().getName());
	}

	/**
	 * @return the defaultTimeZone
	 */
	public static ZoneId getDefaultTimeZone() {
		return defaultTimeZone;
	}

	/**
	 * @param defaultTimeZone the defaultTimeZone to set
	 */
	public static void setDefaultTimeZone(ZoneId defaultTimeZone) {
		SavedErrorMessage.defaultTimeZone = defaultTimeZone;
	}

	/**
	 * Gets the class name.
	 *
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Gets the e.
	 *
	 * @return the e
	 */
	public Throwable getE() {
		return e;
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	public String getLogger() {
		return logger;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Gets the severity.
	 *
	 * @return the severity
	 */
	public SavedErrorMessageSeverity getSeverity() {
		return severity;
	}

	/**
	 * Gets the thread.
	 *
	 * @return the thread
	 */
	public String getThread() {
		return thread;
	}

	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public ZonedDateTime getTime() {
		return time;
	}

	/**
	 * Sets the class name.
	 *
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Sets the e.
	 *
	 * @param e the e to set
	 */
	public void setE(Throwable e) {
		this.e = e;
	}

	/**
	 * Sets the logger.
	 *
	 * @param logger the logger to set
	 */
	public void setLogger(String logger) {
		this.logger = logger;
	}

	/**
	 * Sets the message.
	 *
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Sets the severity.
	 *
	 * @param severity the severity to set
	 */
	public void setSeverity(SavedErrorMessageSeverity severity) {
		this.severity = severity;
	}

	/**
	 * Sets the thread.
	 *
	 * @param thread the thread to set
	 */
	public void setThread(String thread) {
		this.thread = thread;
	}

	/**
	 * Sets the time.
	 *
	 * @param time the time to set
	 */
	public void setTime(ZonedDateTime time) {
		this.time = time;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
			"className=%s | logger=%s | thread=%s | errMessage=%s | message=%s | errClassName=%s",
			className,
			logger,
			thread,
			e == null ? null : e.getMessage(),
			message,
			e == null ? null : e.getClass().getName()
		);
	}
}
