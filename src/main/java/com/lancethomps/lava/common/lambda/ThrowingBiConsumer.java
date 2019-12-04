package com.lancethomps.lava.common.lambda;

import java.util.Objects;
import java.util.function.BiConsumer;

import com.lancethomps.lava.common.Exceptions;

@FunctionalInterface
public interface ThrowingBiConsumer<T, U> {

  void accept(T t, U u) throws Exception;

  default void acceptWithSneakyThrow(T t, U u) {
    try {
      accept(t, u);
    } catch (Exception e) {
      Exceptions.sneakyThrow(e);
    }
  }

  default ThrowingBiConsumer<T, U> andThen(ThrowingBiConsumer<? super T, ? super U> after) throws Exception {
    Objects.requireNonNull(after);
    return (l, r) -> {
      accept(l, r);
      after.accept(l, r);
    };
  }

  default BiConsumer<T, U> withSneakyThrow() {
    return (t, u) -> {
      try {
        accept(t, u);
      } catch (Exception e) {
        Exceptions.sneakyThrow(e);
      }
    };
  }

}
