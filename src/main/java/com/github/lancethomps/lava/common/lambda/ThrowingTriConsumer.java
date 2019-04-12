package com.github.lancethomps.lava.common.lambda;

import java.util.Objects;

@FunctionalInterface
public interface ThrowingTriConsumer<T, U, V> {

  void accept(T t, U u, V v) throws Exception;

  default void acceptIgnoreErrors(T t, U u, V v) {
    try {
      accept(t, u, v);
    } catch (Exception e) {
    }
  }

  default void acceptWrapErrors(T t, U u, V v) {
    try {
      accept(t, u, v);
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw ((RuntimeException) e);
      }
      throw new RuntimeException(e);
    }
  }

  default ThrowingTriConsumer<T, U, V> andThen(ThrowingTriConsumer<? super T, ? super U, ? super V> after) throws Exception {
    Objects.requireNonNull(after);
    return (l, r, f) -> {
      accept(l, r, f);
      after.accept(l, r, f);
    };
  }

  default ThrowingTriConsumer<T, U, V> andThenIgnoreErrors(ThrowingTriConsumer<? super T, ? super U, ? super V> after) {
    Objects.requireNonNull(after);
    return (l, r, f) -> {
      acceptIgnoreErrors(l, r, f);
      after.acceptIgnoreErrors(l, r, f);
    };
  }

}
