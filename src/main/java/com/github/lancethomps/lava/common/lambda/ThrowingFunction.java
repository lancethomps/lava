package com.github.lancethomps.lava.common.lambda;

import java.util.Objects;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Exceptions;
import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Interface ThrowableFunction.
 *
 * @param <T> the generic type
 * @param <R> the generic type
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {

	/**
	 * Identity.
	 *
	 * @param <T> the generic type
	 * @return the throwing function
	 */
	static <T> ThrowingFunction<T, T> identity() {
		return t -> t;
	}

	/**
	 * And then.
	 *
	 * @param <V> the value type
	 * @param after the after
	 * @return the throwing function
	 * @throws Exception the exception
	 */
	default <V> ThrowingFunction<T, V> andThen(ThrowingFunction<? super R, ? extends V> after) throws Exception {
		Objects.requireNonNull(after);
		return (T t) -> after.apply(apply(t));
	}

	/**
	 * And then ignore errors.
	 *
	 * @param <V> the value type
	 * @param after the after
	 * @return the throwing function
	 */
	default <V> ThrowingFunction<T, V> andThenIgnoreErrors(ThrowingFunction<? super R, ? extends V> after) {
		Objects.requireNonNull(after);
		return (T t) -> after.applyIgnoreErrors(applyIgnoreErrors(t));
	}

	/**
	 * Apply.
	 *
	 * @param t the t
	 * @return the r
	 * @throws Exception the exception
	 */
	R apply(T t) throws Exception;

	/**
	 * Apply ignore errors.
	 *
	 * @param value the value
	 * @return the r
	 */
	default R applyIgnoreErrors(T value) {
		try {
			return apply(value);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Apply with sneaky throw.
	 *
	 * @param t the t
	 * @return the r
	 */
	default R applyWithSneakyThrow(T t) {
		try {
			return apply(t);
		} catch (Exception e) {
			return Exceptions.sneakyThrow(e);
		}
	}

	/**
	 * Compose.
	 *
	 * @param <V> the value type
	 * @param before the before
	 * @return the throwing function
	 * @throws Exception the exception
	 */
	default <V> ThrowingFunction<V, R> compose(ThrowingFunction<? super V, ? extends T> before) throws Exception {
		Objects.requireNonNull(before);
		return (V v) -> apply(before.apply(v));
	}

	/**
	 * Compose ignore errors.
	 *
	 * @param <V> the value type
	 * @param before the before
	 * @return the throwing function
	 */
	default <V> ThrowingFunction<V, R> composeIgnoreErrors(ThrowingFunction<? super V, ? extends T> before) {
		Objects.requireNonNull(before);
		return (V v) -> applyIgnoreErrors(before.applyIgnoreErrors(v));
	}

	/**
	 * With sneaky throw.
	 *
	 * @return the function
	 */
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
