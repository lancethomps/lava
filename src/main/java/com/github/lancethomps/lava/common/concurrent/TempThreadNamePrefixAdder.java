package com.github.lancethomps.lava.common.concurrent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.ContextUtil;

/**
 * The Class TempThreadNameSuffixAdder.
 */
public class TempThreadNamePrefixAdder implements AutoCloseable {

	/** The original thread name. */
	private final String originalThreadName;

	/** The thread. */
	private final Thread thread;

	/** The thread name suffix. */
	private final String threadNamePrefix;

	/** The thread name prefix add info. */
	private final String threadNamePrefixAddInfo;

	/** The thread name suffix sep. */
	private final String threadNamePrefixSep;

	/**
	 * Instantiates a new thread name suffix adder.
	 *
	 * @param threadNamePrefix the thread name prefix
	 */
	public TempThreadNamePrefixAdder(@Nonnull String threadNamePrefix) {
		this(threadNamePrefix, Thread.currentThread(), null, null);
	}

	/**
	 * Instantiates a new temp thread name prefix adder.
	 *
	 * @param threadNamePrefix the thread name prefix
	 * @param threadNamePrefixAddInfo the thread name prefix add info
	 */
	public TempThreadNamePrefixAdder(@Nonnull String threadNamePrefix, @Nullable String threadNamePrefixAddInfo) {
		this(threadNamePrefix, Thread.currentThread(), null, threadNamePrefixAddInfo);
	}

	/**
	 * Instantiates a new thread name suffix adder.
	 *
	 * @param threadNamePrefix the thread name prefix
	 * @param thread the thread
	 * @param threadNamePrefixSep the thread name prefix sep
	 * @param threadNamePrefixAddInfo the thread name prefix add info
	 */
	public TempThreadNamePrefixAdder(@Nonnull String threadNamePrefix, @Nonnull Thread thread, @Nullable String threadNamePrefixSep, @Nullable String threadNamePrefixAddInfo) {
		super();
		this.threadNamePrefix = threadNamePrefix;
		this.thread = thread;
		this.threadNamePrefixSep = Checks.defaultIfNull(threadNamePrefixSep, "#");
		this.threadNamePrefixAddInfo = threadNamePrefixAddInfo;
		if (this.thread.getName().equals(this.threadNamePrefix) || this.thread.getName().startsWith(this.threadNamePrefix + this.threadNamePrefixSep)) {
			if (this.threadNamePrefixAddInfo == null) {
				originalThreadName = null;
			} else {
				originalThreadName = this.thread.getName();
				this.thread.setName(originalThreadName + this.threadNamePrefixAddInfo);
			}
		} else {
			originalThreadName = ContextUtil.getThreadNameAndAddPrefix(
				this.threadNamePrefix + StringUtils.defaultString(this.threadNamePrefixAddInfo) + this.threadNamePrefixSep,
				this.thread
			);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		resetThreadName();
	}

	/**
	 * Gets the original thread name.
	 *
	 * @return the originalThreadName
	 */
	public String getOriginalThreadName() {
		return originalThreadName;
	}

	/**
	 * Gets the thread.
	 *
	 * @return the thread
	 */
	public Thread getThread() {
		return thread;
	}

	/**
	 * Gets the thread name suffix.
	 *
	 * @return the threadNameSuffix
	 */
	public String getThreadNamePrefix() {
		return threadNamePrefix;
	}

	/**
	 * Gets the thread name prefix add info.
	 *
	 * @return the threadNamePrefixAddInfo
	 */
	public String getThreadNamePrefixAddInfo() {
		return threadNamePrefixAddInfo;
	}

	/**
	 * Gets the thread name suffix sep.
	 *
	 * @return the threadNameSuffixSep
	 */
	public String getThreadNamePrefixSep() {
		return threadNamePrefixSep;
	}

	/**
	 * Reset thread name.
	 */
	public void resetThreadName() {
		if (originalThreadName != null) {
			thread.setName(originalThreadName);
		}
	}

}
