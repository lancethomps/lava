package com.github.lancethomps.lava.common.metrics;

import java.util.Map;

import com.github.lancethomps.lava.common.Enums;
import com.github.lancethomps.lava.common.lambda.ThrowingFunction;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.Histogram;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.Timer;

/**
 * The Enum MetricType.
 */
public enum MetricType {

	/** The counters. */
	COUNTERS(Counter.class, registry -> StatusMonitor.getCounters(registry)),

	/** The gauges. */
	GAUGES(Gauge.class, registry -> StatusMonitor.getGauges(registry)),

	/** The histograms. */
	HISTOGRAMS(Histogram.class, registry -> StatusMonitor.getHistograms(registry)),

	/** The meters. */
	METERS(Meter.class, registry -> StatusMonitor.getMeters(registry)),

	/** The timers. */
	TIMERS(Timer.class, registry -> StatusMonitor.getTimers(registry));

	/** The data function. */
	private final ThrowingFunction<String, Map<String, ?>> dataFunction;

	/** The class type associated to that enum. */
	private final Class<?> metricType;

	/**
	 * Instantiates a new metric type.
	 *
	 * @param metricType the metric type
	 * @param dataFunction the data function
	 */
	MetricType(Class<?> metricType, ThrowingFunction<String, Map<String, ?>> dataFunction) {
		this.metricType = metricType;
		this.dataFunction = dataFunction;
	}

	static {
		Enums.createStringToTypeMap(MetricType.class);
	}

	/**
	 * @return the dataFunction
	 */
	public ThrowingFunction<String, Map<String, ?>> getDataFunction() {
		return dataFunction;
	}

	/**
	 * Gets the metric type.
	 *
	 * @return the metric type
	 */
	public Class<?> getMetricType() {
		return metricType;
	}

}