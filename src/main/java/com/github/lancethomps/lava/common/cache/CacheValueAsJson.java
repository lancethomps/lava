package com.github.lancethomps.lava.common.cache;

import java.io.Serializable;

import org.springframework.cache.Cache.ValueWrapper;

import com.github.lancethomps.lava.common.ser.Serializer;

/**
 * The Class CacheValueWrapper.
 *
 * @param <T> the generic type
 */
public class CacheValueAsJson<T> implements ValueWrapper, Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1317784357150054308L;

	/** The json. */
	private final String json;

	/** The type. */
	private final Class<T> type;

	/**
	 * Instantiates a new cache value wrapper.
	 *
	 * @param value the value
	 */
	public CacheValueAsJson(T value) {
		if (value != null) {
			type = (Class<T>) value.getClass();
			json = Serializer.toJson(value);
		} else {
			type = null;
			json = null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.cache.Cache.ValueWrapper#get()
	 */
	@Override
	public Object get() {
		return getDeserialized();
	}

	/**
	 * Gets the deserialized.
	 *
	 * @return the deserialized
	 */
	public T getDeserialized() {
		return json == null ? null : Serializer.fromJson(json, type);
	}

	/**
	 * Gets the json.
	 *
	 * @return the json
	 */
	public String getJson() {
		return json;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public Class<T> getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Serializer.toLogString(this);
	}

}
