package com.lancethomps.lava.common.string;

import org.apache.commons.lang3.StringUtils;

public class WordUtil {

  public static String getSingularVersionOfWord(String word) {
    if (word.endsWith("s")) {
      return word.endsWith("ies") ? StringUtils.removeEnd(word, "ies") + 'y' : StringUtils.removeEnd(word, "s");
    }
    return null;
  }

  public static String upperCaseIfLower(String word) {
    if (StringUtils.isNotBlank(word) && Character.isLowerCase(word.charAt(0))) {
      return StringUtils.upperCase(word);
    }
    return word;
  }

}
