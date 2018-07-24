package com.github.lancethomps.lava.common.file;

import java.util.Map;

/**
 * The Interface FileParserPostProcessor.
 */
@FunctionalInterface
public interface FileParserPostProcessor {

	/**
	 * Post process data map.
	 *
	 * @param dataMap the data map
	 * @return the map
	 */
	Map<String, Object> postProcessDataMap(Map<String, Object> dataMap);
}
