package com.github.lancethomps.lava.common.web.config;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class RequestProcessingConfig.
 */
@SuppressWarnings("serial")
public class RequestProcessingConfig extends SimpleDomainObject {

	/** The add headers. */
	private List<ResponseHeaderConfig> addHeaders;

	/** The clear headers. */
	private List<ResponseHeaderConfig> clearHeaders;

	/** The context rules. */
	private List<RequestProcessingRule> contextRules;

	/** The cookie rules. */
	private Map<String, List<RequestProcessingRule>> cookieRules;

	/** The fail status message. */
	private String failStatusMessage;

	/** The fail with. */
	private Integer failWith;

	/** The header rules. */
	private Map<String, List<RequestProcessingRule>> headerRules;

	/** The id. */
	private String id;

	/** The parent info. */
	@JsonIgnore
	private String info;

	/** The locations. */
	private List<RequestProcessingLocation> locations;

	/** The parameter rules. */
	private Map<String, List<RequestProcessingRule>> parameterRules;

	/** The set headers. */
	private List<ResponseHeaderConfig> setHeaders;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.github.lancethomps.lava.common.ser.PostConstructor#afterDeserialization()
	 */
	@Override
	public void afterDeserialization() {
	}

	/**
	 * Derive info recursively.
	 *
	 * @return the request processing config
	 */
	public RequestProcessingConfig deriveInfoRecursively() {
		return deriveInfo(null, id);
	}

	/**
	 * Gets the adds the headers.
	 *
	 * @return the addHeaders
	 */
	public List<ResponseHeaderConfig> getAddHeaders() {
		return addHeaders;
	}

	/**
	 * Gets the clear headers.
	 *
	 * @return the clearHeaders
	 */
	public List<ResponseHeaderConfig> getClearHeaders() {
		return clearHeaders;
	}

	/**
	 * Gets the context rules.
	 *
	 * @return the contextRules
	 */
	public List<RequestProcessingRule> getContextRules() {
		return contextRules;
	}

	/**
	 * Gets the cookie rules.
	 *
	 * @return the cookieRules
	 */
	public Map<String, List<RequestProcessingRule>> getCookieRules() {
		return cookieRules;
	}

	/**
	 * @return the failStatusMessage
	 */
	public String getFailStatusMessage() {
		return failStatusMessage;
	}

	/**
	 * Gets the fail with.
	 *
	 * @return the failWith
	 */
	public Integer getFailWith() {
		return failWith;
	}

	/**
	 * Gets the header rules.
	 *
	 * @return the headerRules
	 */
	public Map<String, List<RequestProcessingRule>> getHeaderRules() {
		return headerRules;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the info.
	 *
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * Gets the locations.
	 *
	 * @return the locations
	 */
	public List<RequestProcessingLocation> getLocations() {
		return locations;
	}

	/**
	 * Gets the parameter rules.
	 *
	 * @return the parameterRules
	 */
	public Map<String, List<RequestProcessingRule>> getParameterRules() {
		return parameterRules;
	}

	/**
	 * Gets the sets the headers.
	 *
	 * @return the setHeaders
	 */
	public List<ResponseHeaderConfig> getSetHeaders() {
		return setHeaders;
	}

	/**
	 * Sets the adds the headers.
	 *
	 * @param addHeaders the addHeaders to set
	 * @return the request processing config
	 */
	public RequestProcessingConfig setAddHeaders(List<ResponseHeaderConfig> addHeaders) {
		this.addHeaders = addHeaders;
		return this;
	}

	/**
	 * Sets the clear headers.
	 *
	 * @param clearHeaders the clearHeaders to set
	 * @return the request processing config
	 */
	public RequestProcessingConfig setClearHeaders(List<ResponseHeaderConfig> clearHeaders) {
		this.clearHeaders = clearHeaders;
		return this;
	}

	/**
	 * Sets the context rules.
	 *
	 * @param contextRules the contextRules to set
	 * @return the request processing config
	 */
	public RequestProcessingConfig setContextRules(List<RequestProcessingRule> contextRules) {
		this.contextRules = contextRules;
		return this;
	}

	/**
	 * Sets the cookie rules.
	 *
	 * @param cookieRules the cookieRules to set
	 * @return the request processing config
	 */
	public RequestProcessingConfig setCookieRules(Map<String, List<RequestProcessingRule>> cookieRules) {
		this.cookieRules = cookieRules;
		return this;
	}

	/**
	 * Sets the fail status message.
	 *
	 * @param failStatusMessage the failStatusMessage to set
	 * @return the request processing config
	 */
	public RequestProcessingConfig setFailStatusMessage(String failStatusMessage) {
		this.failStatusMessage = failStatusMessage;
		return this;
	}

	/**
	 * Sets the fail with.
	 *
	 * @param failWith the failWith to set
	 * @return the request processing config
	 */
	public RequestProcessingConfig setFailWith(Integer failWith) {
		this.failWith = failWith;
		return this;
	}

	/**
	 * Sets the header rules.
	 *
	 * @param headerRules the headerRules to set
	 * @return the request processing config
	 */
	public RequestProcessingConfig setHeaderRules(Map<String, List<RequestProcessingRule>> headerRules) {
		this.headerRules = headerRules;
		return this;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the id to set
	 * @return the request processing config
	 */
	public RequestProcessingConfig setId(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Sets the info.
	 *
	 * @param info the info
	 * @return the request processing config
	 */
	public RequestProcessingConfig setInfo(String info) {
		this.info = info;
		return this;
	}

	/**
	 * Sets the locations.
	 *
	 * @param locations the locations to set
	 * @return the request processing config
	 */
	public RequestProcessingConfig setLocations(List<RequestProcessingLocation> locations) {
		this.locations = locations;
		return this;
	}

	/**
	 * Sets the parameter rules.
	 *
	 * @param parameterRules the parameterRules to set
	 * @return the request processing config
	 */
	public RequestProcessingConfig setParameterRules(Map<String, List<RequestProcessingRule>> parameterRules) {
		this.parameterRules = parameterRules;
		return this;
	}

	/**
	 * Sets the sets the headers.
	 *
	 * @param setHeaders the setHeaders to set
	 * @return the request processing config
	 */
	public RequestProcessingConfig setSetHeaders(List<ResponseHeaderConfig> setHeaders) {
		this.setHeaders = setHeaders;
		return this;
	}

	/**
	 * Derive info.
	 *
	 * @param parent the parent
	 * @param info the info
	 * @return the request processing config
	 */
	protected RequestProcessingConfig deriveInfo(RequestProcessingConfig parent, String info) {
		this.info = ((parent == null) || Checks.isBlank(parent.getInfo()) ? "" : (parent.getInfo() + '.')) + info;
		if (contextRules != null) {
			for (int index = 0; index < contextRules.size(); index++) {
				RequestProcessingRule rule = contextRules.get(index);
				if (rule.getConfig() != null) {
					rule.getConfig().deriveInfo(this, "contextRules[" + index + ']');
				}
			}
		}
		addRulesMapInfo("cookieRules", cookieRules);
		addRulesMapInfo("headerRules", headerRules);
		addRulesMapInfo("parameterRules", parameterRules);
		if (locations != null) {
			for (int index = 0; index < locations.size(); index++) {
				RequestProcessingLocation location = locations.get(index);
				if (location.getConfig() != null) {
					String locationInfo = location.getLocation() == null ? ("locations[" + index + ']') : ("locations['" + location.getLocation() + "']");
					location.getConfig().deriveInfo(this, locationInfo);
				}
			}
		}
		return this;
	}

	/**
	 * Adds the rules map info.
	 *
	 * @param type the type
	 * @param rules the rules
	 */
	private void addRulesMapInfo(String type, Map<String, List<RequestProcessingRule>> rules) {
		if (rules != null) {
			for (Entry<String, List<RequestProcessingRule>> entry : rules.entrySet()) {
				for (int index = 0; index < entry.getValue().size(); index++) {
					RequestProcessingRule rule = entry.getValue().get(index);
					if (rule.getConfig() != null) {
						rule.getConfig().deriveInfo(this, type + '.' + entry.getKey() + '[' + index + ']');
					}
				}
			}
		}
	}
}
