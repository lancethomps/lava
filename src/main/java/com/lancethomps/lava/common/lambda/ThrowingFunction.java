package com.lancethomps.lava.common.lambda;

import java.util.Objects;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.lancethomps.lava.common.Exceptions;
import com.lancethomps.lava.common.logging.Logs;

@FunctionalInterface
public interface ThrowingFunction<T, R> {

  static <T> ThrowingFunction<T, T> identity() {
    return t -> t;
  }

  default <V> ThrowingFunction<T, V> andThen(ThrowingFunction<? super R, ? extends V> after) throws Exception {
    Objects.requireNonNull(after);
    return (T t) -> after.apply(apply(t));
  }

  default <V> ThrowingFunction<T, V> andThenIgnoreErrors(ThrowingFunction<? super R, ? extends V> after) {
    Objects.requireNonNull(after);
    return (T t) -> after.applyIgnoreErrors(applyIgnoreErrors(t));
  }

  R apply(T t) throws Exception;

  default R applyIgnoreErrors(T value) {
    try {
      return apply(value);
    } catch (Exception e) {
      return null;
    }
  }

  default R applyWithSneakyThrow(T t) {
    try {
      return apply(t);
    } catch (Exception e) {
      return Exceptions.sneakyThrow(e);
    }
  }

  default <V> ThrowingFunction<V, R> compose(ThrowingFunction<? super V, ? extends T> before) throws Exception {
    Objects.requireNonNull(before);
    return (V v) -> apply(before.apply(v));
  }

  default <V> ThrowingFunction<V, R> composeIgnoreErrors(ThrowingFunction<? super V, ? extends T> before) {
    Objects.requireNonNull(before);
    return (V v) -> applyIgnoreErrors(before.applyIgnoreErrors(v));
  }

  default Function<T, R> withSneakyThrow() {
    return (t) -> {
      try {
        return apply(t);
      } catch (Exception e) {
        Logs.logError(Logger.getLogger(getClass()), e, "Sneaky throw");
        return Exceptions.sneakyThrow(e);
      }
    };
  }

}
