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

public class CachedAndQueuedThreadPoolExecutor extends java.util.concurrent.ThreadPoolExecutor {

  private final AtomicLong lastContextStoppedTime = new AtomicLong(0L);

  private final AtomicLong lastTimeThreadKilledItself = new AtomicLong(0L);

  private final AtomicInteger submittedCount = new AtomicInteger(0);

  private long threadRenewalDelay = 1000L;

  private boolean useCurrentThreadNameSuffix = true;

  public CachedAndQueuedThreadPoolExecutor(
    int corePoolSize,
    int maximumPoolSize,
    long keepAliveTime,
    TimeUnit unit,
    BlockingQueue<Runnable> workQueue
  ) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new CachedAndQueuedThreadPoolExecutor.RejectHandler());
  }

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

  public void contextStopping() {
    lastContextStoppedTime.set(System.currentTimeMillis());
    int savedCorePoolSize = getCorePoolSize();
    TaskQueue taskQueue = getQueue() instanceof TaskQueue ? (TaskQueue) getQueue() : null;
    if (taskQueue != null) {
      taskQueue.setForcedRemainingCapacity(0);
    }

    setCorePoolSize(0);
    if (taskQueue != null) {
      taskQueue.setForcedRemainingCapacity(null);
    }

    setCorePoolSize(savedCorePoolSize);
  }

  @Override
  public void execute(Runnable command) {
    if (useCurrentThreadNameSuffix && !(command instanceof RunnableFuture) && !(command instanceof WrappedCommandWithOriginalThreadName)) {
      final Runnable wrapper = new WrappedCommandWithOriginalThreadName<>(command);
      this.execute(wrapper, 0L, TimeUnit.MILLISECONDS);
      return;
    }
    this.execute(command, 0L, TimeUnit.MILLISECONDS);
  }

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

  public int getSubmittedCount() {
    return submittedCount.get();
  }

  public long getThreadRenewalDelay() {
    return threadRenewalDelay;
  }

  public void setThreadRenewalDelay(long threadRenewalDelay) {
    this.threadRenewalDelay = threadRenewalDelay;
  }

  public boolean isUseCurrentThreadNameSuffix() {
    return useCurrentThreadNameSuffix;
  }

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

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    submittedCount.decrementAndGet();
    if (t == null) {
      stopCurrentThreadIfNeeded();
    }

  }

  protected boolean currentThreadShouldBeStopped() {
    if ((threadRenewalDelay >= 0L) && (Thread.currentThread() instanceof TaskThread)) {
      TaskThread currentTaskThread = (TaskThread) Thread.currentThread();
      return currentTaskThread.getCreationTime() < lastContextStoppedTime.longValue();
    }

    return false;
  }

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

  private static final class RejectHandler implements RejectedExecutionHandler {

    private RejectHandler() {
    }

    @Override
    public void rejectedExecution(Runnable r, java.util.concurrent.ThreadPoolExecutor executor) {
      throw new RejectedExecutionException();
    }

  }

}
