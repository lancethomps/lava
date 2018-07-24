package com.github.lancethomps.lava.common.ser;

import java.util.Map;

import com.github.lancethomps.lava.common.DynamicDataHandler;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class ExternalizableBeanTest.
 */
public class ExternalizableBeanTest extends ExternalizableBean implements DynamicDataHandler {

	/** The data. */
	@JsonIgnore
	private Map<String, Object> data;

	/** The value. */
	private Object value;

	/**
	 * Instantiates a new externalizable bean test.
	 */
	public ExternalizableBeanTest() {
		super();
	}

	/**
	 * Instantiates a new externalizable bean test.
	 *
	 * @param value the value
	 */
	public ExternalizableBeanTest(Object value) {
		super();
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.DynamicDataHandler#addDataPoint(java.lang.String, java.lang.Object)
	 */
	@Override
	public <T extends DynamicDataHandler> T addDataPoint(String name, Object value) {
		if (name != null) {
			if (name.startsWith("#")) {
				return (T) this;
			} else if (name.equals(Serializer.TYPE_PROPERTY)) {
				throw new AssertionError("Jackson class name property being added to dynamic data map - this shouldn't happen.");
			}
		}
		return DynamicDataHandler.super.addDataPoint(name, value);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.DynamicDataHandler#getData()
	 */
	@Override
	@JsonAnyGetter
	public Map<String, Object> getData() {
		return data;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.DynamicDataHandler#setData(java.util.Map)
	 */
	@Override
	public <T extends DynamicDataHandler> T setData(Map<String, Object> data) {
		this.data = data;
		return (T) this;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

}
