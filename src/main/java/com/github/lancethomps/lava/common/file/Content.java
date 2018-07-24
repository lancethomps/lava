package com.github.lancethomps.lava.common.file;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;

/**
 * The Interface DcrContent.
 */
public class Content extends ExternalizableBean {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3275494732036980499L;

	/** The last modified. */
	private long lastModified;

	/** The dcr path. */
	private String path;

	/** The relative path. */
	private String relativePath;

	/**
	 * Gets the last modified.
	 *
	 * @return the last modified
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * Gets the dcr path.
	 *
	 * @return the dcr path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the relativePath
	 */
	public String getRelativePath() {
		return relativePath;
	}

	/**
	 * Sets the last modified.
	 *
	 * @param lastModified the new last modified
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Sets the dcr path.
	 *
	 * @param path the new dcr path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @param relativePath the relativePath to set
	 */
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

}
