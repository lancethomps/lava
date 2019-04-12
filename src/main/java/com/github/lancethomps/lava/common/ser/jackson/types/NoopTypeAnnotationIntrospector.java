package com.github.lancethomps.lava.common.ser.jackson.types;

import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

public class NoopTypeAnnotationIntrospector extends JacksonAnnotationIntrospector {

  private static final long serialVersionUID = -2706049252130430771L;

  @Override
  protected StdTypeResolverBuilder _constructStdTypeResolverBuilder() {
    return new NoopTypeResolver();
  }

}
