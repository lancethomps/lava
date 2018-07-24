package com.github.lancethomps.lava.common.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A factory for creating InheritedNameThread objects.
 */
public class InheritedNameThreadFactory implements ThreadFactory {

	/** The group. */
	private final ThreadGroup group;

	/** The thread number. */
	private final AtomicInteger threadNumber = new AtomicInteger(1);

	/** The name suffix. */
	private final String nameSuffix;

	/**
	 * Instantiates a new inherited name thread factory.
	 *
	 * @param nameSuffix the name suffix
	 */
	public InheritedNameThreadFactory(String nameSuffix) {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		this.nameSuffix = nameSuffix;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, Thread.currentThread().getName() + '-' + nameSuffix + '-' + threadNumber.getAndIncrement(), 0);
		if (t.isDaemon()) {
			t.setDaemon(false);
		}
		if (t.getPriority() != Thread.NORM_PRIORITY) {
			t.setPriority(Thread.NORM_PRIORITY);
		}
		return t;
	}

}
