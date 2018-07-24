package com.github.lancethomps.lava.common.web.requests.parsers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * The Class QueryStringToSingleValueMapParser.
 */
public class QueryStringToSingleValueMapParser implements RequestParameterParser<Map<String, String>> {

	/** The Constant DELEGATE. */
	private static final QueryStringToMapParser DELEGATE = new QueryStringToMapParser();

	@Override
	public Map<String, String> process(@Nullable RequestFieldInfo<Map<String, String>> info, Map<String, String[]> params, String name) {
		Map<String, List<String>> listVal = DELEGATE.process((RequestFieldInfo) info, params, name);
		return listVal == null ? null : listVal.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().get(0)));
	}

}
