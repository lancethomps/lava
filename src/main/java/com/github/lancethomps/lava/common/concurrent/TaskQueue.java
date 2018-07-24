package com.github.lancethomps.lava.common.concurrent;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * The Class TaskQueue.
 */
public class TaskQueue extends LinkedBlockingQueue<Runnable> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The forced remaining capacity. */
	private Integer forcedRemainingCapacity;

	/** The parent. */
	private volatile CachedAndQueuedThreadPoolExecutor parent;

	/**
	 * Instantiates a new task queue.
	 */
	public TaskQueue() {
	}

	/**
	 * Instantiates a new task queue.
	 *
	 * @param c the c
	 */
	public TaskQueue(Collection<? extends Runnable> c) {
		super(c);
	}

	/**
	 * Instantiates a new task queue.
	 *
	 * @param capacity the capacity
	 */
	public TaskQueue(int capacity) {
		super(capacity);
	}

	/**
	 * Force.
	 *
	 * @param o the o
	 * @return true, if successful
	 */
	public boolean force(Runnable o) {
		if ((parent != null) && !parent.isShutdown()) {
			return super.offer(o);
		}
		throw new RejectedExecutionException("Executor not running, can\'t force a command into the queue");
	}

	/**
	 * Force.
	 *
	 * @param o the o
	 * @param timeout the timeout
	 * @param unit the unit
	 * @return true, if successful
	 * @throws InterruptedException the interrupted exception
	 */
	public boolean force(Runnable o, long timeout, TimeUnit unit) throws InterruptedException {
		if ((parent != null) && !parent.isShutdown()) {
			return super.offer(o, timeout, unit);
		}
		throw new RejectedExecutionException("Executor not running, can\'t force a command into the queue");
	}

	@Override
	public boolean offer(Runnable o) {
		return parent == null ? super.offer(o)
			: (parent.getPoolSize() == parent.getMaximumPoolSize() ? super.offer(o)
				: (parent.getSubmittedCount() < parent.getPoolSize() ? super.offer(o)
					: (parent.getPoolSize() < parent.getMaximumPoolSize() ? false
						: super.offer(o))));
	}

	@Override
	public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
		Runnable runnable = super.poll(timeout, unit);
		if ((runnable == null) && (parent != null)) {
			parent.stopCurrentThreadIfNeeded();
		}

		return runnable;
	}

	@Override
	public int remainingCapacity() {
		return forcedRemainingCapacity != null ? forcedRemainingCapacity.intValue()
			: super.remainingCapacity();
	}

	/**
	 * Sets the forced remaining capacity.
	 *
	 * @param forcedRemainingCapacity the new forced remaining capacity
	 */
	public void setForcedRemainingCapacity(Integer forcedRemainingCapacity) {
		this.forcedRemainingCapacity = forcedRemainingCapacity;
	}

	/**
	 * Sets the parent.
	 *
	 * @param tp the new parent
	 */
	public void setParent(CachedAndQueuedThreadPoolExecutor tp) {
		parent = tp;
	}

	@Override
	public Runnable take() throws InterruptedException {
		return (parent != null) && parent.currentThreadShouldBeStopped()
			? this.poll(parent.getKeepAliveTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
			: (Runnable) super.take();
	}
}
