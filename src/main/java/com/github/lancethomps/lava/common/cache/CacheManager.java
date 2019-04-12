package com.github.lancethomps.lava.common.cache;

public interface CacheManager {

  <K, V> Cache<K, V> getCache(String cacheName);

}
