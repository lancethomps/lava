package com.github.lancethomps.lava.common.web.requests.parsers;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * The Interface RequestParameterParser.
 *
 * @param <R> the generic type
 */
public interface RequestParameterParser<R> {

	/**
	 * Process.
	 *
	 * @param info the info
	 * @param params the params
	 * @param name the name
	 * @return the r
	 * @throws RequestParsingException the request parsing exception
	 */
	R process(@Nullable RequestFieldInfo<R> info, Map<String, String[]> params, String name) throws RequestParsingException;

	/**
	 * Process.
	 *
	 * @param info the info
	 * @param params the params
	 * @param name the name
	 * @param prefix the prefix
	 * @return the r
	 * @throws RequestParsingException the request parsing exception
	 */
	default R process(@Nullable RequestFieldInfo<R> info, Map<String, String[]> params, String name, String prefix) throws RequestParsingException {
		return process(info, params, prefix == null ? name : (prefix + name));
	}

}
