package com.github.lancethomps.lava.common.testing;

import static com.github.lancethomps.lava.common.logging.Logs.println;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.collections.MapUtil;
import com.github.lancethomps.lava.common.lambda.ThrowingFunction;
import com.github.lancethomps.lava.common.lambda.ThrowingIntConsumer;
import com.github.lancethomps.lava.common.lambda.ThrowingRunnable;
import com.github.lancethomps.lava.common.logging.LogIntervalData;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.math.MathUtil;
import com.github.lancethomps.lava.common.sorting.SortClause;
import com.github.lancethomps.lava.common.sorting.SortOrder;
import com.github.lancethomps.lava.common.sorting.Sorting;
import com.github.lancethomps.lava.common.string.StringUtil;
import com.github.lancethomps.lava.common.time.Stopwatch;
import com.github.lancethomps.lava.common.time.Timing;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;

/**
 * The Class SpeedTest.
 */
public class SpeedTest {

	/** The between function. */
	@JsonIgnore
	private ThrowingIntConsumer betweenFunction;

	/** The functions. */
	@JsonIgnore
	private final List<SpeedTestFunction> functions = new ArrayList<>();

	/** The iterations. */
	private long iterations = 100000;

	/** The parallel. */
	private boolean parallel;

	/** The repeat count. */
	private Integer repeatCount;

	/** The results. */
	private Map<String, Map<String, Object>> results;

	/** The reverse. */
	private boolean reverse;

	/** The speed ranking. */
	private List<String> speedRanking;

	/** The test count. */
	@JsonIgnore
	private final AtomicInteger testCount = new AtomicInteger(0);

	/** The warmup. */
	private long warmup = 10000;

	/**
	 * Adds the test.
	 *
	 * @param id the id
	 * @param test the test
	 * @return the speed test
	 */
	public SpeedTest addTest(@Nullable String id, @Nonnull ThrowingFunction<Integer, Long> test) {
		return addTests(new SpeedTestFunction(id != null ? id : getNextId(), test));
	}

	/**
	 * Adds the test.
	 *
	 * @param id the id
	 * @param test the test
	 * @return the speed test
	 */
	public SpeedTest addTest(@Nullable String id, @Nonnull ThrowingIntConsumer test) {
		return addTests(new SpeedTestFunction(id != null ? id : getNextId(), (idx) -> {
			test.accept(idx);
			return null;
		}));
	}

	/**
	 * Adds the test.
	 *
	 * @param id the id
	 * @param test the test
	 * @return the speed test
	 */
	public SpeedTest addTest(@Nullable String id, @Nonnull ThrowingRunnable test) {
		return addTests(new SpeedTestFunction(id != null ? id : getNextId(), (idx) -> {
			test.run();
			return null;
		}));
	}

	/**
	 * Adds the tests.
	 *
	 * @param tests the tests
	 * @return the speed test
	 */
	public SpeedTest addTests(@Nonnull SpeedTestFunction... tests) {
		Stream.of(tests).peek(test -> {
			if (test.getId() == null) {
				test.setId(getNextId());
			}
		}).forEach(functions::add);
		return this;
	}

	/**
	 * Adds the tests.
	 *
	 * @param tests the tests
	 * @return the speed test
	 */
	public SpeedTest addTests(@Nonnull ThrowingFunction<Integer, Long>... tests) {
		Stream.of(tests).map(f -> new SpeedTestFunction(f, getNextId())).forEach(functions::add);
		return this;
	}

	/**
	 * Adds the tests.
	 *
	 * @param tests the tests
	 * @return the speed test
	 */
	public SpeedTest addTests(@Nonnull ThrowingIntConsumer... tests) {
		for (ThrowingIntConsumer test : tests) {
			ThrowingFunction<Integer, Long> wrapper = (idx) -> {
				test.accept(idx);
				return null;
			};
			functions.add(new SpeedTestFunction(wrapper, getNextId()));
		}
		return this;
	}

	/**
	 * Adds the tests.
	 *
	 * @param tests the tests
	 * @return the speed test
	 */
	public SpeedTest addTests(@Nonnull ThrowingRunnable... tests) {
		for (ThrowingRunnable test : tests) {
			ThrowingFunction<Integer, Long> wrapper = (idx) -> {
				test.run();
				return null;
			};
			functions.add(new SpeedTestFunction(wrapper, getNextId()));
		}
		return this;
	}

	/**
	 * Gets the between function.
	 *
	 * @return the betweenFunction
	 */
	public ThrowingIntConsumer getBetweenFunction() {
		return betweenFunction;
	}

	/**
	 * Gets the functions.
	 *
	 * @return the functions
	 */
	public List<SpeedTestFunction> getFunctions() {
		return functions;
	}

	/**
	 * Gets the iterations.
	 *
	 * @return the iterations
	 */
	public long getIterations() {
		return iterations;
	}

	/**
	 * Gets the repeat count.
	 *
	 * @return the repeatCount
	 */
	public Integer getRepeatCount() {
		return repeatCount;
	}

	/**
	 * Gets the results.
	 *
	 * @return the results
	 */
	public Map<String, Map<String, Object>> getResults() {
		return results;
	}

	/**
	 * @return the speedRanking
	 */
	public List<String> getSpeedRanking() {
		return speedRanking;
	}

	/**
	 * Gets the warmup.
	 *
	 * @return the warmup
	 */
	public long getWarmup() {
		return warmup;
	}

	/**
	 * @return the parallel
	 */
	public boolean isParallel() {
		return parallel;
	}

	/**
	 * Checks if is reverse.
	 *
	 * @return the reverse
	 */
	public boolean isReverse() {
		return reverse;
	}

	/**
	 * Run.
	 *
	 * @return the map
	 * @throws Exception the exception
	 */
	public SpeedTest run() throws Exception {
		final List<SpeedTestFunction> functions = new ArrayList<>(this.functions);
		String totalKey = "total";
		String meanKey = "mean";
		if ((repeatCount != null) && (repeatCount > 0)) {
			IntStream.iterate(0, i -> i + 1).limit(repeatCount).forEach(i -> {
				this.functions.stream().map(f -> new SpeedTestFunction(f.getFunction(), f.getId() + "-COPY-" + repeatCount.toString())).forEach(functions::add);
			});
		}
		Logger logger = Logger.getLogger(SpeedTest.class);
		results = new TreeMap<>();
		final IntStream funcStream = createIntStream(functions.size());
		if (parallel) {
			funcStream.parallel();
		}
		funcStream.forEach(funcIdx -> {
			final int functionPos = reverse ? (functions.size() - 1 - funcIdx) : funcIdx;
			SpeedTestFunction testFunction = functions.get(functionPos);
			if (warmup > 0) {
				createIntStream(warmup).forEach(idx -> testFunction.getFunction().applyWithSneakyThrow(idx));
			}
			List<Double> timers = new ArrayList<>((int) iterations);
			LogIntervalData interval = new LogIntervalData(iterations, "speed_test-" + testFunction.getId());
			createIntStream(iterations).forEach(idx -> {
				final Stopwatch watch = Stopwatch.createAndStart();
				Long customTimer = testFunction.getFunction().applyWithSneakyThrow(idx);
				watch.suspend();
				if (customTimer != null) {
					timers.add(customTimer.doubleValue());
				} else {
					timers.add((double) watch.getNanoTime() / (double) Timing.NANO_2_MILLIS);
				}
				if (interval.getWatch().getTime() > 10000) {
					Logs.logInterval(logger, interval, 1);
				} else {
					interval.getCount().incrementAndGet();
				}
			});
			Collections.sort(timers);
			DoubleSummaryStatistics stats = timers.stream().mapToDouble(Double::doubleValue).summaryStatistics();
			BigDecimal totalTime = BigDecimal.valueOf(stats.getSum());
			BigDecimal mean = BigDecimal.valueOf(stats.getAverage());
			BigDecimal median = BigDecimal.valueOf((timers.get((int) Math.floor((timers.size() - 1) / 2)) / 2.0) + (timers.get((int) Math.ceil((timers.size() - 1) / 2)) / 2.0));
			BigDecimal min = BigDecimal.valueOf(stats.getMin());
			BigDecimal max = BigDecimal.valueOf(stats.getMax());
			BigDecimal stdDev = MathUtil.calculateStandardDeviation(timers);
			println(
				"TIMING_STATS: id=%s pos=%s total=%s avg=%s min=%s max=%s stdDev=%s",
				testFunction.getId(),
				functionPos,
				totalTime.toPlainString(),
				mean.toPlainString(),
				min.toPlainString(),
				max.toPlainString(),
				stdDev.toPlainString()
			);
			Map<String, Object> details = Maps.newLinkedHashMap();
			details.put("id", testFunction.getId());
			details.put("pos", functionPos);
			details.put(totalKey, totalTime);
			details.put(meanKey, mean);
			details.put("median", median);
			details.put("min", min);
			details.put("max", max);
			details.put("stdDev", stdDev);
			results.put(testFunction.getId(), details);
			if (betweenFunction != null) {
				betweenFunction.acceptWithSneakyThrow(functionPos);
			}
		});
		int padSize = results.keySet().stream().mapToInt(String::length).max().getAsInt() + 4;
		final Function<Map<String, Object>, String> getResultId = (result) -> {
			return String.format(
				"%s: %s",
				StringUtil.padRight(result.get("id").toString(), padSize),
				StringUtil.padLeft(String.format("%,.3f", ((BigDecimal) result.get(meanKey)).doubleValue()), 10)
			);
		};
		speedRanking = Sorting
			.sort(new ArrayList<>(results.values()), new SortClause(meanKey, SortOrder.asc))
			.stream()
			.map(result -> getResultId.apply(result))
			.collect(Collectors.toList());
		if (results.size() > 1) {
			results.forEach((id, result) -> {
				BigDecimal avg = MapUtil.getFromMap(result, meanKey);
				result.put("rank", speedRanking.indexOf(getResultId.apply(result)) + 1);
				result.put("comparison (this ÷ other)", results.keySet().stream().filter(key -> id.compareTo(key) != 0).collect(Collectors.toMap(Function.identity(), otherPos -> {
					Map<String, Object> other = results.get(otherPos);
					return String.format("%.2f%%", 100d * (avg.doubleValue() / ((BigDecimal) other.get(meanKey)).doubleValue()));
				}, (a, b) -> a, TreeMap::new)));
			});
		}
		return this;
	}

	/**
	 * Sets the between function.
	 *
	 * @param betweenFunction the betweenFunction to set
	 * @return the speed test
	 */
	public SpeedTest setBetweenFunction(ThrowingIntConsumer betweenFunction) {
		this.betweenFunction = betweenFunction;
		return this;
	}

	/**
	 * Sets the iterations.
	 *
	 * @param iterations the iterations to set
	 * @return the speed test
	 */
	public SpeedTest setIterations(long iterations) {
		this.iterations = iterations;
		return this;
	}

	/**
	 * Sets the parallel.
	 *
	 * @param parallel the parallel to set
	 * @return the speed test
	 */
	public SpeedTest setParallel(boolean parallel) {
		this.parallel = parallel;
		return this;
	}

	/**
	 * Sets the repeat count.
	 *
	 * @param repeatCount the repeatCount to set
	 * @return the speed test
	 */
	public SpeedTest setRepeatCount(Integer repeatCount) {
		this.repeatCount = repeatCount;
		return this;
	}

	/**
	 * Sets the reverse.
	 *
	 * @param reverse the reverse to set
	 * @return the speed test
	 */
	public SpeedTest setReverse(boolean reverse) {
		this.reverse = reverse;
		return this;
	}

	/**
	 * Sets the warmup.
	 *
	 * @param warmup the warmup to set
	 * @return the speed test
	 */
	public SpeedTest setWarmup(long warmup) {
		this.warmup = warmup;
		return this;
	}

	/**
	 * Creates the int stream.
	 *
	 * @param limit the limit
	 * @return the int stream
	 */
	private IntStream createIntStream(long limit) {
		IntStream stream = IntStream.iterate(0, idx -> idx + 1).limit(limit);
		return stream;
	}

	/**
	 * Gets the next id.
	 *
	 * @return the next id
	 */
	private String getNextId() {
		return String.valueOf(testCount.getAndIncrement());
	}

}
