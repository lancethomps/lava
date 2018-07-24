package com.github.lancethomps.lava.common.web.config;

import com.github.lancethomps.lava.common.Enums;

/**
 * The Enum RequestProcessingLocationType.
 */
public enum RequestProcessingLocationType {

	/*
	 * If a tilde and asterisk modifier is used, the location block will be interpreted as a case-insensitive regular expression match.
	 */
	/** The case insensitive regex. */
	CASE_INSENSITIVE_REGEX("~*", true),

	/*
	 * If a tilde modifier is present, this location will be interpreted as a case-sensitive regular expression match.
	 */
	/** The case sensitive regex. */
	CASE_SENSITIVE_REGEX("~", true),

	/*
	 * If an equal sign is used, this block will be considered a match if the request URI exactly matches the location given.
	 */
	/** The exact match. */
	EXACT_MATCH("=", false),

	/*
	 * If no modifiers are present, the location is interpreted as a prefix match. This means that the location given will be matched against the beginning of the request URI to
	 * determine a match.
	 */
	/** The prefix. */
	PREFIX_MATCH("", false),

	/*
	 * If a carat and tilde modifier is present, and if this block is selected as the best non-regular expression match, regular expression matching will not take place.
	 */
	/** The prefix match skip regex. */
	PREFIX_MATCH_SKIP_REGEX("^~", false);

	/** The regex. */
	private final boolean regex;

	/** The value. */
	private final String value;

	/**
	 * Instantiates a new request processing location type.
	 *
	 * @param value the value
	 * @param regex the regex
	 */
	RequestProcessingLocationType(String value, boolean regex) {
		this.value = value;
		this.regex = regex;
	}

	static {
		Enums.createStringToTypeMap(RequestProcessingLocationType.class, RequestProcessingLocationType.PREFIX_MATCH, RequestProcessingLocationType::getValue);
	}

	/**
	 * From string.
	 *
	 * @param val the val
	 * @return the request processing location type
	 */
	// @JsonCreator
	public static RequestProcessingLocationType fromString(String val) {
		return Enums.fromString(RequestProcessingLocationType.class, val);
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the regex
	 */
	public boolean isRegex() {
		return regex;
	}
}
