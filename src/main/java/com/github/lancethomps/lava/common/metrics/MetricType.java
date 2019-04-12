package com.github.lancethomps.lava.common.metrics;

import java.util.Map;

import com.github.lancethomps.lava.common.Enums;
import com.github.lancethomps.lava.common.lambda.ThrowingFunction;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.Histogram;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.Timer;

public enum MetricType {

  COUNTERS(Counter.class, registry -> StatusMonitor.getCounters(registry)),

  GAUGES(Gauge.class, registry -> StatusMonitor.getGauges(registry)),

  HISTOGRAMS(Histogram.class, registry -> StatusMonitor.getHistograms(registry)),

  METERS(Meter.class, registry -> StatusMonitor.getMeters(registry)),

  TIMERS(Timer.class, registry -> StatusMonitor.getTimers(registry));

  static {
    Enums.createStringToTypeMap(MetricType.class);
  }

  private final ThrowingFunction<String, Map<String, ?>> dataFunction;
  private final Class<?> metricType;

  MetricType(Class<?> metricType, ThrowingFunction<String, Map<String, ?>> dataFunction) {
    this.metricType = metricType;
    this.dataFunction = dataFunction;
  }

  public ThrowingFunction<String, Map<String, ?>> getDataFunction() {
    return dataFunction;
  }

  public Class<?> getMetricType() {
    return metricType;
  }

}
