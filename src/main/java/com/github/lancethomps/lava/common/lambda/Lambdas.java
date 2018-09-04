package com.github.lancethomps.lava.common.lambda;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.Exceptions;
import com.github.lancethomps.lava.common.concurrent.ExecutorFactory;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.math.BigDecimalSummaryStatistics;

/**
 * A factory for creating Lambda objects.
 */
public class Lambdas {

	/** The Constant FORK_JOIN_ALT_POOL. */
	public static final ThreadPoolExecutor FORK_JOIN_ALT_POOL = ExecutorFactory.getCachedThreadPool(0, Runtime.getRuntime().availableProcessors(), "fork_join_alt_pool");

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(Lambdas.class);

	/**
	 * Action if true.
	 *
	 * @param test the test
	 * @param function the function
	 */
	public static void actionIfTrue(boolean test, final ThrowingRunnable function) {
		actionIfTrue(test, function, null);
	}

	/**
	 * Action if true.
	 *
	 * @param test the test
	 * @param function the function
	 * @param falseFunction the false function
	 */
	public static void actionIfTrue(boolean test, final ThrowingRunnable function, final ThrowingRunnable falseFunction) {
		try {
			if (test) {
				function.run();
			} else if (falseFunction != null) {
				falseFunction.run();
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue running action with test boolean of [%s]", test);
		}
	}

	/**
	 * Completable future parallel stream.
	 *
	 * @param <T> the generic type
	 * @param <R> the generic type
	 * @param stream the stream
	 * @param mapper the mapper
	 * @return the stream
	 */
	public static <T, R> Stream<CompletableFuture<R>> completableFutureParallelStream(@Nonnull Stream<T> stream, @Nonnull Function<? super T, ? extends R> mapper) {
		return stream
			.map(val -> CompletableFuture.supplyAsync(() -> mapper.apply(val), FORK_JOIN_ALT_POOL));
	}

	/**
	 * Consume if non blank.
	 *
	 * @param val the val
	 * @param consumer the consumer
	 */
	public static void consumeIfNonBlank(String val, ThrowingConsumer<String> consumer) {
		consumeIfNonBlank(val, consumer, null);
	}

	/**
	 * Consume if non blank.
	 *
	 * @param val the val
	 * @param consumer the consumer
	 * @param nullConsumer the null consumer
	 */
	public static void consumeIfNonBlank(String val, ThrowingConsumer<String> consumer, ThrowingRunnable nullConsumer) {
		consumeIfTrue(StringUtils.isNotBlank(val), val, consumer, nullConsumer == null ? null : t -> nullConsumer.run());
	}

	/**
	 * Consume if non empty.
	 *
	 * @param <C> the generic type
	 * @param <T> the generic type
	 * @param val the val
	 * @param consumer the consumer
	 */
	public static <C extends Collection<T>, T> void consumeIfNonEmpty(C val, ThrowingConsumer<C> consumer) {
		consumeIfNonEmpty(val, consumer, null);
	}

	/**
	 * Consume if non blank.
	 *
	 * @param <C> the generic type
	 * @param <T> the generic type
	 * @param val the val
	 * @param consumer the consumer
	 * @param nullConsumer the null consumer
	 */
	public static <C extends Collection<T>, T> void consumeIfNonEmpty(C val, ThrowingConsumer<C> consumer, ThrowingRunnable nullConsumer) {
		consumeIfTrue(Checks.isNotEmpty(val), val, consumer, nullConsumer == null ? null : t -> nullConsumer.run());
	}

	/**
	 * Consume if non empty.
	 *
	 * @param <M> the generic type
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param val the val
	 * @param consumer the consumer
	 */
	public static <M extends Map<K, V>, K, V> void consumeIfNonEmpty(M val, ThrowingConsumer<M> consumer) {
		consumeIfNonEmpty(val, consumer, null);
	}

	/**
	 * Consume if non empty.
	 *
	 * @param <M> the generic type
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param val the val
	 * @param consumer the consumer
	 * @param nullConsumer the null consumer
	 */
	public static <M extends Map<K, V>, K, V> void consumeIfNonEmpty(M val, ThrowingConsumer<M> consumer, ThrowingRunnable nullConsumer) {
		consumeIfTrue(Checks.isNotEmpty(val), val, consumer, nullConsumer == null ? null : t -> nullConsumer.run());
	}

	/**
	 * Consume if non null.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param consumer the consumer
	 */
	public static <T> void consumeIfNonNull(T val, ThrowingConsumer<T> consumer) {
		consumeIfNonNull(val, consumer, null);
	}

	/**
	 * Consume if non null.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param consumer the consumer
	 * @param nullConsumer the null consumer
	 */
	public static <T> void consumeIfNonNull(T val, ThrowingConsumer<T> consumer, ThrowingConsumer<T> nullConsumer) {
		consumeIfTrue(val != null, val, consumer, nullConsumer);
	}

	/**
	 * Consume if true.
	 *
	 * @param <T> the generic type
	 * @param test the test
	 * @param val the val
	 * @param consumer the consumer
	 */
	public static <T> void consumeIfTrue(boolean test, T val, ThrowingConsumer<T> consumer) {
		consumeIfTrue(test, val, consumer, null);
	}

	/**
	 * Consume if true.
	 *
	 * @param <T> the generic type
	 * @param test the test
	 * @param val the val
	 * @param consumer the consumer
	 * @param falseConsumer the false consumer
	 */
	public static <T> void consumeIfTrue(boolean test, T val, ThrowingConsumer<T> consumer, ThrowingConsumer<T> falseConsumer) {
		try {
			if (test) {
				consumer.accept(val);
			} else if (falseConsumer != null) {
				falseConsumer.accept(val);
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue consuming val [%s] for test [%s]", val, test);
		}
	}

	/**
	 * Consume if true.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param test the test
	 * @param consumer the consumer
	 * @param falseConsumer the false consumer
	 */
	public static <T> void consumeIfTrue(T val, ThrowingPredicate<T> test, ThrowingConsumer<T> consumer, ThrowingConsumer<T> falseConsumer) {
		try {
			boolean testResult = test.test(val);
			consumeIfTrue(testResult, val, consumer, falseConsumer);
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue consuming val [%s]", val);
		}
	}

	/**
	 * Custom parallel stream for each.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param action the action
	 */
	public static <T> void customParallelStreamForEach(@Nonnull Stream<T> stream, @Nonnull Consumer<? super T> action) {
		stream
			.map(val -> CompletableFuture.runAsync(() -> action.accept(val), FORK_JOIN_ALT_POOL))
			.collect(Collectors.toList())
			.stream()
			.map(CompletableFuture::join)
			.forEach(v -> {
				; //do nothing
			});
	}

	/**
	 * Custom parallel stream map.
	 *
	 * @param <T> the generic type
	 * @param <R> the generic type
	 * @param stream the stream
	 * @param mapper the mapper
	 * @return the stream
	 */
	public static <T, R> Stream<R> customParallelStreamMap(@Nonnull Stream<T> stream, @Nonnull Function<? super T, ? extends R> mapper) {
		return stream
			.map(val -> CompletableFuture.supplyAsync(() -> mapper.apply(val), FORK_JOIN_ALT_POOL))
			.collect(Collectors.toList())
			.stream()
			.map(CompletableFuture::join);
	}

	/**
	 * Exec throwable action.
	 *
	 * @param function the function
	 * @param log the log
	 * @param message the message
	 * @param formatArgs the format args
	 * @return true, if successful
	 */
	public static boolean execThrowableAction(final ThrowingRunnable function, final Logger log, final String message, final Object... formatArgs) {
		try {
			function.run();
			return false;
		} catch (Throwable e) {
			Logs.logError(log, e, message, formatArgs);
			return true;
		}
	}

	/**
	 * Exec throwable function.
	 *
	 * @param <T> the generic type
	 * @param <R> the generic type
	 * @param function the function
	 * @param input the input
	 * @param defaultValue the default value
	 * @param log the log
	 * @param message the message
	 * @param formatArgs the format args
	 * @return the r
	 */
	public static <T, R> R execThrowableFunction(
		final ThrowingFunction<T, R> function,
		final T input,
		final R defaultValue,
		final Logger log,
		final String message,
		final Object... formatArgs
	) {
		try {
			return function.apply(input);
		} catch (Throwable e) {
			Logs.logError(log, e, message, formatArgs);
			return defaultValue;
		}
	}

	/**
	 * Exec throwable supplier.
	 *
	 * @param <R> the generic type
	 * @param function the function
	 * @param defaultValue the default value
	 * @param log the log
	 * @param message the message
	 * @param formatArgs the format args
	 * @return the r
	 */
	public static <R> R execThrowableSupplier(final ThrowingSupplier<R> function, final R defaultValue, final Logger log, final String message, final Object... formatArgs) {
		try {
			return function.get();
		} catch (Throwable e) {
			Logs.logError(log, e, message, formatArgs);
			return defaultValue;
		}
	}

	/**
	 * Flatten recursively.
	 *
	 * @param <T> the generic type
	 * @param docs the docs
	 * @param childrenGetters the children getters
	 * @return the list
	 */
	@SafeVarargs
	public static <T> List<T> flattenRecursively(@Nonnull Collection<T> docs, @Nonnull Function<T, Collection<T>>... childrenGetters) {
		List<T> flattened = new ArrayList<>();
		modifyRecursively(docs, (val, level) -> flattened.add(val), childrenGetters);
		return flattened;
	}

	/**
	 * Flatten recursively.
	 *
	 * @param <T> the generic type
	 * @param doc the doc
	 * @param childrenGetters the children getters
	 * @return the list
	 */
	@SafeVarargs
	public static <T> List<T> flattenRecursively(@Nonnull T doc, @Nonnull Function<T, Collection<T>>... childrenGetters) {
		return flattenRecursively(Arrays.asList(doc), childrenGetters);
	}

	/**
	 * Function if non null.
	 *
	 * @param <T> the generic type
	 * @param <R> the generic type
	 * @param val the val
	 * @param function the function
	 * @return the r
	 */
	public static <T, R> Optional<R> functionIfNonNull(T val, Function<T, R> function) {
		return val != null ? Optional.ofNullable(function.apply(val)) : Optional.empty();
	}

	/**
	 * Function if true.
	 *
	 * @param <T> the generic type
	 * @param test the test
	 * @param val the val
	 * @param function the function
	 * @return the t
	 */
	public static <T> T functionIfTrue(boolean test, T val, Function<T, T> function) {
		if (test) {
			return function.apply(val);
		}
		return val;
	}

	/**
	 * Function if true.
	 *
	 * @param <T> the generic type
	 * @param <R> the generic type
	 * @param val the val
	 * @param test the test
	 * @param trueFunction the true function
	 * @return the optional
	 */
	public static <T, R> Optional<R> functionIfTrue(T val, ThrowingFunction<T, Boolean> test, ThrowingFunction<T, R> trueFunction) {
		return functionIfTrue(val, test, trueFunction, null);
	}

	/**
	 * Function if true.
	 *
	 * @param <T> the generic type
	 * @param <R> the generic type
	 * @param val the val
	 * @param test the test
	 * @param trueFunction the true function
	 * @param falseFunction the false function
	 * @return the optional
	 */
	public static <T, R> Optional<R> functionIfTrue(
		final T val,
		final ThrowingFunction<T, Boolean> test,
		final ThrowingFunction<T, R> trueFunction,
		final ThrowingFunction<T, R> falseFunction
	) {
		try {
			return test.apply(val) ? Optional.ofNullable(trueFunction.apply(val)) : falseFunction != null ? Optional.ofNullable(falseFunction.apply(val)) : Optional.empty();
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Error evaluating function!");
			return Optional.empty();
		}
	}

	/**
	 * Function if true generic.
	 *
	 * @param <T> the generic type
	 * @param <R> the generic type
	 * @param test the test
	 * @param val the val
	 * @param function the function
	 * @return the object
	 */
	public static <T, R> Object functionIfTrueGeneric(boolean test, T val, Function<T, R> function) {
		if (test) {
			return function.apply(val);
		}
		return val;
	}

	/**
	 * Gets the default merge function.
	 *
	 * @param <U> the generic type
	 * @return the default merge function
	 */
	public static <U> BinaryOperator<U> getDefaultMergeFunction() {
		return (a, b) -> a != null ? a : b;
	}

	/**
	 * Gets the non empty collection.
	 *
	 * @param <T> the generic type
	 * @param <E> the element type
	 * @param <R> the generic type
	 * @param bean the bean
	 * @param function the function
	 * @param defaultValue the default value
	 * @return the non empty collection
	 */
	public static <T, E, R extends Collection<E>> R getNonEmpty(T bean, Function<T, R> function, R defaultValue) {
		R result = bean != null ? function.apply(bean) : null;
		return Checks.isNotEmpty(result) ? result : defaultValue;
	}

	/**
	 * Gets the non empty.
	 *
	 * @param <T> the generic type
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <R> the generic type
	 * @param bean the bean
	 * @param function the function
	 * @param defaultValue the default value
	 * @return the non empty
	 */
	public static <T, K, V, R extends Map<K, V>> R getNonEmpty(T bean, Function<T, R> function, R defaultValue) {
		R result = bean != null ? function.apply(bean) : null;
		return Checks.isNotEmpty(result) ? result : defaultValue;
	}

	/**
	 * Gets the overwriting merge function.
	 *
	 * @param <U> the generic type
	 * @return the overwriting merge function
	 */
	public static <U> BinaryOperator<U> getOverwritingMergeFunction() {
		return (a, b) -> a != null ? a : b;
	}

	/**
	 * Iterate.
	 *
	 * @param <T> the generic type
	 * @param size the size
	 * @param supplier the supplier
	 * @return the stream
	 */
	public static <T> Stream<T> iterate(final long size, final Supplier<T> supplier) {
		return IntStream.iterate(0, i -> i + 1).limit(size).mapToObj(i -> supplier.get());
	}

	/**
	 * Modify recursively.
	 *
	 * @param <T> the generic type
	 * @param docs the docs
	 * @param modifier the modifier
	 * @param childrenGetters the children getters
	 */
	@SafeVarargs
	public static <T> void modifyRecursively(@Nonnull Collection<T> docs, @Nonnull BiConsumer<T, Integer> modifier, @Nonnull Function<T, Collection<T>>... childrenGetters) {
		modifyRecursively(docs, modifier, 0, childrenGetters);
	}

	/**
	 * Modify recursively.
	 *
	 * @param <T> the generic type
	 * @param docs the docs
	 * @param modifier the modifier
	 * @param childrenGetters the children getters
	 */
	public static <T> void modifyRecursively(@Nonnull Collection<T> docs, @Nonnull Consumer<T> modifier, @Nonnull Collection<Function<T, Collection<T>>> childrenGetters) {
		for (T doc : docs) {
			modifier.accept(doc);
			for (Function<T, Collection<T>> childrenGetter : childrenGetters) {
				Collection<T> children = childrenGetter.apply(doc);
				if (children != null) {
					modifyRecursively(children, modifier, childrenGetters);
				}
			}
		}
	}

	/**
	 * Modify recursively.
	 *
	 * @param <T> the generic type
	 * @param docs the docs
	 * @param modifier the modifier
	 * @param childrenGetters the children getters
	 */
	@SafeVarargs
	public static <T> void modifyRecursively(@Nonnull Collection<T> docs, @Nonnull Consumer<T> modifier, @Nonnull Function<T, Collection<T>>... childrenGetters) {
		modifyRecursively(docs, (val, level) -> modifier.accept(val), childrenGetters);
	}

	/**
	 * Modify recursively.
	 *
	 * @param <T> the generic type
	 * @param doc the doc
	 * @param modifier the modifier
	 * @param childrenGetters the children getters
	 * @return the t
	 */
	@SafeVarargs
	public static <T> T modifyRecursively(@Nonnull T doc, @Nonnull BiConsumer<T, Integer> modifier, @Nonnull Function<T, Collection<T>>... childrenGetters) {
		modifyRecursively(Arrays.asList(doc), modifier, childrenGetters);
		return doc;
	}

	/**
	 * Modify recursively.
	 *
	 * @param <T> the generic type
	 * @param doc the doc
	 * @param modifier the modifier
	 * @param childrenGetters the children getters
	 * @return the t
	 */
	@SafeVarargs
	public static <T> T modifyRecursively(@Nonnull T doc, @Nonnull Consumer<T> modifier, @Nonnull Function<T, Collection<T>>... childrenGetters) {
		modifyRecursively(Arrays.asList(doc), modifier, childrenGetters);
		return doc;
	}

	/**
	 * Repeat runnable.
	 *
	 * @param count the count
	 * @param runnable the runnable
	 */
	public static void repeatRunnable(long count, final ThrowingRunnable runnable) {
		IntStream.iterate(0, i -> i + 1).limit(count).forEach(i -> {
			try {
				runnable.run();
			} catch (Exception e) {
				Exceptions.sneakyThrow(e);
			}
		});
	}

	/**
	 * Sum big decimal stream.
	 *
	 * @param stream the stream
	 * @return the big decimal
	 */
	public static BigDecimal sumBigDecimalStream(@Nonnull Stream<BigDecimal> stream) {
		return stream.filter(Checks::nonNull).reduce(
			BigDecimal.ZERO,
			(a, b) -> a.add(b, MathContext.DECIMAL128)
		);
	}

	/**
	 * Summarizing big decimal.
	 *
	 * @param <T> the generic type
	 * @param function the function
	 * @return the collector
	 */
	public static <T> Collector<T, ?, BigDecimalSummaryStatistics> summarizingBigDecimal(Function<? super T, BigDecimal> function) {
		return Collector.of(
			BigDecimalSummaryStatistics::new,
			(stats, each) -> stats.accept(function.apply(each)),
			BigDecimalSummaryStatistics::merge,
			Collector.Characteristics.UNORDERED
		);
	}

	/**
	 * Modify recursively.
	 *
	 * @param <T> the generic type
	 * @param docs the docs
	 * @param modifier the modifier
	 * @param level the level
	 * @param childrenGetters the children getters
	 */
	@SafeVarargs
	private static <T> void modifyRecursively(
		@Nonnull Collection<T> docs,
		@Nonnull BiConsumer<T, Integer> modifier,
		@Nonnull Integer level,
		@Nonnull Function<T, Collection<T>>... childrenGetters
	) {
		for (T doc : docs) {
			modifier.accept(doc, level);
			for (Function<T, Collection<T>> childrenGetter : childrenGetters) {
				Collection<T> children = childrenGetter.apply(doc);
				if (children != null) {
					modifyRecursively(children, modifier, level + 1, childrenGetters);
				}
			}
		}
	}
}