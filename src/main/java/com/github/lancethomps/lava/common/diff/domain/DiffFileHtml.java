package com.github.lancethomps.lava.common.diff.domain;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;

/**
 * The Class DiffFileHtml.
 */
public class DiffFileHtml extends ExternalizableBean {

	/** The left. */
	private String left;

	/** The right. */
	private String right;

	/**
	 * @return the left
	 */
	public String getLeft() {
		return left;
	}

	/**
	 * @return the right
	 */
	public String getRight() {
		return right;
	}

	/**
	 * @param left the left to set
	 */
	public void setLeft(String left) {
		this.left = left;
	}

	/**
	 * @param right the right to set
	 */
	public void setRight(String right) {
		this.right = right;
	}
}
