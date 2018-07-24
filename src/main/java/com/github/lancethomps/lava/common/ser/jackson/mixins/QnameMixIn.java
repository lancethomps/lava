package com.github.lancethomps.lava.common.ser.jackson.mixins;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The Class QnameMixIn.
 */
public abstract class QnameMixIn {

	/**
	 * Instantiates a new qname mix in.
	 *
	 * @param localPart the local part
	 */
	@JsonCreator
	public QnameMixIn(String localPart) {
	}

}
