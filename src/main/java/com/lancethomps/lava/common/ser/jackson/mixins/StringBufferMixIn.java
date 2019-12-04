package com.lancethomps.lava.common.ser.jackson.mixins;

import com.fasterxml.jackson.annotation.JsonCreator;

public abstract class StringBufferMixIn {

  @JsonCreator
  public StringBufferMixIn(String val) {

  }

}
