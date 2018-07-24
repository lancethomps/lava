package com.github.lancethomps.lava.common.time;

/**
 * The Class AtomicStopwatch.
 *
 * @author lathomps
 */
public class AtomicStopwatch extends Stopwatch {

	/**
	 * Instantiates a new atomic stopwatch.
	 */
	public AtomicStopwatch() {
		this(false);
	}

	/**
	 * Instantiates a new atomic stopwatch.
	 *
	 * @param start the start
	 */
	public AtomicStopwatch(boolean start) {
		super(start);
	}

	/**
	 * Creates the and start.
	 *
	 * @return the atomic stopwatch
	 */
	public static AtomicStopwatch createAndStart() {
		return new AtomicStopwatch(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.Stopwatch#start()
	 */
	@Override
	public void start() {
		synchronized (this) {
			super.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.Stopwatch#startOrResume()
	 */
	@Override
	public TimerContext startOrResume() {
		synchronized (this) {
			return super.startOrResume();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.Stopwatch#suspend()
	 */
	@Override
	public void suspend() {
		synchronized (this) {
			super.suspend();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.Stopwatch#suspendIfNeeded()
	 */
	@Override
	public void suspendIfNeeded() {
		synchronized (this) {
			super.suspendIfNeeded();
		}
	}
}
