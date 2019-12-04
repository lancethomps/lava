package com.lancethomps.lava.common.diff.domain;

public enum DiffLineType {

  CONTEXT("d2h-cntx"),

  DELETE_CHANGES("d2h-del d2h-change"),

  DELETES("d2h-del"),

  INFO("d2h-info"),

  INSERT_CHANGES("d2h-ins d2h-change"),

  INSERTS("d2h-ins");

  private final String value;

  DiffLineType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
