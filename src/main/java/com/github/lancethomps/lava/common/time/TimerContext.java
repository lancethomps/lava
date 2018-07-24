package com.github.lancethomps.lava.common.time;

import javax.annotation.Nonnull;

import com.github.lancethomps.lava.common.lambda.ThrowingSupplier;

/**
 * The Class TimerContext.
 */
public class TimerContext implements AutoCloseable {

	/** The watch. */
	private final Stopwatch watch;

	/**
	 * Instantiates a new timer context.
	 *
	 * @param watch the watch
	 */
	public TimerContext(@Nonnull Stopwatch watch) {
		super();
		this.watch = watch;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		pause();
	}

	/**
	 * Gets the and close.
	 *
	 * @param <T> the generic type
	 * @param supplier the supplier
	 * @return the and close
	 * @throws Exception the exception
	 */
	public <T> T getAndClose(@Nonnull ThrowingSupplier<T> supplier) throws Exception {
		try {
			return supplier.get();
		} finally {
			pause();
		}
	}

	/**
	 * Gets the watch.
	 *
	 * @return the watch
	 */
	public Stopwatch getWatch() {
		return watch;
	}

	/**
	 * Pause.
	 *
	 * @return the timer context
	 */
	public TimerContext pause() {
		watch.suspend();
		return this;
	}

}
