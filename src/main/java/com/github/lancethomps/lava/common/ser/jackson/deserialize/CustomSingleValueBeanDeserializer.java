package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;

/**
 * The Class CustomSingleStringBeanDeserializer.
 */
public class CustomSingleValueBeanDeserializer extends BeanDeserializer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8952958753951093572L;

	/** The double creator. */
	private Function<Double, Object> doubleCreator;

	/** The number creator. */
	private Function<Number, Object> numberCreator;

	/** The single string creator. */
	private Function<String, Object> stringCreator;

	/**
	 * Instantiates a new custom single value bean deserializer.
	 *
	 * @param defaultDeserializer the default deserializer
	 */
	public CustomSingleValueBeanDeserializer(JsonDeserializer<?> defaultDeserializer) {
		this(defaultDeserializer, null);
	}

	/**
	 * Instantiates a new custom single string bean deserializer.
	 *
	 * @param defaultDeserializer the default deserializer
	 * @param stringCreator the single string creator
	 */
	public CustomSingleValueBeanDeserializer(JsonDeserializer<?> defaultDeserializer, Function<String, Object> stringCreator) {
		super((BeanDeserializerBase) defaultDeserializer);
		this.stringCreator = stringCreator;
	}

	@Override
	public Object deserializeFromDouble(JsonParser p, DeserializationContext ctxt) throws IOException {
		return doubleCreator != null ? doubleCreator.apply(p.getDoubleValue()) : super.deserializeFromDouble(p, ctxt);
	}

	@Override
	public Object deserializeFromNumber(JsonParser p, DeserializationContext ctxt) throws IOException {
		return numberCreator != null ? numberCreator.apply(p.getNumberValue()) : super.deserializeFromNumber(p, ctxt);
	}

	@Override
	public Object deserializeFromString(JsonParser p, DeserializationContext ctxt) throws IOException {
		if (_objectIdReader != null) {
			return deserializeFromObjectId(p, ctxt);
		}
		return stringCreator != null ? stringCreator.apply(p.getText()) : super.deserializeFromString(p, ctxt);
	}

	/**
	 * @return the doubleCreator
	 */
	public Function<Double, Object> getDoubleCreator() {
		return doubleCreator;
	}

	/**
	 * @return the numberCreator
	 */
	public Function<Number, Object> getNumberCreator() {
		return numberCreator;
	}

	/**
	 * Gets the string creator.
	 *
	 * @return the stringCreator
	 */
	public Function<String, Object> getStringCreator() {
		return stringCreator;
	}

	/**
	 * Sets the double creator.
	 *
	 * @param doubleCreator the doubleCreator to set
	 * @return the custom single value bean deserializer
	 */
	public CustomSingleValueBeanDeserializer setDoubleCreator(Function<Double, Object> doubleCreator) {
		this.doubleCreator = doubleCreator;
		return this;
	}

	/**
	 * Sets the number creator.
	 *
	 * @param numberCreator the numberCreator to set
	 * @return the custom single value bean deserializer
	 */
	public CustomSingleValueBeanDeserializer setNumberCreator(Function<Number, Object> numberCreator) {
		this.numberCreator = numberCreator;
		return this;
	}

	/**
	 * Sets the string creator.
	 *
	 * @param stringCreator the stringCreator to set
	 * @return the custom single value bean deserializer
	 */
	public CustomSingleValueBeanDeserializer setStringCreator(Function<String, Object> stringCreator) {
		this.stringCreator = stringCreator;
		return this;
	}
}
