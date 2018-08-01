package com.github.lancethomps.lava.common.ser.jackson.filter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.jackson.JacksonUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

/**
 * The Class FieldInclusionPredicateFilter.
 *
 * @author lancethomps
 */
public class FieldInclusionPredicateFilter extends SimpleBeanPropertyFilter {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(FieldInclusionPredicateFilter.class);

	/** The function by type. */
	private final Map<Class<?>, BiPredicate<Object, Object>> predicateByType;

	/**
	 * Instantiates a new field inclusion predicate filter.
	 *
	 * @param predicateByType the function by type
	 */
	public FieldInclusionPredicateFilter(Map<Class<?>, BiPredicate<Object, Object>> predicateByType) {
		super();
		this.predicateByType = predicateByType;
	}

	/**
	 * Gets the include predicate for type.
	 *
	 * @param predicateByType the predicate by type
	 * @param fieldType the field type
	 * @return the include predicate for type
	 */
	public static BiPredicate<Object, Object> getIncludePredicateForType(Map<Class<?>, BiPredicate<Object, Object>> predicateByType, Class<?> fieldType) {
		BiPredicate<Object, Object> function = predicateByType.get(fieldType);
		if (function == null) {
			function = predicateByType.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(fieldType)).findFirst().map(Entry::getValue).orElse(null);
		}
		return function;
	}

	/**
	 * Gets the predicate by type.
	 *
	 * @return the predicateByType
	 */
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
				Logs.logTrace(LOG, "Filtered field of type [%s] with name [%s] on object [%s] with value [%s]", fieldType, writer.getName(), parent, fieldValue);
			}
		}
	}

}
