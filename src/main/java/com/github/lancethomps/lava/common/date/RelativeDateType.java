package com.github.lancethomps.lava.common.date;

import java.time.temporal.ChronoUnit;

public enum RelativeDateType {

  b(ChronoUnit.DAYS),

  c(null),

  d(ChronoUnit.DAYS),

  h(ChronoUnit.HOURS),

  m(ChronoUnit.MONTHS),

  mi(ChronoUnit.MICROS),

  mm(ChronoUnit.MINUTES),

  ms(ChronoUnit.MILLIS),

  n(ChronoUnit.NANOS),

  q(null),

  s(ChronoUnit.SECONDS),

  w(ChronoUnit.WEEKS),

  y(ChronoUnit.YEARS),

  z(null);

  private final ChronoUnit unit;

  RelativeDateType(ChronoUnit unit) {
    this.unit = unit;
  }

  public ChronoUnit getUnit() {
    return unit;
  }
}
