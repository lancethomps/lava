package com.github.lancethomps.lava.common.lambda;

import java.util.Objects;

import com.github.lancethomps.lava.common.Exceptions;

/**
 * The Interface ThrowableConsumer.
 *
 * @param <T> the generic type
 */
@FunctionalInterface
public interface ThrowingConsumer<T> {

	/**
	 * Accept.
	 *
	 * @param t the t
	 * @throws Exception the exception
	 */
	void accept(T t) throws Exception;

	/**
	 * Accept ignore errors.
	 *
	 * @param t the t
	 */
	default void acceptIgnoreErrors(T t) {
		try {
			accept(t);
		} catch (Exception e) {
			;
		}
	}

	/**
	 * Accept with sneaky throw.
	 *
	 * @param t the t
	 */
	default void acceptWithSneakyThrow(T t) {
		try {
			accept(t);
		} catch (Exception e) {
			Exceptions.sneakyThrow(e);
		}
	}

	/**
	 * And then.
	 *
	 * @param after the after
	 * @return the consumer
	 * @throws Exception the exception
	 */
	default ThrowingConsumer<T> andThen(ThrowingConsumer<? super T> after) throws Exception {
		Objects.requireNonNull(after);
		return (T t) -> {
			accept(t);
			after.accept(t);
		};
	}

}
