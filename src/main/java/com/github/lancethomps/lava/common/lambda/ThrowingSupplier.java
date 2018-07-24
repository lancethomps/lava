package com.github.lancethomps.lava.common.lambda;

/**
 * The Interface ThrowableSupplier.
 *
 * @param <T> the generic type
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {

	/**
	 * Gets the.
	 *
	 * @return the t
	 * @throws Exception the exception
	 */
	T get() throws Exception;

	/**
	 * Gets the ignore errors.
	 *
	 * @return the ignore errors
	 */
	default T getIgnoreErrors() {
		try {
			return get();
		} catch (Exception e) {
			return null;
		}
	}

}
