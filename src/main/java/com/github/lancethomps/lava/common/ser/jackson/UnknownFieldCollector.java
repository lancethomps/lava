package com.github.lancethomps.lava.common.ser.jackson;

/**
 * The Interface UnknownFieldCollector.
 */
public interface UnknownFieldCollector {

	/**
	 * Handle unknown.
	 *
	 * @param name the name
	 * @param val the val
	 */
	void handleUnknownField(String name, Object val);
}
