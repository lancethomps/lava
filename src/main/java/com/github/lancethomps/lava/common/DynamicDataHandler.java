package com.github.lancethomps.lava.common;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The Interface DynamicDataHandler.
 *
 * @author lancethomps
 */
@JsonIgnoreProperties(value = { "@type" })
public interface DynamicDataHandler extends DynamicDataSetter {

	/**
	 * Adds the data.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @return the t
	 */
	default <T extends DynamicDataHandler> T addData(Map<String, Object> data) {
		if (data != null) {
			if (getData() == null) {
				setData(new HashMap<>(data));
			} else {
				getData().putAll(data);
			}
		}
		return (T) this;
	}

	/**
	 * Adds the data point.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @param value the value
	 * @return the t
	 */
	@JsonAnySetter
	default <T extends DynamicDataHandler> T addDataPoint(String name, Object value) {
		if (getData() == null) {
			setData(new HashMap<>());
		}
		getData().put(name, value);
		return (T) this;
	}

	/**
	 * Adds the data point if present.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @param value the value
	 * @return the t
	 */
	default <T extends DynamicDataHandler> T addDataPointIfPresent(String name, Object value) {
		return value == null ? (T) this : addDataPoint(name, value);
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	@JsonAnyGetter
	Map<String, Object> getData();

	/**
	 * Gets the data point.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @return the data point
	 */
	default <T> T getDataPoint(String name) {
		return getData() == null ? null : (T) getData().get(name);
	}

	/**
	 * Sets the data.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @return the t
	 */
	<T extends DynamicDataHandler> T setData(Map<String, Object> data);

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.DynamicDataSetter#setDataPoint(java.lang.String, java.lang.Object)
	 */
	@Override
	default void setDataPoint(String name, Object value) {
		addDataPoint(name, value);
	}

}
