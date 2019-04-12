package com.github.lancethomps.lava.common.properties;

import java.io.File;
import java.util.Locale;

import org.apache.commons.lang3.tuple.Pair;

import com.github.lancethomps.lava.common.web.WebRequestContext;

public interface PropertyParserHelper {

  String DEFAULT_BUNDLE = "webtools";

  default String getDefaultBundle() {
    return DEFAULT_BUNDLE;
  }

  default Pair<String, File> getExpressionsFile(String path) {
    return null;
  }

  default Pair<String, File> getMarkdown(String path, Locale locale) {
    return null;
  }

  default Pair<String, String> getPropertyValue(
    WebRequestContext context,
    Locale locale,
    String bundle,
    String defaultValue,
    String labelKey,
    String override,
    String fallbackKey
  ) {
    return null;
  }

}
