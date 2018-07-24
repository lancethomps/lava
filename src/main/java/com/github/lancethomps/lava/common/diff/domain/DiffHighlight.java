package com.github.lancethomps.lava.common.diff.domain;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;

/**
 * The Class DiffHighlight.
 */
public class DiffHighlight extends ExternalizableBean {

	/** The first. */
	private DiffHighlightBlock first;

	/** The second. */
	private DiffHighlightBlock second;

	/**
	 * Instantiates a new diff highlight.
	 */
	public DiffHighlight() {
	}

	/**
	 * Instantiates a new diff highlight.
	 *
	 * @param first the first
	 * @param second the second
	 */
	public DiffHighlight(DiffHighlightBlock first, DiffHighlightBlock second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * @return the first
	 */
	public DiffHighlightBlock getFirst() {
		return first;
	}

	/**
	 * @return the second
	 */
	public DiffHighlightBlock getSecond() {
		return second;
	}

	/**
	 * @param first the first to set
	 */
	public void setFirst(DiffHighlightBlock first) {
		this.first = first;
	}

	/**
	 * @param second the second to set
	 */
	public void setSecond(DiffHighlightBlock second) {
		this.second = second;
	}
}
