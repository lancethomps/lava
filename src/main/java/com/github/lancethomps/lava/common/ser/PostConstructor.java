package com.github.lancethomps.lava.common.ser;

/**
 * The Class PostConstructor.
 */
public abstract class PostConstructor {

	/**
	 * After deserialization.
	 */
	public void afterDeserialization() {
		// override this method if needed after deserialization using Serializer
	}

}
