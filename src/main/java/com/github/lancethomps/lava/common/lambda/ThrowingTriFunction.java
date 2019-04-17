package com.github.lancethomps.lava.common.lambda;

import java.util.Objects;

import com.github.lancethomps.lava.common.Exceptions;

@FunctionalInterface
public interface ThrowingTriFunction<T, U, W, R> {

  default <V> ThrowingTriFunction<T, U, W, V> andThen(ThrowingFunction<? super R, ? extends V> after) throws Exception {
    Objects.requireNonNull(after);
    return (T t, U u, W w) -> after.apply(apply(t, u, w));
  }

  default <V> ThrowingTriFunction<T, U, W, V> andThenIgnoreErrors(ThrowingFunction<? super R, ? extends V> after) {
    Objects.requireNonNull(after);
    return (T t, U u, W w) -> after.applyIgnoreErrors(applyIgnoreErrors(t, u, w));
  }

  R apply(T t, U u, W w) throws Exception;

  default R applyIgnoreErrors(T t, U u, W w) {
    try {
      return apply(t, u, w);
    } catch (Exception e) {
      return null;
    }
  }

  default R applyWithSneakyThrow(T t, U u, W w) {
    try {
      return apply(t, u, w);
    } catch (Exception e) {
      return Exceptions.sneakyThrow(e);
    }
  }

}
