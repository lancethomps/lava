package com.github.lancethomps.lava.common.ser;

import com.github.lancethomps.lava.common.ser.jackson.SerializationException;

public class SerializationLimitException extends SerializationException {

  private static final long serialVersionUID = -7693589741611316819L;

  private Long limit;

  public SerializationLimitException() {
    this(null);
  }

  public SerializationLimitException(Long limit) {
    this(limit, null);
  }

  public SerializationLimitException(Long limit, String message, Object... formatArgs) {
    this(limit, null, message, formatArgs);
  }

  public SerializationLimitException(Long limit, Throwable cause, String message, Object... formatArgs) {
    super(cause, message, formatArgs);
    this.limit = limit;
  }

  public Long getLimit() {
    return limit;
  }

  public SerializationLimitException setLimit(Long limit) {
    this.limit = limit;
    return this;
  }

  @Override
  public String getMessage() {
    return String.format("Serialization limit exceeded (%,d). ", limit) + super.getMessage();
  }

}
