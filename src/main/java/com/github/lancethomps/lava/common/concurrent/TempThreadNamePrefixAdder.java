package com.github.lancethomps.lava.common.concurrent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.ContextUtil;

public class TempThreadNamePrefixAdder implements AutoCloseable {

  private final String originalThreadName;

  private final Thread thread;

  private final String threadNamePrefix;

  private final String threadNamePrefixAddInfo;

  private final String threadNamePrefixSep;

  public TempThreadNamePrefixAdder(@Nonnull String threadNamePrefix) {
    this(threadNamePrefix, Thread.currentThread(), null, null);
  }

  public TempThreadNamePrefixAdder(@Nonnull String threadNamePrefix, @Nullable String threadNamePrefixAddInfo) {
    this(threadNamePrefix, Thread.currentThread(), null, threadNamePrefixAddInfo);
  }

  public TempThreadNamePrefixAdder(
    @Nonnull String threadNamePrefix,
    @Nonnull Thread thread,
    @Nullable String threadNamePrefixSep,
    @Nullable String threadNamePrefixAddInfo
  ) {
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

  @Override
  public void close() {
    resetThreadName();
  }

  public String getOriginalThreadName() {
    return originalThreadName;
  }

  public Thread getThread() {
    return thread;
  }

  public String getThreadNamePrefix() {
    return threadNamePrefix;
  }

  public String getThreadNamePrefixAddInfo() {
    return threadNamePrefixAddInfo;
  }

  public String getThreadNamePrefixSep() {
    return threadNamePrefixSep;
  }

  public void resetThreadName() {
    if (originalThreadName != null) {
      thread.setName(originalThreadName);
    }
  }

}
