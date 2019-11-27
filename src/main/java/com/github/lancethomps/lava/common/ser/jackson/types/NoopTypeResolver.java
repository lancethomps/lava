package com.github.lancethomps.lava.common.ser.jackson.types;

import java.util.Collection;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;

public class NoopTypeResolver extends DefaultTypeResolverBuilder {

  private static final long serialVersionUID = 6486011674616018279L;

  public NoopTypeResolver() {
    super(DefaultTyping.NON_FINAL);
  }

  @Override
  public boolean useForType(JavaType t) {
    return false;
  }

  @Override
  protected TypeIdResolver idResolver(
      MapperConfig<?> config,
      JavaType baseType,
      PolymorphicTypeValidator subtypeValidator,
      Collection<NamedType> subtypes,
      boolean forSer,
      boolean forDeser
  ) {
    return new NoopTypeIdResolver(baseType, config.getTypeFactory());
  }

}
