package com.lancethomps.lava.common.logging;

import static com.lancethomps.lava.common.logging.Logs.LOG_INTERVAL;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.util.concurrent.AtomicDouble;
import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.time.Stopwatch;
import com.lancethomps.lava.common.time.TimerEnabledBean;
import com.lancethomps.lava.common.time.TimerHandlingBean;
import com.lancethomps.lava.common.time.Timing;

public class LogIntervalData implements TimerEnabledBean, TimerHandlingBean {

  private String id;
  private boolean cancelRequested;
  private AtomicLong count = new AtomicLong();
  private Map<String, AtomicLong> counts;
  private Map<String, Object> debugData;
  private double logInterval = LOG_INTERVAL;

  private String msg;

  private AtomicDouble nextLogPct;

  private Map<String, Long> timerLogs;

  private long total;

  private Stopwatch watch;

  @JsonIgnore
  private Map<String, Stopwatch> watches;

  public LogIntervalData() {
    super();
  }

  public LogIntervalData(long total, String msg) {
    this(total, msg, LOG_INTERVAL);
  }

  public LogIntervalData(long total, String msg, double logInterval) {
    super();
    this.total = total;
    this.msg = msg;
    this.logInterval = logInterval;
    nextLogPct = new AtomicDouble(logInterval);
    watch = Timing.getStopwatch();
  }

  public LogIntervalData addDebugData(Map<String, Object> debugData) {
    if (debugData != null) {
      if (this.debugData == null) {
        this.debugData = new LinkedHashMap<>();
      }
      this.debugData.putAll(debugData);
    }
    return this;
  }

  public LogIntervalData addDebugData(String key, Object val) {
    if ((key != null) && (val != null)) {
      if (debugData == null) {
        debugData = new LinkedHashMap<>();
      }
      debugData.put(key, val);
    }
    return this;
  }

  public LogIntervalData addToCount(@Nonnull String key) {
    return addToCount(key, 1);
  }

  public LogIntervalData addToCount(@Nonnull String key, long delta) {
    if (counts == null) {
      counts = new LinkedHashMap<>();
    }
    counts.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(delta);
    return this;
  }

  public <T extends Number> LogIntervalData addToCounts(@Nonnull Map<String, T> otherCounts) {
    if (Checks.isNotEmpty(otherCounts)) {
      if (counts == null) {
        counts = new LinkedHashMap<>();
      }
      otherCounts.forEach((key, val) -> counts.merge(
        key,
        val instanceof AtomicLong ? (AtomicLong) val : new AtomicLong(val.longValue()),
        (curr, add) -> {
          curr.addAndGet(add.get());
          return curr;
        }
      ));
    }
    return this;
  }

  public AtomicLong getCount() {
    return count;
  }

  public LogIntervalData setCount(AtomicLong count) {
    this.count = count;
    return this;
  }

  public Map<String, AtomicLong> getCounts() {
    return counts;
  }

  public LogIntervalData setCounts(Map<String, AtomicLong> counts) {
    this.counts = counts;
    return this;
  }

  public Map<String, Object> getDebugData() {
    return debugData;
  }

  public LogIntervalData setDebugData(Map<String, Object> debugData) {
    this.debugData = debugData;
    return this;
  }

  public <T> T getDebugDataPoint(String key) {
    return debugData == null ? null : (T) debugData.get(key);
  }

  public String getId() {
    return id;
  }

  public LogIntervalData setId(String id) {
    this.id = id;
    return this;
  }

  public double getLogInterval() {
    return logInterval;
  }

  public LogIntervalData setLogInterval(double logInterval) {
    this.logInterval = logInterval;
    return this;
  }

  public String getMsg() {
    return msg;
  }

  public LogIntervalData setMsg(String msg) {
    this.msg = msg;
    return this;
  }

  public AtomicDouble getNextLogPct() {
    return nextLogPct;
  }

  public LogIntervalData setNextLogPct(AtomicDouble nextLogPct) {
    this.nextLogPct = nextLogPct;
    return this;
  }

  @Override
  public Map<String, Long> getTimerLogs() {
    return timerLogs;
  }

  public long getTotal() {
    return total;
  }

  public LogIntervalData setTotal(long total) {
    this.total = total;
    return this;
  }

  public Stopwatch getWatch() {
    return watch;
  }

  public LogIntervalData setWatch(Stopwatch watch) {
    this.watch = watch;
    return this;
  }

  @Override
  public Map<String, Stopwatch> getWatches() {
    return watches;
  }

  public boolean isCancelRequested() {
    return cancelRequested;
  }

  public LogIntervalData setCancelRequested(boolean cancelRequested) {
    this.cancelRequested = cancelRequested;
    return this;
  }

  @Override
  public <T extends TimerHandlingBean> T setTimerLogs(Map<String, Long> timerLogs) {
    this.timerLogs = timerLogs;
    return (T) this;
  }

  @Override
  public <T extends TimerEnabledBean> T setWatches(Map<String, Stopwatch> watches) {
    this.watches = watches;
    return (T) this;
  }

}
