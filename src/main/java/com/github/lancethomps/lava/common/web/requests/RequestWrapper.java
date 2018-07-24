package com.github.lancethomps.lava.common.web.requests;

import static com.github.lancethomps.lava.common.Checks.isNotEmpty;
import static com.github.lancethomps.lava.common.collections.MapUtil.mapFromString;
import static com.github.lancethomps.lava.common.ser.OutputFormat.json;
import static com.github.lancethomps.lava.common.ser.OutputFormat.xml;
import static com.github.lancethomps.lava.common.ser.Serializer.readJsonAsMap;
import static com.github.lancethomps.lava.common.web.requests.parsers.RequestFactory.isParamInJson;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.CompressionUtil;
import com.github.lancethomps.lava.common.Enums;
import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.merge.Merges;
import com.github.lancethomps.lava.common.ser.OutputFormat;
import com.github.lancethomps.lava.common.ser.OutputParams;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.web.WebRequests;
import com.google.common.collect.Lists;

/**
 * A generic wrapper that enables us to override the request URI etc.
 */
public class RequestWrapper extends HttpServletRequestWrapper {

	/** The Constant DEFAULT_OUTPUT_PARAMS_KEY. */
	public static final String DEFAULT_OUTPUT_PARAMS_KEY = "_zzDefaultOutputParams";

	/** The Constant DEFAULT_PORT. */
	private static final int DEFAULT_PORT = 80;

	/** The Constant JSON_PARAM_NAME. */
	private static final String JSON_PARAM_NAME = "_zzJsonData";

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(RequestWrapper.class);

	/** The Constant PARENT_REQUEST_PARAM. */
	private static final String PARENT_REQUEST_PARAM = "_zzParentRequest";

	/** The original parameters. */
	private Map<String, String[]> originalParameters = new HashMap<>();

	/** The parameters. */
	private Map<String, String[]> parameters = new HashMap<>();

	/** The request defaults applied. */
	private LinkedHashSet<String> requestDefaultsApplied;

	/** The uri. */
	private String uri;

	/**
	 * Instantiates a new request wrapper.
	 *
	 * @param request the request
	 */
	public RequestWrapper(HttpServletRequest request) {
		this(request, request.getRequestURI());
	}

	/**
	 * Instantiates a new request wrapper.
	 *
	 * @param request the request
	 * @param uri the uri
	 */
	public RequestWrapper(HttpServletRequest request, String uri) {
		super(request);
		this.uri = uri;

		Map<String, String[]> reqParameterMap = request.getParameterMap();
		if (!reqParameterMap.isEmpty()) {
			parameters.putAll(reqParameterMap);
		}
		String requestContentType = StringUtils.substringBefore(request.getContentType(), ";");
		OutputFormat contentType = Enums.fromString(OutputFormat.class, requestContentType, null);
		if ("text/plain".equalsIgnoreCase(requestContentType) || ((contentType != null) && ((json == contentType) || (xml == contentType)))) {
			try (ServletInputStream is = request.getInputStream()) {
				String requestBody = IOUtils.toString(is, UTF_8);
				Map<String, Object> jsonMap = isParamInJson(requestBody) ? readJsonAsMap(requestBody)
					: xml == contentType ? Serializer.readXmlAsMap(requestBody) : getJsonMapUrlEncoded(requestBody);
				if (isNotEmpty(jsonMap)) {
					for (Entry<String, Object> ent : jsonMap.entrySet()) {
						String key = ent.getKey();
						List<String> valList = parameters.containsKey(key) ? Lists.newArrayList(parameters.get(key)) : Lists.newArrayList();
						Object val = ent.getValue();
						if ((val instanceof String) || (val instanceof Number) || (val instanceof Boolean)) {
							valList.add(val.toString());
						} else {
							valList.add(Serializer.toJson(val));
						}
						parameters.put(key, valList.toArray(new String[0]));
					}
				}
			} catch (EOFException e) {
				Logs.logWarn(LOG, "EOF when reading JSON request body for [%s].", uri);
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Error parsing JSON request body for [%s].", uri);
			}
		}
		checkForZippedRequest();
		originalParameters.putAll(parameters);
		setAttribute(PARENT_REQUEST_PARAM, request);
	}

	/**
	 * Utility method to add a parameter to the request. We use this as a way to add additional datum
	 * cache keys.
	 *
	 * @param request the request
	 * @param name the name
	 * @param value the value
	 */
	public static void addParameter(HttpServletRequest request, String name, String value) {
		if (request instanceof RequestWrapper) {
			((RequestWrapper) request).addParameter(name, value);
		}
	}

	/**
	 * Gets the parent request.
	 *
	 * @param request the request
	 * @return the parent request
	 */
	public static HttpServletRequest getParentRequest(HttpServletRequest request) {
		HttpServletRequest parentRequest = (HttpServletRequest) request.getAttribute(PARENT_REQUEST_PARAM);
		if (parentRequest != null) {
			return parentRequest;
		}
		return request;
	}

	/**
	 * Adds the parameter.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public void addParameter(String name, String value) {
		String[] values = { value };
		parameters.put(name, values);
	}

	/**
	 * Adds the parameters.
	 *
	 * @param parameters the parameters
	 */
	public void addParameters(Map<String, String> parameters) {
		if (parameters != null) {
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				String[] currentValue = this.parameters.get(key);
				String newValues[];

				if (currentValue == null) {
					newValues = new String[] { value };
				} else {
					// Add to existing String array
					int length = currentValue.length;
					newValues = new String[length + 1];
					System.arraycopy(currentValue, 0, newValues, 0, length);
					newValues[length] = value;
				}

				this.parameters.put(key, newValues);
			}
		}
	}

	/**
	 * Adds the request defaults.
	 *
	 * @param defaults the defaults
	 * @param preProcessExpressionsVariables the pre process expressions variables
	 * @param prefix the prefix
	 * @return the request wrapper
	 * @throws RequestDefaultsDisallowedParametersException the request defaults disallowed parameters
	 *           exception
	 */
	public RequestWrapper addRequestDefaults(RequestDefaultsConfig defaults, RequestDefaultsContext preProcessExpressionsVariables, @Nullable String prefix)
		throws RequestDefaultsDisallowedParametersException {
		if (defaults != null) {
			addRequestDefaultsApplied(defaults.getId());
			final boolean hasPrefix = Checks.isNotBlank(prefix);
			Map<String, String[]> params;
			if (hasPrefix) {
				params = new LinkedHashMap<>(parameters.size());
				List<String> paramNames = new ArrayList<>(parameters.keySet());
				for (String paramName : paramNames) {
					if (StringUtils.startsWith(paramName, prefix)) {
						params.put(StringUtils.removeStart(paramName, prefix), parameters.remove(paramName));
					}
				}
			} else {
				params = parameters;
			}
			List<String> disallowedParams = WebRequests.removeInvalidParameters(params, defaults.getParameterWhiteList(), defaults.getParameterBlackList());
			if (Checks.isNotEmpty(disallowedParams) && defaults.testFailOnDisallowedParameters()) {
				throw new RequestDefaultsDisallowedParametersException(disallowedParams);
			}
			if (defaults.getRequestMap() != null) {
				params = Merges.deepMerge(defaults.getRequestMap(), params, defaults.getParameterMergeConfig());
			}
			if (isNotEmpty(defaults.getPreProcessExpressions())) {
				RequestDefaultsContext vars = preProcessExpressionsVariables == null ? new RequestDefaultsContext() : preProcessExpressionsVariables;
				vars.setRequest(this);
				vars.setParameters(params);
				Map<String, String[]> created = RequestDefaultsConfig.convertGenericMapToRequestParameters(
					ExprFactory.evaluateOutputExpressionsWithCustomRoot(defaults.getPreProcessExpressions(), vars)
				);
				if (Checks.isNotEmpty(created)) {
					params = Merges.deepMerge(created, params, Checks.defaultIfNull(defaults.getPreProcessMergeConfig(), defaults.getParameterMergeConfig()));
				}
			}
			if (defaults.getOutputParams() != null) {
				OutputParams currentOutputParams = (OutputParams) getAttribute(RequestWrapper.DEFAULT_OUTPUT_PARAMS_KEY);
				OutputParams outputParams;
				if (currentOutputParams != null) {
					outputParams = Merges.deepMerge(defaults.getOutputParams().copy(), currentOutputParams.copy(), defaults.getOutputParamsMergeConfig());
					if (Checks.isNotEmpty(outputParams.getCreateExpressions())) {
						ExprFactory.compileCreateExpressions(outputParams.getCreateExpressions(), false);
					}
					if (Checks.isNotEmpty(outputParams.getPostProcessExpressions())) {
						ExprFactory.compileCreateExpressions(outputParams.getPostProcessExpressions(), false);
					}
				} else {
					outputParams = defaults.getOutputParams();
				}
				setAttribute(RequestWrapper.DEFAULT_OUTPUT_PARAMS_KEY, outputParams);
			}
			if (hasPrefix && !params.isEmpty()) {
				for (Entry<String, String[]> entry : params.entrySet()) {
					parameters.put(prefix + entry.getKey(), entry.getValue());
				}
			}
		}
		return this;
	}

	/**
	 * Gets the original parameter map.
	 *
	 * @return the original parameter map
	 */
	public Map<String, String[]> getOriginalParameterMap() {
		return originalParameters;
	}

	@Override
	public String getParameter(String name) {
		String[] values = parameters.get(name);
		String value = null;
		if ((values != null) && (values.length > 0)) {
			value = values[0];
		}
		return value;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return parameters;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return new IteratorEnumeration<>(parameters.keySet().iterator());
	}

	/**
	 * @return the parameters
	 */
	public Map<String, String[]> getParameters() {
		return parameters;
	}

	@Override
	public String[] getParameterValues(String name) {
		return parameters.get(name);
	}

	/**
	 * @return the requestDefaultsApplied
	 */
	public LinkedHashSet<String> getRequestDefaultsApplied() {
		return requestDefaultsApplied;
	}

	@Override
	public String getRequestURI() {
		return uri;
	}

	@Override
	public StringBuffer getRequestURL() {
		int port = getServerPort();
		String queryString = getQueryString();

		StringBuffer buffer = new StringBuffer(200);
		buffer.append(super.getScheme()).append("://");
		buffer.append(super.getServerName());

		if (port != DEFAULT_PORT) {
			buffer.append(':').append(port);
		}

		buffer.append(getRequestURI());

		if (queryString != null) {
			buffer.append('?').append(queryString);
		}

		return buffer;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Adds the request defaults applied.
	 *
	 * @param id the id
	 */
	private void addRequestDefaultsApplied(String id) {
		if (id != null) {
			if (requestDefaultsApplied == null) {
				requestDefaultsApplied = new LinkedHashSet<>();
			}
			requestDefaultsApplied.add(id);
		}
	}

	/**
	 * Check for zipped request.
	 */
	private void checkForZippedRequest() {
		if ((parameters != null) && parameters.containsKey("zipped")) {
			String data = CompressionUtil.inflate(parameters.get("zipped")[0]);
			try {
				Map<String, Object> initialMap = Serializer.readJsonAsMap(data);
				Map<String, String[]> requestMap = new HashMap<>();
				for (Entry<String, Object> entry : initialMap.entrySet()) {
					Object val = entry.getValue();
					String[] values = new String[1];
					if ((val instanceof String) || (val instanceof Number)) {
						values[0] = val.toString();
					} else {
						values[0] = Serializer.toJson(val);
					}
					requestMap.put(entry.getKey(), values);
				}
				if (isNotEmpty(requestMap)) {
					parameters = requestMap;
				}
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Could not read inflated data [%s] into request map!", data);
			}
		}
	}

	/**
	 * Gets the json map url encoded.
	 *
	 * @param requestBody the request body
	 * @return the json map url encoded
	 */
	private Map<String, Object> getJsonMapUrlEncoded(String requestBody) {
		if (requestBody.startsWith(JSON_PARAM_NAME)) {
			return readJsonAsMap(removeStart(requestBody, JSON_PARAM_NAME + '='));
		}
		return mapFromString(requestBody, '=', '&').entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> {
			String val = "";
			try {
				val = decode(e.getValue(), "UTF-8");
			} catch (Throwable err) {
				Logs.logError(LOG, err, "Error decoding parameter with key [%s] and value [%s]!", e.getKey(), e.getValue());
			}
			return val;
		}));
	}

}
