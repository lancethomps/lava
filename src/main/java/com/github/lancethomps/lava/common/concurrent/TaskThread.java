package com.github.lancethomps.lava.common.concurrent;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;

public class TaskThread extends Thread {

  private static final Logger LOG = Logger.getLogger(TaskThread.class);

  private final long creationTime = System.currentTimeMillis();

  public TaskThread(ThreadGroup group, Runnable target, String name) {
    super(group, new TaskThread.WrappingRunnable(target), name);
  }

  public TaskThread(ThreadGroup group, Runnable target, String name, long stackSize) {
    super(group, new TaskThread.WrappingRunnable(target), name, stackSize);
  }

  public final long getCreationTime() {
    return creationTime;
  }

  private static class WrappingRunnable implements Runnable {

    private Runnable wrappedRunnable;

    WrappingRunnable(Runnable wrappedRunnable) {
      this.wrappedRunnable = wrappedRunnable;
    }

    @Override
    public void run() {
      try {
        wrappedRunnable.run();
      } catch (StopPooledThreadException arg1) {
        Logs.logDebug(LOG, "Thread exiting on purpose", arg1);
      }

    }

  }

}
