package com.github.lancethomps.lava.common.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.owasp.esapi.reference.DefaultHTTPUtilities;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.Collect;
import com.github.lancethomps.lava.common.ContextUtil;
import com.github.lancethomps.lava.common.cache.SimpleLruCache;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.properties.PropertyParser;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.time.Stopwatch;
import com.github.lancethomps.lava.common.web.requests.AcceptHeaderMediaType;
import com.github.lancethomps.lava.common.web.ua.UserAgent;
import com.github.lancethomps.lava.common.web.ua.UserAgentParser;
import com.github.lancethomps.lava.common.web.ua.UserAgentParserConfig;

import ua_parser.Client;
import ua_parser.Parser;

/**
 * The Class WebRequests.
 *
 * @author lathomps
 */
public class WebRequests {

	/** The Constant DEFAULT_UA_REGEX_YAML. */
	public static final String DEFAULT_UA_REGEX_YAML = "/com/github/lancethomps/lava/common/web/ua-regexes.yaml";

	/** The Constant CACHED_USER_AGENTS. */
	private static final SimpleLruCache<String, UserAgent> CACHED_USER_AGENTS = new SimpleLruCache<>(500);

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(WebRequests.class);

	/** The user agent parser. */
	private static Parser uaParser;

	/** The user agent parser. */
	private static UserAgentParser userAgentParser;

	/** The user agent regex file. */
	private static String userAgentRegexFile;

	static {
		initializeUserAgentParser();
	}

	/**
	 * Do filter and pause timer.
	 *
	 * @param sreq the sreq
	 * @param sresp the sresp
	 * @param chain the chain
	 * @param context the context
	 * @param timer the timer
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ServletException the servlet exception
	 */
	public static void doFilterAndPauseTimer(ServletRequest sreq, ServletResponse sresp, FilterChain chain, WebRequestContext context, String timer) throws IOException, ServletException {
		if (context != null) {
			context.pause(timer);
		}
		chain.doFilter(sreq, sresp);
	}

	/**
	 * Find ua regexes yaml.
	 *
	 * @return the input stream
	 */
	public static InputStream findUaRegexesYaml() {
		InputStream stream = UserAgentParser.class.getResourceAsStream(DEFAULT_UA_REGEX_YAML);
		if (stream != null) {
			return stream;
		}
		return Parser.class.getResourceAsStream("/ua_parser/regexes.yaml");
	}

	/**
	 * Gets the accepted media types.
	 *
	 * @param request the request
	 * @return the accepted media types
	 */
	public static List<AcceptHeaderMediaType> getAcceptedMediaTypes(HttpServletRequest request) {
		return request == null ? Collections.emptyList() : getAcceptedMediaTypes(request.getHeader("Accept"));
	}

	/**
	 * Gets the accepted media types.
	 *
	 * @param header the header
	 * @return the accepted media types
	 */
	public static List<AcceptHeaderMediaType> getAcceptedMediaTypes(String header) {
		return header == null ? Collections.emptyList()
			: Collect.splitCsvAsList(header).stream().map(AcceptHeaderMediaType::fromHeaderPart).filter(Objects::nonNull).sorted().collect(Collectors.toList());
	}

	/**
	 * Gets the cookie.
	 *
	 * @param request the request
	 * @param name the name
	 * @return the cookie
	 */
	public static Cookie getCookie(@Nonnull HttpServletRequest request, @Nonnull String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			return Stream.of(cookies).filter(cookie -> name.equals(cookie.getName())).findFirst().orElse(null);
		}
		return null;
	}

	/**
	 * Gets the request ip.
	 *
	 * @param request the request
	 * @return the request ip
	 */
	public static String getRequestIp(HttpServletRequest request) {
		return request == null ? null : StringUtils.defaultIfBlank(request.getHeader("X-FORWARDED-FOR"), request.getRemoteAddr());
	}

	/**
	 * @return the userAgentParser
	 */
	public static Parser getUaParser() {
		return uaParser;
	}

	/**
	 * @return the userAgentParser
	 */
	public static UserAgentParser getUserAgentParser() {
		return userAgentParser;
	}

	/**
	 * @return the userAgentRegexFile
	 */
	public static String getUserAgentRegexFile() {
		return userAgentRegexFile;
	}

	/**
	 * Gets the user agent string.
	 *
	 * @param userAgent the user agent
	 * @return the user agent string
	 */
	public static String getUserAgentString(eu.bitwalker.useragentutils.UserAgent userAgent) {
		return userAgent == null ? null : Optional.ofNullable(Serializer.toMap(userAgent)).map(ua -> ua.get("userAgentString")).filter(Objects::nonNull).map(Object::toString).orElse(null);
	}

	/**
	 * Checks if is external url.
	 *
	 * @param fullUrl the full url
	 * @return true, if is external url
	 */
	public static boolean isExternalUrl(@Nonnull String fullUrl) {
		boolean external = false;
		try {
			URL url = new URL(fullUrl);
			int port = url.getPort();
			if ((port == -1) || ("http".equalsIgnoreCase(url.getProtocol()) && (port == 80)) || ("https".equalsIgnoreCase(url.getProtocol()) && (port == 443))) {
				external = true;
			}
		} catch (MalformedURLException | NumberFormatException e) {
			Logs.logError(LOG, e, "Could not get URL object from originatingRequestUrl of [%s].", fullUrl);
		}
		return external;
	}

	/**
	 * Parses the user agent.
	 *
	 * @param userAgent the user agent
	 * @return the user agent
	 */
	public static UserAgent parseUserAgent(String userAgent) {
		return CACHED_USER_AGENTS.computeIfAbsent(userAgent, k -> {
			if (userAgentParser != null) {
				return parseUserAgentCustom(userAgent);
			}
			if (uaParser != null) {
				return UserAgent.fromUaParser(uaParser.parse(userAgent), userAgent);
			}
			return parseUserAgentUsingUserAgentUtils(userAgent);
		});
	}

	/**
	 * Parses the user agent client.
	 *
	 * @param userAgent the user agent
	 * @return the client
	 */
	public static Client parseUserAgentClient(String userAgent) {
		if (uaParser == null) {
			return null;
		}
		Stopwatch watch = LOG.isTraceEnabled() ? Stopwatch.createAndStart() : null;
		try {
			return uaParser.parse(userAgent);
		} finally {
			Logs.logTimer(LOG, watch, "User-Agent Parser - ua-java", Level.TRACE);
		}
	}

	/**
	 * Parses the user agent custom.
	 *
	 * @param userAgent the user agent
	 * @return the user agent
	 */
	public static UserAgent parseUserAgentCustom(String userAgent) {
		Stopwatch watch = LOG.isTraceEnabled() ? Stopwatch.createAndStart() : null;
		try {
			return userAgentParser.parse(userAgent);
		} finally {
			if (watch != null) {
				Logs.logTimer(LOG, watch, "User-Agent Parser", Level.TRACE);
			}
		}
	}

	/**
	 * Parses the user agent user agent utils.
	 *
	 * @param userAgent the user agent
	 * @return the eu.bitwalker.useragentutils. user agent
	 */
	public static eu.bitwalker.useragentutils.UserAgent parseUserAgentUserAgentUtils(String userAgent) {
		Stopwatch watch = LOG.isTraceEnabled() ? Stopwatch.createAndStart() : null;
		try {
			return eu.bitwalker.useragentutils.UserAgent.parseUserAgentString(userAgent);
		} finally {
			if (watch != null) {
				Logs.logTimer(LOG, watch, "User-Agent Parser - UserAgentUtils", Level.TRACE);
			}
		}
	}

	/**
	 * Parses the user agent using user agent utils.
	 *
	 * @param userAgent the user agent
	 * @return the user agent
	 */
	public static UserAgent parseUserAgentUsingUserAgentUtils(String userAgent) {
		return UserAgent.fromUserAgentUtils(parseUserAgentUserAgentUtils(userAgent), userAgent);
	}

	/**
	 * Removes the invalid parameters.
	 *
	 * @param params the params
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @return the list of invalid parameters
	 */
	public static List<String> removeInvalidParameters(Map<String, String[]> params, List<Pattern> whiteList, List<Pattern> blackList) {
		if ((params != null) && (Checks.isNotEmpty(whiteList) || Checks.isNotEmpty(blackList))) {
			List<String> invalid = params
				.keySet()
				.stream()
				.filter(key -> !Checks.passesWhiteAndBlackListCheck(key, whiteList, blackList).getLeft())
				.collect(Collectors.toList());
			if (!invalid.isEmpty()) {
				invalid.forEach(key -> params.remove(key));
				return invalid;
			}
		}
		return null;
	}

	/**
	 * Sanitize java script for html.
	 *
	 * @param script the script
	 * @return the string
	 */
	public static String sanitizeJavaScriptForHtml(String script) {
		return StringUtils.replaceIgnoreCase(script, "</script", "</scr\\ipt");
		// return script == null ? null : JsonSanitizer.sanitize(script);
	}

	/**
	 * Sets the header.
	 *
	 * @param response the response
	 * @param name the name
	 * @param value the value
	 * @return the http servlet response
	 */
	public static HttpServletResponse setHeader(@Nonnull HttpServletResponse response, @Nonnull Object name, @Nonnull Object value) {
		DefaultHTTPUtilities.getInstance().setHeader(response, name.toString(), value.toString());
		return response;
	}

	/**
	 * Sets the header if non null.
	 *
	 * @param response the response
	 * @param name the name
	 * @param value the value
	 * @return the http servlet response
	 */
	public static HttpServletResponse setHeaderIfNonNull(@Nonnull HttpServletResponse response, @Nonnull Object name, @Nullable Object value) {
		if (value != null) {
			return setHeader(response, name, value);
		}
		return response;
	}

	/**
	 * @param userAgentParser the userAgentParser to set
	 */
	public static void setUserAgentParser(UserAgentParser userAgentParser) {
		WebRequests.userAgentParser = userAgentParser;
	}

	/**
	 * @param userAgentRegexFile the userAgentRegexFile to set
	 */
	public static void setUserAgentRegexFile(String userAgentRegexFile) {
		WebRequests.userAgentRegexFile = userAgentRegexFile != null ? PropertyParser.parseAndReplaceWithProps(userAgentRegexFile) : userAgentRegexFile;
		initializeUserAgentParser();
	}

	/**
	 * Should return html.
	 *
	 * @param request the request
	 * @return true, if successful
	 */
	public static boolean shouldReturnHtml(HttpServletRequest request) {
		List<AcceptHeaderMediaType> mediaTypes = getAcceptedMediaTypes(request);
		if (Checks.isEmpty(mediaTypes)) {
			return true;
		}
		double bestQualityFactor = mediaTypes.get(0).getQualityFactor();
		for (AcceptHeaderMediaType type : mediaTypes) {
			if (type.getQualityFactor() < bestQualityFactor) {
				return true;
			}
			if (type.getOutputFormat() != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Initialize default user agent parser.
	 */
	private static void initializeDefaultUserAgentParser() {
		userAgentParser = new UserAgentParser();
		Parser parser = null;
		try (InputStream stream = findUaRegexesYaml()) {
			parser = new Parser(stream);
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Error initializing custom userAgentParser");
			try {
				parser = new Parser();
			} catch (Throwable e2) {
				Logs.logError(LOG, e2, "Error initializing default userAgentParser");
			}
		}
		if (parser != null) {
			uaParser = parser;
		}
	}

	/**
	 * Initialize user agent parser.
	 */
	private static void initializeUserAgentParser() {
		Parser parser = null;
		try {
			File file = null;
			if (Checks.isNotBlank(userAgentRegexFile) && ((file = ContextUtil.getFile(userAgentRegexFile)) != null) && file.isFile()) {
				userAgentParser = new UserAgentParser(Serializer.fromYaml(file, UserAgentParserConfig.class));
				try (FileInputStream stream = new FileInputStream(file)) {
					parser = new Parser(stream);
				} catch (Throwable e) {
					Logs.logError(LOG, e, "Error initializing userAgentParser with custom regex file [%s]", userAgentRegexFile);
					initializeDefaultUserAgentParser();
				}
			} else {
				initializeDefaultUserAgentParser();
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Error initializing userAgentParser");
		}
		if (parser != null) {
			uaParser = parser;
		}
	}

}
