package com.github.lancethomps.lava.common.math;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The Class BigDecimalSummaryStatistics.
 */
public class BigDecimalSummaryStatistics implements Consumer<BigDecimal>, Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The count. */
	private long count;

	/** The max. */
	private BigDecimal max;

	/** The min. */
	private BigDecimal min;

	/** The sum. */
	private BigDecimal sum = BigDecimal.ZERO;

	/*
	 * (non-Javadoc)
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(BigDecimal each) {
		count++;
		if (each != null) {
			sum = sum.add(each);
			min = min == null ? each : min.min(each);
			max = max == null ? each : max.max(each);
		}
	}

	/**
	 * Gets the average.
	 *
	 * @return the average
	 */
	public BigDecimal getAverage() {
		return this.getAverage(MathContext.DECIMAL128);
	}

	/**
	 * Gets the average.
	 *
	 * @param context the context
	 * @return the average
	 */
	public BigDecimal getAverage(MathContext context) {
		return count == 0 ? BigDecimal.ZERO : getSum().divide(BigDecimal.valueOf(count), context);
	}

	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Gets the max.
	 *
	 * @return the max
	 */
	public BigDecimal getMax() {
		return max;
	}

	/**
	 * Gets the max optional.
	 *
	 * @return the max optional
	 */
	public Optional<BigDecimal> getMaxOptional() {
		return Optional.ofNullable(max);
	}

	/**
	 * Gets the min.
	 *
	 * @return the min
	 */
	public BigDecimal getMin() {
		return min;
	}

	/**
	 * Gets the min optional.
	 *
	 * @return the min optional
	 */
	public Optional<BigDecimal> getMinOptional() {
		return Optional.ofNullable(min);
	}

	/**
	 * Gets the sum.
	 *
	 * @return the sum
	 */
	public BigDecimal getSum() {
		return sum;
	}

	/**
	 * Merge.
	 *
	 * @param summaryStatistics the summary statistics
	 * @return the big decimal summary statistics
	 */
	public BigDecimalSummaryStatistics merge(BigDecimalSummaryStatistics summaryStatistics) {
		count += summaryStatistics.count;
		sum = sum.add(summaryStatistics.sum);
		if (summaryStatistics.min != null) {
			min = min == null ? summaryStatistics.min : min.min(summaryStatistics.min);
		}
		if (summaryStatistics.max != null) {
			max = max == null ? summaryStatistics.max : max.max(summaryStatistics.max);
		}
		return this;
	}
}
