package com.github.lancethomps.lava.common.lambda;

import java.util.Objects;

@FunctionalInterface
public interface ThrowingPredicate<T> {

  static <T> ThrowingPredicate<T> isEqual(Object targetRef) {
    return (null == targetRef)
      ? Objects::isNull
      : object -> targetRef.equals(object);
  }

  default ThrowingPredicate<T> and(ThrowingPredicate<? super T> other) throws Exception {
    Objects.requireNonNull(other);
    return (t) -> test(t) && other.test(t);
  }

  default ThrowingPredicate<T> negate() throws Exception {
    return (t) -> !test(t);
  }

  default ThrowingPredicate<T> or(ThrowingPredicate<? super T> other) throws Exception {
    Objects.requireNonNull(other);
    return (t) -> test(t) || other.test(t);
  }

  boolean test(T t) throws Exception;

}
