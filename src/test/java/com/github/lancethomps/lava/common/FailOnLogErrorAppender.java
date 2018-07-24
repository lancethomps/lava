package com.github.lancethomps.lava.common;

import java.util.function.Consumer;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * The Class FailOnLogErrorAppender.
 */
public class FailOnLogErrorAppender extends AppenderSkeleton {

	/** The attached. */
	private static FailOnLogErrorAppender attached;

	/** The consumer. */
	private final Consumer<LoggingEvent> consumer;

	/**
	 * Instantiates a new fail on log error appender.
	 */
	public FailOnLogErrorAppender() {
		this(null);
	}

	/**
	 * Instantiates a new fail on log error appender.
	 *
	 * @param consumer the consumer
	 */
	public FailOnLogErrorAppender(Consumer<LoggingEvent> consumer) {
		super();
		this.consumer = consumer;
	}

	/**
	 * Attach.
	 */
	public static synchronized void attach() {
		attach(null);
	}

	/**
	 * Attach.
	 *
	 * @param consumer the consumer
	 */
	public static synchronized void attach(Consumer<LoggingEvent> consumer) {
		assert attached == null;
		attached = new FailOnLogErrorAppender(consumer);
		Logger.getRootLogger().addAppender(attached);
	}

	/**
	 * Detach.
	 */
	public static synchronized void detach() {
		assert attached != null;
		Logger.getRootLogger().removeAppender(attached);
	}

	/**
	 * Append.
	 *
	 * @param event the event
	 */
	/*
	 * (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
	 */
	@Override
	public void append(LoggingEvent event) {
		if (event.getLevel() == Level.ERROR) {
			if (consumer != null) {
				consumer.accept(event);
			} else {
				throw new AssertionError("logged an error: " + event.getMessage());
			}
		}
	}

	/**
	 * Close.
	 */
	/*
	 * (non-Javadoc)
	 * @see org.apache.log4j.Appender#close()
	 */
	@Override
	public void close() {
	}

	/**
	 * Requires layout.
	 *
	 * @return true, if successful
	 */
	/*
	 * (non-Javadoc)
	 * @see org.apache.log4j.Appender#requiresLayout()
	 */
	@Override
	public boolean requiresLayout() {
		return false;
	}
}