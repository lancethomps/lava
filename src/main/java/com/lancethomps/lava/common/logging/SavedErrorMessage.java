package com.lancethomps.lava.common.logging;

import static java.lang.Thread.currentThread;
import static java.time.ZoneId.SHORT_IDS;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.log4j.Logger;

public class SavedErrorMessage {

  private static ZoneId defaultTimeZone = ZonedDateTime.now(ZoneId.of(SHORT_IDS.get("PST"))).getZone();

  private String className;

  private Throwable e;

  private String logger;

  private String message;

  private SavedErrorMessageSeverity severity;

  private String thread;

  private ZonedDateTime time;

  public SavedErrorMessage() {
    super();
  }

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

  public SavedErrorMessage(Throwable e, String message, Logger logger) {
    this(null, e, message, substringAfterLast(logger.getName(), "."), logger.getName(), currentThread().getName());
  }

  public SavedErrorMessage(Throwable e, String message, String logger) {
    this(null, e, message, logger, null, currentThread().getName());
  }

  public static ZoneId getDefaultTimeZone() {
    return defaultTimeZone;
  }

  public static void setDefaultTimeZone(ZoneId defaultTimeZone) {
    SavedErrorMessage.defaultTimeZone = defaultTimeZone;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public Throwable getE() {
    return e;
  }

  public void setE(Throwable e) {
    this.e = e;
  }

  public String getLogger() {
    return logger;
  }

  public void setLogger(String logger) {
    this.logger = logger;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public SavedErrorMessageSeverity getSeverity() {
    return severity;
  }

  public void setSeverity(SavedErrorMessageSeverity severity) {
    this.severity = severity;
  }

  public String getThread() {
    return thread;
  }

  public void setThread(String thread) {
    this.thread = thread;
  }

  public ZonedDateTime getTime() {
    return time;
  }

  public void setTime(ZonedDateTime time) {
    this.time = time;
  }

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
