package com.github.lancethomps.lava.common.diff.domain;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;

public class DiffFileHtml extends ExternalizableBean {

  private String left;

  private String right;

  public String getLeft() {
    return left;
  }

  public void setLeft(String left) {
    this.left = left;
  }

  public String getRight() {
    return right;
  }

  public void setRight(String right) {
    this.right = right;
  }

}
