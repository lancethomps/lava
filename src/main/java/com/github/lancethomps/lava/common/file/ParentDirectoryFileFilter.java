package com.github.lancethomps.lava.common.file;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.io.filefilter.AbstractFileFilter;

public class ParentDirectoryFileFilter extends AbstractFileFilter implements Serializable {

  private static final long serialVersionUID = -2169482506424144174L;

  private File parentDir;

  public ParentDirectoryFileFilter(File parentDir) {
    this.parentDir = parentDir;
  }

  @Override
  public boolean accept(File file) {
    return FileUtil.fullPath(parentDir).equalsIgnoreCase(FileUtil.fullPath(file.getParentFile()));
  }

}
