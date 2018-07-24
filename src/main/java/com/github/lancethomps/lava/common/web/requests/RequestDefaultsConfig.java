package com.github.lancethomps.lava.common.web.requests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.merge.MergeConfig;
import com.github.lancethomps.lava.common.ser.OutputExpression;
import com.github.lancethomps.lava.common.ser.OutputParams;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * The Class RequestDefaultsConfig.
 *
 * @author lathomps
 */
@SuppressWarnings("serial")
public class RequestDefaultsConfig extends SimpleDomainObject {

	/** The applies to uris. */
	private Set<String> appliesToUris;

	/** The depends on. */
	private List<String> dependsOn;

	/** The fail on disallowed parameters. */
	private Boolean failOnDisallowedParameters;

	/** The id. */
	private String id;

	/** The load after. */
	private List<String> loadAfter;

	/** The output params. */
	private OutputParams outputParams;

	/** The output params merge config. */
	private MergeConfig outputParamsMergeConfig;

	/** The parameter black list. */
	private List<Pattern> parameterBlackList;

	/** The config. */
	private MergeConfig parameterMergeConfig;

	/** The parameter white list. */
	private List<Pattern> parameterWhiteList;

	/** The pre process expressions. */
	private List<OutputExpression> preProcessExpressions;

	/** The pre process merge config. */
	private MergeConfig preProcessMergeConfig;

	/** The request map. */
	@JsonIgnore
	private Map<String, String[]> requestMap;

	/** The required params non blank. */
	private Set<String> requiredParamsNonBlank;

	/** The values. */
	private Map<String, Object> values;

	/**
	 * Convert generic map to request parameters.
	 *
	 * @param values the values
	 * @return the map
	 */
	public static Map<String, String[]> convertGenericMapToRequestParameters(Map<String, Object> values) {
		return values == null ? null : values.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> {
			List<String> vals = new ArrayList<>();
			JsonNode val = Serializer.toTree(e.getValue());
			if (val.isArray()) {
				((ArrayNode) val).elements().forEachRemaining(node -> {
					if (node.isValueNode()) {
						vals.add(node.asText());
					} else {
						vals.add(Serializer.toJson(node));
					}
				});
			} else if (val.isObject()) {
				vals.add(Serializer.toJson(val));
			} else {
				vals.add(val.asText());
			}
			return vals.toArray(new String[] {});
		}));
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.ser.PostConstructor#afterDeserialization()
	 */
	@Override
	public void afterDeserialization() {
		if (values != null) {
			requestMap = convertGenericMapToRequestParameters(values);
		}
		if (preProcessExpressions != null) {
			preProcessExpressions = ExprFactory.compileCreateExpressions(preProcessExpressions, false);
		}
		if ((outputParams != null) && (outputParams.getCreateExpressions() != null)) {
			outputParams.setCreateExpressions(ExprFactory.compileCreateExpressions(outputParams.getCreateExpressions(), false));
		}
		if ((outputParams != null) && (outputParams.getPostProcessExpressions() != null)) {
			outputParams.setPostProcessExpressions(ExprFactory.compileCreateExpressions(outputParams.getPostProcessExpressions(), false));
		}
	}

	/**
	 * Gets the applies to uris.
	 *
	 * @return the appliesToUris
	 */
	public Set<String> getAppliesToUris() {
		return appliesToUris;
	}

	/**
	 * @return the dependsOn
	 */
	public List<String> getDependsOn() {
		return dependsOn;
	}

	/**
	 * @return the failOnDisallowedParameters
	 */
	public Boolean getFailOnDisallowedParameters() {
		return failOnDisallowedParameters;
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
	 * @return the loadAfter
	 */
	public List<String> getLoadAfter() {
		return loadAfter;
	}

	/**
	 * Gets the output params.
	 *
	 * @return the outputParams
	 */
	public OutputParams getOutputParams() {
		return outputParams;
	}

	/**
	 * @return the outputParamsMergeConfig
	 */
	public MergeConfig getOutputParamsMergeConfig() {
		return outputParamsMergeConfig;
	}

	/**
	 * Gets the parameter black list.
	 *
	 * @return the parameterBlackList
	 */
	public List<Pattern> getParameterBlackList() {
		return parameterBlackList;
	}

	/**
	 * Gets the config.
	 *
	 * @return the config
	 */
	public MergeConfig getParameterMergeConfig() {
		return parameterMergeConfig;
	}

	/**
	 * Gets the parameter white list.
	 *
	 * @return the parameterWhiteList
	 */
	public List<Pattern> getParameterWhiteList() {
		return parameterWhiteList;
	}

	/**
	 * @return the preProcessExpressions
	 */
	public List<OutputExpression> getPreProcessExpressions() {
		return preProcessExpressions;
	}

	/**
	 * @return the preProcessMergeConfig
	 */
	public MergeConfig getPreProcessMergeConfig() {
		return preProcessMergeConfig;
	}

	/**
	 * Gets the request map.
	 *
	 * @return the requestMap
	 */
	public Map<String, String[]> getRequestMap() {
		return requestMap;
	}

	/**
	 * @return the requiredParamsNonBlank
	 */
	public Set<String> getRequiredParamsNonBlank() {
		return requiredParamsNonBlank;
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public Map<String, Object> getValues() {
		return values;
	}

	/**
	 * Sets the applies to uris.
	 *
	 * @param appliesToUris the appliesToUris to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setAppliesToUris(Set<String> appliesToUris) {
		this.appliesToUris = appliesToUris;
		return this;
	}

	/**
	 * Sets the depends on.
	 *
	 * @param dependsOn the dependsOn to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setDependsOn(List<String> dependsOn) {
		this.dependsOn = dependsOn;
		return this;
	}

	/**
	 * Sets the fail on disallowed parameters.
	 *
	 * @param failOnDisallowedParameters the failOnDisallowedParameters to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setFailOnDisallowedParameters(Boolean failOnDisallowedParameters) {
		this.failOnDisallowedParameters = failOnDisallowedParameters;
		return this;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the id to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setId(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Sets the load after.
	 *
	 * @param loadAfter the loadAfter to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setLoadAfter(List<String> loadAfter) {
		this.loadAfter = loadAfter;
		return this;
	}

	/**
	 * Sets the output params.
	 *
	 * @param outputParams the outputParams to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setOutputParams(OutputParams outputParams) {
		this.outputParams = outputParams;
		return this;
	}

	/**
	 * Sets the output params merge config.
	 *
	 * @param outputParamsMergeConfig the outputParamsMergeConfig to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setOutputParamsMergeConfig(MergeConfig outputParamsMergeConfig) {
		this.outputParamsMergeConfig = outputParamsMergeConfig;
		return this;
	}

	/**
	 * Sets the parameter black list.
	 *
	 * @param parameterBlackList the parameterBlackList to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setParameterBlackList(List<Pattern> parameterBlackList) {
		this.parameterBlackList = parameterBlackList;
		return this;
	}

	/**
	 * Sets the config.
	 *
	 * @param parameterMergeConfig the parameter merge config
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setParameterMergeConfig(MergeConfig parameterMergeConfig) {
		this.parameterMergeConfig = parameterMergeConfig;
		return this;
	}

	/**
	 * Sets the parameter white list.
	 *
	 * @param parameterWhiteList the parameterWhiteList to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setParameterWhiteList(List<Pattern> parameterWhiteList) {
		this.parameterWhiteList = parameterWhiteList;
		return this;
	}

	/**
	 * Sets the pre process expressions.
	 *
	 * @param preProcessExpressions the preProcessExpressions to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setPreProcessExpressions(List<OutputExpression> preProcessExpressions) {
		this.preProcessExpressions = preProcessExpressions;
		return this;
	}

	/**
	 * Sets the pre process merge config.
	 *
	 * @param preProcessMergeConfig the preProcessMergeConfig to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setPreProcessMergeConfig(MergeConfig preProcessMergeConfig) {
		this.preProcessMergeConfig = preProcessMergeConfig;
		return this;
	}

	/**
	 * Sets the request map.
	 *
	 * @param requestMap the requestMap to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setRequestMap(Map<String, String[]> requestMap) {
		this.requestMap = requestMap;
		return this;
	}

	/**
	 * Sets the required params non blank.
	 *
	 * @param requiredParamsNonBlank the requiredParamsNonBlank to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setRequiredParamsNonBlank(Set<String> requiredParamsNonBlank) {
		this.requiredParamsNonBlank = requiredParamsNonBlank;
		return this;
	}

	/**
	 * Sets the values.
	 *
	 * @param values the values to set
	 * @return the request defaults config
	 */
	public RequestDefaultsConfig setValues(Map<String, Object> values) {
		this.values = values;
		return this;
	}

	/**
	 * Test fail on disallowed parameters.
	 *
	 * @return true, if successful
	 */
	public boolean testFailOnDisallowedParameters() {
		return (failOnDisallowedParameters != null) && failOnDisallowedParameters.booleanValue();
	}
}
