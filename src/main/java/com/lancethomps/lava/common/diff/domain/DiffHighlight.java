package com.lancethomps.lava.common.diff.domain;

import com.lancethomps.lava.common.ser.ExternalizableBean;

public class DiffHighlight extends ExternalizableBean {

  private DiffHighlightBlock first;

  private DiffHighlightBlock second;

  public DiffHighlight() {
  }

  public DiffHighlight(DiffHighlightBlock first, DiffHighlightBlock second) {
    this.first = first;
    this.second = second;
  }

  public DiffHighlightBlock getFirst() {
    return first;
  }

  public void setFirst(DiffHighlightBlock first) {
    this.first = first;
  }

  public DiffHighlightBlock getSecond() {
    return second;
  }

  public void setSecond(DiffHighlightBlock second) {
    this.second = second;
  }

}
