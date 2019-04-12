package com.github.lancethomps.lava.common.lambda;

import java.util.Objects;

import com.github.lancethomps.lava.common.Exceptions;

@FunctionalInterface
public interface ThrowingConsumer<T> {

  void accept(T t) throws Exception;

  default void acceptIgnoreErrors(T t) {
    try {
      accept(t);
    } catch (Exception e) {
    }
  }

  default void acceptWithSneakyThrow(T t) {
    try {
      accept(t);
    } catch (Exception e) {
      Exceptions.sneakyThrow(e);
    }
  }

  default ThrowingConsumer<T> andThen(ThrowingConsumer<? super T> after) throws Exception {
    Objects.requireNonNull(after);
    return (T t) -> {
      accept(t);
      after.accept(t);
    };
  }

}
