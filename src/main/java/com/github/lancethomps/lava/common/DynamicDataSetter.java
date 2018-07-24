package com.github.lancethomps.lava.common;

/**
 * The Interface DynamicDataSetter.
 */
@FunctionalInterface
public interface DynamicDataSetter {

	/**
	 * Adds the data point.
	 *
	 * @param name the name
	 * @param value the value
	 */
	void setDataPoint(String name, Object value);
}
