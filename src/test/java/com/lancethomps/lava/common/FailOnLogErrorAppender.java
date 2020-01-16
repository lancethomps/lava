package com.lancethomps.lava.common;

import java.util.function.Consumer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.google.common.base.Preconditions;
import com.lancethomps.lava.common.logging.Logs;

public class FailOnLogErrorAppender extends AbstractAppender {

  private static FailOnLogErrorAppender attached;
  private final Consumer<LogEvent> consumer;

  public FailOnLogErrorAppender() {
    this(null);
  }

  public FailOnLogErrorAppender(Consumer<LogEvent> consumer) {
    super("FailOnLogError", null, PatternLayout.createDefaultLayout(), true, Property.EMPTY_ARRAY);
    this.consumer = consumer;
  }

  public static synchronized void attach() {
    attach(null);
  }

  public static synchronized void attach(Consumer<LogEvent> consumer) {
    Preconditions.checkArgument(attached == null);
    attached = new FailOnLogErrorAppender(consumer);
    LoggerContext context = Logs.getLoggerContext();
    context.getConfiguration().addAppender(attached);
    context.getConfiguration().getRootLogger().addAppender(attached, null, null);
    context.updateLoggers();
  }

  public static synchronized void detach() {
    Preconditions.checkArgument(attached != null);
    LoggerContext context = Logs.getLoggerContext();
    context.getConfiguration().getRootLogger().removeAppender(attached.getName());
  }

  @Override
  public void append(LogEvent event) {
    if (event.getLevel() == Level.ERROR) {
      if (consumer != null) {
        consumer.accept(event);
      } else {
        throw new AssertionError("logged an error: " + event.getMessage());
      }
    }
  }

}
