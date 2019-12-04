package com.lancethomps.lava.common.ser.jackson.filter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.ser.jackson.JacksonUtils;

public class FieldInclusionPredicateFilter extends SimpleBeanPropertyFilter {

  private static final Logger LOG = Logger.getLogger(FieldInclusionPredicateFilter.class);

  private final Map<Class<?>, BiPredicate<Object, Object>> predicateByType;

  public FieldInclusionPredicateFilter(Map<Class<?>, BiPredicate<Object, Object>> predicateByType) {
    super();
    this.predicateByType = predicateByType;
  }

  public static BiPredicate<Object, Object> getIncludePredicateForType(
    Map<Class<?>, BiPredicate<Object, Object>> predicateByType,
    Class<?> fieldType
  ) {
    BiPredicate<Object, Object> function = predicateByType.get(fieldType);
    if (function == null) {
      function =
        predicateByType.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(fieldType)).findFirst().map(Entry::getValue).orElse(null);
    }
    return function;
  }

  public Map<Class<?>, BiPredicate<Object, Object>> getPredicateByType() {
    return predicateByType;
  }

  @Override
  public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
    Class<?> fieldType = writer instanceof BeanPropertyWriter ? writer.getType().getRawClass() : pojo.getClass();
    BiPredicate<Object, Object> function = getIncludePredicateForType(predicateByType, fieldType);
    if (function == null) {
      super.serializeAsField(pojo, jgen, provider, writer);
    } else {
      Object fieldValue;
      Object parent;
      if (writer instanceof BeanPropertyWriter) {
        fieldValue = JacksonUtils.getProperty(pojo, writer);
        parent = pojo;
      } else {
        fieldValue = pojo;
        parent = null;
      }
      if ((fieldValue != null) && function.test(parent, fieldValue)) {
        super.serializeAsField(pojo, jgen, provider, writer);
      } else {
        Logs.logTrace(
          LOG,
          "Filtered field of type [%s] with name [%s] on object [%s] with value [%s]",
          fieldType,
          writer.getName(),
          parent,
          fieldValue
        );
      }
    }
  }

}
