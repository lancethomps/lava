package com.lancethomps.lava.common.expr;

import java.util.Collection;
import java.util.Objects;

public class ExprFunctions {

  public static double average(Collection<Number> values) {
    return values.stream().filter(Objects::nonNull).mapToDouble(Number::doubleValue).average().orElse(0.0);
  }

  public static double max(Collection<Number> values) {
    return values.stream().filter(Objects::nonNull).mapToDouble(Number::doubleValue).max().orElse(0.0);
  }

  public static double min(Collection<Number> values) {
    return values.stream().filter(Objects::nonNull).mapToDouble(Number::doubleValue).min().orElse(0.0);
  }

  public static double sum(Collection<Number> values) {
    return values.stream().filter(Objects::nonNull).mapToDouble(Number::doubleValue).sum();
  }

}
