package com.github.lancethomps.lava.common.expr;

import com.github.lancethomps.lava.common.Enums;

public enum ExprParser {

  JS("nashorn"),

  OGNL,

  PY("python"),

  SPEL;

  static {
    Enums.createStringToTypeMap(ExprParser.class, null, ExprParser::getEngineName);
  }

  private final String engineName;

  ExprParser() {
    this(null);
  }

  ExprParser(String engineName) {
    this.engineName = engineName;
  }

  public String getEngineName() {
    return engineName;
  }
}
