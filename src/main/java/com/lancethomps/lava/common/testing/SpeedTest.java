package com.lancethomps.lava.common.testing;

import static com.lancethomps.lava.common.logging.Logs.println;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.lancethomps.lava.common.collections.MapUtil;
import com.lancethomps.lava.common.lambda.ThrowingFunction;
import com.lancethomps.lava.common.lambda.ThrowingIntConsumer;
import com.lancethomps.lava.common.lambda.ThrowingRunnable;
import com.lancethomps.lava.common.logging.LogIntervalData;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.math.MathUtil;
import com.lancethomps.lava.common.sorting.SortClause;
import com.lancethomps.lava.common.sorting.SortOrder;
import com.lancethomps.lava.common.sorting.Sorting;
import com.lancethomps.lava.common.string.StringUtil;
import com.lancethomps.lava.common.time.Stopwatch;
import com.lancethomps.lava.common.time.Timing;

public class SpeedTest {

  @JsonIgnore
  private final List<SpeedTestFunction> functions = new ArrayList<>();
  @JsonIgnore
  private final AtomicInteger testCount = new AtomicInteger(0);
  @JsonIgnore
  private ThrowingIntConsumer betweenFunction;
  private long iterations = 100000;
  private boolean parallel;
  private Integer repeatCount;
  private Map<String, Map<String, Object>> results;
  private boolean reverse;
  private List<String> speedRanking;
  private long warmup = 10000;

  public SpeedTest addTest(@Nullable String id, @Nonnull ThrowingFunction<Integer, Long> test) {
    return addTests(new SpeedTestFunction(id != null ? id : getNextId(), test));
  }

  public SpeedTest addTest(@Nullable String id, @Nonnull ThrowingIntConsumer test) {
    return addTests(new SpeedTestFunction(id != null ? id : getNextId(), (idx) -> {
      test.accept(idx);
      return null;
    }));
  }

  public SpeedTest addTest(@Nullable String id, @Nonnull ThrowingRunnable test) {
    return addTests(new SpeedTestFunction(id != null ? id : getNextId(), (idx) -> {
      test.run();
      return null;
    }));
  }

  public SpeedTest addTests(@Nonnull SpeedTestFunction... tests) {
    Stream.of(tests).peek(test -> {
      if (test.getId() == null) {
        test.setId(getNextId());
      }
    }).forEach(functions::add);
    return this;
  }

  public SpeedTest addTests(@Nonnull ThrowingFunction<Integer, Long>... tests) {
    Stream.of(tests).map(f -> new SpeedTestFunction(f, getNextId())).forEach(functions::add);
    return this;
  }

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

  public ThrowingIntConsumer getBetweenFunction() {
    return betweenFunction;
  }

  public SpeedTest setBetweenFunction(ThrowingIntConsumer betweenFunction) {
    this.betweenFunction = betweenFunction;
    return this;
  }

  public List<SpeedTestFunction> getFunctions() {
    return functions;
  }

  public long getIterations() {
    return iterations;
  }

  public SpeedTest setIterations(long iterations) {
    this.iterations = iterations;
    return this;
  }

  public Integer getRepeatCount() {
    return repeatCount;
  }

  public SpeedTest setRepeatCount(Integer repeatCount) {
    this.repeatCount = repeatCount;
    return this;
  }

  public Map<String, Map<String, Object>> getResults() {
    return results;
  }

  public List<String> getSpeedRanking() {
    return speedRanking;
  }

  public long getWarmup() {
    return warmup;
  }

  public SpeedTest setWarmup(long warmup) {
    this.warmup = warmup;
    return this;
  }

  public boolean isParallel() {
    return parallel;
  }

  public SpeedTest setParallel(boolean parallel) {
    this.parallel = parallel;
    return this;
  }

  public boolean isReverse() {
    return reverse;
  }

  public SpeedTest setReverse(boolean reverse) {
    this.reverse = reverse;
    return this;
  }

  public SpeedTest run() throws Exception {
    final List<SpeedTestFunction> functions = new ArrayList<>(this.functions);
    String totalKey = "total";
    String meanKey = "mean";
    if ((repeatCount != null) && (repeatCount > 0)) {
      IntStream.iterate(0, i -> i + 1).limit(repeatCount).forEach(i -> {
        this.functions
          .stream()
          .map(f -> new SpeedTestFunction(f.getFunction(), f.getId() + "-COPY-" + repeatCount.toString()))
          .forEach(functions::add);
      });
    }
    Logger logger = LogManager.getLogger(SpeedTest.class);
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
      BigDecimal median = BigDecimal.valueOf(
        (timers.get((int) Math.floor((timers.size() - 1) / 2)) / 2.0) + (timers.get((int) Math.ceil((timers.size() - 1) / 2)) / 2.0));
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
        result.put(
          "comparison (this ÷ other)",
          results.keySet().stream().filter(key -> id.compareTo(key) != 0).collect(Collectors.toMap(Function.identity(), otherPos -> {
            Map<String, Object> other = results.get(otherPos);
            return String.format("%.2f%%", 100d * (avg.doubleValue() / ((BigDecimal) other.get(meanKey)).doubleValue()));
          }, (a, b) -> a, TreeMap::new))
        );
      });
    }
    return this;
  }

  private IntStream createIntStream(long limit) {
    IntStream stream = IntStream.iterate(0, idx -> idx + 1).limit(limit);
    return stream;
  }

  private String getNextId() {
    return String.valueOf(testCount.getAndIncrement());
  }

}
