package com.github.lancethomps.lava.common.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class CustomNamingThreadFactory implements ThreadFactory {

  private final Boolean daemonFlag;

  private final String namePrefix;

  private final Integer priority;

  private final AtomicLong threadCounter;

  private final ThreadFactory wrappedFactory;

  public CustomNamingThreadFactory(String namePrefix, Integer priority, Boolean daemonFlag) {
    super();
    this.namePrefix = namePrefix;
    this.priority = priority;
    this.daemonFlag = daemonFlag;

    wrappedFactory = Executors.defaultThreadFactory();
    threadCounter = new AtomicLong();
  }

  public final Boolean getDaemonFlag() {
    return daemonFlag;
  }

  public final Integer getPriority() {
    return priority;
  }

  public final ThreadFactory getWrappedFactory() {
    return wrappedFactory;
  }

  public String getNamePrefix() {
    return namePrefix;
  }

  public long getThreadCount() {
    return threadCounter.get();
  }

  @Override
  public Thread newThread(final Runnable runnable) {
    final Thread thread = getWrappedFactory().newThread(runnable);
    initializeThread(thread);

    return thread;
  }

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
