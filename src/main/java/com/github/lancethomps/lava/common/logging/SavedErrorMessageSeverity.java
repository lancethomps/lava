package com.github.lancethomps.lava.common.logging;

/**
 * The Enum Severity.
 *
 * @author lancethomps
 */
public enum SavedErrorMessageSeverity {

	/** The high. */
	HIGH(2),

	/** The low. */
	LOW(0),

	/** The medium. */
	MEDIUM(1);

	/** The level. */
	private final int level;

	/**
	 * Instantiates a new severity.
	 *
	 * @param level the level
	 */
	SavedErrorMessageSeverity(int level) {
		this.level = level;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}
}
