package com.github.lancethomps.lava.common.logging;

import static com.github.lancethomps.lava.common.logging.Logs.LOG_INTERVAL;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.time.Stopwatch;
import com.github.lancethomps.lava.common.time.TimerEnabledBean;
import com.github.lancethomps.lava.common.time.TimerHandlingBean;
import com.github.lancethomps.lava.common.time.Timing;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.util.concurrent.AtomicDouble;

/**
 * The Class LogIntervalData.
 */
public class LogIntervalData implements TimerEnabledBean, TimerHandlingBean {

	/** The cancel requested. */
	private boolean cancelRequested;

	/** The count. */
	private AtomicLong count = new AtomicLong();

	/** The counts. */
	private Map<String, AtomicLong> counts;

	/** The debug data. */
	private Map<String, Object> debugData;

	/** The id. */
	private String id;

	/** The log interval. */
	private double logInterval = LOG_INTERVAL;

	/** The msg. */
	private String msg;

	/** The next log pct. */
	private AtomicDouble nextLogPct;

	/** The timer logs. */
	private Map<String, Long> timerLogs;

	/** The total. */
	private long total;

	/** The watch. */
	private Stopwatch watch;

	/** The watches. */
	@JsonIgnore
	private Map<String, Stopwatch> watches;

	/**
	 * Instantiates a new log interval data.
	 */
	public LogIntervalData() {
		super();
	}

	/**
	 * Instantiates a new log interval data.
	 *
	 * @param total the total
	 * @param msg the msg
	 */
	public LogIntervalData(long total, String msg) {
		this(total, msg, LOG_INTERVAL);
	}

	/**
	 * Instantiates a new log interval data.
	 *
	 * @param total the total
	 * @param msg the msg
	 * @param logInterval the log interval
	 */
	public LogIntervalData(long total, String msg, double logInterval) {
		super();
		this.total = total;
		this.msg = msg;
		this.logInterval = logInterval;
		nextLogPct = new AtomicDouble(logInterval);
		watch = Timing.getStopwatch();
	}

	/**
	 * Adds the debug data.
	 *
	 * @param debugData the debug data
	 * @return the t
	 */
	public LogIntervalData addDebugData(Map<String, Object> debugData) {
		if (debugData != null) {
			if (this.debugData == null) {
				this.debugData = new LinkedHashMap<>();
			}
			this.debugData.putAll(debugData);
		}
		return this;
	}

	/**
	 * Adds the debug data.
	 *
	 * @param key the key
	 * @param val the val
	 * @return the t
	 */
	public LogIntervalData addDebugData(String key, Object val) {
		if ((key != null) && (val != null)) {
			if (debugData == null) {
				debugData = new LinkedHashMap<>();
			}
			debugData.put(key, val);
		}
		return this;
	}

	/**
	 * Adds the to count.
	 *
	 * @param key the key
	 * @return the log interval data
	 */
	public LogIntervalData addToCount(@Nonnull String key) {
		return addToCount(key, 1);
	}

	/**
	 * Adds the to count.
	 *
	 * @param key the key
	 * @param delta the delta
	 * @return the log interval data
	 */
	public LogIntervalData addToCount(@Nonnull String key, long delta) {
		if (counts == null) {
			counts = new LinkedHashMap<>();
		}
		counts.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(delta);
		return this;
	}

	/**
	 * Adds the to counts.
	 *
	 * @param <T> the generic type
	 * @param otherCounts the other counts
	 * @return the log interval data
	 */
	public <T extends Number> LogIntervalData addToCounts(@Nonnull Map<String, T> otherCounts) {
		if (Checks.isNotEmpty(otherCounts)) {
			if (counts == null) {
				counts = new LinkedHashMap<>();
			}
			otherCounts.forEach((key, val) -> counts.merge(key, val instanceof AtomicLong ? (AtomicLong) val : new AtomicLong(val.longValue()), (curr, add) -> {
				curr.addAndGet(add.get());
				return curr;
			}));
		}
		return this;
	}

	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	public AtomicLong getCount() {
		return count;
	}

	/**
	 * @return the counts
	 */
	public Map<String, AtomicLong> getCounts() {
		return counts;
	}

	/**
	 * Gets the debug data.
	 *
	 * @return the debugData
	 */
	public Map<String, Object> getDebugData() {
		return debugData;
	}

	/**
	 * Gets the debug data point.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the debug data point
	 */
	public <T> T getDebugDataPoint(String key) {
		return debugData == null ? null : (T) debugData.get(key);
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the log interval.
	 *
	 * @return the logInterval
	 */
	public double getLogInterval() {
		return logInterval;
	}

	/**
	 * Gets the msg.
	 *
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * Gets the next log pct.
	 *
	 * @return the nextLogPct
	 */
	public AtomicDouble getNextLogPct() {
		return nextLogPct;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerHandlingBean#getTimerLogs()
	 */
	@Override
	public Map<String, Long> getTimerLogs() {
		return timerLogs;
	}

	/**
	 * Gets the total.
	 *
	 * @return the total
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * Gets the watch.
	 *
	 * @return the watch
	 */
	public Stopwatch getWatch() {
		return watch;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#getWatches()
	 */
	@Override
	public Map<String, Stopwatch> getWatches() {
		return watches;
	}

	/**
	 * Checks if is cancel requested.
	 *
	 * @return the cancelRequested
	 */
	public boolean isCancelRequested() {
		return cancelRequested;
	}

	/**
	 * Sets the cancel requested.
	 *
	 * @param cancelRequested the cancelRequested to set
	 * @return the log interval data
	 */
	public LogIntervalData setCancelRequested(boolean cancelRequested) {
		this.cancelRequested = cancelRequested;
		return this;
	}

	/**
	 * Sets the count.
	 *
	 * @param count the count to set
	 * @return the log interval data
	 */
	public LogIntervalData setCount(AtomicLong count) {
		this.count = count;
		return this;
	}

	/**
	 * Sets the counts.
	 *
	 * @param counts the counts to set
	 * @return the log interval data
	 */
	public LogIntervalData setCounts(Map<String, AtomicLong> counts) {
		this.counts = counts;
		return this;
	}

	/**
	 * Sets the debug data.
	 *
	 * @param debugData the debugData to set
	 * @return the log interval data
	 */
	public LogIntervalData setDebugData(Map<String, Object> debugData) {
		this.debugData = debugData;
		return this;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the id to set
	 * @return the log interval data
	 */
	public LogIntervalData setId(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Sets the log interval.
	 *
	 * @param logInterval the logInterval to set
	 * @return the log interval data
	 */
	public LogIntervalData setLogInterval(double logInterval) {
		this.logInterval = logInterval;
		return this;
	}

	/**
	 * Sets the msg.
	 *
	 * @param msg the msg to set
	 * @return the log interval data
	 */
	public LogIntervalData setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	/**
	 * Sets the next log pct.
	 *
	 * @param nextLogPct the nextLogPct to set
	 * @return the log interval data
	 */
	public LogIntervalData setNextLogPct(AtomicDouble nextLogPct) {
		this.nextLogPct = nextLogPct;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerHandlingBean#setTimerLogs(java.util.Map)
	 */
	@Override
	public <T extends TimerHandlingBean> T setTimerLogs(Map<String, Long> timerLogs) {
		this.timerLogs = timerLogs;
		return (T) this;
	}

	/**
	 * Sets the total.
	 *
	 * @param total the total to set
	 * @return the log interval data
	 */
	public LogIntervalData setTotal(long total) {
		this.total = total;
		return this;
	}

	/**
	 * Sets the watch.
	 *
	 * @param watch the watch to set
	 * @return the log interval data
	 */
	public LogIntervalData setWatch(Stopwatch watch) {
		this.watch = watch;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#setWatches(java.util.Map)
	 */
	@Override
	public <T extends TimerEnabledBean> T setWatches(Map<String, Stopwatch> watches) {
		this.watches = watches;
		return (T) this;
	}

}
