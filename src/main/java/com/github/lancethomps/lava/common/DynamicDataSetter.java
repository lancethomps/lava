package com.github.lancethomps.lava.common;

@FunctionalInterface
public interface DynamicDataSetter {

  void setDataPoint(String name, Object value);

}
