package com.github.lancethomps.lava.common.web.requests.parsers;

import java.util.Map;

import javax.annotation.Nullable;

public class DefaultRequestParameterParser<R> implements RequestParameterParser<R> {

  @Override
  public R process(@Nullable RequestFieldInfo<R> info, Map<String, String[]> params, String name) {
    throw new IllegalArgumentException();
  }

}
