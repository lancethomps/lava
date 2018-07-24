package com.github.lancethomps.lava.common.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A factory for creating TaskThread objects.
 */
public class TaskThreadFactory implements ThreadFactory {

	/** The Constant IS_SECURITY_ENABLED. */
	public static final boolean IS_SECURITY_ENABLED = System.getSecurityManager() != null;

	/** The daemon. */
	private final boolean daemon;

	/** The group. */
	private final ThreadGroup group;

	/** The name prefix. */
	private final String namePrefix;

	/** The thread number. */
	private final AtomicInteger threadCounter = new AtomicInteger(1);

	/** The thread priority. */
	private final int threadPriority;

	/**
	 * Instantiates a new task thread factory.
	 *
	 * @param namePrefix the name prefix
	 * @param daemon the daemon
	 * @param priority the priority
	 */
	public TaskThreadFactory(String namePrefix, boolean daemon, int priority) {
		SecurityManager s = System.getSecurityManager();
		group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		this.namePrefix = namePrefix;
		this.daemon = daemon;
		threadPriority = priority;
	}

	/**
	 * @return the namePrefix
	 */
	public String getNamePrefix() {
		return namePrefix;
	}

	/**
	 * @return the threadPriority
	 */
	public int getThreadPriority() {
		return threadPriority;
	}

	/**
	 * @return the daemon
	 */
	public boolean isDaemon() {
		return daemon;
	}

	@Override
	public Thread newThread(Runnable runnable) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		boolean arg8 = false;
		TaskThread thread;
		try {
			arg8 = true;
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			thread = createThread(runnable);
			arg8 = false;
		} finally {
			if (arg8) {
				Thread.currentThread().setContextClassLoader(loader);
			}
		}
		Thread.currentThread().setContextClassLoader(loader);
		return thread;
	}

	/**
	 * Initialize thread.
	 *
	 * @param runnable the runnable
	 * @return the task thread
	 */
	private TaskThread createThread(final Runnable runnable) {
		final TaskThread thread = new TaskThread(group, runnable, namePrefix + '-' + threadCounter.incrementAndGet());
		// final TaskThread thread = new TaskThread(group, runnable, namePrefix + '-' + threadCounter.incrementAndGet() + '#' + Thread.currentThread().getName());
		thread.setDaemon(daemon);
		thread.setPriority(threadPriority);
		return thread;
	}
}
