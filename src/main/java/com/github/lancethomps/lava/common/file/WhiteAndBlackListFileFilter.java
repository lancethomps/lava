package com.github.lancethomps.lava.common.file;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.AbstractFileFilter;

import com.github.lancethomps.lava.common.Checks;

public class WhiteAndBlackListFileFilter extends AbstractFileFilter implements Serializable {

  private static final long serialVersionUID = 7539720980246540828L;

  private final Collection<String> blackList;

  private final Collection<Pattern> blackListPatterns;

  private final Collection<String> whiteList;

  private final Collection<Pattern> whiteListPatterns;

  private final boolean whiteListPriority;

  public WhiteAndBlackListFileFilter(Collection<Pattern> whiteListPatterns, Collection<Pattern> blackListPatterns) {
    this(null, null, whiteListPatterns, blackListPatterns);
  }

  public WhiteAndBlackListFileFilter(
    Collection<String> whiteList,
    Collection<String> blackList,
    Collection<Pattern> whiteListPatterns,
    Collection<Pattern> blackListPatterns
  ) {
    this(whiteList, blackList, whiteListPatterns, blackListPatterns, false);
  }

  public WhiteAndBlackListFileFilter(
    Collection<String> whiteList, Collection<String> blackList, Collection<Pattern> whiteListPatterns, Collection<Pattern> blackListPatterns,
    boolean whiteListPriority
  ) {
    super();
    this.whiteList = whiteList;
    this.blackList = blackList;
    this.whiteListPatterns = whiteListPatterns;
    this.blackListPatterns = blackListPatterns;
    this.whiteListPriority = whiteListPriority;
  }

  @Override
  public boolean accept(final File file) {
    return Checks
      .passesWhiteAndBlackListCheck(FileUtil.fullPath(file), whiteList, blackList, whiteListPatterns, blackListPatterns, whiteListPriority)
      .getLeft();
  }

}
