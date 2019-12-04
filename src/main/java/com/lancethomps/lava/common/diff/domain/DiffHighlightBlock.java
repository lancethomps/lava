package com.lancethomps.lava.common.diff.domain;

import com.lancethomps.lava.common.ser.ExternalizableBean;

public class DiffHighlightBlock extends ExternalizableBean {

  private String line;

  private String prefix;

  public DiffHighlightBlock() {
  }

  public DiffHighlightBlock(String prefix, String line) {
    this.prefix = prefix;
    this.line = line;
  }

  public String getLine() {
    return line;
  }

  public void setLine(String line) {
    this.line = line;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

}
