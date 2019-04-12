package com.github.lancethomps.lava.common.ser.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;

public class ExternalizableBeanConverter implements Converter<ExternalizableBean, ExternalizableBean> {

  private final JavaType subClassType;

  public ExternalizableBeanConverter(MapperConfig<?> config, Annotated annotated) {
    subClassType = annotated.getType();
  }

  @Override
  public ExternalizableBean convert(ExternalizableBean value) {
    value.afterDeserialization();
    return value;
  }

  @Override
  public JavaType getInputType(TypeFactory typeFactory) {
    return subClassType;
  }

  @Override
  public JavaType getOutputType(TypeFactory typeFactory) {
    return subClassType;
  }

}
