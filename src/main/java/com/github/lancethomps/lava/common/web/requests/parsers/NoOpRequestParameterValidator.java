package com.github.lancethomps.lava.common.web.requests.parsers;

import javax.annotation.Nullable;

/**
 * The Class NoOpRequestParameterValidator.
 */
public class NoOpRequestParameterValidator implements RequestParameterValidator<Object> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.lancethomps.lava.common.web.requests.parsers.RequestParameterValidator#validate(java.lang.Object, com.github.lancethomps.lava.common.web.requests.parsers.RequestFieldInfo,
	 * java.lang.Object)
	 */
	@Override
	public <T> void validate(T request, @Nullable RequestFieldInfo<Object> info, Object parameter) throws RequestValidationException {
		// nothing
	}

}
