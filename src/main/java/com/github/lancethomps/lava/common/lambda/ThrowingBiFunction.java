package com.github.lancethomps.lava.common.lambda;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Exceptions;
import com.github.lancethomps.lava.common.logging.Logs;

@FunctionalInterface
public interface ThrowingBiFunction<T, U, R> {

  default <V> ThrowingBiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) throws Exception {
    Objects.requireNonNull(after);
    return (T t, U u) -> after.apply(apply(t, u));
  }

  R apply(T t, U u) throws Exception;

  default R applyWithSneakyThrow(T t, U u) {
    try {
      return apply(t, u);
    } catch (Exception e) {
      return Exceptions.sneakyThrow(e);
    }
  }

  default BiFunction<T, U, R> withSneakyThrow() {
    return (t, u) -> {
      try {
        return apply(t, u);
      } catch (Exception e) {
        Logs.logError(Logger.getLogger(getClass()), e, "Sneaky throw");
        return Exceptions.sneakyThrow(e);
      }
    };
  }

}
