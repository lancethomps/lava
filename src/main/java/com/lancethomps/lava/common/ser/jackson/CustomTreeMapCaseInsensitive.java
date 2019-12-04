package com.lancethomps.lava.common.ser.jackson;

import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.lancethomps.lava.common.string.StringUtil;

@SuppressWarnings("rawtypes")
@JsonTypeInfo(use = Id.NONE)
public class CustomTreeMapCaseInsensitive extends TreeMap {

  private static final long serialVersionUID = -1768147912347101106L;

  @SuppressWarnings("unchecked")
  public CustomTreeMapCaseInsensitive() {
    super(StringUtil.CASE_INSENSITIVE_COMP);
  }

}
