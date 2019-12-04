package com.lancethomps.lava.common.ser.jackson;

public interface UnknownFieldCollector {

  void handleUnknownField(String name, Object val);

}
