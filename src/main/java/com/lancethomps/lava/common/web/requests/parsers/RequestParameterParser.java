package com.lancethomps.lava.common.web.requests.parsers;

import java.util.Map;

import javax.annotation.Nullable;

public interface RequestParameterParser<R> {

  R process(@Nullable RequestFieldInfo<R> info, Map<String, String[]> params, String name) throws RequestParsingException;

  default R process(@Nullable RequestFieldInfo<R> info, Map<String, String[]> params, String name, String prefix) throws RequestParsingException {
    return process(info, params, prefix == null ? name : (prefix + name));
  }

}
