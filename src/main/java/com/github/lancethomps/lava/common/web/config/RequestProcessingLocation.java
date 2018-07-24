package com.github.lancethomps.lava.common.web.config;

import java.util.regex.Pattern;

import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class RequestProcessingLocation.
 */
@SuppressWarnings("serial")
public class RequestProcessingLocation extends SimpleDomainObject {

	/** The config. */
	private RequestProcessingConfig config;

	/** The location. */
	private String location;

	/** The regex. */
	@JsonIgnore
	private Pattern regex;

	/** The type. */
	private RequestProcessingLocationType type = RequestProcessingLocationType.PREFIX_MATCH;

	@Override
	public void afterDeserialization() {
		if (type.isRegex()) {
			switch (type) {
			case CASE_INSENSITIVE_REGEX:
				regex = Pattern.compile(location, Pattern.CASE_INSENSITIVE);
				break;
			case CASE_SENSITIVE_REGEX:
				regex = Pattern.compile(location);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * @return the config
	 */
	public RequestProcessingConfig getConfig() {
		return config;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @return the regex
	 */
	public Pattern getRegex() {
		return regex;
	}

	/**
	 * @return the type
	 */
	public RequestProcessingLocationType getType() {
		return type;
	}

	/**
	 * @param config the config to set
	 */
	public void setConfig(RequestProcessingConfig config) {
		this.config = config;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @param regex the regex to set
	 */
	public void setRegex(Pattern regex) {
		this.regex = regex;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(RequestProcessingLocationType type) {
		this.type = type;
	}
}
