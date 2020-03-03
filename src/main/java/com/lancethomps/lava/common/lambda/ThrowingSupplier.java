package com.lancethomps.lava.common.lambda;

import org.apache.logging.log4j.LogManager;

import com.lancethomps.lava.common.Exceptions;
import com.lancethomps.lava.common.logging.Logs;

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

  default T getWithSneakyThrow() {
    try {
      return get();
    } catch (Exception e) {
      Logs.logError(LogManager.getLogger(getClass()), e, "Sneaky throw");
      return Exceptions.sneakyThrow(e);
    }
  }

}
