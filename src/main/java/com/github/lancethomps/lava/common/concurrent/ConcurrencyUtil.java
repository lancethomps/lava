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

public class ConcurrencyUtil {

  private static final Logger LOG = Logger.getLogger(ConcurrencyUtil.class);

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
        Logs.logDebug(
          LOG,
          "Synchronizing processing for method with key [%s] because count is [%s] and max concurrent requests is set to [%s].",
          key,
          count,
          maxConcurrent
        );
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

  public static <T> void parallelStreamWithLimit(@Nonnull List<T> list, int limit, @Nonnull Consumer<Stream<T>> streamConsumer) {
    List<List<T>> brokenUpLists = Collect.breakUpListByTotalSubLists(list, limit);
    brokenUpLists.parallelStream().map(Collection::stream).forEach(streamConsumer);
  }

}
