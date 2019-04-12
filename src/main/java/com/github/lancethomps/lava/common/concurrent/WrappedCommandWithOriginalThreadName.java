package com.github.lancethomps.lava.common.concurrent;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

public class WrappedCommandWithOriginalThreadName<T> implements Runnable, Callable<T> {

  private final Callable<T> callable;

  private final Runnable runnable;

  private final String threadNamePrefix;

  private WrappedCommandWithOriginalThreadName(Callable<T> callable, Runnable runnable) {
    super();
    this.callable = callable;
    this.runnable = runnable;
    threadNamePrefix = Thread.currentThread().getName();
  }

  public WrappedCommandWithOriginalThreadName(@Nonnull Callable<T> callable) {
    this(callable, null);
  }

  public WrappedCommandWithOriginalThreadName(@Nonnull Runnable runnable) {
    this(null, runnable);
  }

  @Override
  public T call() throws Exception {
    try (TempThreadNamePrefixAdder threadNamePrefixAdder = new TempThreadNamePrefixAdder(threadNamePrefix)) {
      return callable.call();
    }
  }

  @Override
  public void run() {
    try (TempThreadNamePrefixAdder threadNamePrefixAdder = new TempThreadNamePrefixAdder(threadNamePrefix)) {
      if (callable != null) {
        try {
          callable.call();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      } else {
        runnable.run();
      }
    }
  }

}
