package com.lancethomps.lava.common.ser.jackson.deserialize;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.lancethomps.lava.common.ser.jackson.types.CustomTypeIdResolver;

public class ClassKeyDeserializer extends StdKeyDeserializer {

  private static final long serialVersionUID = -7657136735145323436L;

  public ClassKeyDeserializer() {
    super(StdKeyDeserializer.TYPE_CLASS, Class.class);
  }

  @Override
  protected Object _parse(String value, DeserializationContext ctxt) throws Exception {
    value = CustomTypeIdResolver.getCorrectClassName(value);
    try {
      return ctxt.findClass(value);
    } catch (Exception e) {
      throw ctxt.instantiationException(Class.class, ClassUtil.getRootCause(e));
    }
  }

}
