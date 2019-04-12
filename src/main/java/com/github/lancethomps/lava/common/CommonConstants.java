package com.github.lancethomps.lava.common;

import org.apache.commons.lang3.builder.StandardToStringStyle;

import com.github.lancethomps.lava.common.string.OmittingToStringStyle;

public class CommonConstants {

  public static final String CLASSPATH_PREFIX = "classpath:";
  public static final StandardToStringStyle DEFAULT_TO_STRING_STYLE = new StandardToStringStyle();
  public static final String LOCALE_PARAM = "locale";
  public static final String LOG_LINE_SEP = "************************************************************************************";
  public static final String REQUEST_ID_KEY = "wtp.REQUEST_ID";
  public static final String REQUEST_START_KEY = "_zzRequestStartTime";
  public static final OmittingToStringStyle TO_STRING_STYLE_WITHOUT_NULL = OmittingToStringStyle.DEFAULT_INSTANCE;
  public static final String UNKNOWN = "Unknown";
  public static final String UNKNOWN_USER = UNKNOWN;

  static {
    try {
      DEFAULT_TO_STRING_STYLE.setUseIdentityHashCode(false);
      DEFAULT_TO_STRING_STYLE.setUseShortClassName(true);
      DEFAULT_TO_STRING_STYLE.setNullText("null");
      DEFAULT_TO_STRING_STYLE.setFieldSeparator(", ");
    } catch (Throwable e) {
      e.printStackTrace();
      throw e;
    }
  }

}
