package com.lancethomps.lava.common.web.throttle;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.DynamicDataSetter;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.ser.OutputFormat;
import com.lancethomps.lava.common.ser.Serializer;
import com.lancethomps.lava.common.web.DataView;
import com.lancethomps.lava.common.web.RequestThrottleContextInitializer;
import com.lancethomps.lava.common.web.WebRequestContext;

public class RequestThrottle implements HandlerInterceptor {

  public static final AtomicLong OPEN_REQUEST_COUNT = new AtomicLong(0);

  public static final String RATE_LIMIT_HEADER = "X-RateLimit-Limit";

  public static final String RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";

  public static final AtomicLong REQUEST_COUNT = new AtomicLong(0);

  private static final String IP_PREFIX = "ip.";

  private static final Logger LOG = Logger.getLogger(RequestThrottle.class);

  private static final String THROTTLED_USER_KEY = "_zzThrottledUserId";

  private static final String UNKNOWN = "unknown";

  private static final String USER_PREFIX = "user.";
  private final Map<String, AtomicInteger> throttle = new ConcurrentHashMap<>();
  private RequestThrottleConfig config = new RequestThrottleConfig();
  @Autowired(required = false)
  private RequestThrottleContextInitializer requestInitializer;

  public static Map<String, Integer> getThrottleCounts(Map<String, AtomicInteger> throttle, boolean includeZeroOpenRequests) {
    return new ArrayList<>(throttle.entrySet())
      .stream()
      .filter(e -> includeZeroOpenRequests || (e.getValue().get() > 0))
      .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().get(), (a, b) -> a, TreeMap::new));
  }

  public void afterCompletion(HttpServletRequest request) {
    if (shouldDecrementRequestCount(request)) {
      String throttledUserId = (String) request.getAttribute(THROTTLED_USER_KEY);
      decrementRequestCount(throttledUserId);
    }
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    afterCompletion(request);
  }

  public int decrementRequestCount(String user) {
    return Optional.ofNullable(throttle.get(user)).map(count -> count.decrementAndGet()).orElse(0);
  }

  public RequestThrottleConfig getConfig() {
    return config;
  }

  public void setConfig(@Nonnull RequestThrottleConfig config) {
    this.config = config;
  }

  public int getRequestCount(String user, boolean addToCount) {
    AtomicInteger requestCount = throttle.computeIfAbsent(user, k -> new AtomicInteger(0));
    return addToCount ? requestCount.getAndAdd(1) : requestCount.get();
  }

  public Map<String, Integer> getThrottleCounts() {
    return getThrottleCounts(true);
  }

  public Map<String, Integer> getThrottleCounts(boolean includeZeroOpenRequests) {
    return getThrottleCounts(throttle, includeZeroOpenRequests);
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (shouldThrottle(request)) {
      throttle(request, response);
    }
    return true;
  }

  public void setRequestInitializer(RequestThrottleContextInitializer requestInitializer) {
    this.requestInitializer = requestInitializer;
  }

  public boolean shouldDecrementRequestCount(HttpServletRequest request) {
    return request.getAttribute(THROTTLED_USER_KEY) != null;
  }

  public boolean shouldThrottle(HttpServletRequest request) {
    return shouldThrottle(getRequestThrottleContext(request));
  }

  public boolean shouldThrottle(RequestThrottleContext context) {
    if (Checks.isNotEmpty(config.getWhiteList()) || Checks.isNotEmpty(config.getBlackList())) {
      String uri = ObjectUtils.defaultIfNull(context.getResourceIdentifier(), StringUtils.EMPTY);
      Pair<Boolean, Pattern> skipThrottleResult = Checks.passesWhiteAndBlackListCheck(uri, config.getWhiteList(), config.getBlackList(), true);
      if (skipThrottleResult.getLeft()) {
        Logs.logTrace(LOG, "Skipping throttling for request: uri=%s matchedCheck=%s", uri, Checks.defaultIfNull(skipThrottleResult.getRight(), ""));
        return false;
      }
      if (skipThrottleResult.getRight() != null) {
        Logs.logTrace(
          LOG,
          "Throttling due to black list match for request: uri=%s matchedCheck=%s",
          uri,
          Checks.defaultIfNull(skipThrottleResult.getRight(), "")
        );
      }
    }
    return true;
  }

  public void throttle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response) throws RequestThrottleException {
    String user = throttle(getRequestThrottleContext(request), (key, val) -> response.setHeader(key, val.toString()), response);
    request.setAttribute(THROTTLED_USER_KEY, user);
  }

  public String throttle(@Nonnull RequestThrottleContext context, @Nonnull DynamicDataSetter data) throws RequestThrottleException {
    return throttle(context, data, null);
  }

  private Pair<String, Integer> getRequestCount(RequestThrottleContext context, boolean addToCount) {
    String user = getUser(context);
    return Pair.of(user, getRequestCount(user, addToCount));
  }

  private RequestThrottleContext getRequestThrottleContext(HttpServletRequest request) {
    return requestInitializer == null ? WebRequestContext.getRequestContext(request) : requestInitializer.getRequestContext(request);
  }

  private String getUser(RequestThrottleContext context) {
    return !config.isByUser() ? UNKNOWN :
      Optional.ofNullable(requestInitializer == null ? null : requestInitializer.getUserId(context)).map(id -> USER_PREFIX + id).orElseGet(() -> {
        return Optional.ofNullable(context == null ? null : context.getSenderIdentifier()).map(ip -> IP_PREFIX + ip).orElse(UNKNOWN);
      });
  }

  private String throttle(@Nonnull RequestThrottleContext context, @Nonnull DynamicDataSetter data, @Nullable HttpServletResponse response)
    throws RequestThrottleException {
    Pair<String, Integer> openRequests = getRequestCount(context, true);
    int maxRequests = config.getMaxRequestsByUser().getOrDefault(openRequests.getLeft(), config.getDefaultMaxRequests());
    int remaining = maxRequests - openRequests.getRight().intValue();
    data.setDataPoint(RATE_LIMIT_HEADER, maxRequests);
    data.setDataPoint(RATE_LIMIT_REMAINING_HEADER, remaining);
    if (remaining <= 0) {
      decrementRequestCount(openRequests.getLeft());
      if (response != null) {
        response.setStatus(429);
        response.setContentType(OutputFormat.CONTENT_TYPE_JSON);
      }
      Map<String, Object> errorData = new TreeMap<>();
      errorData.put("message", String.format("Rate limit exceeded for '%s'.", openRequests.getLeft()));
      if (response != null) {
        try {
          new DataView(Serializer.toPrettyJson(errorData)).writeData(null, response);
        } catch (Exception e) {
          Logs.logError(LOG, e, "Could not write request throttle error data to response.");
        }
      }
      throw new RequestThrottleException(openRequests.getLeft(), maxRequests);
    }
    return openRequests.getLeft();
  }

}
