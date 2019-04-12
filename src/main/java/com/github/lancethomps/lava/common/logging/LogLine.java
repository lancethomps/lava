package com.github.lancethomps.lava.common.logging;

import java.time.LocalDateTime;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;

public class LogLine extends ExternalizableBean {

  private String category;

  private LocalDateTime date;

  private String message;

  private String priority;

  private String raw;

  private String thread;

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  public String getThread() {
    return thread;
  }

  public void setThread(String thread) {
    this.thread = thread;
  }

}
