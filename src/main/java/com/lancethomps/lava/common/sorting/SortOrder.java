package com.lancethomps.lava.common.sorting;

import com.lancethomps.lava.common.Enums;

public enum SortOrder {

  asc,

  desc;

  static {
    Enums.createStringToTypeMap(SortOrder.class);
  }

  public static SortOrder fromString(String sort, SortOrder defaultValue) {
    return Enums.fromString(SortOrder.class, sort, defaultValue);
  }
}
