package com.github.lancethomps.lava.common.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The Class RejectingQueueThreadPoolExecutor.
 */
public class RejectingQueueThreadPoolExecutor extends ThreadPoolExecutor {

	/** The reject count. */
	private AtomicLong rejectCount = new AtomicLong(0);

	/**
	 * Instantiates a new rejecting queue thread pool executor.
	 *
	 * @param corePoolSize the core pool size
	 * @param maximumPoolSize the maximum pool size
	 * @param keepAliveTime the keep alive time
	 * @param unit the unit
	 * @param workQueue the work queue
	 * @param threadFactory the thread factory
	 * @param handler the handler
	 */
	public RejectingQueueThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
		RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}

	/**
	 * Adds the rejection.
	 */
	public void addRejection() {
		rejectCount.incrementAndGet();
	}

	@Override
	public String toString() {
		String str = super.toString();
		return str.substring(0, str.length() - 1) + ", rejected to queue = " + rejectCount.get() + ']';
	}

}
