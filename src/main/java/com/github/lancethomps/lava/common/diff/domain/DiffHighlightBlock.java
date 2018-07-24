package com.github.lancethomps.lava.common.diff.domain;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;

/**
 * The Class DiffHighlightBlock.
 */
public class DiffHighlightBlock extends ExternalizableBean {

	/** The line. */
	private String line;

	/** The prefix. */
	private String prefix;

	/**
	 * Instantiates a new diff highlight block.
	 */
	public DiffHighlightBlock() {
	}

	/**
	 * Instantiates a new diff highlight block.
	 *
	 * @param prefix the prefix
	 * @param line the line
	 */
	public DiffHighlightBlock(String prefix, String line) {
		this.prefix = prefix;
		this.line = line;
	}

	/**
	 * @return the line
	 */
	public String getLine() {
		return line;
	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @param line the line to set
	 */
	public void setLine(String line) {
		this.line = line;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
