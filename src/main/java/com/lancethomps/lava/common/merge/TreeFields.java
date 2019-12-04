package com.lancethomps.lava.common.merge;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.lancethomps.lava.common.ser.ExternalizableBean;

public class TreeFields extends ExternalizableBean {

  private Map<String, BigDecimal> aggVals = new HashMap<>();

  private Map<String, Comparable<?>> maxVals = new HashMap<>();

  private Map<String, Comparable<?>> minVals = new HashMap<>();

  public Map<String, BigDecimal> getAggVals() {
    return aggVals;
  }

  public void setAggVals(Map<String, BigDecimal> aggVals) {
    this.aggVals = aggVals;
  }

  public Map<String, Comparable<?>> getMaxVals() {
    return maxVals;
  }

  public void setMaxVals(Map<String, Comparable<?>> maxVals) {
    this.maxVals = maxVals;
  }

  public Map<String, Comparable<?>> getMinVals() {
    return minVals;
  }

  public void setMinVals(Map<String, Comparable<?>> minVals) {
    this.minVals = minVals;
  }

}
