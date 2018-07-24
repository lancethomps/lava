package com.github.lancethomps.lava.common.ser.jackson;

import java.util.TreeMap;

import com.github.lancethomps.lava.common.string.StringUtil;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * The Class CustomTreeMapCaseInsensitive.
 */
@SuppressWarnings("rawtypes")
@JsonTypeInfo(use = Id.NONE)
public class CustomTreeMapCaseInsensitive extends TreeMap {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1768147912347101106L;

	/**
	 * Instantiates a new map with factory.
	 */
	@SuppressWarnings("unchecked")
	public CustomTreeMapCaseInsensitive() {
		super(StringUtil.CASE_INSENSITIVE_COMP);
	}
}
