package com.github.lancethomps.lava.common.math;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;
import java.util.function.Consumer;

public class BigDecimalSummaryStatistics implements Consumer<BigDecimal>, Serializable {

  private static final long serialVersionUID = 1L;

  private long count;

  private BigDecimal max;

  private BigDecimal min;

  private BigDecimal sum = BigDecimal.ZERO;

  @Override
  public void accept(BigDecimal each) {
    count++;
    if (each != null) {
      sum = sum.add(each);
      min = min == null ? each : min.min(each);
      max = max == null ? each : max.max(each);
    }
  }

  public BigDecimal getAverage() {
    return this.getAverage(MathContext.DECIMAL128);
  }

  public BigDecimal getAverage(MathContext context) {
    return count == 0 ? BigDecimal.ZERO : getSum().divide(BigDecimal.valueOf(count), context);
  }

  public long getCount() {
    return count;
  }

  public BigDecimal getMax() {
    return max;
  }

  public Optional<BigDecimal> getMaxOptional() {
    return Optional.ofNullable(max);
  }

  public BigDecimal getMin() {
    return min;
  }

  public Optional<BigDecimal> getMinOptional() {
    return Optional.ofNullable(min);
  }

  public BigDecimal getSum() {
    return sum;
  }

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
