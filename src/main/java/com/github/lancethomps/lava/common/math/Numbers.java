package com.github.lancethomps.lava.common.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The Class Numbers.
 */
public class Numbers {

	/**
	 * Round.
	 *
	 * @param value the value
	 * @param places the places
	 * @return the double
	 */
	public static double round(double value, int places) {
		if (places < 0) {
			throw new IllegalArgumentException();
		}
		if (Double.isNaN(value)) {
			return value;
		}
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

}
