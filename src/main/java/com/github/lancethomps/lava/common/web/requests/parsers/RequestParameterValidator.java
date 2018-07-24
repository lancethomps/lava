package com.github.lancethomps.lava.common.web.requests.parsers;

import javax.annotation.Nullable;

/**
 * The Interface RequestParameterValidator.
 *
 * @param <R> the generic type
 */
public interface RequestParameterValidator<R> {

	/**
	 * Validate.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param info the info
	 * @param parameter the parameter
	 * @throws RequestValidationException the request validation exception
	 */
	<T> void validate(T request, @Nullable RequestFieldInfo<R> info, R parameter) throws RequestValidationException;

}
