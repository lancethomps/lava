package com.github.lancethomps.lava.common.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The Class CachedAndQueuedThreadPoolExecutor.
 */
public class CachedAndQueuedThreadPoolExecutor extends java.util.concurrent.ThreadPoolExecutor {

	/** The last context stopped time. */
	private final AtomicLong lastContextStoppedTime = new AtomicLong(0L);

	/** The last time thread killed itself. */
	private final AtomicLong lastTimeThreadKilledItself = new AtomicLong(0L);

	/** The submitted count. */
	private final AtomicInteger submittedCount = new AtomicInteger(0);

	/** The thread renewal delay. */
	private long threadRenewalDelay = 1000L;

	/** The use current thread name suffix. */
	private boolean useCurrentThreadNameSuffix = true;

	/**
	 * Instantiates a new cached and queued thread pool executor.
	 *
	 * @param corePoolSize the core pool size
	 * @param maximumPoolSize the maximum pool size
	 * @param keepAliveTime the keep alive time
	 * @param unit the unit
	 * @param workQueue the work queue
	 */
	public CachedAndQueuedThreadPoolExecutor(
		int corePoolSize,
		int maximumPoolSize,
		long keepAliveTime,
		TimeUnit unit,
		BlockingQueue<Runnable> workQueue
	) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new CachedAndQueuedThreadPoolExecutor.RejectHandler());
	}

	/**
	 * Instantiates a new cached and queued thread pool executor.
	 *
	 * @param corePoolSize the core pool size
	 * @param maximumPoolSize the maximum pool size
	 * @param keepAliveTime the keep alive time
	 * @param unit the unit
	 * @param workQueue the work queue
	 * @param handler the handler
	 */
	public CachedAndQueuedThreadPoolExecutor(
		int corePoolSize,
		int maximumPoolSize,
		long keepAliveTime,
		TimeUnit unit,
		BlockingQueue<Runnable> workQueue,
		RejectedExecutionHandler handler
	) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	/**
	 * Instantiates a new cached and queued thread pool executor.
	 *
	 * @param corePoolSize the core pool size
	 * @param maximumPoolSize the maximum pool size
	 * @param keepAliveTime the keep alive time
	 * @param unit the unit
	 * @param workQueue the work queue
	 * @param threadFactory the thread factory
	 */
	public CachedAndQueuedThreadPoolExecutor(
		int corePoolSize,
		int maximumPoolSize,
		long keepAliveTime,
		TimeUnit unit,
		BlockingQueue<Runnable> workQueue,
		ThreadFactory threadFactory
	) {
		super(
			corePoolSize,
			maximumPoolSize,
			keepAliveTime,
			unit,
			workQueue,
			threadFactory,
			new CachedAndQueuedThreadPoolExecutor.RejectHandler()
		);
	}

	/**
	 * Instantiates a new cached and queued thread pool executor.
	 *
	 * @param corePoolSize the core pool size
	 * @param maximumPoolSize the maximum pool size
	 * @param keepAliveTime the keep alive time
	 * @param unit the unit
	 * @param workQueue the work queue
	 * @param threadFactory the thread factory
	 * @param handler the handler
	 */
	public CachedAndQueuedThreadPoolExecutor(
		int corePoolSize,
		int maximumPoolSize,
		long keepAliveTime,
		TimeUnit unit,
		BlockingQueue<Runnable> workQueue,
		ThreadFactory threadFactory,
		RejectedExecutionHandler handler
	) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}

	/**
	 * Context stopping.
	 */
	public void contextStopping() {
		lastContextStoppedTime.set(System.currentTimeMillis());
		int savedCorePoolSize = getCorePoolSize();
		TaskQueue taskQueue = getQueue() instanceof TaskQueue ? (TaskQueue) getQueue() : null;
		if (taskQueue != null) {
			taskQueue.setForcedRemainingCapacity(Integer.valueOf(0));
		}

		setCorePoolSize(0);
		if (taskQueue != null) {
			taskQueue.setForcedRemainingCapacity((Integer) null);
		}

		setCorePoolSize(savedCorePoolSize);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.ThreadPoolExecutor#execute(java.lang.Runnable)
	 */
	@Override
	public void execute(Runnable command) {
		if (useCurrentThreadNameSuffix && !(command instanceof RunnableFuture) && !(command instanceof WrappedCommandWithOriginalThreadName)) {
			final Runnable wrapper = new WrappedCommandWithOriginalThreadName<>(command);
			this.execute(wrapper, 0L, TimeUnit.MILLISECONDS);
			return;
		}
		this.execute(command, 0L, TimeUnit.MILLISECONDS);
	}

	/**
	 * Execute.
	 *
	 * @param command the command
	 * @param timeout the timeout
	 * @param unit the unit
	 */
	public void execute(Runnable command, long timeout, TimeUnit unit) {
		submittedCount.incrementAndGet();

		try {
			super.execute(command);
		} catch (RejectedExecutionException arg8) {
			if (!(super.getQueue() instanceof TaskQueue)) {
				submittedCount.decrementAndGet();
				throw arg8;
			}

			TaskQueue queue = (TaskQueue) super.getQueue();

			try {
				if (!queue.force(command, timeout, unit)) {
					submittedCount.decrementAndGet();
					throw new RejectedExecutionException("Queue capacity is full.");
				}
			} catch (InterruptedException arg7) {
				submittedCount.decrementAndGet();
				throw new RejectedExecutionException(arg7);
			}
		}

	}

	/**
	 * Gets the submitted count.
	 *
	 * @return the submitted count
	 */
	public int getSubmittedCount() {
		return submittedCount.get();
	}

	/**
	 * Gets the thread renewal delay.
	 *
	 * @return the thread renewal delay
	 */
	public long getThreadRenewalDelay() {
		return threadRenewalDelay;
	}

	/**
	 * @return the useCurrentThreadNameSuffix
	 */
	public boolean isUseCurrentThreadNameSuffix() {
		return useCurrentThreadNameSuffix;
	}

	/**
	 * Sets the thread renewal delay.
	 *
	 * @param threadRenewalDelay the new thread renewal delay
	 */
	public void setThreadRenewalDelay(long threadRenewalDelay) {
		this.threadRenewalDelay = threadRenewalDelay;
	}

	/**
	 * @param useCurrentThreadNameSuffix the useCurrentThreadNameSuffix to set
	 */
	public void setUseCurrentThreadNameSuffix(boolean useCurrentThreadNameSuffix) {
		this.useCurrentThreadNameSuffix = useCurrentThreadNameSuffix;
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		if (useCurrentThreadNameSuffix) {
			final Callable<T> wrapper = new WrappedCommandWithOriginalThreadName<>(task);
			return super.submit(wrapper);
		}
		return super.submit(task);
	}

	@Override
	public Future<?> submit(Runnable task) {
		if (useCurrentThreadNameSuffix) {
			final Runnable wrapper = new WrappedCommandWithOriginalThreadName<>(task);
			return super.submit(wrapper);
		}
		return super.submit(task);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable,
	 * java.lang.Throwable)
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		submittedCount.decrementAndGet();
		if (t == null) {
			stopCurrentThreadIfNeeded();
		}

	}

	/**
	 * Current thread should be stopped.
	 *
	 * @return true, if successful
	 */
	protected boolean currentThreadShouldBeStopped() {
		if ((threadRenewalDelay >= 0L) && (Thread.currentThread() instanceof TaskThread)) {
			TaskThread currentTaskThread = (TaskThread) Thread.currentThread();
			if (currentTaskThread.getCreationTime() < lastContextStoppedTime.longValue()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Stop current thread if needed.
	 */
	protected void stopCurrentThreadIfNeeded() {
		if (currentThreadShouldBeStopped()) {
			long lastTime = lastTimeThreadKilledItself.longValue();
			if (((lastTime + threadRenewalDelay) < System.currentTimeMillis())
				&& lastTimeThreadKilledItself.compareAndSet(lastTime, System.currentTimeMillis() + 1L)) {
				String msg = String.format("Thread [%s] stopped to avoid potential leak!", Thread.currentThread().getName());
				throw new StopPooledThreadException(msg);
			}
		}

	}

	/**
	 * The Class RejectHandler.
	 */
	private static final class RejectHandler implements RejectedExecutionHandler {

		/**
		 * Instantiates a new reject handler.
		 */
		private RejectHandler() {
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.concurrent.RejectedExecutionHandler#rejectedExecution(java.lang.Runnable,
		 * java.util.concurrent.ThreadPoolExecutor)
		 */
		@Override
		public void rejectedExecution(Runnable r, java.util.concurrent.ThreadPoolExecutor executor) {
			throw new RejectedExecutionException();
		}
	}
}
