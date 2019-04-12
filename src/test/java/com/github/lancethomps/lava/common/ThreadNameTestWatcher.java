package com.github.lancethomps.lava.common;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class ThreadNameTestWatcher extends TestWatcher {

  private String originalThreadName;

  @Override
  protected void finished(Description description) {
    Thread.currentThread().setName(originalThreadName);
  }

  @Override
  protected void starting(Description description) {
    originalThreadName = ContextUtil.getThreadNameAndAppendSuffix(description.getDisplayName());
  }

}
