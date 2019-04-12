package com.github.lancethomps.lava.common.web.requests.parsers;

public class RequestParsingException extends Exception {

  private static final long serialVersionUID = -7716936144520697255L;

  public RequestParsingException() {
    super();
  }

  public RequestParsingException(String message) {
    super(message);
  }

  public RequestParsingException(String message, Throwable cause) {
    super(message, cause);
  }

}
