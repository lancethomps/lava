package com.github.lancethomps.lava.common.lambda;

import java.util.Objects;
import java.util.function.IntConsumer;

import com.github.lancethomps.lava.common.Exceptions;

/**
 * The Interface ThrowingIntConsumer.
 */
@FunctionalInterface
public interface ThrowingIntConsumer {

	/**
	 * Accept.
	 *
	 * @param value the value
	 * @throws Exception the exception
	 */
	void accept(int value) throws Exception;

	/**
	 * Accept with sneaky throw.
	 *
	 * @param value the value
	 */
	default void acceptWithSneakyThrow(int value) {
		try {
			accept(value);
		} catch (Exception e) {
			Exceptions.sneakyThrow(e);
		}
	}

	/**
	 * And then.
	 *
	 * @param after the after
	 * @return the throwing int consumer
	 * @throws Exception the exception
	 */
	default ThrowingIntConsumer andThen(ThrowingIntConsumer after) throws Exception {
		Objects.requireNonNull(after);
		return (int t) -> {
			accept(t);
			after.accept(t);
		};
	}

	/**
	 * To non throwing.
	 *
	 * @return the int consumer
	 */
	default IntConsumer toNonThrowing() {
		return i -> {
			try {
				accept(i);
			} catch (Exception e) {
				Exceptions.sneakyThrow(e);
			}
		};
	}
}
