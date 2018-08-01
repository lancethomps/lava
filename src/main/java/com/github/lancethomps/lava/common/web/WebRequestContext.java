package com.github.lancethomps.lava.common.web;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.StringUtils.parseLocaleString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.CommonConstants;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.github.lancethomps.lava.common.time.Stopwatch;
import com.github.lancethomps.lava.common.time.TimerEnabledBean;
import com.github.lancethomps.lava.common.web.throttle.RequestThrottleContext;
import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bitwalker.useragentutils.UserAgent;

/**
 * The Class WebRequestContext.
 *
 * @author lancethomps
 */
public class WebRequestContext extends ExternalizableBean implements TimerEnabledBean, RequestThrottleContext {

	/** The request context key. */
	public static final String REQUEST_CONTEXT_KEY = "_zzRequestContext";

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(WebRequestContext.class);

	/** The attributes. */
	private Map<String, Object> attributes;

	/** The external. */
	private Boolean external;

	/** The ip. */
	private String ip;

	/** The locale. */
	private Locale locale;

	/** The originating request uri. */
	private String originatingRequestUri;

	/** The originating request url. */
	private StringBuffer originatingRequestUrl;

	/** The referrer. */
	private String referrer;

	/** The request id. */
	private String requestId;

	/** The request parameters. */
	private Map<String, Object> requestParameters = new TreeMap<>();

	/** The resource path. */
	private String resourcePath;

	/** The status code. */
	private String statusCode;

	/** The tool. */
	private String tool;

	/** The tool keys. */
	private List<String> toolKeys;

	/** The user agent. */
	private UserAgent userAgent;

	/** The watches. */
	@JsonIgnore
	private Map<String, Stopwatch> watches;

	/**
	 * Instantiates a new web request context.
	 */
	public WebRequestContext() {
		super();
	}

	/**
	 * Instantiates a new web request context.
	 *
	 * @param request the request
	 */
	public WebRequestContext(HttpServletRequest request) {
		super();
		initializeViaRequest(request);
	}

	/**
	 * Instantiates a new web request context.
	 *
	 * @param locale the locale
	 */
	public WebRequestContext(Locale locale) {
		super();
		this.locale = locale;
	}

	/**
	 * Instantiates a new web request context.
	 *
	 * @param tool the tool
	 */
	public WebRequestContext(String tool) {
		super();
		this.tool = tool;
	}

	/**
	 * Gets the request context.
	 *
	 * @param request the request
	 * @return the request context
	 */
	public static WebRequestContext getRequestContext(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		WebRequestContext context = (WebRequestContext) request.getAttribute(REQUEST_CONTEXT_KEY);
		if (context == null) {
			context = new WebRequestContext(request);
			request.setAttribute(REQUEST_CONTEXT_KEY, context);
		}
		return context;
	}

	/**
	 * Adds the parameter map.
	 *
	 * @param parameterMap the parameter map
	 * @return the web request context
	 */
	public WebRequestContext addParameterMap(Map<String, String[]> parameterMap) {
		if (parameterMap != null) {
			for (Entry<String, String[]> entry : parameterMap.entrySet()) {
				if (entry.getValue().length == 1) {
					requestParameters.put(entry.getKey(), entry.getValue()[0]);
				} else {
					requestParameters.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return this;
	}

	/**
	 * Adds the request parameter.
	 *
	 * @param key the key
	 * @param val the val
	 * @return the tool context
	 */
	public WebRequestContext addRequestParameter(String key, String val) {
		if ((key != null) && (val != null)) {
			requestParameters.merge(key, val, (oldVal, newVal) -> {
				if (oldVal instanceof String[]) {
					return ArrayUtils.add((String[]) oldVal, val);
				}
				return new String[] { (String) oldVal, val };
			});
		}
		return this;
	}

	/**
	 * Adds the request parameter.
	 *
	 * @param key the key
	 * @param val the val
	 * @return the tool context
	 */
	public WebRequestContext addRequestParameter(String key, String[] val) {
		if ((key != null) && (val != null) && (val.length > 0)) {
			requestParameters.merge(key, val.length == 1 ? val[0] : val, (oldVal, newVal) -> {
				if (oldVal instanceof String[]) {
					return ArrayUtils.addAll((String[]) oldVal, val);
				}
				return ArrayUtils.add(val, 0, (String) oldVal);
			});
		}
		return this;
	}

	/**
	 * Derive additional tracking data. This method should be overwritten by any subclasses to use when
	 * adding request tracking data
	 *
	 * @return the map
	 */
	public Map<String, Object> deriveAdditionalTrackingData() {
		return null;
	}

	/**
	 * Gets the attribute.
	 *
	 * @param name the name
	 * @return the attribute
	 */
	public Object getAttribute(String name) {
		return attributes == null ? null : attributes.get(name);
	}

	/**
	 * @return the attributes
	 */
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	/**
	 * Gets the external.
	 *
	 * @return the external
	 */
	public Boolean getExternal() {
		return external;
	}

	/**
	 * Gets the ip.
	 *
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Gets the locale.
	 *
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Gets the originating request uri.
	 *
	 * @return the originatingRequestUri
	 */
	public String getOriginatingRequestUri() {
		return originatingRequestUri;
	}

	/**
	 * Gets the originating request url.
	 *
	 * @return the originatingRequestUrl
	 */
	public StringBuffer getOriginatingRequestUrl() {
		return originatingRequestUrl;
	}

	/**
	 * Gets the parameter.
	 *
	 * @param key the key
	 * @return the parameter
	 */
	public String getParameter(String key) {
		Object val = requestParameters == null ? null : requestParameters.get(key);
		if (val == null) {
			return null;
		}
		if (val instanceof String[]) {
			return ((String[]) val)[0];
		}
		return (String) val;
	}

	/**
	 * Gets the referrer.
	 *
	 * @return the referrer
	 */
	public String getReferrer() {
		return referrer;
	}

	/**
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * Gets the request parameter.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the request parameter
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRequestParameter(String key) {
		return requestParameters == null ? null : (T) requestParameters.get(key);
	}

	/**
	 * Gets the request parameters.
	 *
	 * @return the requestParameters
	 */
	public Map<String, Object> getRequestParameters() {
		return requestParameters;
	}

	/**
	 * Gets the request paramters as parameter map.
	 *
	 * @return the request paramters as parameter map
	 */
	public Map<String, String[]> getRequestParamtersAsParameterMap() {
		return requestParameters.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> {
			if (e.getValue() instanceof String[]) {
				return (String[]) e.getValue();
			}
			return new String[] { (String) e.getValue() };
		}));
	}

	@Override
	public String getResourceIdentifier() {
		return originatingRequestUri;
	}

	/**
	 * @return the resourcePath
	 */
	public String getResourcePath() {
		return resourcePath;
	}

	@Override
	public String getSenderIdentifier() {
		return ip;
	}

	/**
	 * @return the statusCode
	 */
	public String getStatusCode() {
		return statusCode;
	}

	/**
	 * Gets the tool.
	 *
	 * @return the tool
	 */
	public String getTool() {
		return tool;
	}

	/**
	 * Gets the tool keys.
	 *
	 * @return the toolKeys
	 */
	public List<String> getToolKeys() {
		return toolKeys;
	}

	/**
	 * Gets the user agent.
	 *
	 * @return the userAgent
	 */
	public UserAgent getUserAgent() {
		return userAgent;
	}

	/**
	 * @return the watches
	 */
	@Override
	public Map<String, Stopwatch> getWatches() {
		return watches;
	}

	/**
	 * Initialize via request.
	 *
	 * @param request the request
	 * @return the web request context
	 */
	public WebRequestContext initializeViaRequest(HttpServletRequest request) {
		if (request != null) {
			setOriginatingRequestUri(request.getRequestURI());
			ip = WebRequests.getRequestIp(request);
			locale = isNotBlank(request.getParameter(CommonConstants.LOCALE_PARAM)) ? parseLocaleString(request.getParameter(CommonConstants.LOCALE_PARAM)) : null;
			referrer = WebRequests.sanitizeJavaScriptForHtml(request.getHeader("referer"));
			userAgent = UserAgent.parseUserAgentString(WebRequests.sanitizeJavaScriptForHtml(request.getHeader("user-agent")));
			originatingRequestUrl = request.getRequestURL();
			external = originatingRequestUrl == null ? false : WebRequests.isExternalUrl(originatingRequestUrl.toString());
			try {
				addParameterMap(request.getParameterMap());
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Error processing HTTP request parameter map for URI [%s] with encoding [%s]!", getOriginatingRequestUri(), request.getCharacterEncoding());
			}
		}
		return this;
	}

	/**
	 * Sets the attribute.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the web request context
	 */
	public WebRequestContext setAttribute(String name, Object value) {
		if (name != null) {
			if (attributes == null) {
				attributes = new LinkedHashMap<>();
			}
			attributes.put(name, value);
		}
		return this;
	}

	/**
	 * Sets the attributes.
	 *
	 * @param attributes the attributes to set
	 * @return the web request context
	 */
	public WebRequestContext setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
		return this;
	}

	/**
	 * Sets the external.
	 *
	 * @param external the external to set
	 * @return the web request context
	 */
	public WebRequestContext setExternal(Boolean external) {
		this.external = external;
		return this;
	}

	/**
	 * Sets the ip.
	 *
	 * @param ip the ip
	 * @return the web request context
	 */
	public WebRequestContext setIp(String ip) {
		this.ip = ip;
		return this;
	}

	/**
	 * Sets the locale.
	 *
	 * @param locale the locale to set
	 * @return the web request context
	 */
	public WebRequestContext setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}

	/**
	 * Sets the originating request uri.
	 *
	 * @param originatingRequestUri the originatingRequestUri to set
	 * @return the web request context
	 */
	public WebRequestContext setOriginatingRequestUri(String originatingRequestUri) {
		this.originatingRequestUri = originatingRequestUri;
		return this;
	}

	/**
	 * Sets the originating request url.
	 *
	 * @param originatingRequestUrl the originatingRequestUrl to set
	 * @return the web request context
	 */
	public WebRequestContext setOriginatingRequestUrl(StringBuffer originatingRequestUrl) {
		this.originatingRequestUrl = originatingRequestUrl;
		return this;
	}

	/**
	 * Sets the referrer.
	 *
	 * @param referrer the referrer to set
	 * @return the web request context
	 */
	public WebRequestContext setReferrer(String referrer) {
		this.referrer = referrer;
		return this;
	}

	/**
	 * Sets the request id.
	 *
	 * @param requestId the requestId to set
	 * @return the web request context
	 */
	public WebRequestContext setRequestId(String requestId) {
		this.requestId = requestId;
		return this;
	}

	/**
	 * Sets the request parameters.
	 *
	 * @param requestParameters the requestParameters to set
	 * @return the web request context
	 */
	public WebRequestContext setRequestParameters(Map<String, Object> requestParameters) {
		this.requestParameters = requestParameters;
		return this;
	}

	/**
	 * @param resourcePath the resourcePath to set
	 */
	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	/**
	 * Sets the status code.
	 *
	 * @param statusCode the statusCode to set
	 * @return the web request context
	 */
	public WebRequestContext setStatusCode(String statusCode) {
		this.statusCode = statusCode;
		return this;
	}

	/**
	 * Sets the tool.
	 *
	 * @param tool the tool to set
	 * @return the web request context
	 */
	public WebRequestContext setTool(String tool) {
		this.tool = tool;
		return this;
	}

	/**
	 * Sets the tool keys.
	 *
	 * @param toolKeys the toolKeys to set
	 * @return the web request context
	 */
	public WebRequestContext setToolKeys(List<String> toolKeys) {
		this.toolKeys = toolKeys;
		return this;
	}

	/**
	 * Sets the user agent.
	 *
	 * @param userAgent the userAgent to set
	 * @return the web request context
	 */
	public WebRequestContext setUserAgent(UserAgent userAgent) {
		this.userAgent = userAgent;
		return this;
	}

	/**
	 * Sets the watches.
	 *
	 * @param <T> the generic type
	 * @param watches the watches to set
	 * @return the t
	 */
	@Override
	public <T extends TimerEnabledBean> T setWatches(Map<String, Stopwatch> watches) {
		this.watches = watches;
		return (T) this;
	}

	/**
	 * Checks if is external.
	 *
	 * @return the external
	 */
	public boolean testExternal() {
		return (external == null) || external;
	}

}
