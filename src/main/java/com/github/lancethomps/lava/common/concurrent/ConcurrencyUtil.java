package com.github.lancethomps.lava.common.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Collect;
import com.github.lancethomps.lava.common.lambda.ThrowingConsumer;
import com.github.lancethomps.lava.common.lambda.ThrowingFunction;
import com.github.lancethomps.lava.common.lambda.ThrowingSupplier;
import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class ConcurrencyUtil.
 */
public class ConcurrencyUtil {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(ConcurrencyUtil.class);

	/**
	 * Exec with max.
	 *
	 * @param <T> the generic type
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param synchObjects the synch objects
	 * @param key the key
	 * @param maxConcurrent the max concurrent
	 * @param countSupplier the count supplier
	 * @param releaser the releaser
	 * @param supplier the supplier
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T, K, V> T execWithMax(
		Map<K, Object> synchObjects,
		K key,
		Long maxConcurrent,
		ThrowingFunction<K, Long> countSupplier,
		ThrowingConsumer<K> releaser,
		ThrowingSupplier<T> supplier
	) throws Exception {
		Long count = countSupplier.apply(key);
		try {
			if ((maxConcurrent != null) && (count != null) && (count >= maxConcurrent)) {
				Logs.logDebug(LOG, "Synchronizing processing for method with key [%s] because count is [%s] and max concurrent requests is set to [%s].", key, count, maxConcurrent);
				Object synchObject = synchObjects.computeIfAbsent(key, k -> new Object());
				synchronized (synchObject) {
					return supplier.get();
				}
			}
			return supplier.get();
		} finally {
			if (releaser != null) {
				releaser.accept(key);
			}
		}
	}

	/**
	 * Parallel stream with limit.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param limit the limit
	 * @param streamConsumer the stream consumer
	 */
	public static <T> void parallelStreamWithLimit(@Nonnull List<T> list, int limit, @Nonnull Consumer<Stream<T>> streamConsumer) {
		List<List<T>> brokenUpLists = Collect.breakUpListByTotalSubLists(list, limit);
		brokenUpLists.parallelStream().map(Collection::stream).forEach(streamConsumer);
	}

}
