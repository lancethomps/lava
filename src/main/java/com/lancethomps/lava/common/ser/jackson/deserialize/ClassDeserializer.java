package com.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.lancethomps.lava.common.ser.jackson.types.CustomTypeIdResolver;

public class ClassDeserializer extends FromStringDeserializer<Class<?>> {

  private static final long serialVersionUID = -2080076029824857141L;

  public ClassDeserializer() {
    super(Class.class);
  }

  @Override
  protected Class<?> _deserialize(String value, DeserializationContext ctxt) throws IOException {
    value = CustomTypeIdResolver.getCorrectClassName(value);
    try {
      return ctxt.findClass(value);
    } catch (Exception e) {
      throw ctxt.instantiationException(Class.class, ClassUtil.getRootCause(e));
    }
  }

}
