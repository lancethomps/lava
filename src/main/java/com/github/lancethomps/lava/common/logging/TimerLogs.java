package com.github.lancethomps.lava.common.logging;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.github.lancethomps.lava.common.time.Stopwatch;

/**
 * The Class TimerLogs.
 */
public class TimerLogs {

	/** The stop watches. */
	private Map<String, Stopwatch> stopWatches = new TreeMap<>();

	/**
	 * Instantiates a new timer logs.
	 *
	 * @param key the key
	 */
	public TimerLogs(String key) {
		super();
		start(key);
	}

	/**
	 * Instantiates a new timer logs.
	 */
	public TimerLogs() {
		super();
	}

	/**
	 * Start.
	 *
	 * @param key the key
	 */
	public void start(String key) {
		if (stopWatches.containsKey(key)) {
			stopWatches.get(key).resume();
		} else {
			Stopwatch watch = new Stopwatch();
			watch.start();
			stopWatches.put(key, watch);
		}
	}

	/**
	 * Stop.
	 *
	 * @param key the key
	 */
	public void stop(String key) {
		if (stopWatches.containsKey(key)) {
			stopWatches.get(key).suspend();
		}
	}

	/**
	 * @return the stopWatches
	 */
	public Map<String, Stopwatch> getStopwatches() {
		return stopWatches;
	}

	/**
	 * Gets the times.
	 *
	 * @return the times
	 */
	public Map<String, Long> getTimes() {
		Map<String, Long> times = new TreeMap<>();
		for (Entry<String, Stopwatch> entry : stopWatches.entrySet()) {
			times.put(entry.getKey(), entry.getValue().getTime());
		}
		return times;
	}

	@Override
	public String toString() {
		String msg = "" + System.lineSeparator();
		for (Entry<String, Stopwatch> entry : stopWatches.entrySet()) {
			msg = msg + entry.getValue().getTime() + "ms" + " - " + entry.getKey() + System.lineSeparator();
		}
		return msg;
	}
}
