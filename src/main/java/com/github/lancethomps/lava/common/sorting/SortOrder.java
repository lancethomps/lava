package com.github.lancethomps.lava.common.sorting;

import com.github.lancethomps.lava.common.Enums;

/**
 * The Enum SortOrder.
 */
public enum SortOrder {

	/** The asc. */
	asc,

	/** The desc. */
	desc;

	static {
		Enums.createStringToTypeMap(SortOrder.class);
	}

	/**
	 * From string.
	 *
	 * @param sort the sort
	 * @param defaultValue the default value
	 * @return the sort order
	 */
	public static SortOrder fromString(String sort, SortOrder defaultValue) {
		return Enums.fromString(SortOrder.class, sort, defaultValue);
	}
}
