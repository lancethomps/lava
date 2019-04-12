package com.github.lancethomps.lava.common.lambda;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Exceptions;
import com.github.lancethomps.lava.common.logging.Logs;

@FunctionalInterface
public interface ThrowingRunnable {

  void run() throws Exception;

  default void runIgnoreErrors() {
    try {
      run();
    } catch (Exception e) {
    }
  }

  default Runnable withLoggingExceptions(Logger logger, String message, Object... args) {
    return () -> {
      try {
        run();
      } catch (Exception e) {
        Logs.logError(logger, e, message, args);
      }
    };
  }

  default Runnable withSneakyThrow() {
    return () -> {
      try {
        run();
      } catch (Exception e) {
        Logs.logError(Logger.getLogger(getClass()), e, "Sneaky throw");
        Exceptions.sneakyThrow(e);
      }
    };
  }

}
