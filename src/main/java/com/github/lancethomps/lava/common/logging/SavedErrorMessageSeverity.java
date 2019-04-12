package com.github.lancethomps.lava.common.logging;

public enum SavedErrorMessageSeverity {

  HIGH(2),

  LOW(0),

  MEDIUM(1);

  private final int level;

  SavedErrorMessageSeverity(int level) {
    this.level = level;
  }

  public int getLevel() {
    return level;
  }
}
