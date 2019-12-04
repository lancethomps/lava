package com.lancethomps.lava.common.concurrent;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class TaskQueue extends LinkedBlockingQueue<Runnable> {

  private static final long serialVersionUID = 1L;

  private Integer forcedRemainingCapacity;

  private volatile CachedAndQueuedThreadPoolExecutor parent;

  public TaskQueue() {
  }

  public TaskQueue(Collection<? extends Runnable> c) {
    super(c);
  }

  public TaskQueue(int capacity) {
    super(capacity);
  }

  public boolean force(Runnable o) {
    if ((parent != null) && !parent.isShutdown()) {
      return super.offer(o);
    }
    throw new RejectedExecutionException("Executor not running, can\'t force a command into the queue");
  }

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
      : (parent.getPoolSize() >= parent.getMaximumPoolSize() && super.offer(o))));
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

  public void setForcedRemainingCapacity(Integer forcedRemainingCapacity) {
    this.forcedRemainingCapacity = forcedRemainingCapacity;
  }

  public void setParent(CachedAndQueuedThreadPoolExecutor tp) {
    parent = tp;
  }

  @Override
  public Runnable take() throws InterruptedException {
    return (parent != null) && parent.currentThreadShouldBeStopped()
      ? this.poll(parent.getKeepAliveTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
      : super.take();
  }

}
