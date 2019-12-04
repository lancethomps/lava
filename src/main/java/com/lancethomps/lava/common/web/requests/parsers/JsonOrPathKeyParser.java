package com.lancethomps.lava.common.web.requests.parsers;

import java.util.Map;

import javax.annotation.Nullable;

public class JsonOrPathKeyParser<R> implements RequestParameterParser<R> {

  @Override
  public R process(@Nullable RequestFieldInfo<R> info, Map<String, String[]> params, String name) throws RequestParsingException {
    return RequestFactory.getJsonOrPathKeyParam(params, name, info == null ? null : info.getType());
  }

}
