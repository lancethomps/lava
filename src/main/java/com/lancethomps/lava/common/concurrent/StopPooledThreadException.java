package com.lancethomps.lava.common.concurrent;

public class StopPooledThreadException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public StopPooledThreadException(String msg) {
    super(msg);
  }

}
