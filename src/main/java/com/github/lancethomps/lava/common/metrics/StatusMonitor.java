package com.github.lancethomps.lava.common.metrics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.Enums;
import com.github.lancethomps.lava.common.collections.MapUtil;
import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.lambda.Lambdas;
import com.github.lancethomps.lava.common.lambda.ThrowingFunction;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.OutputExpression;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.time.Stopwatch;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.Histogram;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.Metric;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.SharedMetricRegistries;
import io.dropwizard.metrics5.Slf4jReporter;
import io.dropwizard.metrics5.Timer;
import io.dropwizard.metrics5.health.HealthCheck;
import io.dropwizard.metrics5.health.HealthCheck.Result;
import io.dropwizard.metrics5.health.HealthCheckRegistry;
import io.dropwizard.metrics5.health.SharedHealthCheckRegistries;

/**
 * The Class StatusMonitor.
 */
public final class StatusMonitor {

	/** The Constant ACTIVE_COUNTER_SUFFIX. */
	public static final String ACTIVE_COUNTER_SUFFIX = "active";

	/** The Constant DEFAULT_REGISTRY. */
	public static final String DEFAULT_REGISTRY = "DEFAULT";

	/** The Constant HEALTHY. */
	public static final String HEALTHY = "HEALTHY";

	/** The Constant PROFILING. */
	public static final String PROFILING = "profiling";

	/** The Constant PROFILING_REGISTRY. */
	public static final MetricRegistry PROFILING_REGISTRY = registry(PROFILING);

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(StatusMonitor.class);

	/** The log reporter. */
	private static Slf4jReporter logReporter;

	/**
	 * Utilty class cannot instantiates a status monitor.
	 */
	private StatusMonitor() {
		throw new IllegalStateException("Utility class");
	}

	static {
		SharedMetricRegistries.setDefault(DEFAULT_REGISTRY);
		SharedHealthCheckRegistries.setDefault(DEFAULT_REGISTRY);
	}

	/**
	 * Check with error message.
	 *
	 * @param errorMessages the error messages
	 * @param healthCondition the health condition (specify condition where error is present)
	 * @param error the error
	 */
	public static void checkWithErrorMessage(List<String> errorMessages, boolean healthCondition, String error) {
		if (healthCondition) {
			errorMessages.add(error);
		}
	}

	/**
	 * Counter.
	 *
	 * @param registry the registry
	 * @param id the id
	 * @param qualifiers the qualifiers
	 * @return the counter
	 */
	public static Counter counter(String registry, String id, String... qualifiers) {
		return registry(registry).counter(qualifiers == null ? MetricName.build(id) : MetricRegistry.name(id, qualifiers));
	}

	/**
	 * Counter increment and get.
	 *
	 * @param registry the registry
	 * @param id the id
	 * @param qualifiers the qualifiers
	 * @return the counter
	 */
	public static Counter counterIncrementAndGet(String registry, String id, String... qualifiers) {
		Counter counter = counter(registry, id, qualifiers);
		counter.inc();
		return counter;
	}

	/**
	 * Error string.
	 *
	 * @param errorMessages the error messages
	 * @return the string
	 */
	public static String errorString(List<String> errorMessages) {
		StringBuilder error = new StringBuilder();
		errorMessages.forEach(s -> error.append(s + ' '));
		return error.toString();
	}

	/**
	 * Gets the counters.
	 *
	 * @return the counters
	 */
	public static Map<String, Counter> getCounters() {
		return adaptMetrics(SharedMetricRegistries.getDefault().getCounters());
	}

	/**
	 * Gets the counters.
	 *
	 * @param registry the registry
	 * @return the counters
	 */
	public static Map<String, Counter> getCounters(String registry) {
		if (registry == null) {
			getCounters();
		}
		if (SharedMetricRegistries.names().contains(registry)) {
			return adaptMetrics(SharedMetricRegistries.getOrCreate(registry).getCounters());
		}
		return null;
	}

	/**
	 * Gets the default health registry.
	 *
	 * @return the default health registry
	 */
	public static HealthCheckRegistry getDefaultHealthRegistry() {
		return SharedHealthCheckRegistries.getDefault();
	}

	/**
	 * Gets the default registry.
	 *
	 * @return the default registry
	 */
	public static MetricRegistry getDefaultRegistry() {
		return SharedMetricRegistries.getDefault();
	}

	/**
	 * Gets the gauges.
	 *
	 * @return the gauges
	 */
	public static Map<String, Gauge> getGauges() {
		return adaptMetrics(SharedMetricRegistries.getDefault().getGauges());
	}

	/**
	 * Gets the gauges.
	 *
	 * @param registry the registry
	 * @return the gauges
	 */
	public static Map<String, Gauge> getGauges(String registry) {
		if (SharedMetricRegistries.names().contains(registry)) {
			return adaptMetrics(SharedMetricRegistries.getOrCreate(registry).getGauges());
		}
		return null;
	}

	/**
	 * Gets the health check config.
	 *
	 * @param name the name
	 * @return the health check config
	 */
	public static Map<String, Object> getHealthCheckConfig(@Nonnull String name) {
		return Optional.ofNullable(MetricFactory.getDependecyTreeConfigs()).map(DependencyTreeConfigs::getHealthCheckConfigs).map(d -> d.get(name)).orElse(Collections.emptyMap());
	}

	/**
	 * Gets the health check registries.
	 *
	 * @return the health check registries
	 */
	public static Map<String, Map<String, Result>> getHealthCheckResult() {
		return SharedHealthCheckRegistries
			.names()
			.stream()
			.collect(Collectors.toMap(Function.identity(), name -> {
				Logs.logWarn(LOG, "Running health checks: registry=%s", name);
				Stopwatch watch = Stopwatch.createAndStart();
				Map<String, Result> result = SharedHealthCheckRegistries.getOrCreate(name).runHealthChecks();
				Logs.logWarn(
					LOG,
					"Health checks complete: timer=%s registry=%s count=%s failures=%s",
					watch.getTime(),
					name,
					result.keySet().size(),
					result.values().stream().filter(res -> !res.isHealthy()).count()
				);
				return result;
			}, Lambdas.getDefaultMergeFunction(), TreeMap::new));
	}

	/**
	 * Gets the health result for registry.
	 *
	 * @param registry the registry
	 * @return the healthregistry
	 */
	public static Map<String, Result> getHealthCheckResultForRegistry(String registry) {
		if (SharedHealthCheckRegistries.names().contains(registry)) {
			return SharedHealthCheckRegistries.getOrCreate(registry).runHealthChecks();
		}
		return null;
	}

	/**
	 * Gets the histograms.
	 *
	 * @return the histograms
	 */
	public static Map<String, Histogram> getHistograms() {
		return adaptMetrics(SharedMetricRegistries.getDefault().getHistograms());
	}

	/**
	 * Gets the histograms.
	 *
	 * @param registry the registry
	 * @return the histograms
	 */
	public static Map<String, Histogram> getHistograms(String registry) {
		if (SharedMetricRegistries.names().contains(registry)) {
			return adaptMetrics(SharedMetricRegistries.getOrCreate(registry).getHistograms());
		}
		return null;
	}

	/**
	 * Gets the meters.
	 *
	 * @return the meters
	 */
	public static Map<String, Meter> getMeters() {
		return adaptMetrics(SharedMetricRegistries.getDefault().getMeters());
	}

	/**
	 * Gets the meters.
	 *
	 * @param registry the registry
	 * @return the meters
	 */
	public static Map<String, Meter> getMeters(String registry) {
		if (SharedMetricRegistries.names().contains(registry)) {
			return adaptMetrics(SharedMetricRegistries.getOrCreate(registry).getMeters());
		}
		return null;
	}

	/**
	 * Gets the metrics.
	 *
	 * @param metricType the metric type
	 * @return the metrics
	 * @throws Exception the exception
	 */
	public static Map<String, ? extends Object> getMetrics(String metricType) throws Exception {
		return getMetrics(metricType, null);
	}

	/**
	 * Gets the metrics.
	 *
	 * @param metricType the metric type
	 * @param registry the registry
	 * @return the metrics
	 * @throws Exception the exception
	 */
	public static Map<String, ? extends Object> getMetrics(String metricType, String registry) throws Exception {
		MetricType metric = Enums.fromString(MetricType.class, metricType);
		if (metric != null) {
			return metric.getDataFunction().apply(registry);
		}
		return null;
	}

	/**
	 * Gets the or create log reporter.
	 *
	 * @return the or create log reporter
	 */
	public static Slf4jReporter getOrCreateLogReporter() {
		synchronized (StatusMonitor.class) {
			if (logReporter == null) {
				logReporter = Slf4jReporter
					.forRegistry(SharedMetricRegistries.getDefault())
					.outputTo(LoggerFactory.getLogger(StatusMonitor.class))
					.convertRatesTo(TimeUnit.SECONDS)
					.convertDurationsTo(TimeUnit.MILLISECONDS)
					.build();
			}
		}
		return logReporter;
	}

	/**
	 * Gets the registries.
	 *
	 * @return the registries
	 */
	public static Map<String, MetricRegistry> getRegistries() {
		return SharedMetricRegistries.names().stream().collect(
			Collectors.toMap(Function.identity(), SharedMetricRegistries::getOrCreate, Lambdas.getDefaultMergeFunction(), TreeMap::new)
		);
	}

	/**
	 * Gets the metrics registry if it exists.
	 *
	 * @param registry the registry
	 * @return the registry
	 */
	public static MetricRegistry getRegistry(String registry) {
		if (SharedMetricRegistries.names().contains(registry)) {
			return SharedMetricRegistries.getOrCreate(registry);
		}
		return null;
	}

	/**
	 * Gets the timers.
	 *
	 * @return the timers
	 */
	public static Map<String, Timer> getTimers() {
		return adaptMetrics(SharedMetricRegistries.getDefault().getTimers());
	}

	/**
	 * Gets the timers.
	 *
	 * @param registry the registry
	 * @return the timers
	 */
	public static Map<String, Timer> getTimers(String registry) {
		if (SharedMetricRegistries.names().contains(registry)) {
			return adaptMetrics(SharedMetricRegistries.getOrCreate(registry).getTimers());
		}
		return null;
	}

	/**
	 * Health registry.
	 *
	 * @param registry the registry
	 * @return the health check registry
	 */
	public static HealthCheckRegistry healthRegistry(String registry) {
		return registry == null ? SharedHealthCheckRegistries.getDefault() : SharedHealthCheckRegistries.getOrCreate(registry);
	}

	/**
	 * Register and mark meter metric.
	 *
	 * @param registry the registry
	 * @param id the id
	 * @param qualifiers the qualifiers
	 */
	public static void mark(String registry, String id, String... qualifiers) {
		meter(registry, id, qualifiers).mark();
	}

	/**
	 * Meter. The codahale library will bring back an existing Meter or create a new one if it doesnt
	 * exist
	 *
	 * @param registry the registry
	 * @param id the id
	 * @param qualifiers the qualifiers
	 * @return the meter
	 */
	public static Meter meter(String registry, String id, String... qualifiers) {
		return registry(registry).meter(qualifiers == null ? MetricName.build(id) : MetricRegistry.name(id, qualifiers));
	}

	/**
	 * Profiling timer.
	 *
	 * @param id the id
	 * @param qualifiers the qualifiers
	 * @return the timer
	 */
	public static Timer profilingTimer(String id, String... qualifiers) {
		return timer(PROFILING, id, qualifiers);
	}

	/**
	 * Register health check.
	 *
	 * @param name the name
	 * @param checker the checker
	 */
	public static void registerHealthCheck(@Nonnull String name, @Nonnull HealthCheck checker) {
		registerHealthCheck(null, name, checker);
	}

	/**
	 * Register health check.
	 *
	 * @param registry the registry
	 * @param name the name
	 * @param checker the checker
	 */
	public static void registerHealthCheck(@Nullable String registry, @Nonnull String name, @Nonnull HealthCheck checker) {
		healthRegistry(registry).register(name, checker);
	}

	/**
	 * Register health check.
	 *
	 * @param registry the registry
	 * @param name the name
	 * @param checker the checker
	 */
	public static void registerHealthCheck(@Nullable String registry, @Nonnull String name, @Nonnull ThrowingFunction<Map<String, Object>, HealthCheck.Result> checker) {
		healthRegistry(registry).register(name, () -> checker.apply(getHealthCheckConfig(name)));
	}

	/**
	 * Register health check.
	 *
	 * @param name the name
	 * @param checker the checker
	 */
	public static void registerHealthCheck(@Nonnull String name, @Nonnull ThrowingFunction<Map<String, Object>, HealthCheck.Result> checker) {
		registerHealthCheck(null, name, checker);
	}

	/**
	 * Gets the Metrics registry.
	 *
	 * @param registry the registry
	 * @return the registry
	 */
	public static MetricRegistry registry(String registry) {
		return registry == null ? SharedMetricRegistries.getDefault() : SharedMetricRegistries.getOrCreate(registry);
	}

	/**
	 * Run health checks and create dependencies.
	 *
	 * @param userService the user service
	 * @return the map
	 */
	public static Map<String, List<HierarchyTree>> runHealthChecksAndCreateDependencies(@Nullable Function<String, HealthCheckOwner> userService) {
		return runHealthChecksAndCreateDependencies(userService, null);
	}

	/**
	 * Run health checks and create dependencies.
	 *
	 * @param userService the user service
	 * @param registry the registry
	 * @return the map
	 */
	public static Map<String, List<HierarchyTree>> runHealthChecksAndCreateDependencies(@Nullable Function<String, HealthCheckOwner> userService, @Nullable String registry) {
		Map<String, Map<String, Result>> healthCheckResult = Checks.isNotBlank(registry) ? MapUtil.createFrom(registry, getHealthCheckResultForRegistry(registry)) : getHealthCheckResult();
		Map<String, Result> allResults = healthCheckResult
			.values()
			.stream()
			.map(Map::entrySet)
			.flatMap(Collection::stream)
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (a, b) -> a, LinkedHashMap::new));
		Map<String, HierarchyTree> hierarchyMap = Serializer.readJsonAsMap(Serializer.toJson(MetricFactory.getOrCreateHierarchyTree(userService)), HierarchyTree.class);
		Set<String> allConfigKeys = new HashSet<>(hierarchyMap.keySet());
		Map<String, List<HierarchyTree>> healthResult = healthCheckResult.entrySet().stream().collect(
			Collectors.toMap(
				e -> e.getKey(),
				e -> {
					List<HierarchyTree> trees = e
						.getValue()
						.entrySet()
						.stream()
						.map(
							healthCheck -> {
								String healthCheckName = healthCheck.getKey();
								allConfigKeys.remove(healthCheckName);
								HierarchyTree hierarchyTree = hierarchyMap.getOrDefault(healthCheckName, new HierarchyTree());
								hierarchyTree.setName(healthCheckName);
								updateHierarchyTreeWithResult(allResults, hierarchyMap, hierarchyTree, healthCheck.getValue());
								return hierarchyTree;
							}
						)
						.collect(Collectors.toList());
					return trees;
				}
			)
		);
		if (!allConfigKeys.isEmpty()) {
			for (String healthCheckName : allConfigKeys) {
				HierarchyTree hierarchyTree = hierarchyMap.get(healthCheckName);
				if (hierarchyTree == null) {
					continue;
				} else if (Checks.isEmpty(hierarchyTree.getDependsOn())) {
					continue;
				}
				hierarchyTree.setName(healthCheckName);
				updateHierarchyTreeWithResult(allResults, hierarchyMap, hierarchyTree, null);
				if (hierarchyTree.getHealthState() != null) {
					healthResult.get(StatusMonitor.DEFAULT_REGISTRY).add(hierarchyTree);
				} else {
					Logs.logForSplunk(
						LOG,
						"BAD_DEPENDENCY_TREE_CONFIG",
						"Dependency defined without a health check and none of its dependencies have health checks either - this should be removed: %s",
						healthCheckName
					);
				}
			}
		}
		return healthResult;
	}

	/**
	 * Timer. The codahale library will bring back an existing Timer or create a new one if it doesnt
	 * exist
	 *
	 * @param registry the registry
	 * @param id the id
	 * @param qualifiers the qualifiers
	 * @return the timer
	 */
	public static Timer timer(String registry, String id, String... qualifiers) {
		return registry(registry).timer(qualifiers == null ? MetricName.build(id) : MetricRegistry.name(id, qualifiers));
	}

	/**
	 * Timer start.
	 *
	 * @param registry the registry
	 * @param id the id
	 * @param qualifiers the qualifiers
	 * @return the timer. context
	 */
	public static Timer.Context timerStart(String registry, String id, String... qualifiers) {
		return timer(registry, id, qualifiers).time();
	}

	/**
	 * Unregister health check.
	 *
	 * @param name the name
	 */
	public static void unregisterHealthCheck(@Nonnull String name) {
		unregisterHealthCheck(null, name);
	}

	/**
	 * Unregister health check.
	 *
	 * @param registry the registry
	 * @param name the name
	 */
	public static void unregisterHealthCheck(@Nullable String registry, @Nonnull String name) {
		healthRegistry(registry).unregister(name);
	}

	/**
	 * Adapt metrics.
	 *
	 * @param <T> the generic type
	 * @param metrics the metrics
	 * @return the sorted map
	 */
	private static <T extends Metric> SortedMap<String, T> adaptMetrics(Map<MetricName, T> metrics) {
		final SortedMap<String, T> items = new TreeMap<>();
		for (Map.Entry<MetricName, T> entry : metrics.entrySet()) {
			items.put(entry.getKey().getKey(), entry.getValue());
		}
		return Collections.unmodifiableSortedMap(items);
	}

	/**
	 * Update hierarchy tree with result.
	 *
	 * @param results the results
	 * @param trees the trees
	 * @param hierarchyTree the hierarchy tree
	 * @param result the result
	 * @return the hierarchy tree
	 */
	private static HierarchyTree updateHierarchyTreeWithResult(Map<String, Result> results, Map<String, HierarchyTree> trees, HierarchyTree hierarchyTree, Result result) {
		hierarchyTree.setHealthState(result);
		hierarchyTree.setHealthy(result == null ? true : result.isHealthy());
		if ((result != null) && Checks.isNotEmpty(hierarchyTree.getHealthCheckExpressions())) {
			final HealthCheckExpressionContext exprContext = new HealthCheckExpressionContext(results, trees, hierarchyTree, result);
			final Map<String, Object> created = new LinkedHashMap<>();
			exprContext.setResult(created);
			boolean healthy = result.isHealthy();
			for (OutputExpression expr : ExprFactory.compileCreateExpressions(hierarchyTree.getHealthCheckExpressions(), false, false, true)) {
				Object val = ExprFactory.eval(exprContext, expr.getCompiledExpression(), false);
				if (val != null) {
					if (val instanceof Boolean) {
						Serializer.addPathKeyToMap(created, expr.getPath(), val);
						if (!((Boolean) val).booleanValue()) {
							healthy = false;
						}
					} else if (expr.testReturnsPathKeyMap()) {
						healthy = false;
						(val instanceof Map ? ((Map<?, ?>) val) : Serializer.toMap(val, false)).forEach((key, mapVal) -> {
							Serializer.addPathKeyToMap(created, key.toString(), mapVal);
						});
					} else {
						healthy = false;
						Serializer.addPathKeyToMap(created, expr.getPath(), val);
					}
				}
			}
			hierarchyTree.setHealthy(healthy).setHealthCheckExpressionsOutput(created);
		}
		if (Checks.isNotEmpty(hierarchyTree.getDependencies())) {
			hierarchyTree
				.getDependencies()
				.entrySet()
				.stream()
				.forEach(e -> {
					updateHierarchyTreeWithResult(results, trees, e.getValue(), results.get(e.getKey()));
				});
			List<String> failures = hierarchyTree
				.getDependencies()
				.entrySet()
				.stream()
				.filter(e -> !e.getValue().testHealthy())
				.map(e -> e.getKey())
				.collect(Collectors.toList());
			if (!failures.isEmpty()) {
				hierarchyTree.setFailingDependencies(failures);
				if (result == null) {
					hierarchyTree.setHealthState(Result.unhealthy("Failing dependencies."));
				}
			} else if (result == null) {
				if (hierarchyTree.getDependencies().values().stream().anyMatch(dep -> dep.getHealthState() != null)) {
					hierarchyTree.setHealthState(Result.healthy("This has no health check, but all dependencies are healthy."));
				}
			}
		}
		return hierarchyTree;
	}

}
