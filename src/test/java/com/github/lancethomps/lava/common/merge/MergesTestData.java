package com.github.lancethomps.lava.common.merge;

import java.util.List;

public class MergesTestData {

  private String id;
  private List<MergesTestData> childrenVal;
  private Double doubleVal;
  private Integer intVal;

  private List<String> listVal;

  private String strVal;

  public List<MergesTestData> getChildrenVal() {
    return childrenVal;
  }

  public MergesTestData setChildrenVal(List<MergesTestData> childrenVal) {
    this.childrenVal = childrenVal;
    return this;
  }

  public Double getDoubleVal() {
    return doubleVal;
  }

  public MergesTestData setDoubleVal(Double doubleVal) {
    this.doubleVal = doubleVal;
    return this;
  }

  public String getId() {
    return id;
  }

  public MergesTestData setId(String id) {
    this.id = id;
    return this;
  }

  public Integer getIntVal() {
    return intVal;
  }

  public MergesTestData setIntVal(Integer intVal) {
    this.intVal = intVal;
    return this;
  }

  public List<String> getListVal() {
    return listVal;
  }

  public MergesTestData setListVal(List<String> listVal) {
    this.listVal = listVal;
    return this;
  }

  public String getStrVal() {
    return strVal;
  }

  public MergesTestData setStrVal(String strVal) {
    this.strVal = strVal;
    return this;
  }

}
