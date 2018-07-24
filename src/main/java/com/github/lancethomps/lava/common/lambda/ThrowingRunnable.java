package com.github.lancethomps.lava.common.lambda;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Exceptions;
import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Interface ThrowingRunnable.
 */
@FunctionalInterface
public interface ThrowingRunnable {

	/**
	 * Run.
	 *
	 * @throws Exception the exception
	 */
	void run() throws Exception;

	/**
	 * Run ignore errors.
	 */
	default void runIgnoreErrors() {
		try {
			run();
		} catch (Exception e) {
			;
		}
	}

	/**
	 * With logging exceptions.
	 *
	 * @param logger the logger
	 * @param message the message
	 * @param args the args
	 * @return the runnable
	 */
	default Runnable withLoggingExceptions(Logger logger, String message, Object... args) {
		return () -> {
			try {
				run();
			} catch (Exception e) {
				Logs.logError(logger, e, message, args);
			}
		};
	}

	/**
	 * With sneaky throw.
	 *
	 * @return the runnable
	 */
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
