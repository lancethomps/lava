package com.lancethomps.lava.common.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.logging.Logs;

public class Numbers {

  private static final Logger LOG = LogManager.getLogger(Numbers.class);

  public static BigDecimal calculateAverage(@Nonnull Collection<BigDecimal> values) {
    return values.isEmpty() ? null :
      values.stream().filter(Checks::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add).divide(new BigDecimal(values.size()), MathContext.DECIMAL128);
  }

  public static BigDecimal pow(BigDecimal n1, BigDecimal n2) {
    BigDecimal result = null;
    int signOf2 = n2.signum();
    try {

      double dn1 = n1.doubleValue();
      n2 = n2.multiply(new BigDecimal(signOf2));
      BigDecimal remainderOf2 = n2.remainder(BigDecimal.ONE);
      BigDecimal n2IntPart = n2.subtract(remainderOf2);

      BigDecimal intPow = n1.pow(n2IntPart.intValueExact(), MathContext.DECIMAL128);
      BigDecimal doublePow = new BigDecimal(Math.pow(dn1, remainderOf2.doubleValue()));
      result = intPow.multiply(doublePow);

      if (signOf2 == -1) {
        result = BigDecimal.ONE.divide(result, MathContext.DECIMAL128);
      }
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Error calculating BigDecimal [%s] to the power of [%s]!", n1, n2);
    }
    return result;
  }

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
