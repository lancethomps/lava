package com.lancethomps.lava.common.math;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.logging.Logs;

public class MathUtil {

  public static final MathContext MC = MathContext.DECIMAL128;

  public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

  private static final Logger LOG = LogManager.getLogger(MathUtil.class);

  public static BigDecimal calculateHarmonicMean(Collection<BigDecimal> values) {
    if (Checks.isEmpty(values)) {
      return null;
    }
    BigDecimal denom = values.stream().reduce(ZERO, (a, b) -> a.add(ONE.divide(b, MC), MC));
    return new BigDecimal(values.size()).divide(denom, MC);
  }

  public static BigDecimal calculatePortfolioHarmonicMean(Collection<BigDecimal> values) {
    if (Checks.isEmpty(values)) {
      return null;
    }
    BigDecimal denom = values.stream().reduce(ZERO, (a, b) -> a.add(b, MC));
    return equalsZero(denom) ? ZERO : ONE.divide(denom, MC);
  }

  public static <T extends Number> BigDecimal calculateStandardDeviation(@Nonnull Collection<T> values) {
    return calculateStandardDeviation(
      values
        .stream()
        .mapToDouble(Number::doubleValue)
        .toArray()
    );
  }

  public static BigDecimal calculateStandardDeviation(@Nonnull double[] values) {
    double stdDev = new StandardDeviation().evaluate(values);
    if (!Double.isFinite(stdDev)) {
      Logs.logWarn(LOG, "Standard deviation was not finite!");
    }
    return BigDecimal.valueOf(stdDev);
  }

  public static boolean equalsZero(BigDecimal val) {
    return ZERO.compareTo(val) == 0;
  }

  public static boolean equalsZeroOrNull(BigDecimal val) {
    return (val == null) || (ZERO.compareTo(val) == 0);
  }

  public static boolean equalsZeroOrNull(Double val) {
    return (val == null) || !Double.isFinite(val) || (val.compareTo(0d) == 0);
  }

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

  public static boolean greaterThan(BigDecimal baseValue, BigDecimal testGreater) {
    return (baseValue == null) || ((testGreater != null) && (baseValue.compareTo(testGreater) == -1));
  }

  public static boolean greaterThanOrEqualTo(BigDecimal baseValue, BigDecimal testGreater) {
    return (baseValue == null) || ((testGreater != null) && (baseValue.compareTo(testGreater) <= 0));
  }

  public static boolean greaterThanZero(BigDecimal val) {
    return ZERO.compareTo(val) == -1;
  }

  public static boolean lessThan(BigDecimal v1, BigDecimal v2) {
    return (v1 == null) || ((v2 != null) && (v1.compareTo(v2) == 1));
  }

  public static boolean lessThanZero(BigDecimal val) {
    return ZERO.compareTo(val) == 1;
  }

  public static BigDecimal toBigDecimal(Double val) {
    return (val == null) || !Double.isFinite(val) ? null : BigDecimal.valueOf(val);
  }

  public static BigDecimal toBigDecimal(String val) {
    return val == null ? null : new BigDecimal(val);
  }

}
