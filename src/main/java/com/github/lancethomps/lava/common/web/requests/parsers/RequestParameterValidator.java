package com.github.lancethomps.lava.common.web.requests.parsers;

import javax.annotation.Nullable;

public interface RequestParameterValidator<R> {

  <T> void validate(T request, @Nullable RequestFieldInfo<R> info, R parameter) throws RequestValidationException;

}
