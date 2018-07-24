package com.github.lancethomps.lava.common.diff.domain;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;

/**
 * The Class DiffLine.
 */
public class DiffLine extends ExternalizableBean {

	/** The content. */
	private String content;

	/** The new number. */
	private Integer newNumber;

	/** The old number. */
	private Integer oldNumber;

	/** The type. */
	private DiffLineType type;

	/**
	 * Gets the content.
	 *
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Gets the new number.
	 *
	 * @return the newNumber
	 */
	public Integer getNewNumber() {
		return newNumber;
	}

	/**
	 * Gets the old number.
	 *
	 * @return the oldNumber
	 */
	public Integer getOldNumber() {
		return oldNumber;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public DiffLineType getType() {
		return type;
	}

	/**
	 * Sets the content.
	 *
	 * @param content the content to set
	 * @return the diff line
	 */
	public DiffLine setContent(String content) {
		this.content = content;
		return this;
	}

	/**
	 * Sets the new number.
	 *
	 * @param newNumber the newNumber to set
	 * @return the diff line
	 */
	public DiffLine setNewNumber(Integer newNumber) {
		this.newNumber = newNumber;
		return this;
	}

	/**
	 * Sets the old number.
	 *
	 * @param oldNumber the oldNumber to set
	 * @return the diff line
	 */
	public DiffLine setOldNumber(Integer oldNumber) {
		this.oldNumber = oldNumber;
		return this;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type to set
	 * @return the diff line
	 */
	public DiffLine setType(DiffLineType type) {
		this.type = type;
		return this;
	}

}
