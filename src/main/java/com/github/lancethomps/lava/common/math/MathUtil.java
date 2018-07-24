package com.github.lancethomps.lava.common.math;

import static com.github.lancethomps.lava.common.logging.Logs.logWarn;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;

/**
 * The Class MathUtil.
 */
public class MathUtil {

	/** The Constant MC. */
	public static final MathContext MC = MathContext.DECIMAL128;

	/** The Constant ONE_HUNDRED. */
	public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(MathUtil.class);

	/**
	 * Calculate harmonic mean.
	 *
	 * @param values the values
	 * @return the big decimal
	 */
	public static BigDecimal calculateHarmonicMean(Collection<BigDecimal> values) {
		if (Checks.isEmpty(values)) {
			return null;
		}
		BigDecimal denom = values.stream().reduce(ZERO, (a, b) -> a.add(ONE.divide(b, MC), MC));
		return new BigDecimal(values.size()).divide(denom, MC);
	}

	/**
	 * Calculate portfolio harmonic mean.
	 *
	 * @param values the values
	 * @return the big decimal
	 */
	public static BigDecimal calculatePortfolioHarmonicMean(Collection<BigDecimal> values) {
		if (Checks.isEmpty(values)) {
			return null;
		}
		BigDecimal denom = values.stream().reduce(ZERO, (a, b) -> a.add(b, MC));
		return equalsZero(denom) ? ZERO : ONE.divide(denom, MC);
	}

	/**
	 * Calculate standard deviation.
	 *
	 * @param <T> the generic type
	 * @param values the values
	 * @return the big decimal
	 */
	public static <T extends Number> BigDecimal calculateStandardDeviation(@Nonnull Collection<T> values) {
		return calculateStandardDeviation(
			values
				.stream()
				.mapToDouble(Number::doubleValue)
				.toArray()
		);
	}

	/**
	 * Calculate standard deviation.
	 *
	 * @param values the values
	 * @return the big decimal
	 */
	public static BigDecimal calculateStandardDeviation(@Nonnull double[] values) {
		double stdDev = new StandardDeviation().evaluate(values);
		if (!Double.isFinite(stdDev)) {
			logWarn(LOG, "Standard deviation was not finite!");
		}
		return BigDecimal.valueOf(stdDev);
	}

	/**
	 * Equals zero.
	 *
	 * @param val the val
	 * @return true, if successful
	 */
	public static boolean equalsZero(BigDecimal val) {
		return ZERO.compareTo(val) == 0;
	}

	/**
	 * Equals zero or null.
	 *
	 * @param val the val
	 * @return true, if successful
	 */
	public static boolean equalsZeroOrNull(BigDecimal val) {
		return (val == null) || (ZERO.compareTo(val) == 0);
	}

	/**
	 * Equals zero or null.
	 *
	 * @param val the val
	 * @return true, if successful
	 */
	public static boolean equalsZeroOrNull(Double val) {
		return (val == null) || !Double.isFinite(val) || (val.compareTo(0d) == 0);
	}

	/**
	 * Gets the average.
	 *
	 * @param <T> the generic type
	 * @param vals the vals
	 * @return the average
	 */
	public static <T extends Number> Double getAverage(Collection<T> vals) {
		if (isNotEmpty(vals)) {
			double sum = 0d;
			for (Number val : vals) {
				sum += val.doubleValue();
			}
			return sum / vals.size();
		}
		return null;
	}

	/**
	 * Greater than.
	 *
	 * @param baseValue the v1
	 * @param testGreater the v2
	 * @return true, if successful
	 */
	public static boolean greaterThan(BigDecimal baseValue, BigDecimal testGreater) {
		return (baseValue == null) || ((testGreater != null) && (baseValue.compareTo(testGreater) == -1));
	}

	/**
	 * Greater than or equal to.
	 *
	 * @param baseValue the base value
	 * @param testGreater the test greater
	 * @return true, if successful
	 */
	public static boolean greaterThanOrEqualTo(BigDecimal baseValue, BigDecimal testGreater) {
		return (baseValue == null) || ((testGreater != null) && (baseValue.compareTo(testGreater) <= 0));
	}

	/**
	 * Greater than zero.
	 *
	 * @param val the val
	 * @return true, if successful
	 */
	public static boolean greaterThanZero(BigDecimal val) {
		return ZERO.compareTo(val) == -1;
	}

	/**
	 * Less than.
	 *
	 * @param v1 the v1
	 * @param v2 the v2
	 * @return true, if successful
	 */
	public static boolean lessThan(BigDecimal v1, BigDecimal v2) {
		return (v1 == null) || ((v2 != null) && (v1.compareTo(v2) == 1));
	}

	/**
	 * Less than zero.
	 *
	 * @param val the val
	 * @return true, if successful
	 */
	public static boolean lessThanZero(BigDecimal val) {
		return ZERO.compareTo(val) == 1;
	}

	/**
	 * Value of.
	 *
	 * @param val the val
	 * @return the big decimal
	 */
	public static BigDecimal toBigDecimal(Double val) {
		return (val == null) || !Double.isFinite(val) ? null : BigDecimal.valueOf(val);
	}

	/**
	 * To big decimal.
	 *
	 * @param val the val
	 * @return the big decimal
	 */
	public static BigDecimal toBigDecimal(String val) {
		return val == null ? null : new BigDecimal(val);
	}
}
