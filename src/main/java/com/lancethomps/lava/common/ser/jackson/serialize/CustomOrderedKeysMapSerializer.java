package com.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.lancethomps.lava.common.Collect;

public class CustomOrderedKeysMapSerializer extends MapSerializer {

  private static final long serialVersionUID = 942838694300709663L;

  protected CustomOrderedKeysMapSerializer(
    MapSerializer src, BeanProperty property,
    JsonSerializer<?> keySerializer, JsonSerializer<?> valueSerializer,
    Set<String> ignoredEntries
  ) {
    super(src, property, keySerializer, valueSerializer, ignoredEntries);
  }

  protected CustomOrderedKeysMapSerializer(MapSerializer src, Object filterId, boolean sortKeys) {
    super(src, filterId, sortKeys);
  }

  protected CustomOrderedKeysMapSerializer(MapSerializer src, TypeSerializer vts, Object suppressableValue, boolean suppressNulls) {
    super(src, vts, suppressableValue, suppressNulls);
  }

  @Override
  public MapSerializer _withValueTypeSerializer(TypeSerializer vts) {
    if (_valueTypeSerializer == vts) {
      return this;
    }
    return new CustomOrderedKeysMapSerializer(this, vts, _suppressableValue, _suppressNulls);
  }

  @Override
  public void serialize(Map<?, ?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    super.serialize(value, gen, provider);
  }

  @Override
  public MapSerializer withContentInclusion(Object suppressableValue) {
    if (suppressableValue == _suppressableValue) {
      return this;
    }
    return new CustomOrderedKeysMapSerializer(this, _valueTypeSerializer, suppressableValue, _suppressNulls);
  }

  @Override
  public MapSerializer withContentInclusion(Object suppressableValue, boolean suppressNulls) {
    if ((suppressableValue == _suppressableValue) && (suppressNulls == _suppressNulls)) {
      return this;
    }
    return new CustomOrderedKeysMapSerializer(this, _valueTypeSerializer, suppressableValue, suppressNulls);
  }

  @Override
  public MapSerializer withFilterId(Object filterId) {
    if (_filterId == filterId) {
      return this;
    }
    return new CustomOrderedKeysMapSerializer(this, filterId, _sortKeys);
  }

  @Override
  public MapSerializer withResolved(
    BeanProperty property,
    JsonSerializer<?> keySerializer, JsonSerializer<?> valueSerializer,
    Set<String> ignored, boolean sortKeys
  ) {
    CustomOrderedKeysMapSerializer ser = new CustomOrderedKeysMapSerializer(this, property, keySerializer, valueSerializer, ignored);
    if (sortKeys != ser._sortKeys) {
      ser = new CustomOrderedKeysMapSerializer(ser, _filterId, sortKeys);
    }
    return ser;
  }

  @Override
  protected Map<?, ?> _orderEntries(Map<?, ?> input, JsonGenerator gen, SerializerProvider provider) throws IOException {
    return orderMap(input, gen, provider);
  }

  @SuppressWarnings("unchecked")
  private Map<?, ?> orderMap(Map<?, ?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    if ((value != null) && !value.isEmpty()) {
      Object firstKey = value.keySet().iterator().next();
      if (firstKey == null) {
        return value;
      }
      if (firstKey instanceof String) {
        if (!(value instanceof SortedMap) || ((value instanceof TreeMap) && (((TreeMap<?, ?>) value).comparator() == null))) {
          value = Collect.createOrderedMap((Map<String, ?>) value);
        }
      } else if (firstKey instanceof Comparable) {
        if (!(value instanceof SortedMap) || ((value instanceof TreeMap) && (((TreeMap<?, ?>) value).comparator() == null))) {
          value = new TreeMap<>(value);
        }
      }

    }
    return value;
  }

}
