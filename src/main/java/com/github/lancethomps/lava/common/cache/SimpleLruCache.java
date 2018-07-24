package com.github.lancethomps.lava.common.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class SimpleCache.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class SimpleLruCache<K, V> implements Cache<K, V> {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(SimpleLruCache.class);

	/** The cache. */
	private Map<K, V> cache;

	/** The before remove consumer. */
	private final Consumer<V> beforeRemoveConsumer;

	/**
	 * Instantiates a new simple lru cache.
	 *
	 * @param capacity the capacity
	 */
	public SimpleLruCache(int capacity) {
		this(capacity, null);
	}

	/**
	 * Instantiates a new simple LRU cache.
	 *
	 * @param capacity the capacity
	 * @param beforeRemoveConsumer the before remove consumer
	 */
	public SimpleLruCache(int capacity, Consumer<V> beforeRemoveConsumer) {
		// LinkedHashMap is not thread-safe, so wrap in a synchronized map
		this.cache = Collections.synchronizedMap(new LruCache(capacity));
		this.beforeRemoveConsumer = beforeRemoveConsumer;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.cache.Cache#clear()
	 */
	@Override
	public void clear() throws CacheException {
		cache.clear();
	}

	/**
	 * Compute if absent.
	 *
	 * @param key the key
	 * @param mappingFunction the mapping function
	 * @return the v
	 */
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return cache.computeIfAbsent(key, mappingFunction);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.cache.Cache#dispose()
	 */
	@Override
	public void dispose() {
		cache.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.cache.Cache#get(java.lang.Object)
	 */
	@Override
	public V get(K key) {
		return cache.get(key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.cache.Cache#getName()
	 */
	@Override
	public String getName() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.cache.Cache#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void put(K key, V value) throws CacheException {
		cache.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.cache.Cache#put(java.lang.Object, java.lang.Object, int)
	 */
	@Override
	public void put(K key, V value, int timeToLive) throws CacheException {
		throw new CacheException("TTLs are not supported by the SimpleLruCache");
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.cache.Cache#putSafely(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void putSafely(K key, V value) {
		cache.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.cache.Cache#remove(java.lang.Object)
	 */
	@Override
	public void remove(K key) throws CacheException {
		cache.remove(key);
	}

	/**
	 * Size.
	 *
	 * @return the int
	 */
	public int size() {
		return cache.size();
	}

	/**
	 * The Class LruCache.
	 */
	private class LruCache extends LinkedHashMap<K, V> {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -6046206192135877316L;

		/** The capacity. */
		private final int capacity;

		/**
		 * Cache.
		 *
		 * @param capacity the capacity
		 */
		LruCache(int capacity) {
			super(capacity + 1, 1.1f, true);
			this.capacity = capacity;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
		 */
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
