package com.github.lancethomps.lava.common.cache;

public interface Cache<K, V> {

  void clear() throws CacheException;

  void dispose();

  V get(K key);

  String getName();

  void put(K key, V value) throws CacheException;

  void put(K key, V value, int timeToLive) throws CacheException;

  default V putIfAbsent(K key, V value) {
    V v = get(key);
    if (v == null) {
      putSafely(key, value);
    }

    return v;
  }

  void putSafely(K key, V value);

  default void putSafely(K key, V value, int timeToLive) {
    try {
      put(key, value, timeToLive);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  void remove(K key) throws CacheException;

  default void removeIgnoreErrors(K key) {
    try {
      remove(key);
    } catch (CacheException e) {
    }
  }

}
