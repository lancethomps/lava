package com.lancethomps.lava.common.file;

import java.io.File;
import java.util.function.Predicate;

import org.apache.commons.io.filefilter.AbstractFileFilter;

public class PredicateFileFilter extends AbstractFileFilter {

  private final Predicate<File> include;

  public PredicateFileFilter(Predicate<File> include) {
    this.include = include;
  }

  @Override
  public boolean accept(File file) {
    return include.test(file);
  }

}
