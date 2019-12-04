// CHECKSTYLE.OFF:
package com.lancethomps.lava.common.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

public class FastHashMap<K, V> extends HashMap<K, V> {

  private static final long serialVersionUID = -4184506928639913323L;

  private boolean fast;

  private HashMap<K, V> map;

  public FastHashMap() {
    this(false);
  }

  public FastHashMap(boolean fast) {
    super();
    this.map = new HashMap<>();
    this.fast = fast;
  }

  public FastHashMap(int capacity) {
    super();
    this.map = new HashMap<>(capacity);
  }

  public FastHashMap(int capacity, float factor) {
    super();
    this.map = new HashMap<>(capacity, factor);
  }

  public FastHashMap(Map<K, V> map) {
    this(map, false);
  }

  public FastHashMap(Map<K, V> map, boolean fast) {
    super();
    this.map = new HashMap<>(map);
    this.fast = fast;
  }

  public static <E> Set<E> createBackedSet() {
    return Collections.newSetFromMap(new FastHashMap<>(true));
  }

  public static <E> Set<E> createBackedSet(Collection<? extends E> c) {
    FastHashMap<E, Boolean> map = new FastHashMap<>(false);
    Set<E> set = Collections.newSetFromMap(map);
    if (c != null) {
      set.addAll(c);
    }
    map.setFast(true);
    return set;
  }

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

  @Override
  public boolean containsKey(Object key) {
    if (fast) {
      return (map.containsKey(key));
    }
    synchronized (map) {
      return (map.containsKey(key));
    }
  }

  @Override
  public boolean containsValue(Object value) {
    if (fast) {
      return (map.containsValue(value));
    }
    synchronized (map) {
      return (map.containsValue(value));
    }
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return new EntrySet();
  }

  @Override
  public V get(Object key) {
    if (fast) {
      return (map.get(key));
    }
    synchronized (map) {
      return (map.get(key));
    }
  }

  @JsonAnyGetter
  public HashMap<K, V> getDelegate() {
    return map;
  }

  public boolean getFast() {
    return (this.fast);
  }

  public void setFast(boolean fast) {
    this.fast = fast;
  }

  @Override
  public boolean isEmpty() {
    if (fast) {
      return (map.isEmpty());
    }
    synchronized (map) {
      return (map.isEmpty());
    }
  }

  @Override
  public Set keySet() {
    return new KeySet();
  }

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

  @Override
  public int size() {
    if (fast) {
      return (map.size());
    }
    synchronized (map) {
      return (map.size());
    }
  }

  @Override
  public Collection values() {
    return new Values();
  }

  @Override
  public boolean equals(Object o) {

    if (o == this) {
      return (true);
    } else if (!(o instanceof Map)) {
      return (false);
    }
    Map mo = (Map) o;

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

  private abstract class CollectionView implements Collection {

    CollectionView() {
    }

    @Override
    public boolean add(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection c) {
      throw new UnsupportedOperationException();
    }

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

    @Override
    public boolean contains(Object o) {
      if (fast) {
        return get(map).contains(o);
      }
      synchronized (map) {
        return get(map).contains(o);
      }
    }

    @Override
    public boolean containsAll(Collection o) {
      if (fast) {
        return get(map).containsAll(o);
      }
      synchronized (map) {
        return get(map).containsAll(o);
      }
    }

    @Override
    public boolean isEmpty() {
      if (fast) {
        return get(map).isEmpty();
      }
      synchronized (map) {
        return get(map).isEmpty();
      }
    }

    @Override
    public Iterator iterator() {
      return new CollectionViewIterator();
    }

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

    @Override
    public int size() {
      if (fast) {
        return get(map).size();
      }
      synchronized (map) {
        return get(map).size();
      }
    }

    @Override
    public Object[] toArray() {
      if (fast) {
        return get(map).toArray();
      }
      synchronized (map) {
        return get(map).toArray();
      }
    }

    @Override
    public Object[] toArray(Object[] o) {
      if (fast) {
        return get(map).toArray(o);
      }
      synchronized (map) {
        return get(map).toArray(o);
      }
    }

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

    @Override
    public int hashCode() {
      if (fast) {
        return get(map).hashCode();
      }
      synchronized (map) {
        return get(map).hashCode();
      }
    }

    protected abstract Collection get(Map map);

    protected abstract Object iteratorNext(Map.Entry entry);

    private class CollectionViewIterator implements Iterator {

      private Map expected;

      private Iterator iterator;

      private Map.Entry lastReturned;

      CollectionViewIterator() {
        this.expected = map;
        this.iterator = expected.entrySet().iterator();
      }

      @Override
      public boolean hasNext() {
        if (expected != map) {
          throw new ConcurrentModificationException();
        }
        return iterator.hasNext();
      }

      @Override
      public Object next() {
        if (expected != map) {
          throw new ConcurrentModificationException();
        }
        lastReturned = (Map.Entry) iterator.next();
        return iteratorNext(lastReturned);
      }

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

  private class EntrySet extends CollectionView implements Set {

    @Override
    protected Collection get(Map map) {
      return map.entrySet();
    }

    @Override
    protected Object iteratorNext(Map.Entry entry) {
      return entry;
    }

  }

  private class KeySet extends CollectionView implements Set {

    @Override
    protected Collection get(Map map) {
      return map.keySet();
    }

    @Override
    protected Object iteratorNext(Map.Entry entry) {
      return entry.getKey();
    }

  }

  private class Values extends CollectionView {

    @Override
    protected Collection get(Map map) {
      return map.values();
    }

    @Override
    protected Object iteratorNext(Map.Entry entry) {
      return entry.getValue();
    }

  }

}
