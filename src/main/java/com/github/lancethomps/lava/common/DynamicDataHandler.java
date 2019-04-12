package com.github.lancethomps.lava.common;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"@type"})
public interface DynamicDataHandler extends DynamicDataSetter {

  default <T extends DynamicDataHandler> T addData(Map<String, Object> data) {
    if (data != null) {
      if (getData() == null) {
        setData(new HashMap<>(data));
      } else {
        getData().putAll(data);
      }
    }
    return (T) this;
  }

  @JsonAnySetter
  default <T extends DynamicDataHandler> T addDataPoint(String name, Object value) {
    if (getData() == null) {
      setData(new HashMap<>());
    }
    getData().put(name, value);
    return (T) this;
  }

  default <T extends DynamicDataHandler> T addDataPointIfPresent(String name, Object value) {
    return value == null ? (T) this : addDataPoint(name, value);
  }

  @JsonAnyGetter
  Map<String, Object> getData();

  <T extends DynamicDataHandler> T setData(Map<String, Object> data);

  default <T> T getDataPoint(String name) {
    return getData() == null ? null : (T) getData().get(name);
  }

  @Override
  default void setDataPoint(String name, Object value) {
    addDataPoint(name, value);
  }

}
