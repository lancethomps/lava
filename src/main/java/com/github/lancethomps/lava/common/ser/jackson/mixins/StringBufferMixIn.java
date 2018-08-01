package com.github.lancethomps.lava.common.ser.jackson.mixins;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The Class StringBufferMixIn.
 *
 * @author lancethomps
 */
public abstract class StringBufferMixIn {

	/**
	 * Instantiates a new string buffer mix in.
	 *
	 * @param val the val
	 */
	@JsonCreator
	public StringBufferMixIn(String val) {

	}
}
