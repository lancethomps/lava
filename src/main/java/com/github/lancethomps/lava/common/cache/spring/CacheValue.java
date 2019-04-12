package com.github.lancethomps.lava.common.cache.spring;

import java.io.Serializable;

import org.springframework.cache.Cache.ValueWrapper;

import com.github.lancethomps.lava.common.ser.Serializer;

public class CacheValue implements ValueWrapper, Serializable {

  private static final long serialVersionUID = 2914933604736801458L;

  private final Object value;

  public CacheValue(Object value) {
    if ((value != null) && !(value instanceof Serializable)) {
      throw new RuntimeException("Value to be cached must be Serializable: " + value);
    }
    this.value = value;
  }

  @Override
  public Object get() {
    return value;
  }

  @Override
  public String toString() {
    return Serializer.toLogString(this);
  }

}
