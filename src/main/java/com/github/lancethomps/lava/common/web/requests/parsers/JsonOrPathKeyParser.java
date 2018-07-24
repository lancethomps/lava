package com.github.lancethomps.lava.common.web.requests.parsers;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * The Class JsonOrPathKeyParser.
 *
 * @param <R> the generic type
 */
public class JsonOrPathKeyParser<R> implements RequestParameterParser<R> {

	/*
	 * (non-Javadoc)
	 * @see
	 * com.github.lancethomps.lava.common.web.requests.parsers.RequestParameterParser#process(com.github.lancethomps.lava.
	 * common.web.requests.parsers.RequestFieldInfo, java.util.Map, java.lang.String)
	 */
	@Override
	public R process(@Nullable RequestFieldInfo<R> info, Map<String, String[]> params, String name) throws RequestParsingException {
		return RequestFactory.getJsonOrPathKeyParam(params, name, info == null ? null : info.getType());
	}

}
