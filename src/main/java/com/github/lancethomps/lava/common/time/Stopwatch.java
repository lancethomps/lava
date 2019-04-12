package com.github.lancethomps.lava.common.time;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;

public class Stopwatch extends StopWatch {

  private static final Logger LOG = Logger.getLogger(Stopwatch.class);
  private final Level level;
  private long addTimeNanos;

  public Stopwatch() {
    this(false);
  }

  public Stopwatch(boolean start) {
    this(start, Level.ERROR);
  }

  public Stopwatch(boolean start, final Level level) {
    super();
    this.level = level == null ? Level.ERROR : level;
    if (start) {
      start();
    }
  }

  public static Stopwatch createAndStart() {
    return new Stopwatch(true);
  }

  public Stopwatch addTimeFromOther(Stopwatch other) {
    if (other != null) {
      synchronized (this) {
        addTimeNanos += other.getNanoTime();
      }
    }
    return this;
  }

  public TimerContext createTimerContext() {
    return new TimerContext(this);
  }

  public long getAddTimeNanos() {
    return addTimeNanos;
  }

  @Override
  public long getNanoTime() {
    try {
      return super.getNanoTime() + addTimeNanos;
    } catch (Throwable e) {
      Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
      return -1L;
    }
  }

  @Override
  public long getSplitNanoTime() {
    try {
      return super.getSplitNanoTime();
    } catch (Throwable e) {
      Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
      return -1L;
    }
  }

  @Override
  public long getSplitTime() {
    try {
      return super.getSplitTime();
    } catch (Throwable e) {
      Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
      return -1L;
    }
  }

  public long getSplitTime(boolean split) {
    if (split) {
      split();
    }
    long time = getSplitTime();
    if (split) {
      unsplit();
    }
    return time;
  }

  @Override
  public long getStartTime() {
    try {
      return super.getStartTime();
    } catch (Throwable e) {
      Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
      return -1L;
    }
  }

  @Override
  public long getTime() {
    try {
      return super.getTime();
    } catch (Throwable e) {
      Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
      return -1L;
    }
  }

  @Override
  public long getTime(final TimeUnit timeUnit) {
    try {
      return super.getTime(timeUnit);
    } catch (Throwable e) {
      Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
      return -1L;
    }
  }

  public long getTimeAndReset(boolean start) {
    long time = getTime();
    reset(start);
    return time;
  }

  @Override
  public void reset() {
    try {
      super.reset();
    } catch (Throwable e) {
      Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
    }
  }

  public Stopwatch reset(boolean start) {
    reset();
    if (start) {
      start();
    }
    return this;
  }

  @Override
  public void resume() {
    try {
      super.resume();
    } catch (Throwable e) {
      Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
    }
  }

  @Override
  public void split() {
    try {
      super.split();
    } catch (Throwable e) {
      Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
    }
  }

  @Override
  public void start() {
    try {
      super.start();
    } catch (Throwable e) {
      Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
    }
  }

  public TimerContext startOrResume() {
    if (!isStarted()) {
      start();
    } else if (isSuspended()) {
      resume();
    }
    return createTimerContext();
  }

  @Override
  public void stop() {
    try {
      super.stop();
    } catch (Throwable e) {
      Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
    }
  }

  @Override
  public void suspend() {
    try {
      super.suspend();
    } catch (Throwable e) {
      Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
    }
  }

  public void suspendIfNeeded() {
    if (!isSuspended()) {
      suspend();
    }
  }

  @Override
  public void unsplit() {
    try {
      super.unsplit();
    } catch (Throwable e) {
      Logs.logLevel(LOG, level, e, "Issue with underlying Stopwatch.");
    }
  }

}
