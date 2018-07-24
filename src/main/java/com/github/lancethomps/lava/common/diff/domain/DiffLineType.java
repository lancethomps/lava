package com.github.lancethomps.lava.common.diff.domain;

/**
 * The Enum DiffLineType.
 */
public enum DiffLineType {

	/** The context. */
	CONTEXT("d2h-cntx"),

	/** The del ch. */
	DELETE_CHANGES("d2h-del d2h-change"),

	/** The del. */
	DELETES("d2h-del"),

	/** The info. */
	INFO("d2h-info"),

	/** The ins ch. */
	INSERT_CHANGES("d2h-ins d2h-change"),

	/** The ins. */
	INSERTS("d2h-ins");

	/** The value. */
	private final String value;

	/**
	 * Instantiates a new diff line type.
	 *
	 * @param value the value
	 */
	DiffLineType(String value) {
		this.value = value;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}
