package com.lancethomps.lava.common.lambda;

@FunctionalInterface
public interface ThrowingSupplier<T> {

  T get() throws Exception;

  default T getIgnoreErrors() {
    try {
      return get();
    } catch (Exception e) {
      return null;
    }
  }

}
