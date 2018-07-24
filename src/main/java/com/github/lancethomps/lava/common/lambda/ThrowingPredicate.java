package com.github.lancethomps.lava.common.lambda;

import java.util.Objects;

/**
 * The Interface ThrowablePredicate.
 *
 * @param <T> the generic type
 */
@FunctionalInterface
public interface ThrowingPredicate<T> {

	/**
	 * Checks if is equal.
	 *
	 * @param <T> the generic type
	 * @param targetRef the target ref
	 * @return the throwable predicate
	 */
	static <T> ThrowingPredicate<T> isEqual(Object targetRef) {
		return (null == targetRef)
			? Objects::isNull
			: object -> targetRef.equals(object);
	}

	/**
	 * And.
	 *
	 * @param other the other
	 * @return the throwable predicate
	 * @throws Exception the exception
	 */
	default ThrowingPredicate<T> and(ThrowingPredicate<? super T> other) throws Exception {
		Objects.requireNonNull(other);
		return (t) -> test(t) && other.test(t);
	}

	/**
	 * Negate.
	 *
	 * @return the throwable predicate
	 * @throws Exception the exception
	 */
	default ThrowingPredicate<T> negate() throws Exception {
		return (t) -> !test(t);
	}

	/**
	 * Or.
	 *
	 * @param other the other
	 * @return the throwable predicate
	 * @throws Exception the exception
	 */
	default ThrowingPredicate<T> or(ThrowingPredicate<? super T> other) throws Exception {
		Objects.requireNonNull(other);
		return (t) -> test(t) || other.test(t);
	}

	/**
	 * Test.
	 *
	 * @param t the t
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	boolean test(T t) throws Exception;
}
