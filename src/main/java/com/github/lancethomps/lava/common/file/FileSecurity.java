package com.github.lancethomps.lava.common.file;

public class FileSecurity {

  public static String ensureAllowedPath(String path) {
    if (path.contains("..")) {
      throw new SecurityException("Directory traversal attempt using '..' detected for path [" + path + ']');
    }

    return path;
  }

}
