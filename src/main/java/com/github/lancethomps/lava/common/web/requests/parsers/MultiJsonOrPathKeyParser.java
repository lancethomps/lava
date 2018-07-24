package com.github.lancethomps.lava.common.web.requests.parsers;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * The Class MultiJsonOrPathKeyParser.
 *
 * @param <T> the generic type
 */
public class MultiJsonOrPathKeyParser<T> implements RequestParameterParser<List<T>> {

	/*
	 * (non-Javadoc)
	 * @see
	 * com.github.lancethomps.lava.common.web.requests.parsers.RequestParameterParser#process(com.github.lancethomps.lava.
	 * common.web.requests.parsers.RequestFieldInfo, java.util.Map, java.lang.String)
	 */
	@Override
	public List<T> process(@Nullable RequestFieldInfo<List<T>> info, Map<String, String[]> params, String name) {
		return RequestFactory.getBeanListParam(params, name, info == null ? null : info.getFieldType().getContentType(), null);
	}

}
