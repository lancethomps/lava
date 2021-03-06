package com.lancethomps.lava.common;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.lancethomps.lava.common.ser.PostConstructor;
import com.lancethomps.lava.common.ser.Serializer;

@SuppressWarnings("serial")
public class SimpleDomainObject extends PostConstructor implements Serializable {

  @Override
  public String toString() {
    return Serializer.toLogString(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

}
