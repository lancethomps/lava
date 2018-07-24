package com.github.lancethomps.lava.common.properties;

import java.io.File;
import java.util.Locale;

import org.apache.commons.lang3.tuple.Pair;

import com.github.lancethomps.lava.common.web.WebRequestContext;

/**
 * The Interface PropertyParserHelper.
 */
public interface PropertyParserHelper {

	/** The Constant DEFAULT_BUNDLE. */
	String DEFAULT_BUNDLE = "webtools";

	/**
	 * Gets the default bundle.
	 *
	 * @return the default bundle
	 */
	default String getDefaultBundle() {
		return DEFAULT_BUNDLE;
	}

	/**
	 * Gets the expressions file.
	 *
	 * @param path the path
	 * @return the expressions file
	 */
	default Pair<String, File> getExpressionsFile(String path) {
		return null;
	}

	/**
	 * Gets the markdown html.
	 *
	 * @param path the path
	 * @param locale the locale
	 * @return the markdown html
	 */
	default Pair<String, File> getMarkdown(String path, Locale locale) {
		return null;
	}

	/**
	 * Gets the property value.
	 *
	 * @param context the context
	 * @param locale the locale
	 * @param bundle the bundle
	 * @param defaultValue the default value
	 * @param labelKey the label key
	 * @param override the override
	 * @param fallbackKey the fallback key
	 * @return the property value
	 */
	default Pair<String, String> getPropertyValue(WebRequestContext context, Locale locale, String bundle, String defaultValue, String labelKey, String override, String fallbackKey) {
		return null;
	}
}
