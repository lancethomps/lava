package com.github.lancethomps.lava.common.cache;

public class CacheException extends Exception {

  private static final long serialVersionUID = 1197603538578807474L;

  private String name;

  public CacheException() {
    super();
  }

  public CacheException(String message) {
    super(message);
  }

  public CacheException(String message, String name, Throwable t) {
    super(message, t);
    this.name = name;
  }

  public CacheException(String message, Throwable t) {
    super(message, t);
  }

  public CacheException(Throwable t) {
    super(t);
  }

  public String getName() {
    return name;
  }

}
