package com.github.lancethomps.lava.common.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A factory for creating CustomNamingThread objects.
 */
public class CustomNamingThreadFactory implements ThreadFactory {

	/** The daemon flag. */
	private final Boolean daemonFlag;

	/** The name prefix. */
	private final String namePrefix;

	/** The priority. */
	private final Integer priority;

	/** The thread counter. */
	private final AtomicLong threadCounter;

	/** The wrapped factory. */
	private final ThreadFactory wrappedFactory;

	/**
	 * Instantiates a new custom naming thread factory.
	 *
	 * @param namePrefix the name prefix
	 * @param priority the priority
	 * @param daemonFlag the daemon flag
	 */
	public CustomNamingThreadFactory(String namePrefix, Integer priority, Boolean daemonFlag) {
		super();
		this.namePrefix = namePrefix;
		this.priority = priority;
		this.daemonFlag = daemonFlag;

		wrappedFactory = Executors.defaultThreadFactory();
		threadCounter = new AtomicLong();
	}

	/**
	 * Gets the daemon flag.
	 *
	 * @return the daemon flag
	 */
	public final Boolean getDaemonFlag() {
		return daemonFlag;
	}

	/**
	 * Gets the name prefix.
	 *
	 * @return the namePrefix
	 */
	public String getNamePrefix() {
		return namePrefix;
	}

	/**
	 * Gets the priority.
	 *
	 * @return the priority
	 */
	public final Integer getPriority() {
		return priority;
	}

	/**
	 * Gets the thread count.
	 *
	 * @return the thread count
	 */
	public long getThreadCount() {
		return threadCounter.get();
	}

	/**
	 * Gets the wrapped factory.
	 *
	 * @return the wrapped factory
	 */
	public final ThreadFactory getWrappedFactory() {
		return wrappedFactory;
	}

	/**
	 * New thread.
	 *
	 * @param runnable the runnable
	 * @return the thread
	 */
	@Override
	public Thread newThread(final Runnable runnable) {
		final Thread thread = getWrappedFactory().newThread(runnable);
		initializeThread(thread);

		return thread;
	}

	/**
	 * Initialize thread.
	 *
	 * @param thread the thread
	 */
	private void initializeThread(final Thread thread) {
		thread.setName(namePrefix + '-' + threadCounter.incrementAndGet() + '#' + Thread.currentThread().getName());

		if (getPriority() != null) {
			thread.setPriority(getPriority().intValue());
		}

		if (getDaemonFlag() != null) {
			thread.setDaemon(getDaemonFlag().booleanValue());
		}
	}

}
