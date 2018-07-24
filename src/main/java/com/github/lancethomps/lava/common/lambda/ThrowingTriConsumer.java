package com.github.lancethomps.lava.common.lambda;

import java.util.Objects;

/**
 * The Interface ThrowableBiConsumer.
 *
 * @param <T> the generic type
 * @param <U> the generic type
 * @param <V> the value type
 */
@FunctionalInterface
public interface ThrowingTriConsumer<T, U, V> {

	/**
	 * Accept.
	 *
	 * @param t the t
	 * @param u the u
	 * @param v the v
	 * @throws Exception the exception
	 */
	void accept(T t, U u, V v) throws Exception;

	/**
	 * Accept safe.
	 *
	 * @param t the t
	 * @param u the u
	 * @param v the v
	 */
	default void acceptIgnoreErrors(T t, U u, V v) {
		try {
			accept(t, u, v);
		} catch (Exception e) {
			;
		}
	}

	/**
	 * Accept wrap errors.
	 *
	 * @param t the t
	 * @param u the u
	 * @param v the v
	 */
	default void acceptWrapErrors(T t, U u, V v) {
		try {
			accept(t, u, v);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw ((RuntimeException) e);
			}
			throw new RuntimeException(e);
		}
	}

	/**
	 * And then.
	 *
	 * @param after the after
	 * @return the throwable bi consumer
	 * @throws Exception the exception
	 */
	default ThrowingTriConsumer<T, U, V> andThen(ThrowingTriConsumer<? super T, ? super U, ? super V> after) throws Exception {
		Objects.requireNonNull(after);
		return (l, r, f) -> {
			accept(l, r, f);
			after.accept(l, r, f);
		};
	}

	/**
	 * And then ignore errors.
	 *
	 * @param after the after
	 * @return the throwing tri consumer
	 */
	default ThrowingTriConsumer<T, U, V> andThenIgnoreErrors(ThrowingTriConsumer<? super T, ? super U, ? super V> after) {
		Objects.requireNonNull(after);
		return (l, r, f) -> {
			acceptIgnoreErrors(l, r, f);
			after.acceptIgnoreErrors(l, r, f);
		};
	}

}
