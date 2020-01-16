package com.lancethomps.lava.common.web;

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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.esapi.reference.DefaultHTTPUtilities;

import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.Collect;
import com.lancethomps.lava.common.ContextUtil;
import com.lancethomps.lava.common.cache.SimpleLruCache;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.properties.PropertyParser;
import com.lancethomps.lava.common.ser.Serializer;
import com.lancethomps.lava.common.time.Stopwatch;
import com.lancethomps.lava.common.web.requests.AcceptHeaderMediaType;
import com.lancethomps.lava.common.web.ua.UserAgent;
import com.lancethomps.lava.common.web.ua.UserAgentParser;
import com.lancethomps.lava.common.web.ua.UserAgentParserConfig;

import ua_parser.Client;
import ua_parser.Parser;

public class WebRequests {

  public static final String DEFAULT_UA_REGEX_YAML = "/com/lancethomps/lava/common/web/ua-regexes.yaml";
  private static final SimpleLruCache<String, UserAgent> CACHED_USER_AGENTS = new SimpleLruCache<>(500);
  private static final Logger LOG = LogManager.getLogger(WebRequests.class);
  private static Parser uaParser;
  private static UserAgentParser userAgentParser;
  private static String userAgentRegexFile;

  static {
    initializeUserAgentParser();
  }

  public static void doFilterAndPauseTimer(ServletRequest sreq, ServletResponse sresp, FilterChain chain, WebRequestContext context, String timer)
    throws IOException, ServletException {
    if (context != null) {
      context.pause(timer);
    }
    chain.doFilter(sreq, sresp);
  }

  public static InputStream findUaRegexesYaml() {
    InputStream stream = UserAgentParser.class.getResourceAsStream(DEFAULT_UA_REGEX_YAML);
    if (stream != null) {
      return stream;
    }
    return Parser.class.getResourceAsStream("/ua_parser/regexes.yaml");
  }

  public static List<AcceptHeaderMediaType> getAcceptedMediaTypes(HttpServletRequest request) {
    return request == null ? Collections.emptyList() : getAcceptedMediaTypes(request.getHeader("Accept"));
  }

  public static List<AcceptHeaderMediaType> getAcceptedMediaTypes(String header) {
    return header == null ? Collections.emptyList()
      : Collect
      .splitCsvAsList(header)
      .stream()
      .map(AcceptHeaderMediaType::fromHeaderPart)
      .filter(Objects::nonNull)
      .sorted()
      .collect(Collectors.toList());
  }

  public static Cookie getCookie(@Nonnull HttpServletRequest request, @Nonnull String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      return Stream.of(cookies).filter(cookie -> name.equals(cookie.getName())).findFirst().orElse(null);
    }
    return null;
  }

  public static String getRequestIp(HttpServletRequest request) {
    return request == null ? null : StringUtils.defaultIfBlank(request.getHeader("X-FORWARDED-FOR"), request.getRemoteAddr());
  }

  public static Parser getUaParser() {
    return uaParser;
  }

  public static UserAgentParser getUserAgentParser() {
    return userAgentParser;
  }

  public static void setUserAgentParser(UserAgentParser userAgentParser) {
    WebRequests.userAgentParser = userAgentParser;
  }

  public static String getUserAgentRegexFile() {
    return userAgentRegexFile;
  }

  public static void setUserAgentRegexFile(String userAgentRegexFile) {
    WebRequests.userAgentRegexFile = userAgentRegexFile != null ? PropertyParser.parseAndReplaceWithProps(userAgentRegexFile) : userAgentRegexFile;
    initializeUserAgentParser();
  }

  public static String getUserAgentString(eu.bitwalker.useragentutils.UserAgent userAgent) {
    return userAgent == null ? null : Optional
      .ofNullable(Serializer.toMap(userAgent))
      .map(ua -> ua.get("userAgentString"))
      .filter(Objects::nonNull)
      .map(Object::toString)
      .orElse(null);
  }

  public static boolean isExternalUrl(@Nonnull String fullUrl) {
    boolean external = false;
    try {
      URL url = new URL(fullUrl);
      int port = url.getPort();
      if ((port == -1) || ("http".equalsIgnoreCase(url.getProtocol()) && (port == 80)) ||
        ("https".equalsIgnoreCase(url.getProtocol()) && (port == 443))) {
        external = true;
      }
    } catch (MalformedURLException | NumberFormatException e) {
      Logs.logError(LOG, e, "Could not get URL object from originatingRequestUrl of [%s].", fullUrl);
    }
    return external;
  }

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

  public static UserAgent parseUserAgentUsingUserAgentUtils(String userAgent) {
    return UserAgent.fromUserAgentUtils(parseUserAgentUserAgentUtils(userAgent), userAgent);
  }

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

  public static String sanitizeJavaScriptForHtml(String script) {
    return StringUtils.replaceIgnoreCase(script, "</script", "</scr\\ipt");

  }

  public static HttpServletResponse setHeader(@Nonnull HttpServletResponse response, @Nonnull Object name, @Nonnull Object value) {
    DefaultHTTPUtilities.getInstance().setHeader(response, name.toString(), value.toString());
    return response;
  }

  public static HttpServletResponse setHeaderIfNonNull(@Nonnull HttpServletResponse response, @Nonnull Object name, @Nullable Object value) {
    if (value != null) {
      return setHeader(response, name, value);
    }
    return response;
  }

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
