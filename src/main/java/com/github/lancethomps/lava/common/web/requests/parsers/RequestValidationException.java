package com.github.lancethomps.lava.common.web.requests.parsers;

public class RequestValidationException extends Exception {

  private static final long serialVersionUID = 6585970547612832425L;

  public RequestValidationException() {
    super();
  }

  public RequestValidationException(String message) {
    super(message);
  }

  public RequestValidationException(String message, Throwable cause) {
    super(message, cause);
  }

}
