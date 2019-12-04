package com.lancethomps.lava.common.ser.jackson.filter;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

public class JacksonAnnotationIntrospectorSingleFilter extends JacksonAnnotationIntrospector {

  private static final long serialVersionUID = 7063429226703685319L;

  private String filterId;

  public JacksonAnnotationIntrospectorSingleFilter() {
    this(null);
  }

  public JacksonAnnotationIntrospectorSingleFilter(String filterId) {
    super();
    this.filterId = filterId;
  }

  @Override
  public Object findFilterId(Annotated a) {
    return filterId;
  }

}
