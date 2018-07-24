package com.github.lancethomps.lava.common.cache;

/**
 * The Interface CacheManager.
 *
 * @author lathomps
 */
public interface CacheManager {

	/**
	 * Gets the cache.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param cacheName the cache name
	 * @return the cache
	 */
	<K, V> Cache<K, V> getCache(String cacheName);

}
