package com.github.lancethomps.lava.common.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskThreadFactory implements ThreadFactory {

  public static final boolean IS_SECURITY_ENABLED = System.getSecurityManager() != null;

  private final boolean daemon;

  private final ThreadGroup group;

  private final String namePrefix;

  private final AtomicInteger threadCounter = new AtomicInteger(1);

  private final int threadPriority;

  public TaskThreadFactory(String namePrefix, boolean daemon, int priority) {
    SecurityManager s = System.getSecurityManager();
    group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    this.namePrefix = namePrefix;
    this.daemon = daemon;
    threadPriority = priority;
  }

  public String getNamePrefix() {
    return namePrefix;
  }

  public int getThreadPriority() {
    return threadPriority;
  }

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

  private TaskThread createThread(final Runnable runnable) {
    final TaskThread thread = new TaskThread(group, runnable, namePrefix + '-' + threadCounter.incrementAndGet());

    thread.setDaemon(daemon);
    thread.setPriority(threadPriority);
    return thread;
  }

}
