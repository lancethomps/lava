package com.lancethomps.lava.common;

import java.util.function.Consumer;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class FailOnLogErrorAppender extends AppenderSkeleton {

  private static FailOnLogErrorAppender attached;

  private final Consumer<LoggingEvent> consumer;

  public FailOnLogErrorAppender() {
    this(null);
  }

  public FailOnLogErrorAppender(Consumer<LoggingEvent> consumer) {
    super();
    this.consumer = consumer;
  }

  public static synchronized void attach() {
    attach(null);
  }

  public static synchronized void attach(Consumer<LoggingEvent> consumer) {
    assert attached == null;
    attached = new FailOnLogErrorAppender(consumer);
    Logger.getRootLogger().addAppender(attached);
  }

  public static synchronized void detach() {
    assert attached != null;
    Logger.getRootLogger().removeAppender(attached);
  }

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

  @Override
  public void close() {
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

}
