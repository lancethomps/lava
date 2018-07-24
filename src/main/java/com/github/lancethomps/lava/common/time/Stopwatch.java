package com.github.lancethomps.lava.common.time;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class Stopwatch. An extension of the Apache commons Stopwatch that does not throw exceptions,
 * along with some added methods for common functionality
 */
public class Stopwatch extends StopWatch {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(Stopwatch.class);

	/** The add time nanos. */
	private long addTimeNanos;

	/** The level. */
	private final Level level;

	/**
	 * Instantiates a new stopwatch.
	 */
	public Stopwatch() {
		this(false);
	}

	/**
	 * Instantiates a new stopwatch.
	 *
	 * @param start the start
	 */
	public Stopwatch(boolean start) {
		this(start, Level.ERROR);
	}

	/**
	 * Instantiates a new stopwatch.
	 *
	 * @param start the start
	 * @param level the level
	 */
	public Stopwatch(boolean start, final Level level) {
		super();
		this.level = level == null ? Level.ERROR : level;
		if (start) {
			start();
		}
	}

	/**
	 * Creates the and start.
	 *
	 * @return the stopwatch
	 */
	public static Stopwatch createAndStart() {
		return new Stopwatch(true);
	}

	/**
	 * Adds the time from other.
	 *
	 * @param other the other
	 * @return the stopwatch
	 */
	public Stopwatch addTimeFromOther(Stopwatch other) {
		if (other != null) {
			synchronized (this) {
				addTimeNanos += other.getNanoTime();
			}
		}
		return this;
	}

	/**
	 * Creates the timer context.
	 *
	 * @return the timer context
	 */
	public TimerContext createTimerContext() {
		return new TimerContext(this);
	}

	/**
	 * @return the addTimeNanos
	 */
	public long getAddTimeNanos() {
		return addTimeNanos;
	}

	@Override
	public long getNanoTime() {
		try {
			return super.getNanoTime() + addTimeNanos;
		} catch (Throwable e) {
			Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
			return -1L;
		}
	}

	@Override
	public long getSplitNanoTime() {
		try {
			return super.getSplitNanoTime();
		} catch (Throwable e) {
			Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
			return -1L;
		}
	}

	@Override
	public long getSplitTime() {
		try {
			return super.getSplitTime();
		} catch (Throwable e) {
			Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
			return -1L;
		}
	}

	/**
	 * Gets the split time.
	 *
	 * @param split the split
	 * @return the split time
	 */
	public long getSplitTime(boolean split) {
		if (split) {
			split();
		}
		long time = getSplitTime();
		if (split) {
			unsplit();
		}
		return time;
	}

	@Override
	public long getStartTime() {
		try {
			return super.getStartTime();
		} catch (Throwable e) {
			Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
			return -1L;
		}
	}

	@Override
	public long getTime() {
		try {
			return super.getTime();
		} catch (Throwable e) {
			Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
			return -1L;
		}
	}

	@Override
	public long getTime(final TimeUnit timeUnit) {
		try {
			return super.getTime(timeUnit);
		} catch (Throwable e) {
			Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
			return -1L;
		}
	}

	/**
	 * Gets the time and reset.
	 *
	 * @param start the start
	 * @return the time and reset
	 */
	public long getTimeAndReset(boolean start) {
		long time = getTime();
		reset(start);
		return time;
	}

	@Override
	public void reset() {
		try {
			super.reset();
		} catch (Throwable e) {
			Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
		}
	}

	/**
	 * Reset.
	 *
	 * @param start the start
	 * @return the stopwatch
	 */
	public Stopwatch reset(boolean start) {
		reset();
		if (start) {
			start();
		}
		return this;
	}

	@Override
	public void resume() {
		try {
			super.resume();
		} catch (Throwable e) {
			Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
		}
	}

	@Override
	public void split() {
		try {
			super.split();
		} catch (Throwable e) {
			Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
		}
	}

	@Override
	public void start() {
		try {
			super.start();
		} catch (Throwable e) {
			Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
		}
	}

	/**
	 * Start or resume.
	 *
	 * @return the timer context
	 */
	public TimerContext startOrResume() {
		if (!isStarted()) {
			start();
		} else if (isSuspended()) {
			resume();
		}
		return createTimerContext();
	}

	@Override
	public void stop() {
		try {
			super.stop();
		} catch (Throwable e) {
			Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
		}
	}

	@Override
	public void suspend() {
		try {
			super.suspend();
		} catch (Throwable e) {
			Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
		}
	}

	/**
	 * Suspend if needed.
	 */
	public void suspendIfNeeded() {
		if (!isSuspended()) {
			suspend();
		}
	}

	@Override
	public void unsplit() {
		try {
			super.unsplit();
		} catch (Throwable e) {
			Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
		}
	}

}
