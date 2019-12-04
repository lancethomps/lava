package com.lancethomps.lava.common.ser.jackson.types;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class NoopTypeIdResolver extends ClassNameIdResolver {

  public NoopTypeIdResolver(JavaType baseType, TypeFactory typeFactory) {
    super(baseType, typeFactory);
  }

  @Override
  public String idFromValue(Object value) {
    return null;
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> type) {
    return null;
  }

}
