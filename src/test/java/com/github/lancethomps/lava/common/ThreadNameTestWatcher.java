package com.github.lancethomps.lava.common;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * The Class ThreadNameTestWatcher.
 */
public class ThreadNameTestWatcher extends TestWatcher {

	/** The original thread name. */
	private String originalThreadName;

	/*
	 * (non-Javadoc)
	 * @see org.junit.rules.TestWatcher#finished(org.junit.runner.Description)
	 */
	@Override
	protected void finished(Description description) {
		Thread.currentThread().setName(originalThreadName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.junit.rules.TestWatcher#starting(org.junit.runner.Description)
	 */
	@Override
	protected void starting(Description description) {
		originalThreadName = ContextUtil.appendSuffixAndGetOriginalThreadName(description.getDisplayName());
	}
}