package com.github.lancethomps.lava.common.date;

import com.github.lancethomps.lava.common.Enums;

public enum RelativeDateOperator {

  MINUS("-"),

  PLUS("+"),

  ROUND("/"),

  SETTING(":");

  static {
    Enums.createStringToTypeMap(RelativeDateOperator.class, null, RelativeDateOperator::getSymbol);
  }

  private final String symbol;

  RelativeDateOperator(String symbol) {
    this.symbol = symbol;
  }

  public String getSymbol() {
    return symbol;
  }

}
