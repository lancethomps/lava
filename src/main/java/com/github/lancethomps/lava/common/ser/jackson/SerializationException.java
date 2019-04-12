package com.github.lancethomps.lava.common.ser.jackson;

import com.github.lancethomps.lava.common.format.Formatting;

public class SerializationException extends RuntimeException {

  private static final long serialVersionUID = 2606633865857608126L;

  public SerializationException() {
    this(null);
  }

  public SerializationException(String message) {
    this(null, message);
  }

  public SerializationException(Throwable cause, String message, Object... formatArgs) {
    super(Formatting.getMessage(message, formatArgs), cause);
  }

}
