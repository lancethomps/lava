package com.lancethomps.lava.common.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lancethomps.lava.common.logging.Logs;

public class SimpleLruCache<K, V> implements Cache<K, V> {

  private static final Logger LOG = LogManager.getLogger(SimpleLruCache.class);
  private final Consumer<V> beforeRemoveConsumer;
  private Map<K, V> cache;

  public SimpleLruCache(int capacity) {
    this(capacity, null);
  }

  public SimpleLruCache(int capacity, Consumer<V> beforeRemoveConsumer) {

    this.cache = Collections.synchronizedMap(new LruCache(capacity));
    this.beforeRemoveConsumer = beforeRemoveConsumer;
  }

  @Override
  public void clear() throws CacheException {
    cache.clear();
  }

  public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
    return cache.computeIfAbsent(key, mappingFunction);
  }

  @Override
  public void dispose() {
    cache.clear();
  }

  @Override
  public V get(K key) {
    return cache.get(key);
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public void put(K key, V value) throws CacheException {
    cache.put(key, value);
  }

  @Override
  public void put(K key, V value, int timeToLive) throws CacheException {
    throw new CacheException("TTLs are not supported by the SimpleLruCache");
  }

  @Override
  public void putSafely(K key, V value) {
    cache.put(key, value);
  }

  @Override
  public void remove(K key) throws CacheException {
    cache.remove(key);
  }

  public int size() {
    return cache.size();
  }

  private class LruCache extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = -6046206192135877316L;

    private final int capacity;

    LruCache(int capacity) {
      super(capacity + 1, 1.1f, true);
      this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
      boolean remove = false;
      if (size() > capacity) {
        Logs.logTrace(LOG, "Removing least recently used cache entry [%s]", eldest.getKey());
        remove = true;
        if (beforeRemoveConsumer != null) {
          beforeRemoveConsumer.accept(eldest.getValue());
        }
      }
      return remove;
    }

  }

}
