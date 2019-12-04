package com.lancethomps.lava.common.web.requests.parsers;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class MultiJsonOrPathKeyParser<T> implements RequestParameterParser<List<T>> {

  @Override
  public List<T> process(@Nullable RequestFieldInfo<List<T>> info, Map<String, String[]> params, String name) {
    return RequestFactory.getBeanListParam(params, name, info == null ? null : info.getFieldType().getContentType(), null);
  }

}
