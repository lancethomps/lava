package com.lancethomps.lava.common.lambda;

import java.util.Objects;
import java.util.function.IntConsumer;

import com.lancethomps.lava.common.Exceptions;

@FunctionalInterface
public interface ThrowingIntConsumer {

  void accept(int value) throws Exception;

  default void acceptWithSneakyThrow(int value) {
    try {
      accept(value);
    } catch (Exception e) {
      Exceptions.sneakyThrow(e);
    }
  }

  default ThrowingIntConsumer andThen(ThrowingIntConsumer after) throws Exception {
    Objects.requireNonNull(after);
    return (int t) -> {
      accept(t);
      after.accept(t);
    };
  }

  default IntConsumer toNonThrowing() {
    return i -> {
      try {
        accept(i);
      } catch (Exception e) {
        Exceptions.sneakyThrow(e);
      }
    };
  }

}
