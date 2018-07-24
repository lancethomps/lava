package com.github.lancethomps.lava.common.web.requests.parsers;

import static com.github.lancethomps.lava.common.Checks.isNotEmpty;
import static com.github.lancethomps.lava.common.Collect.DEFAULT_KEYVAL_SEP;
import static com.github.lancethomps.lava.common.Collect.DEFAULT_PARAM_SEP;
import static com.github.lancethomps.lava.common.Collect.splitCsvAsList;
import static com.github.lancethomps.lava.common.Collect.splitNumberCsvAsList;
import static com.github.lancethomps.lava.common.lambda.ThrowingFunction.identity;
import static java.lang.Double.parseDouble;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.math.NumberUtils.isNumber;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.Collect;
import com.github.lancethomps.lava.common.CompressionUtil;
import com.github.lancethomps.lava.common.Enums;
import com.github.lancethomps.lava.common.Reflections;
import com.github.lancethomps.lava.common.collections.FastHashMap;
import com.github.lancethomps.lava.common.collections.MapUtil;
import com.github.lancethomps.lava.common.date.Dates;
import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.expr.ExprParser;
import com.github.lancethomps.lava.common.lambda.Lambdas;
import com.github.lancethomps.lava.common.lambda.ThrowingBiFunction;
import com.github.lancethomps.lava.common.lambda.ThrowingFunction;
import com.github.lancethomps.lava.common.lambda.ThrowingTriFunction;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.merge.MergeConfig;
import com.github.lancethomps.lava.common.merge.Merges;
import com.github.lancethomps.lava.common.ser.OutputExpression;
import com.github.lancethomps.lava.common.ser.OutputFormat;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.ser.jackson.types.CustomTypeIdResolver;
import com.github.lancethomps.lava.common.spring.SpringUtil;
import com.github.lancethomps.lava.common.string.WordUtil;
import com.github.lancethomps.lava.common.time.Stopwatch;
import com.github.lancethomps.lava.common.web.requests.RequestWrapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A factory for creating Request objects.
 */
@SuppressWarnings("unchecked")
public class RequestFactory {

	/** The Constant DEFAULT_REQUEST_KEY. */
	public static final String DEFAULT_REQUEST_KEY = "default";

	/** The Constant REQUEST_AS_JSON_PARAM. */
	public static final String REQUEST_AS_JSON_PARAM = "toolsRequestJson";

	/** The Constant REQUEST_KEYS_COMMON_PREFIX. */
	public static final String REQUEST_KEYS_COMMON_PREFIX = "common.";

	/** The Constant REQUEST_KEYS_PARAM. */
	public static final String REQUEST_KEYS_PARAM = "requestKeys";

	/** The Constant REQUESTS_AS_JSON_PARAM. */
	public static final String REQUESTS_AS_JSON_PARAM = "requests";

	/** The Constant ALLOW_JSON_PARAM_TYPES. */
	private static final Set<Class<?>> ALLOW_JSON_PARAM_TYPES = Sets.newConcurrentHashSet();

	/** The Constant DEFAULT_POST_PROCESS_METHODS. */
	private static final FastHashMap<Class<?>, ThrowingBiFunction<Map<String, String[]>, ?, ?>> DEFAULT_POST_PROCESS_METHODS = new FastHashMap<>(true);

	/** The Constant INFO_CACHE. */
	private static final Map<String, RequestParserInfo> INFO_CACHE = new HashMap<>();

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(RequestFactory.class);

	/** The Constant POST_PROCESS_METHODS. */
	private static final FastHashMap<Class<?>, ThrowingBiFunction<Map<String, String[]>, ?, ?>> POST_PROCESS_METHODS = new FastHashMap<>(true);

	/** The Constant PROCESS_METHODS. */
	private static final FastHashMap<String, ThrowingBiFunction<Map<String, String[]>, String, ?>> PROCESS_METHODS = new FastHashMap<>(false);

	/** The request defaults config applier. */
	private static ThrowingBiFunction<RequestWrapper, String, Map<String, String[]>> requestDefaultsConfigApplier;

	/** The use new request parser. */
	private static boolean useNewRequestParser;

	static {

		try {
			// Simple single object types
			PROCESS_METHODS.put("BigDecimal", RequestFactory::getBigDecimalParam);
			PROCESS_METHODS.put("Boolean", RequestFactory::getBooleanParam);
			PROCESS_METHODS.put("boolean", RequestFactory::getBooleanParam);
			PROCESS_METHODS.put("Class<?>", RequestFactory::getTypeParam);
			PROCESS_METHODS.put("Double", RequestFactory::getDoubleParam);
			PROCESS_METHODS.put("double", RequestFactory::getDoubleParam);
			PROCESS_METHODS.put("int", RequestFactory::getIntParam);
			PROCESS_METHODS.put("Integer", RequestFactory::getIntParam);
			PROCESS_METHODS.put("LocalDate", RequestFactory::getLocalDateParam);
			PROCESS_METHODS.put("LocalDateTime", RequestFactory::getLocalDateTimeParam);
			PROCESS_METHODS.put("Locale", RequestFactory::getLocaleParam);
			PROCESS_METHODS.put("Long", RequestFactory::getLongParam);
			PROCESS_METHODS.put("Object", RequestFactory::getObjectParam);
			PROCESS_METHODS.put("String", RequestFactory::getRequestParam);

			// List/Map types
			PROCESS_METHODS.put("List<BigDecimal>", RequestFactory::getBigDecimalListParam);
			PROCESS_METHODS.put("List<Double>", RequestFactory::getDoubleListParam);
			PROCESS_METHODS.put("List<Integer>", RequestFactory::getIntegerListParam);
			PROCESS_METHODS.put("List<LocalDate>", RequestFactory::getLocalDateListParam);
			PROCESS_METHODS.put("List<LocalDateTime>", RequestFactory::getLocalDateTimeListParam);
			PROCESS_METHODS.put("List<Map<String, Object>>", RequestFactory::getMapListParam);
			PROCESS_METHODS.put("List<OutputExpression>", RequestFactory::getOutputExpressions);
			PROCESS_METHODS.put("List<Pattern>", RequestFactory::getPatternListParam);
			PROCESS_METHODS.put("List<String>", RequestFactory::getStringListParam);
			PROCESS_METHODS.put("Map<String, List<String>>", RequestFactory::getStringListMapParam);
			PROCESS_METHODS.put("Map<String, Object>", RequestFactory::getMapParam);
			PROCESS_METHODS.put("Map<String, String>", RequestFactory::getStringMapParam);
			PROCESS_METHODS.put("Pair<String, String>", RequestFactory::getPairParam);
			PROCESS_METHODS.put("Set<Integer>", RequestFactory::getIntegerSetParam);
			PROCESS_METHODS.put("Set<LocalDate>", RequestFactory::getLocalDateSetParam);
			PROCESS_METHODS.put("Set<LocalDateTime>", RequestFactory::getLocalDateTimeSetParam);
			PROCESS_METHODS.put("Set<Pattern>", RequestFactory::getPatternSetParam);
			PROCESS_METHODS.put("Set<String>", RequestFactory::getStringSetParam);

			// Complex types
			PROCESS_METHODS.put("MergeConfig", RequestFactory::getMergeConfig);
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Error setting default process methods!");
		} finally {
			PROCESS_METHODS.setFast(true);
		}
	}

	/**
	 * Adds the allow json param types.
	 *
	 * @param types the types
	 * @return true, if successful
	 */
	public static boolean addAllowJsonParamTypes(@Nonnull Class<?>... types) {
		assert Checks.isNotEmpty(types);
		return ALLOW_JSON_PARAM_TYPES.addAll(Arrays.asList(types));
	}

	/**
	 * Adds the default post process methods.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param method the method
	 * @return the throwing bi function
	 */
	public static <T> ThrowingBiFunction<Map<String, String[]>, T, T> addDefaultPostProcessMethods(
		@Nonnull Class<T> type,
		@Nonnull ThrowingBiFunction<Map<String, String[]>, T, T> method
	) {
		assert ((type != null) && (method != null));
		return (ThrowingBiFunction<Map<String, String[]>, T, T>) DEFAULT_POST_PROCESS_METHODS.put(type, method);
	}

	/**
	 * Adds the post process method.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param method the method
	 * @return the throwing bi consumer
	 */
	public static <T> ThrowingBiFunction<Map<String, String[]>, T, T> addPostProcessMethod(@Nonnull Class<T> type, @Nonnull ThrowingBiFunction<Map<String, String[]>, T, T> method) {
		assert ((type != null) && (method != null));
		try {
			return (ThrowingBiFunction<Map<String, String[]>, T, T>) POST_PROCESS_METHODS.put(type, method);
		} finally {
			synchronized (INFO_CACHE) {
				INFO_CACHE.clear();
			}
		}
	}

	/**
	 * Adds the process method.
	 *
	 * @param fieldDisplay the field display
	 * @param method the method
	 * @return the throwing bi function
	 */
	public static ThrowingBiFunction<Map<String, String[]>, String, ?> addProcessMethod(
		@Nonnull String fieldDisplay,
		@Nonnull ThrowingBiFunction<Map<String, String[]>, String, ?> method
	) {
		assert ((fieldDisplay != null) && (method != null));
		try {
			return PROCESS_METHODS.put(fieldDisplay, method);
		} finally {
			synchronized (INFO_CACHE) {
				INFO_CACHE.clear();
			}
		}
	}

	/**
	 * Clear info cache.
	 */
	public static void clearInfoCache() {
		synchronized (INFO_CACHE) {
			INFO_CACHE.clear();
		}
	}

	/**
	 * Creates a new Request object.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @param includeSuperClass the include super class
	 * @param requireAnnotation the require annotation
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T createBeanFromRequest(Class<T> clazz, Map<String, String[]> httpReq, boolean includeSuperClass, boolean requireAnnotation) throws Exception {
		return createBeanFromRequest(clazz, httpReq, null, includeSuperClass, requireAnnotation);
	}

	/**
	 * Creates a new Request object.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @param prefix the prefix
	 * @param includeSuperClass the include super class
	 * @param requireAnnotation the require annotation
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T createBeanFromRequest(Class<T> clazz, Map<String, String[]> httpReq, String prefix, boolean includeSuperClass, boolean requireAnnotation) throws Exception {
		return createBeanFromRequest(null, clazz, httpReq, prefix, includeSuperClass, requireAnnotation);
	}

	/**
	 * Creates a new Request object.
	 *
	 * @param <T> the generic type
	 * @param current the current
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @param includeSuperClass the include super class
	 * @param requireAnnotation the require annotation
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T createBeanFromRequest(T current, Class<T> clazz, Map<String, String[]> httpReq, boolean includeSuperClass, boolean requireAnnotation) throws Exception {
		return createBeanFromRequest(current, clazz, httpReq, includeSuperClass, requireAnnotation, null);
	}

	/**
	 * Creates a new Request object.
	 *
	 * @param <T> the generic type
	 * @param current the current
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @param includeSuperClass the include super class
	 * @param requireAnnotation the require annotation
	 * @param jsonParamName the json param name
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T createBeanFromRequest(T current, Class<T> clazz, Map<String, String[]> httpReq, boolean includeSuperClass, boolean requireAnnotation, String jsonParamName)
		throws Exception {
		return createBeanFromRequest(current, clazz, httpReq, null, includeSuperClass, requireAnnotation, jsonParamName);
	}

	/**
	 * Creates a new Request object.
	 *
	 * @param <T> the generic type
	 * @param current the current
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @param includeSuperClass the include super class
	 * @param requireAnnotation the require annotation
	 * @param jsonParamName the json param name
	 * @param prefix the prefix
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T createBeanFromRequest(
		T current,
		Class<T> clazz,
		Map<String, String[]> httpReq,
		boolean includeSuperClass,
		boolean requireAnnotation,
		String jsonParamName,
		String prefix
	) throws Exception {
		return createBeanFromRequest(current, clazz, httpReq, includeSuperClass, requireAnnotation, jsonParamName, prefix, null);
	}

	/**
	 * Creates a new Request object.
	 *
	 * @param <T> the generic type
	 * @param current the current
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @param includeSuperClass the include super class
	 * @param requireAnnotation the require annotation
	 * @param jsonParamName the json param name
	 * @param prefix the prefix
	 * @param currentField the current field
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T createBeanFromRequest(
		T current,
		Class<T> clazz,
		Map<String, String[]> httpReq,
		boolean includeSuperClass,
		boolean requireAnnotation,
		String jsonParamName,
		String prefix,
		RequestFieldInfo<?> currentField
	) throws Exception {
		Stopwatch watch = LOG.isDebugEnabled() ? new Stopwatch(true) : null;
		try {
			Map<String, String[]> req = getPossiblyZippedRequestMap(httpReq);
			//			String fullJsonParamName = (jsonParamName == null) || (prefix == null) ? jsonParamName : (prefix + jsonParamName);
			String fullJsonParamName = jsonParamName;
			String defaultJsonParam = prefix == null ? REQUEST_AS_JSON_PARAM : (prefix + REQUEST_AS_JSON_PARAM);
			if ((fullJsonParamName != null) && isNotEmpty(req.get(fullJsonParamName))) {
				T val = null;
				for (String jsonVal : req.get(fullJsonParamName)) {
					T subVal = Serializer.fromJson(jsonVal, clazz);
					if (subVal != null) {
						if (val == null) {
							val = subVal;
						} else {
							val = Merges.deepMerge(subVal, val);
						}
					}
				}
				return val;
			} else if (isNotEmpty(req.get(defaultJsonParam))) {
				if (!ALLOW_JSON_PARAM_TYPES.isEmpty() && Reflections.isInstanceofType(ALLOW_JSON_PARAM_TYPES, clazz)) {
					T val = null;
					for (String jsonVal : req.get(defaultJsonParam)) {
						T subVal = Serializer.fromJson(jsonVal, clazz);
						if (subVal != null) {
							if (val == null) {
								val = subVal;
							} else {
								val = Merges.deepMerge(subVal, val);
							}
						}
					}
					return val;
				}
				Logs.logWarn(LOG, "Trying to deserialize instance of bean from JSON request param, but this type is not enabled: type=%s", clazz);
			}
			T request = current == null ? ClassUtil.createInstance(clazz, false) : current;
			RequestParserInfo parserInfo = getRequestParserInfo(clazz, requireAnnotation);
			if (useNewRequestParser) {
				Map<String, RequestFieldInfo<?>> infos = parserInfo.getInfosMap();
				req.keySet().stream().map(infos::get).filter(Objects::nonNull).filter(info -> includeSuperClass || !info.isFromSuperClass()).forEach(info -> info.process(request, req, prefix));
				parserInfo.getComplexRequestInfos().forEach(info -> info.process(request, req, prefix));
			} else {
				List<RequestFieldInfo<?>> infos = parserInfo.getInfos();
				for (RequestFieldInfo<?> info : infos) {
					if (currentField == info) {
						continue;
					}
					if (includeSuperClass || !info.isFromSuperClass()) {
						info.process(request, req, prefix);
					}
				}
			}
			if (!DEFAULT_POST_PROCESS_METHODS.isEmpty()) {
				for (Entry<Class<?>, ThrowingBiFunction<Map<String, String[]>, ?, ?>> e : DEFAULT_POST_PROCESS_METHODS
					.entrySet()
					.stream()
					.filter(e -> e.getKey().isAssignableFrom(request.getClass()))
					.collect(Collectors.toList())) {
					ThrowingBiFunction<Map<String, String[]>, T, T> method = (ThrowingBiFunction<Map<String, String[]>, T, T>) e.getValue();
					method.apply(req, request);
				}
			}
			return Lambdas.functionIfNonNull(parserInfo.getRequestBeanInfo(), requestBeanInfo -> requestBeanInfo.postProcess(httpReq, request)).orElse(request);
		} finally {
			Lambdas.consumeIfNonNull(watch, w -> Logs.logTimer(LOG, watch, String.format("Request Parsing [%s]", clazz)));
		}
	}

	/**
	 * Creates a new Request object.
	 *
	 * @param <T> the generic type
	 * @param current the current
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @param prefix the prefix
	 * @param includeSuperClass the include super class
	 * @param requireAnnotation the require annotation
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T createBeanFromRequest(T current, Class<T> clazz, Map<String, String[]> httpReq, String prefix, boolean includeSuperClass, boolean requireAnnotation)
		throws Exception {
		return createBeanFromRequest(current, clazz, httpReq, prefix, includeSuperClass, requireAnnotation, null);
	}

	/**
	 * Creates a new Request object.
	 *
	 * @param <T> the generic type
	 * @param current the current
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @param prefix the prefix
	 * @param includeSuperClass the include super class
	 * @param requireAnnotation the require annotation
	 * @param jsonParamName the json param name
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T createBeanFromRequest(
		T current,
		Class<T> clazz,
		Map<String, String[]> httpReq,
		String prefix,
		boolean includeSuperClass,
		boolean requireAnnotation,
		String jsonParamName
	) throws Exception {
		return createBeanFromRequest(current, clazz, httpReq, includeSuperClass, requireAnnotation, jsonParamName, prefix);
	}

	/**
	 * Creates a new Request object.
	 *
	 * @param <T> the generic type
	 * @param current the current
	 * @param clazz the clazz
	 * @param httpRequest the http request
	 * @param includeSuperClass the include super class
	 * @param requireAnnotation the require annotation
	 * @return the list
	 * @throws Exception the exception
	 */
	public static <T> List<T> createBeansFromRequest(T current, Class<T> clazz, HttpServletRequest httpRequest, boolean includeSuperClass, boolean requireAnnotation) throws Exception {
		return createBeansFromRequest(current, clazz, httpRequest, null, includeSuperClass, requireAnnotation);
	}

	/**
	 * Creates a new Request object.
	 *
	 * @param <T> the generic type
	 * @param current the current
	 * @param clazz the clazz
	 * @param httpRequest the http request
	 * @param prefix the prefix
	 * @param includeSuperClass the include super class
	 * @param requireAnnotation the require annotation
	 * @return the list
	 * @throws Exception the exception
	 */
	public static <T> List<T> createBeansFromRequest(T current, Class<T> clazz, HttpServletRequest httpRequest, String prefix, boolean includeSuperClass, boolean requireAnnotation)
		throws Exception {
		Stopwatch watch = LOG.isDebugEnabled() ? Stopwatch.createAndStart() : null;
		try {
			Map<String, String[]> httpReq = httpRequest.getParameterMap();
			Map<String, String[]> req = getPossiblyZippedRequestMap(httpReq);
			if (req.containsKey(REQUESTS_AS_JSON_PARAM) && Reflections.isInstanceofType(ALLOW_JSON_PARAM_TYPES, clazz)) {
				List<T> requests = null;
				for (String jsonVal : req.get(REQUESTS_AS_JSON_PARAM)) {
					List<T> reqs = Serializer.readJsonAsList(jsonVal, clazz);
					if (reqs != null) {
						if (requests == null) {
							requests = reqs;
						} else {
							requests.addAll(reqs);
						}
					}
				}
				return requests;
			}
			Set<String> requestKeys = getStringSetParam(req, REQUEST_KEYS_PARAM);
			if (Checks.isNotEmpty(requestKeys)) {
				RequestWrapper wrapper = Checks.defaultIfNull(SpringUtil.getRequestWrapper(httpRequest), () -> new RequestWrapper(httpRequest));
				List<T> requests = new ArrayList<>();
				for (String key : requestKeys) {
					String fullPrefix = prefix == null ? (key + '.') : (prefix + key + '.');
					Map<String, String[]> reqWithDefaults = Checks.defaultIfNull(requestDefaultsConfigApplier == null ? null : requestDefaultsConfigApplier.apply(wrapper, fullPrefix), req);
					T request = createBeanFromRequest(clazz, reqWithDefaults, fullPrefix, true, true);
					if (request instanceof MultiRequestWithKeyHandling) {
						((MultiRequestWithKeyHandling) request).setRequestKey(key);
					}
					requests.add(request);
				}
				return requests;
			}
			T request = createBeanFromRequest(current, clazz, req, includeSuperClass, requireAnnotation);
			if (request != null) {
				List<T> requests = new ArrayList<>();
				requests.add(request);
				return requests;
			}
			return null;
		} finally {
			Lambdas.consumeIfNonNull(watch, w -> Logs.logTimer(LOG, watch, String.format("request_parsing_multiple-%s", clazz)));
		}
	}

	/**
	 * Creates a new Request object.
	 *
	 * @param <T> the generic type
	 * @param entries the entries
	 * @param type the type
	 * @param paramPrefix the param prefix
	 * @return the t
	 */
	public static <T> T createFromPathKeyParams(Set<Entry<String, String[]>> entries, Class<T> type, String paramPrefix) {
		return createFromPathKeyParams(entries, Serializer.constructType(type), paramPrefix);
	}

	/**
	 * Creates a new Request object.
	 *
	 * @param <T> the generic type
	 * @param entries the entries
	 * @param type the type
	 * @param paramPrefix the param prefix
	 * @return the t
	 */
	public static <T> T createFromPathKeyParams(Set<Entry<String, String[]>> entries, JavaType type, String paramPrefix) {
		Map<String, Object> pathKeyMap = new HashMap<>();
		entries.stream().filter(entry -> Checks.isNotEmpty(entry.getValue())).filter(entry -> (paramPrefix == null) || entry.getKey().startsWith(paramPrefix)).forEach(entry -> {
			Serializer.addPathKeyToMap(
				pathKeyMap,
				paramPrefix == null ? entry.getKey() : StringUtils.removeStart(entry.getKey(), paramPrefix),
				entry.getValue().length > 1 ? entry.getValue() : entry.getValue()[0]
			);
		});
		if (!pathKeyMap.isEmpty()) {
			return Serializer.fromMap(pathKeyMap, type);
		}
		return null;
	}

	/**
	 * From http parameters.
	 *
	 * @param request the request
	 * @return the map
	 */
	public static Map<String, Object> fromHttpParameters(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
			map.put(entry.getKey(), entry.getValue()[0]);
		}
		return map;
	}

	/**
	 * Gets the annotated request.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param httpRequest the http request
	 * @return the annotated request
	 * @throws Exception the exception
	 */
	public static <T> T getAnnotatedRequest(Class<T> clazz, HttpServletRequest httpRequest) throws Exception {
		return getAnnotatedRequest(clazz, httpRequest, null);
	}

	/**
	 * Gets the annotated request.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param httpRequest the http request
	 * @param prefix the prefix
	 * @return the annotated request
	 * @throws Exception the exception
	 */
	public static <T> T getAnnotatedRequest(Class<T> clazz, HttpServletRequest httpRequest, String prefix) throws Exception {
		return getAnnotatedRequest(clazz, httpRequest, prefix, null);
	}

	/**
	 * Gets the annotated request.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param httpRequest the http request
	 * @param prefix the prefix
	 * @param jsonParamName the json param name
	 * @return the annotated request
	 * @throws Exception the exception
	 */
	public static <T> T getAnnotatedRequest(Class<T> clazz, HttpServletRequest httpRequest, String prefix, String jsonParamName) throws Exception {
		return getAnnotatedRequest(clazz, httpRequest.getParameterMap(), prefix, jsonParamName);
	}

	/**
	 * Derive through annotations.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T getAnnotatedRequest(Class<T> clazz, Map<String, String[]> httpReq) throws Exception {
		return getAnnotatedRequest(clazz, httpReq, null);
	}

	/**
	 * Gets the annotated request.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @param includeSuperClass the include super class
	 * @return the annotated request
	 * @throws Exception the exception
	 */
	public static <T> T getAnnotatedRequest(Class<T> clazz, Map<String, String[]> httpReq, boolean includeSuperClass) throws Exception {
		return getAnnotatedRequest(clazz, httpReq, null, null, includeSuperClass);
	}

	/**
	 * Gets the annotated request.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @param prefix the prefix
	 * @return the annotated request
	 * @throws Exception the exception
	 */
	public static <T> T getAnnotatedRequest(Class<T> clazz, Map<String, String[]> httpReq, String prefix) throws Exception {
		return getAnnotatedRequest(clazz, httpReq, prefix, null);
	}

	/**
	 * Gets the annotated request.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @param prefix the prefix
	 * @param jsonParamName the json param name
	 * @return the annotated request
	 * @throws Exception the exception
	 */
	public static <T> T getAnnotatedRequest(Class<T> clazz, Map<String, String[]> httpReq, String prefix, String jsonParamName) throws Exception {
		return getAnnotatedRequest(clazz, httpReq, prefix, jsonParamName, true);
	}

	/**
	 * Gets the annotated request.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @param prefix the prefix
	 * @param jsonParamName the json param name
	 * @param includeSuperClass the include super class
	 * @return the annotated request
	 * @throws Exception the exception
	 */
	public static <T> T getAnnotatedRequest(Class<T> clazz, Map<String, String[]> httpReq, String prefix, String jsonParamName, boolean includeSuperClass) throws Exception {
		return createBeanFromRequest(null, clazz, httpReq, prefix, includeSuperClass, true, jsonParamName);
	}

	/**
	 * Gets the annotated request.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param clazz the clazz
	 * @param httpRequest the http request
	 * @return the annotated request
	 * @throws Exception the exception
	 */
	public static <T> T getAnnotatedRequest(T request, Class<T> clazz, HttpServletRequest httpRequest) throws Exception {
		return createBeanFromRequest(request, clazz, httpRequest.getParameterMap(), true, true);
	}

	/**
	 * Gets the annotated request.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param clazz the clazz
	 * @param httpReq the http req
	 * @return the annotated request
	 * @throws Exception the exception
	 */
	public static <T> T getAnnotatedRequest(T request, Class<T> clazz, Map<String, String[]> httpReq) throws Exception {
		return createBeanFromRequest(request, clazz, httpReq, true, true);
	}

	/**
	 * Gets the annotated requests.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param httpRequest the http request
	 * @return the annotated requests
	 * @throws Exception the exception
	 */
	public static <T> List<T> getAnnotatedRequests(Class<T> clazz, HttpServletRequest httpRequest) throws Exception {
		return createBeansFromRequest(null, clazz, httpRequest, true, true);
	}

	/**
	 * Gets the annotated request without super class.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param httpRequest the http request
	 * @return the annotated request without super class
	 * @throws Exception the exception
	 */
	public static <T> T getAnnotatedRequestWithoutSuperClass(Class<T> clazz, HttpServletRequest httpRequest) throws Exception {
		return getAnnotatedRequest(clazz, httpRequest.getParameterMap(), false);
	}

	/**
	 * Gets the bean list param.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @param nonJsonCreator the non json creator
	 * @return the bean list param
	 */
	public static <T> List<T> getBeanListParam(
		Map<String, String[]> request,
		String paramName,
		Class<T> type,
		Function<String, T> nonJsonCreator
	) {
		return getBeanListParam(request, paramName, Serializer.constructType(type), nonJsonCreator);
	}

	/**
	 * Gets the bean list param.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @param nonJsonCreator the non json creator
	 * @return the bean list param
	 */
	public static <T> List<T> getBeanListParam(
		Map<String, String[]> request,
		String paramName,
		JavaType type,
		Function<String, T> nonJsonCreator
	) {
		List<T> list = new ArrayList<>();
		if (request.containsKey(paramName)) {
			for (String paramVal : request.get(paramName)) {
				if ((nonJsonCreator != null) && !Serializer.isJsonList(paramVal)) {
					try {
						for (String val : Collect.splitCsv(paramVal)) {
							list.add(nonJsonCreator.apply(val));
						}
					} catch (Exception e) {
						Logs.logError(
							LOG,
							e,
							"Issue getting list param from JSON using custom non-JSON creator for param [%s] of type [%s] with value [%s] in request %s",
							paramName,
							type,
							paramVal,
							request
						);
					}
				} else {
					list.addAll(Serializer.readJsonAsList(paramVal, type));
				}
			}
		}
		String singleParam = WordUtil.getSingularVersionOfWord(paramName);
		if (singleParam != null) {
			if (request.containsKey(singleParam)) {
				for (String paramVal : request.get(singleParam)) {
					if (Checks.isBlank(paramVal)) {
						continue;
					}
					if (Serializer.isJsonObject(paramVal)) {
						try {
							list.add(Serializer.fromJsonWithException(paramVal, type));
						} catch (Exception e) {
							Logs.logWarn(LOG, e, "Could not parse JSON request parameter: type=%s value=%s", type, paramVal);
						}
					} else if (nonJsonCreator != null) {
						list.add(nonJsonCreator.apply(paramVal));
					} else {
						list.add(createFromPathKeyParams(MapUtil.createFromQueryString(paramVal).entrySet(), type, null));
					}
				}
			}
			T singleParamData = parseFromPossiblePathKeyParams(request, singleParam, type, null);
			if (singleParamData != null) {
				list.add(singleParamData);
			}
		}

		return list.isEmpty() ? null : list;
	}

	/**
	 * Gets the bean map param.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @param nonJsonCreator the non json creator
	 * @return the bean map param
	 * @throws RequestParsingException the request parsing exception
	 */
	public static <T> Map<String, T> getBeanMapParam(
		Map<String, String[]> request,
		String paramName,
		Class<T> type,
		Function<String, T> nonJsonCreator
	) throws RequestParsingException {
		return getBeanMapParam(request, paramName, Serializer.constructType(type), nonJsonCreator);
	}

	/**
	 * Gets the bean map param.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @param nonJsonCreator the non json creator
	 * @return the bean map param
	 * @throws RequestParsingException the request parsing exception
	 */
	public static <T> Map<String, T> getBeanMapParam(
		Map<String, String[]> request,
		String paramName,
		JavaType type,
		Function<String, T> nonJsonCreator
	) throws RequestParsingException {
		String sep = ":";
		Map<String, T> map = new LinkedHashMap<>();
		String[] vals = request.get(paramName);
		if (vals != null) {
			for (String paramVal : vals) {
				if ((nonJsonCreator != null) && !Serializer.isJsonMap(paramVal)) {
					try {
						for (String val : Collect.splitCsv(paramVal)) {
							map.put(StringUtils.substringBefore(val, sep), nonJsonCreator.apply(Checks.defaultIfBlank(StringUtils.substringAfter(val, sep), val)));
						}
					} catch (Exception e) {
						Logs.logError(
							LOG,
							e,
							"Issue getting map param using custom non-JSON creator: name=%s type=%s value=%s request=%s",
							paramName,
							type,
							paramVal,
							request
						);
						String error = String.format(
							"Issue getting map param using custom non-JSON creator: name=%s type=%s value=%s request=%s",
							paramName,
							type,
							paramVal,
							request
						);
						throw new RequestParsingException(error, e);
					}
				} else {
					try {
						map.putAll(Serializer.fromJsonWithException(paramVal, Serializer.constructMapType(LinkedHashMap.class, String.class, type)));
					} catch (Exception e) {
						String error = String.format(
							"Issue getting map param using JSON creator: name=%s type=%s value=%s request=%s",
							paramName,
							type,
							paramVal,
							request
						);
						throw new RequestParsingException(error, e);
					}
				}
			}
		}
		String singleParam = WordUtil.getSingularVersionOfWord(paramName);
		if (singleParam != null) {
			String[] singleVals = request.get(singleParam);
			if (singleVals != null) {
				for (String val : singleVals) {
					if (Checks.isBlank(val)) {
						continue;
					}
					String key = StringUtils.substringBefore(val, sep);
					String paramVal = StringUtils.substringAfter(val, sep);
					if (Serializer.isJsonObject(paramVal)) {
						try {
							map.put(key, Serializer.fromJsonWithException(paramVal, type));
						} catch (Exception e) {
							Logs.logWarn(LOG, e, "Could not parse JSON request parameter: type=%s value=%s", type, paramVal);
						}
					} else if (nonJsonCreator != null) {
						map.put(key, nonJsonCreator.apply(Checks.defaultIfBlank(paramVal, key)));
					} else {
						map.put(key, createFromPathKeyParams(MapUtil.createFromQueryString(paramVal).entrySet(), type, null));
					}
				}
			}
			//			T singleParamData = parseFromPossiblePathKeyParams(request, singleParam, type, null);
			//			if (singleParamData != null) {
			//				list.add(singleParamData);
			//			}
		}

		return map.isEmpty() ? null : map;
	}

	/**
	 * Gets the big decimal list param.
	 *
	 * @param req the req
	 * @param paramName the param name
	 * @return the big decimal list param
	 */
	public static List<BigDecimal> getBigDecimalListParam(Map<String, String[]> req, String paramName) {
		return getBigDecimalListParam(req, paramName, null);
	}

	/**
	 * Gets the big decimal list param.
	 *
	 * @param req the req
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the big decimal list param
	 */
	public static List<BigDecimal> getBigDecimalListParam(Map<String, String[]> req, String paramName, List<BigDecimal> defaultValue) {
		return Optional.ofNullable((List<BigDecimal>) getCollectionParam(req, paramName, ArrayList.class, BigDecimal.class, BigDecimal::new)).orElse(defaultValue);
	}

	/**
	 * Gets the big decimal param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the big decimal param
	 * @throws Exception the exception
	 */
	public static BigDecimal getBigDecimalParam(Map<String, String[]> request, String paramName) throws Exception {
		return getBigDecimalParam(request, paramName, null);
	}

	/**
	 * Gets the big decimal param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the big decimal param
	 * @throws Exception the exception
	 */
	public static BigDecimal getBigDecimalParam(Map<String, String[]> request, String paramName, BigDecimal defaultValue) throws Exception {
		String param = getRequestParam(request, paramName);
		if (isNotBlank(param) && NumberUtils.isCreatable(param)) {
			return new BigDecimal(param);
		}
		return defaultValue;
	}

	/**
	 * Gets the boolean param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the boolean param
	 */
	public static Boolean getBooleanParam(Map<String, String[]> request, String paramName) {
		return getBooleanParam(request, paramName, null);
	}

	/**
	 * Gets the boolean param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the boolean param
	 */
	public static Boolean getBooleanParam(Map<String, String[]> request, String paramName, Boolean defaultValue) {
		String param = getRequestParam(request, paramName);
		if (isNotBlank(param)) {
			return BooleanUtils.toBoolean(param);
		}
		return defaultValue;
	}

	/**
	 * Gets the collection list param.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @return the collection list param
	 */
	public static <T extends Collection<String>> T getCollectionListParam(Map<String, String[]> request, String paramName, Class<T> type) {
		T val;
		try {
			val = ClassUtil.createInstance(type, false);
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue creating new instance of collection of type [%s]!", type);
			return null;
		}
		if (request.containsKey(paramName)) {
			for (String param : request.get(paramName)) {
				if (isNotBlank(param)) {
					val.addAll(splitCsvAsList(param));
				}
			}
		}
		String singleParam = WordUtil.getSingularVersionOfWord(paramName);
		if ((singleParam != null) && request.containsKey(singleParam)) {
			for (String singleVal : request.get(singleParam)) {
				val.add(singleVal);
			}
		}
		return val.isEmpty() ? null : val;
	}

	/**
	 * Gets the collection param.
	 *
	 * @param <T> the generic type
	 * @param <E> the element type
	 * @param request the request
	 * @param paramName the param name
	 * @param collectionType the collection type
	 * @param elementType the element type
	 * @param constructor the constructor
	 * @return the collection param
	 */
	public static <T extends Collection<E>, E> T getCollectionParam(
		Map<String, String[]> request,
		String paramName,
		final Class<T> collectionType,
		final Class<E> elementType,
		final ThrowingFunction<String, E> constructor
	) {
		return getCollectionParam(request, paramName, collectionType, elementType, constructor, ',');
	}

	/**
	 * Gets the collection param.
	 *
	 * @param <T> the generic type
	 * @param <E> the element type
	 * @param request the request
	 * @param paramName the param name
	 * @param collectionType the collection type
	 * @param elementType the element type
	 * @param constructor the constructor
	 * @param sep the sep
	 * @return the collection param
	 */
	public static <T extends Collection<E>, E> T getCollectionParam(
		Map<String, String[]> request,
		String paramName,
		final Class<T> collectionType,
		final Class<E> elementType,
		final ThrowingFunction<String, E> constructor,
		final char sep
	) {
		T coll;
		try {
			coll = ClassUtil.createInstance(collectionType, false);
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Could not create a new instance of collection type [%s]!", collectionType);
			return null;
		}
		if (request.containsKey(paramName)) {
			for (String paramVal : request.get(paramName)) {
				if (StringUtils.isBlank(paramVal)) {
					; // skip
				} else if (Serializer.isJsonList(paramVal)) {
					Lambdas.consumeIfNonNull(Serializer.readJsonAsList(paramVal, elementType), coll::addAll);
				} else if (Serializer.isJsonMap(paramVal)) {
					Lambdas.consumeIfNonNull(Serializer.fromJson(paramVal, elementType), coll::add);
				} else {
					Arrays.stream(Collect.splitCsv(paramVal, sep)).map(t -> {
						try {
							return constructor.apply(t);
						} catch (Exception e) {
							Logs.logError(LOG, e, "Issue applying collection element constructor for value [%s]!", t);
							return null;
						}
					}).filter(Objects::nonNull).forEach(coll::add);
				}
			}
		}
		String singleParam = WordUtil.getSingularVersionOfWord(paramName);
		if ((singleParam != null) && request.containsKey(singleParam)) {
			for (String paramVal : request.get(singleParam)) {
				if (StringUtils.isBlank(paramVal)) {
					; // skip
				} else if (Serializer.isJsonObject(paramVal)) {
					Lambdas.consumeIfNonNull(Serializer.fromJson(paramVal, elementType), coll::add);
				} else {
					try {
						coll.add(constructor.apply(paramVal));
					} catch (Exception e) {
						Logs.logError(LOG, e, "Could not create a new instance of element type [%s] from value [%s]!", elementType, paramVal);
					}
				}
			}
		}
		return coll.size() == 0 ? null : coll;
	}

	/**
	 * Gets the date param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param format the format
	 * @return the date param
	 * @throws Exception the exception
	 */
	public static Date getDateParam(Map<String, String[]> request, String paramName, FastDateFormat format) throws Exception {
		String param = getRequestParam(request, paramName);
		if (isNotBlank(param)) {
			try {
				return format.parse(param);
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Error parsing date param for key [%s] and value [%s].", paramName, param);
			}
		}
		return null;
	}

	/**
	 * Gets the default request field annotation.
	 *
	 * @return the default request field annotation
	 */
	public static RequestField getDefaultRequestFieldAnnotation() {
		return C.class.getAnnotation(RequestField.class);
	}

	/**
	 * Gets the double list param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the double list param
	 * @throws Exception the exception
	 */
	public static List<Double> getDoubleListParam(Map<String, String[]> request, String paramName) throws Exception {
		String param = getRequestParam(request, paramName);
		if (isNotBlank(param)) {
			return splitNumberCsvAsList(param, Double.class);
		}
		return null;
	}

	/**
	 * Gets the double param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the double param
	 * @throws Exception the exception
	 */
	public static Double getDoubleParam(Map<String, String[]> request, String paramName) throws Exception {
		return getDoubleParam(request, paramName, null);
	}

	/**
	 * Gets the double param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the double param
	 * @throws Exception the exception
	 */
	public static Double getDoubleParam(Map<String, String[]> request, String paramName, Double defaultValue) throws Exception {
		String param = getRequestParam(request, paramName);
		if (isNotBlank(param) && isNumber(param)) {
			return parseDouble(param);
		}
		return defaultValue;
	}

	/**
	 * Gets the enum list param.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param request the request
	 * @param paramName the param name
	 * @return the enum list param
	 */
	public static <T extends Enum<T>> List<T> getEnumListParam(Map<String, String[]> request, String paramName, Class<T> type) {
		return getCollectionParam(request, paramName, ArrayList.class, type, str -> Enums.fromString(type, str));
	}

	/**
	 * Gets the enum param.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @return the enum param
	 */
	public static <T extends Enum<T>> T getEnumParam(Map<String, String[]> request, String paramName, Class<T> type) {
		return getEnumParam(request, paramName, type, null);
	}

	/**
	 * Gets the enum param.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @param defaultValue the default value
	 * @return the enum param
	 */
	public static <T extends Enum<T>> T getEnumParam(Map<String, String[]> request, String paramName, Class<T> type, T defaultValue) {
		return Enums.fromString(type, getRequestParam(request, paramName), defaultValue);
	}

	/**
	 * Gets the enum set param.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @return the enum set param
	 */
	public static <T extends Enum<T>> Set<T> getEnumSetParam(Map<String, String[]> request, String paramName, Class<T> type) {
		return getCollectionParam(request, paramName, LinkedHashSet.class, type, str -> Enums.fromString(type, str));
	}

	/**
	 * Gets the integer list param.
	 *
	 * @param req the req
	 * @param paramName the param name
	 * @return the integer list param
	 */
	public static List<Integer> getIntegerListParam(Map<String, String[]> req, String paramName) {
		return getIntListParam(req, paramName, null);
	}

	/**
	 * Gets the integer set param.
	 *
	 * @param req the req
	 * @param paramName the param name
	 * @return the integer set param
	 */
	public static Set<Integer> getIntegerSetParam(Map<String, String[]> req, String paramName) {
		return getIntegerSetParam(req, paramName, null);
	}

	/**
	 * Gets the integer set param.
	 *
	 * @param req the req
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the integer set param
	 */
	public static Set<Integer> getIntegerSetParam(Map<String, String[]> req, String paramName, Set<Integer> defaultValue) {
		return Optional.ofNullable((Set<Integer>) getCollectionParam(req, paramName, HashSet.class, Integer.class, Integer::valueOf)).orElse(defaultValue);
	}

	/**
	 * Gets the int list param.
	 *
	 * @param req the req
	 * @param paramName the param name
	 * @return the int list param
	 */
	public static List<Integer> getIntListParam(Map<String, String[]> req, String paramName) {
		return getIntListParam(req, paramName, null);
	}

	/**
	 * Gets the int list param.
	 *
	 * @param req the req
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the int list param
	 */
	public static List<Integer> getIntListParam(Map<String, String[]> req, String paramName, List<Integer> defaultValue) {
		return Optional.ofNullable((List<Integer>) getCollectionParam(req, paramName, ArrayList.class, Integer.class, Integer::valueOf)).orElse(defaultValue);
	}

	/**
	 * Gets the int param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the int param
	 * @throws Exception the exception
	 */
	public static Integer getIntParam(Map<String, String[]> request, String paramName) throws Exception {
		return getIntParam(request, paramName, null);
	}

	/**
	 * Gets the int param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the int param
	 * @throws Exception the exception
	 */
	public static Integer getIntParam(Map<String, String[]> request, String paramName, Integer defaultValue) throws Exception {
		String param = getRequestParam(request, paramName);
		if (isNotBlank(param)) {
			if (isNumber(param)) {
				return NumberUtils.toInt(param, Integer.MAX_VALUE);
			} else if ("max".equalsIgnoreCase(param)) {
				return Integer.MAX_VALUE;
			} else if ("min".equalsIgnoreCase(param)) {
				return Integer.MIN_VALUE;
			}
		}
		return defaultValue;
	}

	/**
	 * Gets the json map param.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param existing the existing
	 * @param type the type
	 * @return the json map param
	 * @throws Exception the exception
	 */
	public static <T> Map<String, T> getJsonMapParam(Map<String, String[]> request, String paramName, Map<String, T> existing, Class<T> type) throws Exception {
		Map<String, T> val = Maps.newTreeMap();
		if (existing != null) {
			val.putAll(existing);
		}
		if (request.containsKey(paramName)) {
			for (String param : request.get(paramName)) {
				if (isParamInJson(param)) {
					Map<String, T> paramVal = Serializer.readJsonAsMap(param, type);
					val.putAll(paramVal);
				} else {
					String key = StringUtils.substringBefore(param, ":");
					T paramVal = Serializer.fromJson(StringUtils.substringAfter(param, ":"), type);
					val.put(key, paramVal);
				}
			}
		}
		String singleParam = WordUtil.getSingularVersionOfWord(paramName);
		if ((singleParam != null) && request.containsKey(singleParam)) {
			for (String param : request.get(singleParam)) {
				if (isParamInJson(param)) {
					Map<String, T> paramVal = Serializer.readJsonAsMap(param, type);
					val.putAll(paramVal);
				} else {
					String key = StringUtils.substringBefore(param, ":");
					T paramVal = Serializer.fromJson(StringUtils.substringAfter(param, ":"), type);
					val.put(key, paramVal);
				}
			}
		}
		return val.isEmpty() ? existing : val;
	}

	/**
	 * Gets the web user datas.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @return the web user datas
	 * @throws RequestParsingException the request parsing exception
	 */
	public static <T> T getJsonOrPathKeyParam(Map<String, String[]> request, String paramName, Class<T> type) throws RequestParsingException {
		String dataJson = getRequestParam(request, paramName);
		T data;
		try {
			data = parseParamValAsJsonOrQueryString(dataJson, type);
			return parseFromPossiblePathKeyParams(request, paramName, type, data);
		} catch (Exception e) {
			throw new RequestParsingException(String.format("%s: paramName=%s type=%s", e.getMessage(), paramName, type == null ? null : type.getSimpleName()), e);
		}
	}

	/**
	 * Gets the list param from json.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @return the list param from json
	 * @throws RequestParsingException the request parsing exception
	 */
	public static <T> List<T> getListParamFromJson(Map<String, String[]> request, String paramName, Class<T> type) throws RequestParsingException {
		return getListParamFromJson(request, paramName, type, null);
	}

	/**
	 * Gets the list param from json.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @param nonJsonCreator the non json creator
	 * @return the list param from json
	 * @throws RequestParsingException the request parsing exception
	 */
	public static <T> List<T> getListParamFromJson(
		Map<String, String[]> request,
		String paramName,
		Class<T> type,
		ThrowingTriFunction<Map<String, String[]>, String, String, Collection<T>> nonJsonCreator
	) throws RequestParsingException {
		List<T> list = new ArrayList<>();
		if (request.containsKey(paramName)) {
			for (String paramVal : request.get(paramName)) {
				if ((nonJsonCreator != null) && !Serializer.isJsonList(paramVal)) {
					try {
						Collection<T> multiVals = nonJsonCreator.apply(request, paramName, paramVal);
						if (multiVals != null) {
							list.addAll(multiVals);
						}
					} catch (Exception e) {
						Logs.logError(
							LOG,
							e,
							"Issue getting list param from JSON using custom non-JSON creator for param [%s] of type [%s] with value [%s] in request %s",
							paramName,
							type,
							paramVal,
							request
						);
						throw new RequestParsingException(String.format("%s: paramName=%s type=%s", e.getMessage(), paramName, type == null ? null : type.getSimpleName()), e);
					}
				} else {
					List<T> deserializedVals;
					try {
						deserializedVals = Serializer.fromJsonWithException(paramVal, Serializer.constructCollectionType(ArrayList.class, type));
					} catch (Exception e) {
						throw new RequestParsingException(String.format("%s: paramName=%s type=%s", e.getMessage(), paramName, type == null ? null : type.getSimpleName()), e);
					}
					list.addAll(deserializedVals);
				}
			}
		}
		String singleParam = WordUtil.getSingularVersionOfWord(paramName);
		if (singleParam != null) {
			try {
				if (request.containsKey(singleParam)) {
					for (String paramVal : request.get(singleParam)) {
						T singleParamData = parseParamValAsJsonOrQueryString(paramVal, type);
						if (singleParamData != null) {
							list.add(singleParamData);
						}
					}
				}
				T singleParamData = parseFromPossiblePathKeyParams(request, singleParam, type, null);
				if (singleParamData != null) {
					list.add(singleParamData);
				}
			} catch (Exception e) {
				throw new RequestParsingException(String.format("%s: paramName=%s type=%s", e.getMessage(), paramName, type == null ? null : type.getSimpleName()), e);
			}
		}

		return list.size() == 0 ? null : list;
	}

	/**
	 * Gets the list param from json or tilda.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @param supplier the supplier
	 * @return the list param from json or tilda
	 */
	public static <T> List<T> getListParamFromJsonOrTilda(Map<String, String[]> request, String paramName, final Class<T> type, final BiFunction<String, String, T> supplier) {
		List<T> list = new ArrayList<>();
		if (request.containsKey(paramName)) {
			for (String paramVal : request.get(paramName)) {
				Lambdas.consumeIfNonNull(isParamInJson(paramVal) ? Serializer.readJsonAsList(paramVal, type) : readTildaPipeAsList(paramVal, type, supplier), list::addAll);
			}
		}
		String singleParam = WordUtil.getSingularVersionOfWord(paramName);
		if ((singleParam != null) && request.containsKey(singleParam)) {
			for (String paramVal : request.get(singleParam)) {
				if (isParamInJson(paramVal)) {
					list.add(Serializer.fromJson(paramVal, type));
				} else {
					Lambdas.consumeIfNonNull(readTildaPipeAsList(paramVal, type, supplier), list::addAll);
				}
			}
		}
		return list.size() == 0 ? null : list;
	}

	/**
	 * Gets the local date list param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the local date list param
	 */
	public static List<LocalDate> getLocalDateListParam(Map<String, String[]> request, String paramName) {
		return getCollectionParam(request, paramName, ArrayList.class, LocalDate.class, param -> parseLocalDateParamValue(paramName, param, null));
	}

	/**
	 * Gets the local date param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the local date param
	 * @throws Exception the exception
	 */
	public static LocalDate getLocalDateParam(Map<String, String[]> request, String paramName) throws Exception {
		return getLocalDateParam(request, paramName, null);
	}

	/**
	 * Gets the local date param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the local date param
	 * @throws Exception the exception
	 */
	public static LocalDate getLocalDateParam(Map<String, String[]> request, String paramName, LocalDate defaultValue) throws Exception {
		String param = getRequestParam(request, paramName);
		return parseLocalDateParamValue(paramName, param, defaultValue);
	}

	/**
	 * Gets the local date set param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the local date set param
	 */
	public static Set<LocalDate> getLocalDateSetParam(Map<String, String[]> request, String paramName) {
		return getCollectionParam(request, paramName, HashSet.class, LocalDate.class, param -> parseLocalDateParamValue(paramName, param, null));
	}

	/**
	 * Gets the local date time list param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the local date time list param
	 */
	public static List<LocalDateTime> getLocalDateTimeListParam(Map<String, String[]> request, String paramName) {
		return getCollectionParam(request, paramName, ArrayList.class, LocalDateTime.class, param -> parseLocalDateTimeParamValue(paramName, param, null));
	}

	/**
	 * Gets the local date time param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the local date time param
	 * @throws Exception the exception
	 */
	public static LocalDateTime getLocalDateTimeParam(Map<String, String[]> request, String paramName) throws Exception {
		return getLocalDateTimeParam(request, paramName, null);
	}

	/**
	 * Gets the local date time param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the local date time param
	 * @throws Exception the exception
	 */
	public static LocalDateTime getLocalDateTimeParam(Map<String, String[]> request, String paramName, LocalDateTime defaultValue) throws Exception {
		String param = getRequestParam(request, paramName);
		return parseLocalDateTimeParamValue(paramName, param, defaultValue);
	}

	/**
	 * Gets the local date time set param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the local date time set param
	 */
	public static Set<LocalDateTime> getLocalDateTimeSetParam(Map<String, String[]> request, String paramName) {
		return getCollectionParam(request, paramName, HashSet.class, LocalDateTime.class, param -> parseLocalDateTimeParamValue(paramName, param, null));
	}

	/**
	 * Gets the locale param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the locale param
	 * @throws Exception the exception
	 */
	public static Locale getLocaleParam(Map<String, String[]> request, String paramName) throws Exception {
		return getLocaleParam(request, paramName, null);
	}

	/**
	 * Gets the locale param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the locale param
	 * @throws Exception the exception
	 */
	public static Locale getLocaleParam(Map<String, String[]> request, String paramName, Locale defaultValue) throws Exception {
		String param = getRequestParam(request, paramName);
		if (isNotBlank(param)) {
			String[] parts = StringUtils.split(param, '_');
			return new Locale(parts[0], parts.length > 1 ? parts[1] : "");
		}
		return defaultValue;
	}

	/**
	 * Gets the long param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the long param
	 * @throws Exception the exception
	 */
	public static Long getLongParam(Map<String, String[]> request, String paramName) throws Exception {
		return getLongParam(request, paramName, null);
	}

	/**
	 * Gets the long param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the long param
	 * @throws Exception the exception
	 */
	public static Long getLongParam(Map<String, String[]> request, String paramName, Long defaultValue) throws Exception {
		String param = getRequestParam(request, paramName);
		if (isNotBlank(param)) {
			if (isNumber(param)) {
				return NumberUtils.toLong(param, Long.MAX_VALUE);
			} else if ("max".equalsIgnoreCase(param)) {
				return Long.MAX_VALUE;
			} else if ("min".equalsIgnoreCase(param)) {
				return Long.MIN_VALUE;
			}
		}
		return defaultValue;
	}

	/**
	 * Gets the map list param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the map list param
	 * @throws Exception the exception
	 */
	public static List<Map<String, Object>> getMapListParam(Map<String, String[]> request, String paramName) throws Exception {
		String param = getRequestParam(request, paramName);
		if (isNotBlank(param)) {
			return Serializer.readJsonAsList(param);
		}
		return null;
	}

	/**
	 * Gets the map param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the map param
	 */
	public static Map<String, Object> getMapParam(Map<String, String[]> request, String paramName) {
		return getMapParam(request, paramName, LinkedHashMap.class, String.class, Object.class, identity(), Serializer::parseStringAsBestFit);
	}

	/**
	 * Gets the map param.
	 *
	 * @param <T> the generic type
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param request the request
	 * @param paramName the param name
	 * @param mapType the map type
	 * @param keyType the key type
	 * @param valueType the value type
	 * @param keyCreator the key creator
	 * @param valueCreator the value creator
	 * @return the map param
	 */
	public static <T extends Map<K, V>, K, V> T getMapParam(
		Map<String, String[]> request,
		String paramName,
		final Class<T> mapType,
		final Class<K> keyType,
		final Class<V> valueType,
		final ThrowingFunction<String, K> keyCreator,
		final ThrowingFunction<String, V> valueCreator
	) {
		return getMapParam(request, paramName, mapType, keyType, valueType, keyCreator, valueCreator, DEFAULT_KEYVAL_SEP, DEFAULT_PARAM_SEP);
	}

	/**
	 * Gets the map param.
	 *
	 * @param <T> the generic type
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param request the request
	 * @param paramName the param name
	 * @param mapType the map type
	 * @param keyType the key type
	 * @param valueType the value type
	 * @param keyCreator the key creator
	 * @param valueCreator the value creator
	 * @param keyValSep the key val sep
	 * @param paramSep the param sep
	 * @return the map param
	 */
	public static <T extends Map<K, V>, K, V> T getMapParam(
		Map<String, String[]> request,
		String paramName,
		final Class<T> mapType,
		final Class<K> keyType,
		final Class<V> valueType,
		final ThrowingFunction<String, K> keyCreator,
		final ThrowingFunction<String, V> valueCreator,
		final char keyValSep,
		final char paramSep
	) {
		return getMapParam(request, paramName, mapType, keyType, valueType, keyCreator, valueCreator, keyValSep, paramSep, null);
	}

	/**
	 * Gets the map param.
	 *
	 * @param <T> the generic type
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param request the request
	 * @param paramName the param name
	 * @param mapType the map type
	 * @param keyType the key type
	 * @param valueType the value type
	 * @param keyCreator the key creator
	 * @param valueCreator the value creator
	 * @param keyValSep the key val sep
	 * @param paramSep the param sep
	 * @param existing the existing
	 * @return the map param
	 */
	public static <T extends Map<K, V>, K, V> T getMapParam(
		Map<String, String[]> request,
		String paramName,
		final Class<T> mapType,
		final Class<K> keyType,
		final Class<V> valueType,
		final ThrowingFunction<String, K> keyCreator,
		final ThrowingFunction<String, V> valueCreator,
		final char keyValSep,
		final char paramSep,
		T existing
	) {
		T map;
		if (existing == null) {
			try {
				map = ClassUtil.createInstance(mapType, false);
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Could not create a new instance of map type [%s]!", mapType);
				return null;
			}
		} else {
			map = existing;
		}
		final BiConsumer<String, String> paramHandler = (key, val) -> {
			try {
				map.put(keyCreator.apply(key), valueCreator.apply(val));
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Could not get map entry of types [%s] [%s] from [%s] [%s].", keyType, valueType, key, val);
			}
		};
		if (request.containsKey(paramName)) {
			for (String paramVal : request.get(paramName)) {
				if (Serializer.isJsonMap(paramVal)) {
					Lambdas.consumeIfNonNull(Serializer.readJsonAsMap(paramVal, mapType, keyType, valueType), map::putAll);
				} else {
					MapUtil.mapFromString(paramVal, keyValSep, paramSep).forEach(paramHandler::accept);
				}
			}
		}
		String singleParam = WordUtil.getSingularVersionOfWord(paramName);
		if ((singleParam != null) && request.containsKey(singleParam)) {
			for (String paramVal : request.get(singleParam)) {
				if (Serializer.isJsonMap(paramVal)) {
					Lambdas.consumeIfNonNull(Serializer.readJsonAsMap(paramVal, mapType, keyType, valueType), map::putAll);
				} else {
					MapUtil.mapFromString(paramVal, keyValSep, paramSep).forEach(paramHandler::accept);
				}
			}
		}
		return map.size() == 0 ? null : map;
	}

	/**
	 * Gets the map param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param existing the existing
	 * @return the map param
	 */
	public static Map<String, Object> getMapParam(Map<String, String[]> request, String paramName, Map<String, Object> existing) {
		String[] params = request.get(paramName);
		if ((params == null) || (params.length < 1) || isBlank(params[0])) {
			return existing;
		}
		Map<String, Object> map = existing == null ? new TreeMap<>() : existing;
		for (String param : params) {
			int pipeTildaCount = countMatches(param, "~") + countMatches(param, "|");
			boolean isJson = isParamInJson(param);
			try {
				if (isJson) {
					map.putAll(Serializer.readJsonAsMap(param));
				} else if (pipeTildaCount > 0) {
					MapUtil.mapFromString(param, '~', '|', map);
				} else {
					String[] vals = Collect.splitCsv(param, ':');
					map.put(vals[0], Serializer.parseStringAsBestFit(vals[1]));
				}
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Could not read map param [%s].", param);
			}
		}
		return map;
	}

	/**
	 * Gets the merge config.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the merge config
	 * @throws RequestParsingException the request parsing exception
	 */
	public static MergeConfig getMergeConfig(Map<String, String[]> request, String paramName) throws RequestParsingException {
		return getJsonOrPathKeyParam(request, paramName, MergeConfig.class);
	}

	/**
	 * Gets the method args.
	 *
	 * @param httpRequest the http request
	 * @param method the method
	 * @return the method args
	 * @throws Exception the exception
	 */
	public static Object[] getMethodArgs(HttpServletRequest httpRequest, Method method) throws Exception {
		Object[] args = null;
		Parameter[] parameters = method.getParameters();
		if ((parameters != null) && (parameters.length > 0)) {
			List<Object> argsList = Lists.newArrayList();
			for (Parameter param : parameters) {
				if (Reflections.isInstanceofType(ALLOW_JSON_PARAM_TYPES, param.getType())) {
					argsList.add(RequestFactory.getAnnotatedRequest(param.getType(), httpRequest));
				} else {
					RequestField ann = RequestFactory.getDefaultRequestFieldAnnotation();
					RequestFieldInfo<?> requestField = new RequestFieldInfo<>(param, ann);
					if (requestField.getProcessFunction() != null) {
						try {
							argsList.add(requestField.getVal(null, httpRequest.getParameterMap()));
						} catch (Throwable e) {
							argsList.add(null);
						}
					} else {
						argsList.add(null);
					}
				}
			}
			args = argsList.toArray();
		}
		return args;
	}

	/**
	 * Gets the object param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the object param
	 */
	public static Object getObjectParam(Map<String, String[]> request, String paramName) {
		return getObjectParam(request, paramName, null);
	}

	/**
	 * Gets the object param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the object param
	 */
	public static Object getObjectParam(Map<String, String[]> request, String paramName, Object defaultValue) {
		String param = getRequestParam(request, paramName);
		Object val = defaultValue;
		if (isNotBlank(param)) {
			String type = "Date";
			if (StringUtils.contains(param, '~')) {
				type = StringUtils.substringBefore(param, "~");
			}
			try {
				switch (type) {
				case "Integer":
					val = NumberUtils.toInt(param, (Integer) defaultValue);
					break;
				case "Double":
					val = NumberUtils.toDouble(param, (Double) defaultValue);
					break;
				case "BigDecimal":
					val = new BigDecimal(param);
					break;
				default:
					break;
				}
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Error parsing object param [%s]!", paramName);
			}
		}
		return val;
	}

	/**
	 * Gets the output expressions.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the output expressions
	 * @throws RequestParsingException the request parsing exception
	 */
	public static List<OutputExpression> getOutputExpressions(Map<String, String[]> request, String paramName) throws RequestParsingException {
		List<OutputExpression> vals = getListParamFromJson(request, paramName, OutputExpression.class, (req, name, val) -> {
			ExprParser type = Enums.fromString(ExprParser.class, getRequestParam(req, "createExpressionsType"), ExprFactory.getDefaultExprParser());
			return Collect.splitCsvAsList(val).stream().map(expression -> {
				return new OutputExpression().setExpression(expression).setType(type).setPath('"' + expression + '"');
			}).collect(Collectors.toList());
		});
		if (vals != null) {
			vals.stream().filter(expr -> (expr.getPath() == null) && (expr.getExpression() != null)).forEach(expr -> expr.setPath('"' + expr.getExpression() + '"'));
		}
		return vals;
	}

	/**
	 * Gets the output format.
	 *
	 * @param request the request
	 * @return the output format
	 */
	public static OutputFormat getOutputFormat(HttpServletRequest request) {
		return OutputFormat.fromString(request.getParameter("outputFormat"));
	}

	/**
	 * Gets the pair param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the pair param
	 */
	public static Pair<String, String> getPairParam(Map<String, String[]> request, String paramName) {
		Pair<String, String> val = null;
		if (request.containsKey(paramName)) {
			for (String param : request.get(paramName)) {
				if (isParamInJson(param)) {
					val = Serializer.fromJson(param, Serializer.JSON_MAPPER.getTypeFactory().constructParametricType(Pair.class, String.class, String.class));
				} else {
					val = Pair.of(substringBefore(param, ":"), substringAfter(param, ":"));
				}
			}
		}
		String leftKey = paramName + ".left";
		String rightKey = paramName + ".right";
		if (request.containsKey(leftKey) || request.containsKey(rightKey)) {
			val = Pair.of(getRequestParam(request, leftKey, val == null ? null : val.getLeft()), getRequestParam(request, rightKey, val == null ? null : val.getRight()));
		}
		return val;
	}

	/**
	 * Gets the param as map.
	 *
	 * @param param the param
	 * @return the param as map
	 */
	public static Map<String, String> getParamAsMap(String param) {
		if (isBlank(param)) {
			return null;
		}
		if (isParamInJson(param)) {
			return Serializer.readJsonAsMap(param, String.class);
		}
		return MapUtil.mapFromString(param, '~', '|');
	}

	/**
	 * Gets the pattern list param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the pattern list param
	 */
	public static List<Pattern> getPatternListParam(Map<String, String[]> request, String paramName) {
		return getCollectionParam(request, paramName, ArrayList.class, Pattern.class, Pattern::compile);
	}

	/**
	 * Gets the pattern set param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the pattern set param
	 */
	public static Set<Pattern> getPatternSetParam(Map<String, String[]> request, String paramName) {
		return getCollectionParam(request, paramName, LinkedHashSet.class, Pattern.class, Pattern::compile);
	}

	/**
	 * Gets the possibly zipped request map.
	 *
	 * @param request the request
	 * @return the possibly zipped request map
	 */
	public static Map<String, String[]> getPossiblyZippedRequestMap(HttpServletRequest request) {
		return getPossiblyZippedRequestMap(request.getParameterMap());
	}

	/**
	 * Gets the possibly zipped request map.
	 *
	 * @param request the request
	 * @return the possibly zipped request map
	 */
	public static Map<String, String[]> getPossiblyZippedRequestMap(Map<String, String[]> request) {
		if ((request != null) && request.containsKey("zipped")) {
			String data = CompressionUtil.inflate(request.get("zipped")[0]);
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
				return requestMap;
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Could not read inflated data [%s] into request map!", data);
			}
		}
		return request;
	}

	/**
	 * Gets the post process method.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @return the post process method
	 */
	public static <T> ThrowingBiFunction<Map<String, String[]>, T, T> getPostProcessMethod(Class<T> type) {
		return (ThrowingBiFunction<Map<String, String[]>, T, T>) POST_PROCESS_METHODS.get(type);
	}

	/**
	 * Gets the process method.
	 *
	 * @param fieldDisplay the field display
	 * @return the process method
	 */
	public static ThrowingBiFunction<Map<String, String[]>, String, ?> getProcessMethod(@Nonnull String fieldDisplay) {
		return PROCESS_METHODS.get(fieldDisplay);
	}

	/**
	 * Gets the process methods.
	 *
	 * @return the process methods
	 */
	public static Map<String, ThrowingBiFunction<Map<String, String[]>, String, ?>> getProcessMethods() {
		return new HashMap<>(PROCESS_METHODS);
	}

	/**
	 * Gets the request bean info.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @return the request bean info
	 */
	public static <T> RequestFieldInfo<T> getRequestBeanInfo(Class<?> clazz) {
		return (RequestFieldInfo<T>) getRequestParserInfo(clazz, true).getRequestBeanInfo();
	}

	/**
	 * @return the requestDefaultsConfigApplier
	 */
	public static ThrowingBiFunction<RequestWrapper, String, Map<String, String[]>> getRequestDefaultsConfigApplier() {
		return requestDefaultsConfigApplier;
	}

	/**
	 * Gets the request fields.
	 *
	 * @param clazz the clazz
	 * @return the request fields
	 */
	public static List<RequestFieldInfo<?>> getRequestFields(Class<?> clazz) {
		return getRequestFields(clazz, true);
	}

	/**
	 * Gets the request fields.
	 *
	 * @param clazz the clazz
	 * @param requireAnnotation the require annotation
	 * @return the request fields
	 */
	public static List<RequestFieldInfo<?>> getRequestFields(Class<?> clazz, boolean requireAnnotation) {
		return getRequestParserInfo(clazz, requireAnnotation).getInfos();
	}

	/**
	 * Gets the request param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the request param
	 */
	public static String getRequestParam(Map<String, String[]> request, String paramName) {
		return getRequestParam(request, paramName, null);
	}

	/**
	 * Gets the request param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the request param
	 */
	public static String getRequestParam(Map<String, String[]> request, String paramName, String defaultValue) {
		String[] values = request.get(paramName);
		if ((values != null) && (values.length > 0)) {
			return values[0];
		}
		return defaultValue;
	}

	/**
	 * Gets the request param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the request param
	 */
	public static String getRequestParameter(HttpServletRequest request, String paramName, String defaultValue) {
		return StringUtils.defaultIfBlank(request.getParameter(paramName), defaultValue);
	}

	/**
	 * Gets the request parser info.
	 *
	 * @param clazz the clazz
	 * @param requireAnnotation the require annotation
	 * @return the request parser info
	 */
	public static RequestParserInfo getRequestParserInfo(Class<?> clazz, boolean requireAnnotation) {
		return getRequestParserInfo(clazz, requireAnnotation, null);
	}

	/**
	 * Gets the http request parser info.
	 *
	 * @param clazz the clazz
	 * @param requireAnnotation the require annotation
	 * @param prefixes the prefixes
	 * @return the http request parser info
	 */
	public static RequestParserInfo getRequestParserInfo(Class<?> clazz, boolean requireAnnotation, List<String> prefixes) {
		String key = String.format("type=%s requireAnnotation=%s prefixes=%s", clazz, requireAnnotation, prefixes);
		RequestParserInfo info = INFO_CACHE.get(key);
		if (info == null) {
			synchronized (INFO_CACHE) {
				info = INFO_CACHE.get(key);
				if (info == null) {
					info = new RequestParserInfo();
					info.setRequestBeanInfo(clazz.isAnnotationPresent(RequestField.class) ? new RequestFieldInfo<>(clazz) : null);
					if ((prefixes == null) && (info.getRequestBeanInfo() != null)) {
						prefixes = info.getRequestBeanInfo().getPrefixes();
					}
					List<RequestFieldInfo<?>> fields = new ArrayList<>();
					List<RequestFieldInfo<?>> complexRequestInfos = new ArrayList<>();
					info.setInfos(fields);
					Class<?> superClazz = clazz;
					boolean fromSuperClass = false;

					while ((superClazz != null) && (superClazz != Object.class)) {
						for (AccessibleObject member : superClazz.getDeclaredFields()) {
							if (member.isAnnotationPresent(RequestField.class)) {
								member.setAccessible(true);
								RequestFieldInfo<?> field = new RequestFieldInfo<>(clazz, member, fromSuperClass);
								field.setPrefixes(prefixes);
								if (field.isValidField()) {
									fields.add(field);
								}
							} else if (!requireAnnotation) {
								member.setAccessible(true);
								RequestFieldInfo<?> field = new RequestFieldInfo<>(clazz, member, fromSuperClass, getDefaultRequestFieldAnnotation());
								field.setPrefixes(prefixes);
								if (field.isValidField() && ((field.getProcessFunction() != null) || (field.getProcessFunctionWithPrefix() != null))) {
									fields.add(field);
								}
							}
						}
						for (AccessibleObject member : superClazz.getDeclaredMethods()) {
							if (member.isAnnotationPresent(RequestField.class)) {
								member.setAccessible(true);
								RequestFieldInfo<?> field = new RequestFieldInfo<>(clazz, member, fromSuperClass);
								field.setPrefixes(prefixes);
								if (field.isValidField()) {
									fields.add(field);
								}
							}
						}
						superClazz = superClazz.getSuperclass();
						fromSuperClass = true;
					}
					info.setInfosMap(fields.stream().filter(field -> {
						if (field.isCustomPojoField()) {
							complexRequestInfos.add(field);
							return false;
						}
						return true;
					}).flatMap(field -> field.getParamNames().stream().map(name -> Pair.of(name, field))).collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
					info.setComplexRequestInfos(complexRequestInfos);

					INFO_CACHE.put(key, info);
				}
			}
		}
		return info;
	}

	/**
	 * Gets the sorted list cache key.
	 *
	 * @param list the list
	 * @return the sorted list cache key
	 */
	public static String getSortedListCacheKey(List<String> list) {
		if (CollectionUtils.isEmpty(list)) {
			return "[]";
		}
		Collections.sort(list);
		return list.toString();
	}

	/**
	 * Gets the string list map param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the string list map param
	 */
	public static Map<String, List<String>> getStringListMapParam(Map<String, String[]> request, String paramName) {
		Map<String, List<String>> map = new HashMap<>();
		if (paramName.endsWith("*")) {
			String prefix = StringUtils.substringBeforeLast(paramName, "*");
			request.forEach((key, vals) -> {
				if (key.startsWith(prefix)) {
					map.computeIfAbsent(StringUtils.substringAfter(key, prefix), k -> new ArrayList<>()).addAll(Arrays.asList(vals));
				}
			});
		} else if (request.containsKey(paramName)) {
			for (String param : request.get(paramName)) {
				Map<String, List<String>> subMap = isParamInJson(param) ? Serializer.fromJson(
					param,
					Serializer.constructMapType(HashMap.class, String.class, Serializer.constructCollectionType(ArrayList.class, String.class))
				) : MapUtil.stringListMapFromString(param, '~', '|');
				if (map.isEmpty()) {
					map.putAll(subMap);
				} else {
					subMap.forEach((key, val) -> map.computeIfAbsent(key, k -> new ArrayList<>()).addAll(val));
				}
			}
		}
		return map.isEmpty() ? null : map;
	}

	/**
	 * Gets the string list param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the string list param
	 */
	public static List<String> getStringListParam(Map<String, String[]> request, String paramName) {
		return getCollectionParam(request, paramName, ArrayList.class, String.class, ThrowingFunction.identity());
	}

	/**
	 * Gets the string map param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the string map param
	 */
	public static Map<String, String> getStringMapParam(Map<String, String[]> request, String paramName) {
		return getMapParam(request, paramName, LinkedHashMap.class, String.class, String.class, ThrowingFunction.identity(), ThrowingFunction.identity());
	}

	/**
	 * Gets the string set param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the string set param
	 */
	public static Set<String> getStringSetParam(Map<String, String[]> request, String paramName) {
		return getCollectionParam(request, paramName, LinkedHashSet.class, String.class, ThrowingFunction.identity());
	}

	/**
	 * Gets the type param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return the type param
	 * @throws Exception the exception
	 */
	public static Class<?> getTypeParam(Map<String, String[]> request, String paramName) throws Exception {
		return getTypeParam(request, paramName, null);
	}

	/**
	 * Gets the type param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @param defaultValue the default value
	 * @return the type param
	 * @throws Exception the exception
	 */
	public static Class<?> getTypeParam(Map<String, String[]> request, String paramName, Class<?> defaultValue) throws Exception {
		String value = getRequestParam(request, paramName);
		if (Checks.isNotBlank(value)) {
			String className = Optional.ofNullable(CustomTypeIdResolver.getCorrectClassName(value)).filter(Checks::isNotBlank).orElse(null);
			if (className != null) {
				return Class.forName(className);
			}
		}
		return defaultValue;
	}

	/**
	 * Checks for param.
	 *
	 * @param request the request
	 * @param paramName the param name
	 * @return true, if successful
	 */
	public static boolean hasParam(Map<String, String[]> request, String paramName) {
		return isNotBlank(getRequestParam(request, paramName));
	}

	/**
	 * Checks if is param in JSON format.
	 *
	 * @param param the param
	 * @return true, if param is in JSON format.
	 */
	public static boolean isParamInJson(String param) {
		return Serializer.isJsonObject(param);
	}

	/**
	 * Checks if is param in pipe tilda format.
	 *
	 * @param param the param
	 * @return true, if is param in pipe tilda format
	 */
	public static boolean isParamInPipeTildaFormat(String param) {
		int pipeTildaCount = countMatches(param, "~") + countMatches(param, "|");
		int jsonCount = countMatches(param, "{") + countMatches(param, "}");
		if (pipeTildaCount > jsonCount) {
			return true;
		}
		return false;
	}

	/**
	 * @return the useNewRequestParser
	 */
	public static boolean isUseNewRequestParser() {
		return useNewRequestParser;
	}

	/**
	 * Parses the from possible path key params.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @param data the data
	 * @return the t
	 */
	public static <T> T parseFromPossiblePathKeyParams(Map<String, String[]> request, String paramName, Class<T> type, T data) {
		return parseFromPossiblePathKeyParams(request, paramName, Serializer.constructType(type), data);
	}

	/**
	 * Parses the from possible path key params.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param paramName the param name
	 * @param type the type
	 * @param data the data
	 * @return the t
	 */
	public static <T> T parseFromPossiblePathKeyParams(Map<String, String[]> request, String paramName, JavaType type, T data) {
		if ((request != null) && !request.isEmpty()) {
			T otherData = createFromPathKeyParams(request.entrySet(), type, paramName + '.');
			if (otherData != null) {
				if (data == null) {
					data = otherData;
				} else {
					Merges.deepMerge(otherData, data);
				}
			}
		}
		return data;
	}

	/**
	 * Parses the local date param value.
	 *
	 * @param paramName the param name
	 * @param param the param
	 * @param defaultValue the default value
	 * @return the local date
	 * @throws RequestParsingException the request parsing exception
	 */
	public static LocalDate parseLocalDateParamValue(@Nonnull String paramName, @Nullable String param, @Nullable LocalDate defaultValue) throws RequestParsingException {
		if (isNotBlank(param)) {
			try {
				return Dates.parseDateTimeUnsafe(param, false).toLocalDate();
			} catch (Throwable e) {
				throw new RequestParsingException(String.format("Error parsing LocalDate param: key=%s value=%s", paramName, param), e);
			}
		}
		return defaultValue;
	}

	/**
	 * Parses the local date time param value.
	 *
	 * @param paramName the param name
	 * @param param the param
	 * @param defaultValue the default value
	 * @return the local date time
	 * @throws RequestParsingException the request parsing exception
	 */
	public static LocalDateTime parseLocalDateTimeParamValue(@Nonnull String paramName, @Nullable String param, @Nullable LocalDateTime defaultValue) throws RequestParsingException {
		if (isNotBlank(param)) {
			try {
				return Dates.parseDateTimeUnsafe(param, false);
			} catch (Throwable e) {
				throw new RequestParsingException(String.format("Error parsing LocalDateTime param: key=%s value=%s", paramName, param), e);
			}
		}
		return defaultValue;
	}

	/**
	 * Parses the param va as json or query string.
	 *
	 * @param <T> the generic type
	 * @param dataJson the data json
	 * @param type the type
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T parseParamValAsJsonOrQueryString(String dataJson, Class<T> type) throws JsonParseException, JsonMappingException, IOException {
		T data;
		if (StringUtils.isBlank(dataJson)) {
			data = null;
		} else if (Serializer.isProbablyJsonObject(dataJson)) {
			data = Serializer.fromJsonWithException(dataJson, type);
		} else {
			data = createFromPathKeyParams(MapUtil.createFromQueryString(dataJson).entrySet(), type, null);
		}
		return data;
	}

	/**
	 * Read tilda pipe as list.
	 *
	 * @param <T> the generic type
	 * @param paramVal the param val
	 * @param type the type
	 * @param supplier the supplier
	 * @return the list
	 */
	public static <T> List<T> readTildaPipeAsList(String paramVal, final Class<T> type, final BiFunction<String, String, T> supplier) {
		Map<String, String> keyVals = MapUtil.mapFromString(paramVal, '~', '|', true);
		List<T> list = new ArrayList<>();
		for (Entry<String, String> entry : keyVals.entrySet()) {
			list.add(supplier.apply(entry.getKey(), entry.getValue()));
		}
		return list.size() == 0 ? null : list;
	}

	/**
	 * Removes the process method.
	 *
	 * @param fieldDisplay the field display
	 * @return the throwing bi function
	 */
	public static ThrowingBiFunction<Map<String, String[]>, String, ?> removeProcessMethod(@Nonnull String fieldDisplay) {
		assert (fieldDisplay != null);
		try {
			return PROCESS_METHODS.remove(fieldDisplay);
		} finally {
			synchronized (INFO_CACHE) {
				INFO_CACHE.clear();
			}
		}
	}

	/**
	 * @param requestDefaultsConfigApplier the requestDefaultsConfigApplier to set
	 */
	public static void setRequestDefaultsConfigApplier(ThrowingBiFunction<RequestWrapper, String, Map<String, String[]>> requestDefaultsConfigApplier) {
		RequestFactory.requestDefaultsConfigApplier = requestDefaultsConfigApplier;
	}

	/**
	 * @param useNewRequestParser the useNewRequestParser to set
	 */
	public static void setUseNewRequestParser(boolean useNewRequestParser) {
		RequestFactory.useNewRequestParser = useNewRequestParser;
	}

	/**
	 * The Class c.
	 */
	@RequestField
	private static final class C {
		//
	}

}
