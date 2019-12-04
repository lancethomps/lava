package com.lancethomps.lava.common.ser.csv;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.lancethomps.lava.common.ser.SerializerFactory;

public class CsvAnnotationIntrospector extends JacksonAnnotationIntrospector {

  private static final long serialVersionUID = 7645118621906559253L;

  private static boolean useDefaultUnwrapping;

  @Override
  public Object findFilterId(Annotated a) {
    return SerializerFactory.CSV_FILTER;
  }

  @Override
  public NameTransformer findUnwrappingNameTransformer(AnnotatedMember member) {
    if (useDefaultUnwrapping) {
      return super.findUnwrappingNameTransformer(member);
    }
    return null;
  }

}
