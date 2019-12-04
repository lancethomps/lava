package com.lancethomps.lava.common.file;

import java.util.Map;

@FunctionalInterface
public interface FileParserPostProcessor {

  Map<String, Object> postProcessDataMap(Map<String, Object> dataMap);

}
