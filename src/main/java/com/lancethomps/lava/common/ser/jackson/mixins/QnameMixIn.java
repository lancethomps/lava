package com.lancethomps.lava.common.ser.jackson.mixins;

import com.fasterxml.jackson.annotation.JsonCreator;

public abstract class QnameMixIn {

  @JsonCreator
  public QnameMixIn(String localPart) {
  }

}
