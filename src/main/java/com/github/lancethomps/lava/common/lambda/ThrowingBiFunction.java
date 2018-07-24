package com.github.lancethomps.lava.common.lambda;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Exceptions;
import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Interface ThrowableBiFunction.
 *
 * @param <T> the generic type
 * @param <U> the generic type
 * @param <R> the generic type
 */
@FunctionalInterface
public interface ThrowingBiFunction<T, U, R> {

	/**
	 * And then.
	 *
	 * @param <V> the value type
	 * @param after the after
	 * @return the throwable bi function
	 * @throws Exception the exception
	 */
	default <V> ThrowingBiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) throws Exception {
		Objects.requireNonNull(after);
		return (T t, U u) -> after.apply(apply(t, u));
	}

	/**
	 * Apply.
	 *
	 * @param t the t
	 * @param u the u
	 * @return the r
	 * @throws Exception the exception
	 */
	R apply(T t, U u) throws Exception;

	/**
	 * Apply with sneaky throw.
	 *
	 * @param t the t
	 * @param u the u
	 * @return the r
	 */
	default R applyWithSneakyThrow(T t, U u) {
		try {
			return apply(t, u);
		} catch (Exception e) {
			return Exceptions.sneakyThrow(e);
		}
	}

	/**
	 * With sneaky throw.
	 *
	 * @return the bi function
	 */
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
