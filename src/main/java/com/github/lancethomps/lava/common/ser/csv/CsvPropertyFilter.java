package com.github.lancethomps.lava.common.ser.csv;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanPropertyWriter;

/**
 * The Class CsvPropertyFilter.
 */
public class CsvPropertyFilter extends SimpleBeanPropertyFilter {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(CsvPropertyFilter.class);

	@Override
	public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
		String name = writer.getName();
		// Logs.logDebug(LOG, "Writing for prop [%s].", name);
		if ((writer instanceof BeanPropertyWriter) && !(writer instanceof UnwrappingBeanPropertyWriter)) {
			Class<?> type = ((BeanPropertyWriter) writer).getType().getRawClass();
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

	/**
	 * Checks if is custom field.
	 *
	 * @param jgen the jgen
	 * @param name the name
	 * @param type the type
	 * @return true, if is custom field
	 * @throws Exception the exception
	 */
	private boolean isCustomField(JsonGenerator jgen, String name, Class<?> type) throws Exception {
		if (Map.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type) || Temporal.class.isAssignableFrom(type)) {
			jgen.writeFieldName(name);
			return true;
		}
		return false;
	}

	/**
	 * Write field val.
	 *
	 * @param jgen the jgen
	 * @param name the name
	 * @param type the type
	 * @param val the val
	 * @throws Exception the exception
	 */
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
