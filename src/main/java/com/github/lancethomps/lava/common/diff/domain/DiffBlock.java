package com.github.lancethomps.lava.common.diff.domain;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.google.common.collect.Lists;

public class DiffBlock extends ExternalizableBean {

  private AtomicInteger addedLines = new AtomicInteger(0);

  private AtomicInteger deletedLines = new AtomicInteger(0);

  private String header;

  private List<DiffLine> lines = Lists.newArrayList();

  private Integer newStartLine;

  private Integer oldStartLine;

  private Integer oldStartLine2;

  public AtomicInteger getAddedLines() {
    return addedLines;
  }

  public AtomicInteger getDeletedLines() {
    return deletedLines;
  }

  public String getHeader() {
    return header;
  }

  public DiffBlock setHeader(String header) {
    this.header = header;
    return this;
  }

  public List<DiffLine> getLines() {
    return lines;
  }

  public DiffBlock setLines(List<DiffLine> lines) {
    this.lines = lines;
    return this;
  }

  public Integer getNewStartLine() {
    return newStartLine;
  }

  public DiffBlock setNewStartLine(Integer newStartLine) {
    this.newStartLine = newStartLine;
    return this;
  }

  public Integer getOldStartLine() {
    return oldStartLine;
  }

  public DiffBlock setOldStartLine(Integer oldStartLine) {
    this.oldStartLine = oldStartLine;
    return this;
  }

  public Integer getOldStartLine2() {
    return oldStartLine2;
  }

  public DiffBlock setOldStartLine2(Integer oldStartLine2) {
    this.oldStartLine2 = oldStartLine2;
    return this;
  }

}
