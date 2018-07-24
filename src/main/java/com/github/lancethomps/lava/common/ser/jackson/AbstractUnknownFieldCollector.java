package com.github.lancethomps.lava.common.ser.jackson;

import java.util.Map;
import java.util.TreeMap;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;

/**
 * The Class AbstractOtherFieldCollector.
 */
public abstract class AbstractUnknownFieldCollector extends ExternalizableBean implements UnknownFieldCollector {

	/** The unknown. */
	private Map<String, Object> unknown;

	/**
	 * Gets the unknown.
	 *
	 * @return the unknown
	 */
	public Map<String, Object> getUnknown() {
		return unknown;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.lancethomps.lava.common.ser.jackson.UnknownFieldCollector#handleUnknownField(java.lang.String, java.lang.Object)
	 */
	@Override
	public void handleUnknownField(String name, Object val) {
		if (unknown == null) {
			unknown = new TreeMap<>();
		}
		unknown.put(name, val);
	}

	/**
	 * Sets the unknown.
	 *
	 * @param unknown the unknown
	 */
	public void setUnknown(Map<String, Object> unknown) {
		this.unknown = unknown;
	}
}
