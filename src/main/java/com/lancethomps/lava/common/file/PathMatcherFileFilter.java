package com.lancethomps.lava.common.file;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.lang3.StringUtils;

public class PathMatcherFileFilter extends AbstractFileFilter {

  private final File baseDir;

  private final Collection<String> blackList;

  private final List<PathMatcher> blackListMatchers;

  private final boolean hasWhiteList;

  private final Collection<String> whiteList;

  private final List<PathMatcher> whiteListMatchers;

  public PathMatcherFileFilter(Collection<String> whiteList, Collection<String> blackList) {
    this(whiteList, blackList, null);
  }

  public PathMatcherFileFilter(Collection<String> whiteList, Collection<String> blackList, File baseDir) {
    super();
    this.whiteList = whiteList;
    this.blackList = blackList;
    this.baseDir = baseDir;

    whiteListMatchers = (whiteList == null) || whiteList.isEmpty() ? null
      : whiteList
      .stream()
      .map(val -> File.separatorChar != '/' ? StringUtils.replace(val, "/", File.separator) : val)
      .map(val -> {
        if (!StringUtils.startsWithAny(val, "glob:", "regex:")) {
          return "glob:" + val;
        }
        return val;
      })
      .map(FileSystems.getDefault()::getPathMatcher)
      .collect(Collectors.toList());
    blackListMatchers = (blackList == null) || blackList.isEmpty() ? null
      : blackList
      .stream()
      .map(val -> File.separatorChar != '/' ? StringUtils.replace(val, "/", File.separator) : val)
      .map(val -> {
        if (!StringUtils.startsWithAny(val, "glob:", "regex:")) {
          return "glob:" + val;
        }
        return val;
      })
      .map(FileSystems.getDefault()::getPathMatcher)
      .collect(Collectors.toList());

    hasWhiteList = (whiteListMatchers != null) && !whiteListMatchers.isEmpty();
  }

  @Override
  public boolean accept(final File file) {
    final Path path = baseDir == null ? file.toPath() : new File(FileUtil.getRelativePath(baseDir, file)).toPath();
    if (blackListMatchers != null) {
      for (PathMatcher matcher : blackListMatchers) {
        if (matcher.matches(path)) {
          return false;
        }
      }
    }
    if (hasWhiteList) {
      for (PathMatcher matcher : whiteListMatchers) {
        if (matcher.matches(path)) {
          return true;
        }
      }
    }
    return !hasWhiteList;
  }

  @Override
  public boolean accept(final File dir, final String name) {
    return accept(new File(dir, name));
  }

}
