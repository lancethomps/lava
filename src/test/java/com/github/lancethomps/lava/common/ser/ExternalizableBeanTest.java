package com.github.lancethomps.lava.common.ser;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.lancethomps.lava.common.DynamicDataHandler;

public class ExternalizableBeanTest extends ExternalizableBean implements DynamicDataHandler {

  @JsonIgnore
  private Map<String, Object> data;

  private Object value;

  public ExternalizableBeanTest() {
    super();
  }

  public ExternalizableBeanTest(Object value) {
    super();
    this.value = value;
  }

  @Override
  public <T extends DynamicDataHandler> T addDataPoint(String name, Object value) {
    if (name != null) {
      if (name.startsWith("#")) {
        return (T) this;
      } else if (name.equals(Serializer.TYPE_PROPERTY)) {
        throw new AssertionError("Jackson class name property being added to dynamic data map - this shouldn't happen.");
      }
    }
    return DynamicDataHandler.super.addDataPoint(name, value);
  }

  @Override
  @JsonAnyGetter
  public Map<String, Object> getData() {
    return data;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public <T extends DynamicDataHandler> T setData(Map<String, Object> data) {
    this.data = data;
    return (T) this;
  }

}
