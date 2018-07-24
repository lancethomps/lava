package com.github.lancethomps.lava.common.lambda;

import java.util.Objects;
import java.util.function.BiConsumer;

import com.github.lancethomps.lava.common.Exceptions;

/**
 * The Interface ThrowingBiConsumer.
 *
 * @param <T> the generic type
 * @param <U> the generic type
 */
@FunctionalInterface
public interface ThrowingBiConsumer<T, U> {

	/**
	 * Accept.
	 *
	 * @param t the t
	 * @param u the u
	 * @throws Exception the exception
	 */
	void accept(T t, U u) throws Exception;

	/**
	 * Accept with sneaky throw.
	 *
	 * @param t the t
	 * @param u the u
	 */
	default void acceptWithSneakyThrow(T t, U u) {
		try {
			accept(t, u);
		} catch (Exception e) {
			Exceptions.sneakyThrow(e);
		}
	}

	/**
	 * And then.
	 *
	 * @param after the after
	 * @return the throwing bi consumer
	 * @throws Exception the exception
	 */
	default ThrowingBiConsumer<T, U> andThen(ThrowingBiConsumer<? super T, ? super U> after) throws Exception {
		Objects.requireNonNull(after);
		return (l, r) -> {
			accept(l, r);
			after.accept(l, r);
		};
	}

	/**
	 * With sneaky throw.
	 *
	 * @return the bi consumer
	 */
	default BiConsumer<T, U> withSneakyThrow() {
		return (t, u) -> {
			try {
				accept(t, u);
			} catch (Exception e) {
				Exceptions.sneakyThrow(e);
			}
		};
	}
}
