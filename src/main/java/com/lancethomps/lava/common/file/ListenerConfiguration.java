package com.lancethomps.lava.common.file;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

public class ListenerConfiguration {

  private boolean includeSubDirs = true;

  private AbstractFileListener listener;

  private String pattern;

  private boolean skipEmptyFiles = true;

  private List<String> skipPatterns;

  private List<String> subDirs;

  public AbstractFileListener getListener() {
    return listener;
  }

  @Required
  public void setListener(AbstractFileListener listener) {
    if (listener == null) {
      throw new NullPointerException("Listener cannot be null");
    }
    this.listener = listener;
  }

  public String getPattern() {
    return pattern;
  }

  public ListenerConfiguration setPattern(String pattern) {
    this.pattern = StringUtils.trimToNull(pattern);
    return this;
  }

  public List<String> getSkipPatterns() {
    return skipPatterns;
  }

  public ListenerConfiguration setSkipPatterns(List<String> skipPatterns) {
    this.skipPatterns = skipPatterns;
    return this;
  }

  public List<String> getSubDirs() {
    return subDirs;
  }

  public ListenerConfiguration setSubDirs(List<String> subDirs) {
    this.subDirs = subDirs;
    return this;
  }

  public boolean isIncludeSubDirs() {
    return includeSubDirs;
  }

  public ListenerConfiguration setIncludeSubDirs(boolean includeSubDirs) {
    this.includeSubDirs = includeSubDirs;
    return this;
  }

  public boolean isSkipEmptyFiles() {
    return skipEmptyFiles;
  }

  public ListenerConfiguration setSkipEmptyFiles(boolean skipEmptyFiles) {
    this.skipEmptyFiles = skipEmptyFiles;
    return this;
  }

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
