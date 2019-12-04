package com.lancethomps.lava.common.web.requests.parsers;

import java.util.Map;

import javax.annotation.Nullable;

public class JsonSanitizingStringParameterParser implements RequestParameterParser<String> {

  public static String getRequestParam(Map<String, String[]> params, String name) {
    String val = RequestFactory.getRequestParam(params, name);
    if (val != null) {
      val = val.replaceAll("\\(", "%28").replaceAll("\\)", "%29").replaceAll("\\{", "%7B").replaceAll("\\}", "%7D")
        .replaceAll("\\[", "%5B").replaceAll("\\]", "%5D").replaceAll("\\<", "%3C").replaceAll("\\>", "%3E")
        .replaceAll("\\'", "%27").replaceAll("\\:", "%3A");
    }
    return val;
  }

  @Override
  public String process(@Nullable RequestFieldInfo<String> info, Map<String, String[]> params, String name) {
    return getRequestParam(params, name);
  }

}
