package com.lancethomps.lava.common.file;

import com.lancethomps.lava.common.ser.ExternalizableBean;

public class Content extends ExternalizableBean {

  private static final long serialVersionUID = 3275494732036980499L;

  private long lastModified;

  private String path;

  private String relativePath;

  public long getLastModified() {
    return lastModified;
  }

  public void setLastModified(long lastModified) {
    this.lastModified = lastModified;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getRelativePath() {
    return relativePath;
  }

  public void setRelativePath(String relativePath) {
    this.relativePath = relativePath;
  }

}
