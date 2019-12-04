package com.lancethomps.lava.common.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RejectingQueueThreadPoolExecutor extends ThreadPoolExecutor {

  private AtomicLong rejectCount = new AtomicLong(0);

  public RejectingQueueThreadPoolExecutor(
    int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
    RejectedExecutionHandler handler
  ) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
  }

  public void addRejection() {
    rejectCount.incrementAndGet();
  }

  @Override
  public String toString() {
    String str = super.toString();
    return str.substring(0, str.length() - 1) + ", rejected to queue = " + rejectCount.get() + ']';
  }

}
