package com.lancethomps.lava.common.ser;

import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

public class EnumToStringMatcher implements Matcher {

  @SuppressWarnings("rawtypes")
  @Override
  public Transform match(Class type) throws Exception {
    if (type.isEnum()) {
      return new EnumToStringTransform(type);
    }

    return null;
  }

  @SuppressWarnings("rawtypes")
  public class EnumToStringTransform implements Transform<Enum> {

    private final Class type;

    public EnumToStringTransform(Class type) {
      this.type = type;
    }

    @Override
    public Enum read(String value) throws Exception {
      for (Object o : type.getEnumConstants()) {
        if (o.toString().equals(value)) {
          return (Enum) o;
        }
      }
      return null;
    }

    @Override
    public String write(Enum value) throws Exception {
      return value.toString();
    }

  }

}
