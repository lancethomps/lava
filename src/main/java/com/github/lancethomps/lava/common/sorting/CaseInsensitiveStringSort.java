package com.github.lancethomps.lava.common.sorting;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;

/**
 * The Class CaseInsensitiveStringSort.
 */
public class CaseInsensitiveStringSort implements Comparator<String>, Serializable {

	/** The Constant INSTANCE. */
	public static final CaseInsensitiveStringSort INSTANCE = new CaseInsensitiveStringSort();

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3641889656421370106L;

	@Override
	public int compare(String s1, String s2) {
		if (s1 == null) {
			return -1;
		} else if (s2 == null) {
			return 1;
		}
		return Optional.of(s1.compareToIgnoreCase(s2)).map(res -> res == 0 ? s1.compareTo(s2) : res).get();
	}

}
