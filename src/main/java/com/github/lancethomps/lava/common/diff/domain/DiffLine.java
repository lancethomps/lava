package com.github.lancethomps.lava.common.diff.domain;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;

public class DiffLine extends ExternalizableBean {

  private String content;

  private Integer newNumber;

  private Integer oldNumber;

  private DiffLineType type;

  public String getContent() {
    return content;
  }

  public DiffLine setContent(String content) {
    this.content = content;
    return this;
  }

  public Integer getNewNumber() {
    return newNumber;
  }

  public DiffLine setNewNumber(Integer newNumber) {
    this.newNumber = newNumber;
    return this;
  }

  public Integer getOldNumber() {
    return oldNumber;
  }

  public DiffLine setOldNumber(Integer oldNumber) {
    this.oldNumber = oldNumber;
    return this;
  }

  public DiffLineType getType() {
    return type;
  }

  public DiffLine setType(DiffLineType type) {
    this.type = type;
    return this;
  }

}
