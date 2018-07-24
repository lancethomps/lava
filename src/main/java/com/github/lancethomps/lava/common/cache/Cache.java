package com.github.lancethomps.lava.common.cache;

/**
 * The Interface Cache.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface Cache<K, V> {

	/**
	 * Clear.
	 *
	 * @throws CacheException the cache exception
	 */
	void clear() throws CacheException;

	/**
	 * Dispose.
	 */
	void dispose();

	/**
	 * Gets the.
	 *
	 * @param key the key
	 * @return the v
	 */
	V get(K key);

	/**
	 * Gets the name. May return null.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Put.
	 *
	 * @param key the key
	 * @param value the value
	 * @throws CacheException the cache exception
	 */
	void put(K key, V value) throws CacheException;

	/**
	 * Put an entry into the cache with a time to live specified in seconds.
	 *
	 * @param key the key
	 * @param value the value
	 * @param timeToLive the time to live in seconds
	 * @throws CacheException the cache exception
	 */
	void put(K key, V value, int timeToLive) throws CacheException;

	/**
	 * Put if absent.
	 *
	 * @param key the key
	 * @param value the value
	 * @return the v
	 */
	default V putIfAbsent(K key, V value) {
		V v = get(key);
		if (v == null) {
			putSafely(key, value);
		}

		return v;
	}

	/**
	 * Put safely.
	 *
	 * @param key the key
	 * @param value the value
	 */
	void putSafely(K key, V value);

	/**
	 * Put safely.
	 *
	 * @param key the key
	 * @param value the value
	 * @param timeToLive the time to live
	 */
	default void putSafely(K key, V value, int timeToLive) {
		try {
			put(key, value, timeToLive);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes the.
	 *
	 * @param key the key
	 * @throws CacheException the cache exception
	 */
	void remove(K key) throws CacheException;

	/**
	 * Removes the ignore errors.
	 *
	 * @param key the key
	 */
	default void removeIgnoreErrors(K key) {
		try {
			remove(key);
		} catch (CacheException e) {
			;
		}
	}
}
