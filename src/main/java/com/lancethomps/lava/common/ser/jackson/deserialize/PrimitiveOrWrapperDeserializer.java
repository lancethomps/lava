package com.lancethomps.lava.common.ser.jackson.deserialize;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.util.AccessPattern;

public abstract class PrimitiveOrWrapperDeserializer<T> extends StdScalarDeserializer<T> {

  private static final long serialVersionUID = 1874806793620357569L;

  // CHECKSTYLE.OFF: VisibilityModifier
  protected final T emptyValue;

  protected final T nullValue;

  protected final boolean primitive;
  // CHECKSTYLE.ON: VisibilityModifier

  public PrimitiveOrWrapperDeserializer(Class<T> vc, T nullValue, T emptyValue) {
    super(vc);
    this.nullValue = nullValue;
    this.emptyValue = emptyValue;
    this.primitive = vc.isPrimitive();
  }

  @Override
  public final T getNullValue(DeserializationContext ctxt) throws JsonMappingException {
    if (primitive && ctxt.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)) {
      ctxt.reportInputMismatch(
        this,
        "Cannot map `null` into type %s (set DeserializationConfig.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES to 'false' to allow)",
        handledType().toString()
      );
    }
    return nullValue;
  }

  @Override
  public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
    return emptyValue;
  }

  @Override
  public AccessPattern getNullAccessPattern() {
    if (primitive) {
      return AccessPattern.DYNAMIC;
    }
    if (nullValue == null) {
      return AccessPattern.ALWAYS_NULL;
    }
    return AccessPattern.CONSTANT;
  }

}
