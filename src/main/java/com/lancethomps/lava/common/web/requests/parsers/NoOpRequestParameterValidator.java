package com.lancethomps.lava.common.web.requests.parsers;

import javax.annotation.Nullable;

public class NoOpRequestParameterValidator implements RequestParameterValidator<Object> {

  @Override
  public <T> void validate(T request, @Nullable RequestFieldInfo<Object> info, Object parameter) throws RequestValidationException {

  }

}
