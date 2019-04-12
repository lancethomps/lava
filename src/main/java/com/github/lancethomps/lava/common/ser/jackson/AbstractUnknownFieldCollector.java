package com.github.lancethomps.lava.common.ser.jackson;

import java.util.Map;
import java.util.TreeMap;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;

public abstract class AbstractUnknownFieldCollector extends ExternalizableBean implements UnknownFieldCollector {

  private Map<String, Object> unknown;

  public Map<String, Object> getUnknown() {
    return unknown;
  }

  public void setUnknown(Map<String, Object> unknown) {
    this.unknown = unknown;
  }

  @Override
  public void handleUnknownField(String name, Object val) {
    if (unknown == null) {
      unknown = new TreeMap<>();
    }
    unknown.put(name, val);
  }

}
