package com.github.lancethomps.lava.common.web.throttle;

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

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.DynamicDataSetter;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.OutputFormat;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.web.DataView;
import com.github.lancethomps.lava.common.web.RequestThrottleContextInitializer;
import com.github.lancethomps.lava.common.web.WebRequestContext;

/**
 * The Class RequestThrottle.
 */
public class RequestThrottle implements HandlerInterceptor {

	/** The Constant OPEN_REQUEST_COUNT. */
	public static final AtomicLong OPEN_REQUEST_COUNT = new AtomicLong(0);

	/** The Constant RATE_LIMIT_HEADER. */
	public static final String RATE_LIMIT_HEADER = "X-RateLimit-Limit";

	/** The Constant RATE_LIMIT_REMAINING_HEADER. */
	public static final String RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";

	/** The Constant REQUEST_COUNT. */
	public static final AtomicLong REQUEST_COUNT = new AtomicLong(0);

	/** The Constant IP_PREFIX. */
	private static final String IP_PREFIX = "ip.";

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(RequestThrottle.class);

	/** The Constant THROTTLED_USER_KEY. */
	private static final String THROTTLED_USER_KEY = "_zzThrottledUserId";

	/** The Constant UNKNOWN. */
	private static final String UNKNOWN = "unknown";

	/** The Constant USER_PREFIX. */
	private static final String USER_PREFIX = "user.";

	/** The config. */
	private RequestThrottleConfig config = new RequestThrottleConfig();

	/** The request initializer. */
	@Autowired(required = false)
	private RequestThrottleContextInitializer requestInitializer;

	/** The throttle. */
	private final Map<String, AtomicInteger> throttle = new ConcurrentHashMap<>();

	/**
	 * Gets the throttle counts.
	 *
	 * @param throttle the throttle
	 * @param includeZeroOpenRequests the include zero open requests
	 * @return the throttle counts
	 */
	public static Map<String, Integer> getThrottleCounts(Map<String, AtomicInteger> throttle, boolean includeZeroOpenRequests) {
		return new ArrayList<>(throttle.entrySet())
			.stream()
			.filter(e -> includeZeroOpenRequests || (e.getValue().get() > 0))
			.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().get(), (a, b) -> a, TreeMap::new));
	}

	/**
	 * After completion.
	 *
	 * @param request the request
	 */
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

	/**
	 * Decrement request count.
	 *
	 * @param user the user
	 * @return the int
	 */
	public int decrementRequestCount(String user) {
		return Optional.ofNullable(throttle.get(user)).map(count -> count.decrementAndGet()).orElse(0);
	}

	/**
	 * @return the config
	 */
	public RequestThrottleConfig getConfig() {
		return config;
	}

	/**
	 * Gets the request count.
	 *
	 * @param user the user
	 * @param addToCount the add to count
	 * @return the request count
	 */
	public int getRequestCount(String user, boolean addToCount) {
		AtomicInteger requestCount = throttle.computeIfAbsent(user, k -> new AtomicInteger(0));
		return addToCount ? requestCount.getAndAdd(1) : requestCount.get();
	}

	/**
	 * Gets the throttle counts.
	 *
	 * @return the throttle counts
	 */
	public Map<String, Integer> getThrottleCounts() {
		return getThrottleCounts(true);
	}

	/**
	 * Gets the throttle counts.
	 *
	 * @param includeZeroOpenRequests the include zero open requests
	 * @return the throttle counts
	 */
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

	/**
	 * @param config the config to set
	 */
	public void setConfig(@Nonnull RequestThrottleConfig config) {
		this.config = config;
	}

	/**
	 * @param requestInitializer the requestInitializer to set
	 */
	public void setRequestInitializer(RequestThrottleContextInitializer requestInitializer) {
		this.requestInitializer = requestInitializer;
	}

	/**
	 * Should decrement request count.
	 *
	 * @param request the request
	 * @return true, if successful
	 */
	public boolean shouldDecrementRequestCount(HttpServletRequest request) {
		return request.getAttribute(THROTTLED_USER_KEY) != null;
	}

	/**
	 * Should throttle.
	 *
	 * @param request the request
	 * @return true, if successful
	 */
	public boolean shouldThrottle(HttpServletRequest request) {
		return shouldThrottle(getRequestThrottleContext(request));
	}

	/**
	 * Should throttle.
	 *
	 * @param context the context
	 * @return true, if successful
	 */
	public boolean shouldThrottle(RequestThrottleContext context) {
		if (Checks.isNotEmpty(config.getWhiteList()) || Checks.isNotEmpty(config.getBlackList())) {
			String uri = ObjectUtils.defaultIfNull(context.getResourceIdentifier(), StringUtils.EMPTY);
			Pair<Boolean, Pattern> skipThrottleResult = Checks.passesWhiteAndBlackListCheck(uri, config.getWhiteList(), config.getBlackList(), true);
			if (skipThrottleResult.getLeft()) {
				Logs.logTrace(LOG, "Skipping throttling for request: uri=%s matchedCheck=%s", uri, Checks.defaultIfNull(skipThrottleResult.getRight(), ""));
				return false;
			}
			if (skipThrottleResult.getRight() != null) {
				Logs.logTrace(LOG, "Throttling due to black list match for request: uri=%s matchedCheck=%s", uri, Checks.defaultIfNull(skipThrottleResult.getRight(), ""));
			}
		}
		return true;
	}

	/**
	 * Throttle.
	 *
	 * @param request the request
	 * @param response the response
	 * @throws RequestThrottleException the request throttle exception
	 */
	public void throttle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response) throws RequestThrottleException {
		String user = throttle(getRequestThrottleContext(request), (key, val) -> response.setHeader(key, val.toString()), response);
		request.setAttribute(THROTTLED_USER_KEY, user);
	}

	/**
	 * Throttle.
	 *
	 * @param context the context
	 * @param data the data
	 * @return the string
	 * @throws RequestThrottleException the request throttle exception
	 */
	public String throttle(@Nonnull RequestThrottleContext context, @Nonnull DynamicDataSetter data) throws RequestThrottleException {
		return throttle(context, data, null);
	}

	/**
	 * Gets the request count.
	 *
	 * @param context the context
	 * @param addToCount the add to count
	 * @return the request count
	 */
	private Pair<String, Integer> getRequestCount(RequestThrottleContext context, boolean addToCount) {
		String user = getUser(context);
		return Pair.of(user, getRequestCount(user, addToCount));
	}

	/**
	 * Gets the request throttle context.
	 *
	 * @param request the request
	 * @return the request throttle context
	 */
	private RequestThrottleContext getRequestThrottleContext(HttpServletRequest request) {
		return requestInitializer == null ? WebRequestContext.getRequestContext(request) : requestInitializer.getRequestContext(request);
	}

	/**
	 * Gets the user.
	 *
	 * @param context the context
	 * @return the user
	 */
	private String getUser(RequestThrottleContext context) {
		return !config.isByUser() ? UNKNOWN : Optional.ofNullable(requestInitializer == null ? null : requestInitializer.getUserId(context)).map(id -> USER_PREFIX + id).orElseGet(() -> {
			return Optional.ofNullable(context == null ? null : context.getSenderIdentifier()).map(ip -> IP_PREFIX + ip).orElse(UNKNOWN);
		});
	}

	/**
	 * Throttle.
	 *
	 * @param context the context
	 * @param data the data
	 * @param response the response
	 * @return the string
	 * @throws RequestThrottleException the request throttle exception
	 */
	private String throttle(@Nonnull RequestThrottleContext context, @Nonnull DynamicDataSetter data, @Nullable HttpServletResponse response) throws RequestThrottleException {
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
