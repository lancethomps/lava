package com.lancethomps.lava.common.cache;

import java.io.Serializable;

import org.springframework.cache.Cache.ValueWrapper;

import com.lancethomps.lava.common.ser.Serializer;

public class CacheValueAsJson<T> implements ValueWrapper, Serializable {

  private static final long serialVersionUID = -1317784357150054308L;

  private final String json;

  private final Class<T> type;

  public CacheValueAsJson(T value) {
    if (value != null) {
      type = (Class<T>) value.getClass();
      json = Serializer.toJson(value);
    } else {
      type = null;
      json = null;
    }
  }

  @Override
  public Object get() {
    return getDeserialized();
  }

  public T getDeserialized() {
    return json == null ? null : Serializer.fromJson(json, type);
  }

  public String getJson() {
    return json;
  }

  public Class<T> getType() {
    return type;
  }

  @Override
  public String toString() {
    return Serializer.toLogString(this);
  }

}
