package com.lancethomps.lava.common.ser.csv;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanPropertyWriter;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.ser.Serializer;

public class CsvPropertyFilter extends SimpleBeanPropertyFilter {

  private static final Logger LOG = Logger.getLogger(CsvPropertyFilter.class);

  @Override
  public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
    String name = writer.getName();

    if ((writer instanceof BeanPropertyWriter) && !(writer instanceof UnwrappingBeanPropertyWriter)) {
      Class<?> type = writer.getType().getRawClass();
      if (isCustomField(jgen, name, type)) {
        Object val = ((BeanPropertyWriter) writer).get(pojo);
        writeFieldVal(jgen, name, type, val);
      } else {
        super.serializeAsField(pojo, jgen, provider, writer);
      }
    } else {
      super.serializeAsField(pojo, jgen, provider, writer);
    }
  }

  private boolean isCustomField(JsonGenerator jgen, String name, Class<?> type) throws Exception {
    if (Map.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type) || Temporal.class.isAssignableFrom(type)) {
      jgen.writeFieldName(name);
      return true;
    }
    return false;
  }

  private void writeFieldVal(JsonGenerator jgen, String name, Class<?> type, Object val) {
    try {
      if (val == null) {
        jgen.writeNull();
      } else if (Map.class.isAssignableFrom(type)) {
        jgen.writeString("keys:" + Serializer.toJson(((Map<?, ?>) val).keySet()));
      } else if (Collection.class.isAssignableFrom(type) || type.isArray()) {
        jgen.writeString(Serializer.toJson(val));
      } else if (Temporal.class.isAssignableFrom(type)) {
        jgen.writeNumber(NumberUtils.toLong(Serializer.toJson(val)));
      } else {
        jgen.writeString(val.toString());
      }
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Error writing field [%s] of type [%s] with val [%s].", name, type, val);
    }
  }

}
