package com.github.lancethomps.lava.common.diff.domain;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.google.common.collect.Lists;

/**
 * The Class DiffBlock.
 */
public class DiffBlock extends ExternalizableBean {

	/** The added lines. */
	private AtomicInteger addedLines = new AtomicInteger(0);

	/** The deleted lines. */
	private AtomicInteger deletedLines = new AtomicInteger(0);

	/** The header. */
	private String header;

	/** The lines. */
	private List<DiffLine> lines = Lists.newArrayList();

	/** The new start line. */
	private Integer newStartLine;

	/** The old start line. */
	private Integer oldStartLine;

	/** The old start line2. */
	private Integer oldStartLine2;

	/**
	 * @return the addedLines
	 */
	public AtomicInteger getAddedLines() {
		return addedLines;
	}

	/**
	 * @return the deletedLines
	 */
	public AtomicInteger getDeletedLines() {
		return deletedLines;
	}

	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * Gets the lines.
	 *
	 * @return the lines
	 */
	public List<DiffLine> getLines() {
		return lines;
	}

	/**
	 * Gets the new start line.
	 *
	 * @return the newStartLine
	 */
	public Integer getNewStartLine() {
		return newStartLine;
	}

	/**
	 * Gets the old start line.
	 *
	 * @return the oldStartLine
	 */
	public Integer getOldStartLine() {
		return oldStartLine;
	}

	/**
	 * Gets the old start line2.
	 *
	 * @return the oldStartLine2
	 */
	public Integer getOldStartLine2() {
		return oldStartLine2;
	}

	/**
	 * Sets the header.
	 *
	 * @param header the header to set
	 * @return the diff block
	 */
	public DiffBlock setHeader(String header) {
		this.header = header;
		return this;
	}

	/**
	 * Sets the lines.
	 *
	 * @param lines the lines to set
	 * @return the diff block
	 */
	public DiffBlock setLines(List<DiffLine> lines) {
		this.lines = lines;
		return this;
	}

	/**
	 * Sets the new start line.
	 *
	 * @param newStartLine the newStartLine to set
	 * @return the diff block
	 */
	public DiffBlock setNewStartLine(Integer newStartLine) {
		this.newStartLine = newStartLine;
		return this;
	}

	/**
	 * Sets the old start line.
	 *
	 * @param oldStartLine the oldStartLine to set
	 * @return the diff block
	 */
	public DiffBlock setOldStartLine(Integer oldStartLine) {
		this.oldStartLine = oldStartLine;
		return this;
	}

	/**
	 * Sets the old start line2.
	 *
	 * @param oldStartLine2 the oldStartLine2 to set
	 * @return the diff block
	 */
	public DiffBlock setOldStartLine2(Integer oldStartLine2) {
		this.oldStartLine2 = oldStartLine2;
		return this;
	}

}
