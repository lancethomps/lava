package com.github.lancethomps.lava.common.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class Numbers.
 */
public class Numbers {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(Numbers.class);

	/**
	 * Calculate average.
	 *
	 * @param values the values
	 * @return the big decimal
	 */
	public static BigDecimal calculateAverage(@Nonnull Collection<BigDecimal> values) {
		return values.isEmpty() ? null : values.stream().filter(Checks::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add).divide(new BigDecimal(values.size()), MathContext.DECIMAL128);
	}

	/**
	 * Pow.
	 *
	 * @param n1 the n1
	 * @param n2 the n2
	 * @return the big decimal
	 */
	public static BigDecimal pow(BigDecimal n1, BigDecimal n2) {
		BigDecimal result = null;
		int signOf2 = n2.signum();
		try {
			// Perform X^(A+B)=X^A*X^B (B = remainder)
			double dn1 = n1.doubleValue();
			n2 = n2.multiply(new BigDecimal(signOf2));
			BigDecimal remainderOf2 = n2.remainder(BigDecimal.ONE);
			BigDecimal n2IntPart = n2.subtract(remainderOf2);
			// Calculate big part of the power using context -
			// bigger range and performance but lower accuracy
			BigDecimal intPow = n1.pow(n2IntPart.intValueExact(), MathContext.DECIMAL128);
			BigDecimal doublePow = new BigDecimal(Math.pow(dn1, remainderOf2.doubleValue()));
			result = intPow.multiply(doublePow);
			// Fix negative power
			if (signOf2 == -1) {
				result = BigDecimal.ONE.divide(result, MathContext.DECIMAL128);
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Error calculating BigDecimal [%s] to the power of [%s]!", n1, n2);
		}
		return result;
	}

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
