package com.github.lancethomps.lava.common.lambda;

import java.util.Objects;

/**
 * The Interface ThrowableTriFunction.
 *
 * @param <T> the generic type
 * @param <U> the generic type
 * @param <W> the generic type
 * @param <R> the generic type
 */
@FunctionalInterface
public interface ThrowingTriFunction<T, U, W, R> {

	/**
	 * And then.
	 *
	 * @param <V> the value type
	 * @param after the after
	 * @return the throwable tri function
	 * @throws Exception the exception
	 */
	default <V> ThrowingTriFunction<T, U, W, V> andThen(ThrowingFunction<? super R, ? extends V> after) throws Exception {
		Objects.requireNonNull(after);
		return (T t, U u, W w) -> after.apply(apply(t, u, w));
	}

	/**
	 * And then ignore errors.
	 *
	 * @param <V> the value type
	 * @param after the after
	 * @return the throwing tri function
	 */
	default <V> ThrowingTriFunction<T, U, W, V> andThenIgnoreErrors(ThrowingFunction<? super R, ? extends V> after) {
		Objects.requireNonNull(after);
		return (T t, U u, W w) -> after.applyIgnoreErrors(applyIgnoreErrors(t, u, w));
	}

	/**
	 * Apply.
	 *
	 * @param t the t
	 * @param u the u
	 * @param w the w
	 * @return the r
	 * @throws Exception the exception
	 */
	R apply(T t, U u, W w) throws Exception;

	/**
	 * Apply ignore errors.
	 *
	 * @param t the t
	 * @param u the u
	 * @param w the w
	 * @return the r
	 */
	default R applyIgnoreErrors(T t, U u, W w) {
		try {
			return apply(t, u, w);
		} catch (Exception e) {
			return null;
		}
	}

}
