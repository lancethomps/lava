package com.github.lancethomps.lava.common.expr;

import java.util.Collection;
import java.util.Objects;

/**
 * The Class ExpressionFunctions.
 */
public class ExprFunctions {

	/**
	 * Average.
	 *
	 * @param values the values
	 * @return the double
	 */
	public static double average(Collection<Number> values) {
		return values.stream().filter(Objects::nonNull).mapToDouble(Number::doubleValue).average().orElse(0.0);
	}

	/**
	 * Max.
	 *
	 * @param values the values
	 * @return the double
	 */
	public static double max(Collection<Number> values) {
		return values.stream().filter(Objects::nonNull).mapToDouble(Number::doubleValue).max().orElse(0.0);
	}

	/**
	 * Min.
	 *
	 * @param values the values
	 * @return the double
	 */
	public static double min(Collection<Number> values) {
		return values.stream().filter(Objects::nonNull).mapToDouble(Number::doubleValue).min().orElse(0.0);
	}

	/**
	 * Sum.
	 *
	 * @param values the values
	 * @return the double
	 */
	public static double sum(Collection<Number> values) {
		return values.stream().filter(Objects::nonNull).mapToDouble(Number::doubleValue).sum();
	}

}
