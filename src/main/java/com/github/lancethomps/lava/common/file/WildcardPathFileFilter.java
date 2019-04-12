package com.github.lancethomps.lava.common.file;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class WildcardPathFileFilter extends WildcardFileFilter {

  private static final long serialVersionUID = -6185684705917032266L;

  private final IOCase caseSensitivity;

  private final String[] wildcards;

  public WildcardPathFileFilter(final List<String> wildcards) {
    this(wildcards, IOCase.SENSITIVE);
  }

  public WildcardPathFileFilter(final List<String> wildcards, final IOCase caseSensitivity) {
    super(wildcards, caseSensitivity);
    if (wildcards == null) {
      throw new IllegalArgumentException("The wildcard list must not be null");
    }
    this.wildcards = wildcards.toArray(new String[wildcards.size()]);
    this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
  }

  public WildcardPathFileFilter(final String wildcard) {
    this(wildcard, IOCase.SENSITIVE);
  }

  public WildcardPathFileFilter(final String wildcard, final IOCase caseSensitivity) {
    super(wildcard, caseSensitivity);
    if (wildcard == null) {
      throw new IllegalArgumentException("The wildcard must not be null");
    }
    wildcards = new String[]{wildcard};
    this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
  }

  public WildcardPathFileFilter(final String[] wildcards) {
    this(wildcards, IOCase.SENSITIVE);
  }

  public WildcardPathFileFilter(final String[] wildcards, final IOCase caseSensitivity) {
    super(wildcards, caseSensitivity);
    if (wildcards == null) {
      throw new IllegalArgumentException("The wildcard array must not be null");
    }
    this.wildcards = new String[wildcards.length];
    System.arraycopy(wildcards, 0, this.wildcards, 0, wildcards.length);
    this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
  }

  @Override
  public boolean accept(final File file) {
    final String path = FileUtil.fullPath(file);
    for (final String wildcard : wildcards) {
      if (FilenameUtils.wildcardMatch(path, wildcard, caseSensitivity)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean accept(final File dir, final String name) {
    return accept(new File(dir, name));
  }

}
