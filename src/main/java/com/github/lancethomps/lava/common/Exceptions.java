package com.github.lancethomps.lava.common;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.lancethomps.lava.common.format.Formatting;

/**
 * The Class Exceptions.
 */
public class Exceptions {

	/**
	 * Find root cause.
	 *
	 * @param exception the exception
	 * @return the throwable
	 */
	public static Throwable findRootCause(@Nonnull Throwable exception) {
		Throwable root = exception;
		while (root.getCause() != null) {
			root = root.getCause();
		}
		return root;
	}

	/**
	 * Gets the stack trace.
	 *
	 * @param error the error
	 * @return the stack trace
	 */
	public static String getStackTrace(Throwable error) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream stream;
		try {
			stream = new PrintStream(baos, true, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		error.printStackTrace(stream);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}

	/**
	 * Sneaky throw.
	 *
	 * @param <T> the generic type
	 * @param <R> the generic type
	 * @param t the t
	 * @return the r
	 * @throws T the t
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Exception, R> R sneakyThrow(Throwable t) throws T {
		throw (T) t;
	}

	/**
	 * Throw if.
	 *
	 * @param <E> the element type
	 * @param test the test
	 * @param type the type
	 * @param msg the msg
	 * @param formatArgs the format args
	 * @throws E the e
	 */
	public static <E extends Exception> void throwIf(boolean test, Class<E> type, String msg, Object... formatArgs) throws E {
		if (test) {
			try {
				E exception = type.getConstructor(String.class).newInstance(Formatting.getMessage(msg, formatArgs));
				throw exception;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				if (e.getClass() == type) {
					throw (E) e;
				}
				throw new RuntimeException(String.format("Could not instantiate exception of type [%s]", type), e);
			}
		}
	}

	/**
	 * Throw if true.
	 *
	 * @param <E> the element type
	 * @param test the test
	 * @param type the type
	 * @param msg the msg
	 */
	public static <E extends RuntimeException> void throwIfTrue(boolean test, Class<E> type, String msg) {
		if (test) {
			try {
				E exception = type.getConstructor(String.class).newInstance(msg);
				throw exception;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(String.format("Could not instantiate exception of type [%s]", type), e);
			}
		}
	}

	/**
	 * Throw if true.
	 *
	 * @param <E> the element type
	 * @param test the test
	 * @param type the type
	 * @param msgGetter the msg getter
	 * @throws E the e
	 */
	public static <E extends Exception> void throwIfTrue(boolean test, Class<E> type, Supplier<String> msgGetter) throws E {
		if (test) {
			try {
				E exception = type.getConstructor(String.class).newInstance(msgGetter.get());
				throw exception;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(String.format("Could not instantiate exception of type [%s]", type), e);
			}
		}
	}
}
