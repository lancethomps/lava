package com.github.lancethomps.lava.common.properties;

/**
 * The Class PropertyParserKey.
 */
public class PropertyParserKey {

	/** The bundle. */
	private String bundle;

	/** The default value. */
	private String defaultValue;

	/** The prop key. */
	private String propKey;

	/** The type. */
	private String type;

	/**
	 * Gets the bundle.
	 *
	 * @return the bundle
	 */
	public String getBundle() {
		return bundle;
	}

	/**
	 * Gets the default value.
	 *
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Gets the prop key.
	 *
	 * @return the propKey
	 */
	public String getPropKey() {
		return propKey;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the bundle.
	 *
	 * @param bundle the bundle to set
	 * @return the property parser key
	 */
	public PropertyParserKey setBundle(String bundle) {
		this.bundle = bundle;
		return this;
	}

	/**
	 * Sets the default value.
	 *
	 * @param defaultValue the defaultValue to set
	 * @return the property parser key
	 */
	public PropertyParserKey setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	/**
	 * Sets the prop key.
	 *
	 * @param propKey the propKey to set
	 * @return the property parser key
	 */
	public PropertyParserKey setPropKey(String propKey) {
		this.propKey = propKey;
		return this;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type to set
	 * @return the property parser key
	 */
	public PropertyParserKey setType(String type) {
		this.type = type;
		return this;
	}

}
