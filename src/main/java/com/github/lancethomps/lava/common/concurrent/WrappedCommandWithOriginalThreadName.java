package com.github.lancethomps.lava.common.concurrent;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

/**
 * The Class WrappedRunnableWithOriginalThreadName.
 *
 * @param <T> the generic type
 */
public class WrappedCommandWithOriginalThreadName<T> implements Runnable, Callable<T> {

	/** The callable. */
	private final Callable<T> callable;

	/** The runnable. */
	private final Runnable runnable;

	/** The thread name prefix. */
	private final String threadNamePrefix;

	/**
	 * Instantiates a new wrapped runnable with original thread name.
	 *
	 * @param callable the callable
	 */
	public WrappedCommandWithOriginalThreadName(@Nonnull Callable<T> callable) {
		this(callable, null);
	}

	/**
	 * Instantiates a new wrapped runnable with original thread name.
	 *
	 * @param runnable the runnable
	 */
	public WrappedCommandWithOriginalThreadName(@Nonnull Runnable runnable) {
		this(null, runnable);
	}

	/**
	 * Instantiates a new wrapped runnable with original thread name.
	 *
	 * @param callable the callable
	 * @param runnable the runnable
	 */
	private WrappedCommandWithOriginalThreadName(Callable<T> callable, Runnable runnable) {
		super();
		this.callable = callable;
		this.runnable = runnable;
		threadNamePrefix = Thread.currentThread().getName();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public T call() throws Exception {
		try (TempThreadNamePrefixAdder threadNamePrefixAdder = new TempThreadNamePrefixAdder(threadNamePrefix)) {
			return callable.call();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try (TempThreadNamePrefixAdder threadNamePrefixAdder = new TempThreadNamePrefixAdder(threadNamePrefix)) {
			if (callable != null) {
				try {
					callable.call();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				runnable.run();
			}
		}
	}

}
