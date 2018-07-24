package com.github.lancethomps.lava.common.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A factory for creating ScheduledThread objects.
 */
public class ScheduledThreadFactory implements ThreadFactory {

	/** The group. */
	private final ThreadGroup group;

	/** The thread number. */
	private final AtomicInteger threadNumber = new AtomicInteger(1);

	/** The name prefix. */
	private final String namePrefix;

	/**
	 * Instantiates a new scheduled thread factory.
	 *
	 * @param name the name
	 */
	public ScheduledThreadFactory(String name) {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		namePrefix = name + "-thread-";
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r,
			namePrefix + threadNumber.getAndIncrement(),
			0);
		if (t.isDaemon()) {
			t.setDaemon(false);
		}
		if (t.getPriority() != Thread.NORM_PRIORITY) {
			t.setPriority(Thread.NORM_PRIORITY);
		}
		return t;
	}
}
