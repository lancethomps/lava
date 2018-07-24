package com.github.lancethomps.lava.common.file;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * The Class ListenerConfiguration.
 */
public class ListenerConfiguration {

	/** The include sub dirs. */
	private boolean includeSubDirs = true;

	/** The listener. */
	private AbstractFileListener listener;

	/** The pattern. */
	private String pattern;

	/** The skip empty files. */
	private boolean skipEmptyFiles = true;

	/** The skip patterns. */
	private List<String> skipPatterns;

	/** The sub dirs. */
	private List<String> subDirs;

	/**
	 * Gets the listener.
	 *
	 * @return the listener
	 */
	public AbstractFileListener getListener() {
		return listener;
	}

	/**
	 * Gets the pattern.
	 *
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Gets the skip patterns.
	 *
	 * @return the skipPatterns
	 */
	public List<String> getSkipPatterns() {
		return skipPatterns;
	}

	/**
	 * Gets the sub dirs.
	 *
	 * @return the subDirs
	 */
	public List<String> getSubDirs() {
		return subDirs;
	}

	/**
	 * Checks if is include sub dirs.
	 *
	 * @return the includeSubDirs
	 */
	public boolean isIncludeSubDirs() {
		return includeSubDirs;
	}

	/**
	 * Checks if is skip empty files.
	 *
	 * @return the skipEmptyFiles
	 */
	public boolean isSkipEmptyFiles() {
		return skipEmptyFiles;
	}

	/**
	 * Sets the include sub dirs.
	 *
	 * @param includeSubDirs the includeSubDirs to set
	 * @return the listener configuration
	 */
	public ListenerConfiguration setIncludeSubDirs(boolean includeSubDirs) {
		this.includeSubDirs = includeSubDirs;
		return this;
	}

	/**
	 * Sets the listener.
	 *
	 * @param listener the new listener
	 */
	@Required
	public void setListener(AbstractFileListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener cannot be null");
		}
		this.listener = listener;
	}

	/**
	 * Sets the pattern.
	 *
	 * @param pattern the new pattern
	 * @return the listener configuration
	 */
	public ListenerConfiguration setPattern(String pattern) {
		this.pattern = StringUtils.trimToNull(pattern);
		return this;
	}

	/**
	 * Sets the skip empty files.
	 *
	 * @param skipEmptyFiles the skipEmptyFiles to set
	 * @return the listener configuration
	 */
	public ListenerConfiguration setSkipEmptyFiles(boolean skipEmptyFiles) {
		this.skipEmptyFiles = skipEmptyFiles;
		return this;
	}

	/**
	 * Sets the skip patterns.
	 *
	 * @param skipPatterns the skipPatterns to set
	 * @return the listener configuration
	 */
	public ListenerConfiguration setSkipPatterns(List<String> skipPatterns) {
		this.skipPatterns = skipPatterns;
		return this;
	}

	/**
	 * Sets the sub dirs.
	 *
	 * @param subDirs the subDirs to set
	 * @return the listener configuration
	 */
	public ListenerConfiguration setSubDirs(List<String> subDirs) {
		this.subDirs = subDirs;
		return this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "ListenerConfiguration{" + listener.getClass().getSimpleName();
		if (pattern != null) {
			result += ", '" + pattern + "'";
		}
		result += "}";
		return result;
	}
}