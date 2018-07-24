package com.github.lancethomps.lava.common.logging;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;
import com.google.common.collect.Lists;

/**
 * The Class Log4jHandler.
 */
public class Log4jHandler extends Handler {

	/** The actually log. */
	private boolean actuallyLog = true;

	/** The all messages. */
	private List<String> allMessages;

	/** The formatter. */
	private final SimpleFormatter formatter = new SimpleFormatter();

	/** The keep all messages. */
	private boolean keepAllMessages;

	/**
	 * Instantiates a new log4j handler.
	 */
	public Log4jHandler() {
		this(false);
	}

	/**
	 * Instantiates a new log 4 j handler.
	 *
	 * @param keepAllMessages the keep all messages
	 */
	public Log4jHandler(boolean keepAllMessages) {
		this(keepAllMessages, true);
	}

	/**
	 * Instantiates a new log4j handler.
	 *
	 * @param keepAllMessages the keep all messages
	 * @param actuallyLog the actually log
	 */
	public Log4jHandler(boolean keepAllMessages, boolean actuallyLog) {
		this.keepAllMessages = keepAllMessages;
		this.actuallyLog = actuallyLog;
		if (keepAllMessages) {
			allMessages = Lists.newArrayList();
		}
	}

	@Override
	public void close() throws SecurityException {
		;
	}

	@Override
	public void flush() {
		;
	}

	/**
	 * @return the allMessages
	 */
	public List<String> getAllMessages() {
		keepAllMessages = false;
		return allMessages;
	}

	@Override
	public void publish(LogRecord record) {
		Logger log = Logger.getLogger(Checks.defaultIfNull(Checks.defaultIfNull(record.getLoggerName(), record.getSourceClassName()), getClass().getName()));
		Level level = record.getLevel();
		String message = formatter.formatMessage(record);

		if (actuallyLog) {
			if (Level.SEVERE.equals(level)) {
				Logs.logError(log, record.getThrown(), message);
			} else if (Level.WARNING.equals(level)) {
				Logs.logWarn(log, record.getThrown(), message);
			} else if (Level.INFO.equals(level)) {
				Logs.logInfo(log, record.getThrown(), message);
			} else if (Level.CONFIG.equals(level) || Level.FINE.equals(level)) {
				Logs.logDebug(log, record.getThrown(), message);
			} else if (Level.FINER.equals(level) || Level.FINEST.equals(level)) {
				Logs.logTrace(log, record.getThrown(), message);
			} else {
				Logs.logInfo(log, record.getThrown(), message);
			}
		}
		if (keepAllMessages) {
			synchronized (allMessages) {
				allMessages.add(message);
			}
		}
	}

}
