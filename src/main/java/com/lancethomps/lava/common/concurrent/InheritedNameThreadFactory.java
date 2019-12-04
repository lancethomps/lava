package com.lancethomps.lava.common.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class InheritedNameThreadFactory implements ThreadFactory {

  private final ThreadGroup group;
  private final String nameSuffix;
  private final AtomicInteger threadNumber = new AtomicInteger(1);

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
