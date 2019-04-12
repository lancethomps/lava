package com.github.lancethomps.lava.common.time;

import javax.annotation.Nonnull;

import com.github.lancethomps.lava.common.lambda.ThrowingSupplier;

public class TimerContext implements AutoCloseable {

  private final Stopwatch watch;

  public TimerContext(@Nonnull Stopwatch watch) {
    super();
    this.watch = watch;
  }

  @Override
  public void close() {
    pause();
  }

  public <T> T getAndClose(@Nonnull ThrowingSupplier<T> supplier) throws Exception {
    try {
      return supplier.get();
    } finally {
      pause();
    }
  }

  public Stopwatch getWatch() {
    return watch;
  }

  public TimerContext pause() {
    watch.suspend();
    return this;
  }

}
