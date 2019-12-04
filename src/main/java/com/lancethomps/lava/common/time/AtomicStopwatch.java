package com.lancethomps.lava.common.time;

public class AtomicStopwatch extends Stopwatch {

  public AtomicStopwatch() {
    this(false);
  }

  public AtomicStopwatch(boolean start) {
    super(start);
  }

  public static AtomicStopwatch createAndStart() {
    return new AtomicStopwatch(true);
  }

  @Override
  public void start() {
    synchronized (this) {
      super.start();
    }
  }

  @Override
  public TimerContext startOrResume() {
    synchronized (this) {
      return super.startOrResume();
    }
  }

  @Override
  public void suspend() {
    synchronized (this) {
      super.suspend();
    }
  }

  @Override
  public void suspendIfNeeded() {
    synchronized (this) {
      super.suspendIfNeeded();
    }
  }

}
