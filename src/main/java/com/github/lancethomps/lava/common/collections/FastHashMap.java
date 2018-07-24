// CHECKSTYLE.OFF:
package com.github.lancethomps.lava.common.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

/**
 * The Class FastHashMap.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class FastHashMap<K, V> extends HashMap<K, V> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4184506928639913323L;

	/** Are we currently operating in "fast" mode?. */
	private boolean fast;

	/**
	 * The underlying map we are managing.
	 */
	private HashMap<K, V> map;

	/**
	 * Construct an empty map.
	 */
	public FastHashMap() {
		this(false);
	}

	/**
	 * Instantiates a new fast hash map.
	 *
	 * @param fast the fast
	 */
	public FastHashMap(boolean fast) {
		super();
		this.map = new HashMap<>();
		this.fast = fast;
	}

	// Constructors
	// ----------------------------------------------------------------------

	/**
	 * Construct an empty map with the specified capacity.
	 *
	 * @param capacity the initial capacity of the empty map
	 */
	public FastHashMap(int capacity) {
		super();
		this.map = new HashMap<>(capacity);
	}

	/**
	 * Construct an empty map with the specified capacity and load factor.
	 *
	 * @param capacity the initial capacity of the empty map
	 * @param factor the load factor of the new map
	 */
	public FastHashMap(int capacity, float factor) {
		super();
		this.map = new HashMap<>(capacity, factor);
	}

	/**
	 * Instantiates a new fast hash map.
	 *
	 * @param map the map
	 */
	public FastHashMap(Map<K, V> map) {
		this(map, false);
	}

	/**
	 * Construct a new map with the same mappings as the specified map.
	 *
	 * @param map the map whose mappings are to be copied
	 * @param fast the fast
	 */
	public FastHashMap(Map<K, V> map, boolean fast) {
		super();
		this.map = new HashMap<>(map);
		this.fast = fast;
	}

	/**
	 * Creates the backed set.
	 *
	 * @param <E> the element type
	 * @return the sets the
	 */
	public static <E> Set<E> createBackedSet() {
		return Collections.newSetFromMap(new FastHashMap<>(true));
	}

	/**
	 * Creates the backed set.
	 *
	 * @param <E> the element type
	 * @param c the c
	 * @return the sets the
	 */
	public static <E> Set<E> createBackedSet(Collection<? extends E> c) {
		FastHashMap<E, Boolean> map = new FastHashMap<>(false);
		Set<E> set = Collections.newSetFromMap(map);
		if (c != null) {
			set.addAll(c);
		}
		map.setFast(true);
		return set;
	}

	/**
	 * Remove all mappings from this map.
	 */
	@Override
	public void clear() {
		if (fast) {
			synchronized (this) {
				map = new HashMap<>();
			}
		} else {
			synchronized (map) {
				map.clear();
			}
		}
	}

	// Property access
	// ----------------------------------------------------------------------

	/**
	 * Return a shallow copy of this <code>FastHashMap</code> instance. The keys and values themselves
	 * are not copied.
	 *
	 * @return a clone of this map
	 */
	@Override
	public Object clone() {
		FastHashMap<K, V> results = null;
		if (fast) {
			results = new FastHashMap<>(map);
		} else {
			synchronized (map) {
				results = new FastHashMap<>(map);
			}
		}
		results.setFast(getFast());
		return (results);
	}

	// Map access
	// ----------------------------------------------------------------------
	// These methods can forward straight to the wrapped Map in 'fast' mode.
	// (because they are query methods)

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		if (fast) {
			if (map.containsKey(key)) {
				return map.get(key);
			}
		} else {
			synchronized (map) {
				if (map.containsKey(key)) {
					return map.get(key);
				}
			}
		}
		V val = mappingFunction.apply(key);
		put(key, val);
		return val;
	}

	/**
	 * Return <code>true</code> if this map contains a mapping for the specified key.
	 *
	 * @param key the key to be searched for
	 * @return true if the map contains the key
	 */
	@Override
	public boolean containsKey(Object key) {
		if (fast) {
			return (map.containsKey(key));
		}
		synchronized (map) {
			return (map.containsKey(key));
		}
	}

	/**
	 * Return <code>true</code> if this map contains one or more keys mapping to the specified value.
	 *
	 * @param value the value to be searched for
	 * @return true if the map contains the value
	 */
	@Override
	public boolean containsValue(Object value) {
		if (fast) {
			return (map.containsValue(value));
		}
		synchronized (map) {
			return (map.containsValue(value));
		}
	}

	/**
	 * Return a collection view of the mappings contained in this map. Each element in the returned
	 * collection is a <code>Map.Entry</code>.
	 *
	 * @return the sets the
	 */
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new EntrySet();
	}

	/**
	 * Compare the specified object with this list for equality. This implementation uses exactly the
	 * code that is used to define the list equals function in the documentation for the
	 * <code>Map.equals</code> method.
	 *
	 * @param o the object to be compared to this list
	 * @return true if the two maps are equal
	 */
	@Override
	public boolean equals(Object o) {
		// Simple tests that require no synchronization
		if (o == this) {
			return (true);
		} else if (!(o instanceof Map)) {
			return (false);
		}
		Map mo = (Map) o;

		// Compare the two maps for equality
		if (fast) {
			if (mo.size() != map.size()) {
				return (false);
			}
			Iterator i = map.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				Object key = e.getKey();
				Object value = e.getValue();
				if (value == null) {
					if (!((mo.get(key) == null) && mo.containsKey(key))) {
						return (false);
					}
				} else {
					if (!value.equals(mo.get(key))) {
						return (false);
					}
				}
			}
			return (true);

		}
		synchronized (map) {
			if (mo.size() != map.size()) {
				return (false);
			}
			Iterator i = map.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				Object key = e.getKey();
				Object value = e.getValue();
				if (value == null) {
					if (!((mo.get(key) == null) && mo.containsKey(key))) {
						return (false);
					}
				} else {
					if (!value.equals(mo.get(key))) {
						return (false);
					}
				}
			}
			return (true);
		}
	}

	/**
	 * Return the value to which this map maps the specified key. Returns <code>null</code> if the map
	 * contains no mapping for this key, or if there is a mapping with a value of <code>null</code>. Use
	 * the <code>containsKey()</code> method to disambiguate these cases.
	 *
	 * @param key the key whose value is to be returned
	 * @return the value mapped to that key, or null
	 */
	@Override
	public V get(Object key) {
		if (fast) {
			return (map.get(key));
		}
		synchronized (map) {
			return (map.get(key));
		}
	}

	// Map modification
	// ----------------------------------------------------------------------
	// These methods perform special behaviour in 'fast' mode.
	// The map is cloned, updated and then assigned back.
	// See the comments at the top as to why this won't always work.

	/**
	 * Gets the delegate.
	 *
	 * @return the delegate
	 */
	@JsonAnyGetter
	public HashMap<K, V> getDelegate() {
		return map;
	}

	/**
	 * Returns true if this map is operating in fast mode.
	 *
	 * @return true if this map is operating in fast mode
	 */
	public boolean getFast() {
		return (this.fast);
	}

	/**
	 * Return the hash code value for this map. This implementation uses exactly the code that is used
	 * to define the list hash function in the documentation for the <code>Map.hashCode</code> method.
	 *
	 * @return suitable integer hash code
	 */
	@Override
	public int hashCode() {
		if (fast) {
			int h = 0;
			Iterator i = map.entrySet().iterator();
			while (i.hasNext()) {
				h += i.next().hashCode();
			}
			return (h);
		}
		synchronized (map) {
			int h = 0;
			Iterator i = map.entrySet().iterator();
			while (i.hasNext()) {
				h += i.next().hashCode();
			}
			return (h);
		}
	}

	/**
	 * Return <code>true</code> if this map contains no mappings.
	 *
	 * @return is the map currently empty
	 */
	@Override
	public boolean isEmpty() {
		if (fast) {
			return (map.isEmpty());
		}
		synchronized (map) {
			return (map.isEmpty());
		}
	}

	// Basic object methods
	// ----------------------------------------------------------------------

	/**
	 * Return a set view of the keys contained in this map.
	 *
	 * @return the sets the
	 */
	@Override
	public Set keySet() {
		return new KeySet();
	}

	/**
	 * Associate the specified value with the specified key in this map. If the map previously contained
	 * a mapping for this key, the old value is replaced and returned.
	 *
	 * @param key the key with which the value is to be associated
	 * @param value the value to be associated with this key
	 * @return the value previously mapped to the key, or null
	 */
	@Override
	public V put(K key, V value) {
		if (fast) {
			synchronized (this) {
				HashMap<K, V> temp = (HashMap<K, V>) map.clone();
				V result = temp.put(key, value);
				map = temp;
				return (result);
			}
		}
		synchronized (map) {
			return (map.put(key, value));
		}
	}

	/**
	 * Copy all of the mappings from the specified map to this one, replacing any mappings with the same
	 * keys.
	 *
	 * @param in the map whose mappings are to be copied
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> in) {
		if (fast) {
			synchronized (this) {
				HashMap<K, V> temp = (HashMap<K, V>) map.clone();
				temp.putAll(in);
				map = temp;
			}
		} else {
			synchronized (map) {
				map.putAll(in);
			}
		}
	}

	/**
	 * Remove any mapping for this key, and return any previously mapped value.
	 *
	 * @param key the key whose mapping is to be removed
	 * @return the value removed, or null
	 */
	@Override
	public V remove(Object key) {
		if (fast) {
			synchronized (this) {
				HashMap<K, V> temp = (HashMap<K, V>) map.clone();
				V result = temp.remove(key);
				map = temp;
				return (result);
			}
		}
		synchronized (map) {
			return (map.remove(key));
		}
	}

	// Map views
	// ----------------------------------------------------------------------

	/**
	 * Sets whether this map is operating in fast mode.
	 *
	 * @param fast true if this map should operate in fast mode
	 */
	public void setFast(boolean fast) {
		this.fast = fast;
	}

	/**
	 * Return the number of key-value mappings in this map.
	 *
	 * @return the current size of the map
	 */
	@Override
	public int size() {
		if (fast) {
			return (map.size());
		}
		synchronized (map) {
			return (map.size());
		}
	}

	/**
	 * Return a collection view of the values contained in this map.
	 *
	 * @return the collection
	 */
	@Override
	public Collection values() {
		return new Values();
	}

	// Map view inner classes
	// ----------------------------------------------------------------------

	/**
	 * Abstract collection implementation shared by keySet(), values() and entrySet().
	 */
	private abstract class CollectionView implements Collection {

		/**
		 * Instantiates a new collection view.
		 */
		CollectionView() {
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Collection#add(java.lang.Object)
		 */
		@Override
		public boolean add(Object o) {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Collection#addAll(java.util.Collection)
		 */
		@Override
		public boolean addAll(Collection c) {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Collection#clear()
		 */
		@Override
		public void clear() {
			if (fast) {
				synchronized (FastHashMap.this) {
					map = new HashMap();
				}
			} else {
				synchronized (map) {
					get(map).clear();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Collection#contains(java.lang.Object)
		 */
		@Override
		public boolean contains(Object o) {
			if (fast) {
				return get(map).contains(o);
			}
			synchronized (map) {
				return get(map).contains(o);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Collection#containsAll(java.util.Collection)
		 */
		@Override
		public boolean containsAll(Collection o) {
			if (fast) {
				return get(map).containsAll(o);
			}
			synchronized (map) {
				return get(map).containsAll(o);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (fast) {
				return get(map).equals(o);
			}
			synchronized (map) {
				return get(map).equals(o);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			if (fast) {
				return get(map).hashCode();
			}
			synchronized (map) {
				return get(map).hashCode();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Collection#isEmpty()
		 */
		@Override
		public boolean isEmpty() {
			if (fast) {
				return get(map).isEmpty();
			}
			synchronized (map) {
				return get(map).isEmpty();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Collection#iterator()
		 */
		@Override
		public Iterator iterator() {
			return new CollectionViewIterator();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Collection#remove(java.lang.Object)
		 */
		@Override
		public boolean remove(Object o) {
			if (fast) {
				synchronized (FastHashMap.this) {
					HashMap temp = (HashMap) map.clone();
					boolean r = get(temp).remove(o);
					map = temp;
					return r;
				}
			}
			synchronized (map) {
				return get(map).remove(o);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Collection#removeAll(java.util.Collection)
		 */
		@Override
		public boolean removeAll(Collection o) {
			if (fast) {
				synchronized (FastHashMap.this) {
					HashMap temp = (HashMap) map.clone();
					boolean r = get(temp).removeAll(o);
					map = temp;
					return r;
				}
			}
			synchronized (map) {
				return get(map).removeAll(o);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Collection#retainAll(java.util.Collection)
		 */
		@Override
		public boolean retainAll(Collection o) {
			if (fast) {
				synchronized (FastHashMap.this) {
					HashMap temp = (HashMap) map.clone();
					boolean r = get(temp).retainAll(o);
					map = temp;
					return r;
				}
			}
			synchronized (map) {
				return get(map).retainAll(o);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Collection#size()
		 */
		@Override
		public int size() {
			if (fast) {
				return get(map).size();
			}
			synchronized (map) {
				return get(map).size();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Collection#toArray()
		 */
		@Override
		public Object[] toArray() {
			if (fast) {
				return get(map).toArray();
			}
			synchronized (map) {
				return get(map).toArray();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Collection#toArray(java.lang.Object[])
		 */
		@Override
		public Object[] toArray(Object[] o) {
			if (fast) {
				return get(map).toArray(o);
			}
			synchronized (map) {
				return get(map).toArray(o);
			}
		}

		/**
		 * Gets the.
		 *
		 * @param map the map
		 * @return the collection
		 */
		protected abstract Collection get(Map map);

		/**
		 * Iterator next.
		 *
		 * @param entry the entry
		 * @return the object
		 */
		protected abstract Object iteratorNext(Map.Entry entry);

		/**
		 * The Class CollectionViewIterator.
		 */
		private class CollectionViewIterator implements Iterator {

			/** The expected. */
			private Map expected;

			/** The iterator. */
			private Iterator iterator;

			/** The last returned. */
			private Map.Entry lastReturned;

			/**
			 * Instantiates a new collection view iterator.
			 */
			CollectionViewIterator() {
				this.expected = map;
				this.iterator = expected.entrySet().iterator();
			}

			/*
			 * (non-Javadoc)
			 * @see java.util.Iterator#hasNext()
			 */
			@Override
			public boolean hasNext() {
				if (expected != map) {
					throw new ConcurrentModificationException();
				}
				return iterator.hasNext();
			}

			/*
			 * (non-Javadoc)
			 * @see java.util.Iterator#next()
			 */
			@Override
			public Object next() {
				if (expected != map) {
					throw new ConcurrentModificationException();
				}
				lastReturned = (Map.Entry) iterator.next();
				return iteratorNext(lastReturned);
			}

			/*
			 * (non-Javadoc)
			 * @see java.util.Iterator#remove()
			 */
			@Override
			public void remove() {
				if (lastReturned == null) {
					throw new IllegalStateException();
				}
				if (fast) {
					synchronized (FastHashMap.this) {
						if (expected != map) {
							throw new ConcurrentModificationException();
						}
						FastHashMap.this.remove(lastReturned.getKey());
						lastReturned = null;
						expected = map;
					}
				} else {
					iterator.remove();
					lastReturned = null;
				}
			}
		}
	}

	/**
	 * Set implementation over the entries of the FastHashMap.
	 */
	private class EntrySet extends CollectionView implements Set {

		/*
		 * (non-Javadoc)
		 * @see com.github.lancethomps.lava.common.collections.FastHashMap.CollectionView#get(java.util.Map)
		 */
		@Override
		protected Collection get(Map map) {
			return map.entrySet();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.github.lancethomps.lava.common.collections.FastHashMap.CollectionView#iteratorNext(java.util.Map.Entry)
		 */
		@Override
		protected Object iteratorNext(Map.Entry entry) {
			return entry;
		}

	}

	/**
	 * Set implementation over the keys of the FastHashMap.
	 */
	private class KeySet extends CollectionView implements Set {

		/*
		 * (non-Javadoc)
		 * @see com.github.lancethomps.lava.common.collections.FastHashMap.CollectionView#get(java.util.Map)
		 */
		@Override
		protected Collection get(Map map) {
			return map.keySet();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.github.lancethomps.lava.common.collections.FastHashMap.CollectionView#iteratorNext(java.util.Map.Entry)
		 */
		@Override
		protected Object iteratorNext(Map.Entry entry) {
			return entry.getKey();
		}

	}

	/**
	 * Collection implementation over the values of the FastHashMap.
	 */
	private class Values extends CollectionView {

		/*
		 * (non-Javadoc)
		 * @see com.github.lancethomps.lava.common.collections.FastHashMap.CollectionView#get(java.util.Map)
		 */
		@Override
		protected Collection get(Map map) {
			return map.values();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.github.lancethomps.lava.common.collections.FastHashMap.CollectionView#iteratorNext(java.util.Map.Entry)
		 */
		@Override
		protected Object iteratorNext(Map.Entry entry) {
			return entry.getValue();
		}
	}

}
