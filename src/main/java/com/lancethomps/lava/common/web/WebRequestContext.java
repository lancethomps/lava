package com.lancethomps.lava.common.web;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lancethomps.lava.common.CommonConstants;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.ser.ExternalizableBean;
import com.lancethomps.lava.common.time.Stopwatch;
import com.lancethomps.lava.common.time.TimerEnabledBean;
import com.lancethomps.lava.common.web.throttle.RequestThrottleContext;

import eu.bitwalker.useragentutils.UserAgent;

public class WebRequestContext extends ExternalizableBean implements TimerEnabledBean, RequestThrottleContext {

  public static final String REQUEST_CONTEXT_KEY = "_zzRequestContext";

  private static final Logger LOG = Logger.getLogger(WebRequestContext.class);

  private Map<String, Object> attributes;

  private Boolean external;

  private String ip;

  private Locale locale;

  private String originatingRequestUri;

  private StringBuffer originatingRequestUrl;

  private String referrer;

  private String requestId;

  private Map<String, Object> requestParameters = new TreeMap<>();

  private String resourcePath;

  private String statusCode;

  private String tool;

  private List<String> toolKeys;

  private UserAgent userAgent;

  @JsonIgnore
  private Map<String, Stopwatch> watches;

  public WebRequestContext() {
    super();
  }

  public WebRequestContext(HttpServletRequest request) {
    super();
    initializeViaRequest(request);
  }

  public WebRequestContext(Locale locale) {
    super();
    this.locale = locale;
  }

  public WebRequestContext(String tool) {
    super();
    this.tool = tool;
  }

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

  public WebRequestContext addRequestParameter(String key, String val) {
    if ((key != null) && (val != null)) {
      requestParameters.merge(key, val, (oldVal, newVal) -> {
        if (oldVal instanceof String[]) {
          return ArrayUtils.add((String[]) oldVal, val);
        }
        return new String[]{(String) oldVal, val};
      });
    }
    return this;
  }

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

  public Map<String, Object> deriveAdditionalTrackingData() {
    return null;
  }

  public Object getAttribute(String name) {
    return attributes == null ? null : attributes.get(name);
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public WebRequestContext setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
    return this;
  }

  public Boolean getExternal() {
    return external;
  }

  public WebRequestContext setExternal(Boolean external) {
    this.external = external;
    return this;
  }

  public String getIp() {
    return ip;
  }

  public WebRequestContext setIp(String ip) {
    this.ip = ip;
    return this;
  }

  public Locale getLocale() {
    return locale;
  }

  public WebRequestContext setLocale(Locale locale) {
    this.locale = locale;
    return this;
  }

  public String getOriginatingRequestUri() {
    return originatingRequestUri;
  }

  public WebRequestContext setOriginatingRequestUri(String originatingRequestUri) {
    this.originatingRequestUri = originatingRequestUri;
    return this;
  }

  public StringBuffer getOriginatingRequestUrl() {
    return originatingRequestUrl;
  }

  public WebRequestContext setOriginatingRequestUrl(StringBuffer originatingRequestUrl) {
    this.originatingRequestUrl = originatingRequestUrl;
    return this;
  }

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

  public String getReferrer() {
    return referrer;
  }

  public WebRequestContext setReferrer(String referrer) {
    this.referrer = referrer;
    return this;
  }

  public String getRequestId() {
    return requestId;
  }

  public WebRequestContext setRequestId(String requestId) {
    this.requestId = requestId;
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T getRequestParameter(String key) {
    return requestParameters == null ? null : (T) requestParameters.get(key);
  }

  public Map<String, Object> getRequestParameters() {
    return requestParameters;
  }

  public WebRequestContext setRequestParameters(Map<String, Object> requestParameters) {
    this.requestParameters = requestParameters;
    return this;
  }

  public Map<String, String[]> getRequestParamtersAsParameterMap() {
    return requestParameters.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> {
      if (e.getValue() instanceof String[]) {
        return (String[]) e.getValue();
      }
      return new String[]{(String) e.getValue()};
    }));
  }

  @Override
  public String getResourceIdentifier() {
    return originatingRequestUri;
  }

  public String getResourcePath() {
    return resourcePath;
  }

  public void setResourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  @Override
  public String getSenderIdentifier() {
    return ip;
  }

  public String getStatusCode() {
    return statusCode;
  }

  public WebRequestContext setStatusCode(String statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  public String getTool() {
    return tool;
  }

  public WebRequestContext setTool(String tool) {
    this.tool = tool;
    return this;
  }

  public List<String> getToolKeys() {
    return toolKeys;
  }

  public WebRequestContext setToolKeys(List<String> toolKeys) {
    this.toolKeys = toolKeys;
    return this;
  }

  public UserAgent getUserAgent() {
    return userAgent;
  }

  public WebRequestContext setUserAgent(UserAgent userAgent) {
    this.userAgent = userAgent;
    return this;
  }

  @Override
  public Map<String, Stopwatch> getWatches() {
    return watches;
  }

  public WebRequestContext initializeViaRequest(HttpServletRequest request) {
    if (request != null) {
      setOriginatingRequestUri(request.getRequestURI());
      ip = WebRequests.getRequestIp(request);
      locale =
        isNotBlank(request.getParameter(CommonConstants.LOCALE_PARAM)) ? parseLocaleString(request.getParameter(CommonConstants.LOCALE_PARAM)) : null;
      referrer = WebRequests.sanitizeJavaScriptForHtml(request.getHeader("referer"));
      userAgent = UserAgent.parseUserAgentString(WebRequests.sanitizeJavaScriptForHtml(request.getHeader("user-agent")));
      originatingRequestUrl = request.getRequestURL();
      external = originatingRequestUrl != null && WebRequests.isExternalUrl(originatingRequestUrl.toString());
      try {
        addParameterMap(request.getParameterMap());
      } catch (Throwable e) {
        Logs.logError(
          LOG,
          e,
          "Error processing HTTP request parameter map for URI [%s] with encoding [%s]!",
          getOriginatingRequestUri(),
          request.getCharacterEncoding()
        );
      }
    }
    return this;
  }

  public WebRequestContext setAttribute(String name, Object value) {
    if (name != null) {
      if (attributes == null) {
        attributes = new LinkedHashMap<>();
      }
      attributes.put(name, value);
    }
    return this;
  }

  @Override
  public <T extends TimerEnabledBean> T setWatches(Map<String, Stopwatch> watches) {
    this.watches = watches;
    return (T) this;
  }

  public boolean testExternal() {
    return (external == null) || external;
  }

}
