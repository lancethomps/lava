package com.lancethomps.lava.common.logging;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.apache.log4j.Logger;

import com.lancethomps.lava.common.Checks;
import com.google.common.collect.Lists;

public class Log4jHandler extends Handler {

  private final SimpleFormatter formatter = new SimpleFormatter();
  private boolean actuallyLog = true;
  private List<String> allMessages;
  private boolean keepAllMessages;

  public Log4jHandler() {
    this(false);
  }

  public Log4jHandler(boolean keepAllMessages) {
    this(keepAllMessages, true);
  }

  public Log4jHandler(boolean keepAllMessages, boolean actuallyLog) {
    this.keepAllMessages = keepAllMessages;
    this.actuallyLog = actuallyLog;
    if (keepAllMessages) {
      allMessages = Lists.newArrayList();
    }
  }

  @Override
  public void close() throws SecurityException {
  }

  @Override
  public void flush() {
  }

  public List<String> getAllMessages() {
    keepAllMessages = false;
    return allMessages;
  }

  @Override
  public void publish(LogRecord record) {
    Logger log =
      Logger.getLogger(Checks.defaultIfNull(Checks.defaultIfNull(record.getLoggerName(), record.getSourceClassName()), getClass().getName()));
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
