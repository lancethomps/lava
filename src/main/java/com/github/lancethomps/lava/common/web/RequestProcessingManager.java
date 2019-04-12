package com.github.lancethomps.lava.common.web;

import static com.github.lancethomps.lava.common.Checks.defaultIfBlank;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.util.WebUtils;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.ContextUtil;
import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.file.AbstractFileListener;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.ser.jackson.SerializationException;
import com.github.lancethomps.lava.common.spring.SpringUtil;
import com.github.lancethomps.lava.common.web.config.RequestProcessingConfig;
import com.github.lancethomps.lava.common.web.config.RequestProcessingLocation;
import com.github.lancethomps.lava.common.web.config.RequestProcessingLocationType;
import com.github.lancethomps.lava.common.web.config.RequestProcessingRule;
import com.github.lancethomps.lava.common.web.config.ResponseHeaderConfig;

public class RequestProcessingManager extends AbstractFileListener {

  private static final Logger LOG = Logger.getLogger(RequestProcessingManager.class);

  private final List<RequestProcessingConfig> configs = new CopyOnWriteArrayList<>();

  private String rootDir = "config/requests/processing/";

  public static boolean doesLocationMatch(@Nonnull final String uri, @Nonnull final RequestProcessingLocation location) {
    switch (location.getType()) {
      case CASE_INSENSITIVE_REGEX:
      case CASE_SENSITIVE_REGEX:
        return location.getRegex().matcher(uri).matches();
      case EXACT_MATCH:
        return uri.equals(location.getLocation());
      case PREFIX_MATCH:
      case PREFIX_MATCH_SKIP_REGEX:
        return StringUtils.startsWith(uri, location.getLocation());
      default:
        throw new IllegalArgumentException("this_should_not_happen");
    }
  }

  public static RequestProcessingLocation findBestLocationMatch(
    @Nonnull HttpServletRequest request, @Nullable WebRequestContext context,
    @Nonnull Collection<RequestProcessingLocation> locations
  ) {
    final String uri = defaultIfBlank(defaultIfBlank(context != null ? context.getResourcePath() : null, request.getRequestURI()), "/");
    RequestProcessingLocation bestPrefixMatch = null;
    for (RequestProcessingLocation location : locations) {
      if (!location.getType().isRegex() && doesLocationMatch(uri, location)) {
        if (location.getType() == RequestProcessingLocationType.EXACT_MATCH) {
          return location;
        }
        if ((bestPrefixMatch == null) || (location.getLocation().length() > bestPrefixMatch.getLocation().length())) {
          bestPrefixMatch = location;
        }
      }
    }
    if ((bestPrefixMatch != null) && (bestPrefixMatch.getType() == RequestProcessingLocationType.PREFIX_MATCH_SKIP_REGEX)) {
      return bestPrefixMatch;
    }
    if ((bestPrefixMatch != null) && (bestPrefixMatch.getConfig().getLocations() != null)) {
      for (RequestProcessingLocation location : bestPrefixMatch.getConfig().getLocations()) {
        if (location.getType().isRegex() && doesLocationMatch(uri, location)) {
          return location;
        }
      }
    }
    for (RequestProcessingLocation location : locations) {
      if (location.getType().isRegex() && doesLocationMatch(uri, location)) {
        return location;
      }
    }
    return bestPrefixMatch;
  }

  public static String getResponseHeaderValue(@Nonnull Object value) {
    if (value instanceof TemporalAccessor) {
      return DateTimeFormatter.RFC_1123_DATE_TIME.format((TemporalAccessor) value);
    }
    return value.toString();
  }

  public void addConfig(@Nonnull String id, @Nonnull RequestProcessingConfig config) {
    removeConfig(id);
    config.setId(id);
    config.deriveInfoRecursively();
    configs.add(config);
    Logs.logTrace(LOG, "Added config for ID [%s].", id);
  }

  @Override
  public void afterBaseDirSet() throws Exception {
    loadAllFilesInDir(ContextUtil.getCpFile(rootDir));
    loadAllFilesInDir(new File(getBaseDir()));
  }

  public List<RequestProcessingConfig> getConfigs() {
    return new ArrayList<>(configs);
  }

  public String getRootDir() {
    return rootDir;
  }

  public void setRootDir(String rootDir) {
    this.rootDir = rootDir;
  }

  @Override
  public void handleFileDelete(File file) {
    removeConfig(FilenameUtils.getBaseName(file.getName()));
  }

  @Override
  public void handleFileLoad(File origFile) {
    File file = null;
    try {
      file = getBaseDir() == null ? origFile : ContextUtil.getConfigFile(origFile.getName(), getBaseDir(), rootDir);
      String id = FilenameUtils.getBaseName(file.getName());
      RequestProcessingConfig config = Serializer.fromUnknown(file, RequestProcessingConfig.class);
      if (config == null) {
        Logs.logError(
          LOG,
          new SerializationException("Deserialization failed."),
          "Deserialization error when parsing request processing file [%s] mapped to best file [%s]",
          origFile,
          file
        );
      } else {
        addConfig(id, config);
      }
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Error deserializing request processing file [%s] mapped to best file [%s]", origFile, file);
    }
  }

  public boolean process(
    @Nonnull HttpServletRequest request,
    @Nonnull HttpServletResponse response,
    @Nullable WebRequestContext context,
    @Nullable Object userInfo
  ) {
    boolean valid = true;
    if (Checks.isEmpty(configs)) {
      return valid;
    }
    RequestProcessingContext exprContext = new RequestProcessingContext(request, response, context, userInfo);
    for (RequestProcessingConfig config : configs) {
      if (!addConfigToResponse(request, response, config, exprContext)) {
        valid = false;
      }
    }
    return valid;
  }

  public boolean removeConfig(@Nonnull String id) {
    boolean removed = configs.removeIf(config -> id.equals(config.getId()));
    Logs.logTrace(LOG, "Remove config result for ID [%s]: %s", id, removed);
    return removed;
  }

  private boolean addConfigToResponse(
    @Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, RequestProcessingConfig config,
    @Nonnull RequestProcessingContext exprContext
  ) {
    boolean valid = true;
    if (config == null) {
      return valid;
    }
    if (config.getClearHeaders() != null) {
      final ResponseWrapper responseWrapper = SpringUtil.getResponseWrapper(response);
      if (responseWrapper != null) {
        for (ResponseHeaderConfig header : config.getClearHeaders()) {
          responseWrapper.ignoreHeader(header.getName());
        }
      } else {
        Logs.logWarn(
          LOG,
          "Could not find ResponseWrapper on HttpServletResponse - clearHeaders cannot be applied without using the ResponseWrapper type."
        );
      }
    }
    if (!processConfigRules(request, response, config, exprContext)) {
      valid = false;
      exprContext.setValid(false);
    }
    if (config.getAddHeaders() != null) {
      for (ResponseHeaderConfig header : config.getAddHeaders()) {
        Object value;
        if (header.getValueExpression() != null) {
          value = ExprFactory.evalOutputExpression(header.getValueExpression(), exprContext);
        } else {
          value = header.getValue();
        }
        if (value != null) {
          response.addHeader(header.getName(), getResponseHeaderValue(value));
        }
      }
    }
    if (config.getSetHeaders() != null) {
      for (ResponseHeaderConfig header : config.getSetHeaders()) {
        Object value;
        if (header.getValueExpression() != null) {
          value = ExprFactory.evalOutputExpression(header.getValueExpression(), exprContext);
        } else {
          value = header.getValue();
        }
        if (value != null) {
          response.setHeader(header.getName(), getResponseHeaderValue(value));
        }
      }
    }
    RequestProcessingLocation locationMatch;
    if ((config.getLocations() != null) &&
      ((locationMatch = findBestLocationMatch(request, exprContext.getContext(), config.getLocations())) != null)) {
      if (!addConfigToResponse(request, response, locationMatch.getConfig(), exprContext)) {
        valid = false;
        exprContext.setValid(false);
      }
    }
    return valid;
  }

  private boolean processConfigRules(
    @Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull RequestProcessingConfig config,
    @Nonnull RequestProcessingContext exprContext
  ) {
    boolean valid = true;
    if (config.getFailWith() != null) {
      if (exprContext.testValid()) {
        try {
          if (config.getFailStatusMessage() != null) {
            response.sendError(config.getFailWith(), config.getFailStatusMessage());
          } else {
            response.sendError(config.getFailWith());
          }
          exprContext.setValid(false);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      Logs.logForSplunk(LOG, "REQUEST_PROCESSING_FOUND_FAILURE", "%s|%s", config.getFailWith(), config.getInfo());
      Logs.logTrace(LOG, "Request processing failure with status code [%s] for config with ID [%s] and info [%s] during request: %s",
        config.getFailWith(), config.getId(), config.getInfo(), exprContext.getContext()
      );
      valid = false;
    }
    if (config.getCookieRules() != null) {
      for (Entry<String, List<RequestProcessingRule>> entry : config.getCookieRules().entrySet()) {
        Cookie cookie = WebUtils.getCookie(request, entry.getKey());
        if (cookie != null) {
          String value = cookie.getValue();
          for (RequestProcessingRule rule : entry.getValue()) {
            if (!Checks.passesWhiteAndBlackListCheck(value, rule.getWhiteList(), rule.getBlackList(), true).getLeft()) {
              if (!addConfigToResponse(request, response, rule.getConfig(), exprContext)) {
                valid = false;
              }
            }
          }
        }
      }
    }
    if (config.getHeaderRules() != null) {
      for (Entry<String, List<RequestProcessingRule>> entry : config.getHeaderRules().entrySet()) {
        Enumeration<String> values = request.getHeaders(entry.getKey());
        if (values != null) {
          while (values.hasMoreElements()) {
            String value = values.nextElement();
            for (RequestProcessingRule rule : entry.getValue()) {
              if (!Checks.passesWhiteAndBlackListCheck(value, rule.getWhiteList(), rule.getBlackList(), true).getLeft()) {
                if (!addConfigToResponse(request, response, rule.getConfig(), exprContext)) {
                  valid = false;
                }
              }
            }
          }
        }
      }
    }
    if (config.getParameterRules() != null) {
      for (Entry<String, List<RequestProcessingRule>> entry : config.getParameterRules().entrySet()) {
        String[] values = request.getParameterValues(entry.getKey());
        if (Checks.isNotEmpty(values)) {
          for (String value : values) {
            for (RequestProcessingRule rule : entry.getValue()) {
              if (!Checks.passesWhiteAndBlackListCheck(value, rule.getWhiteList(), rule.getBlackList(), true).getLeft()) {
                if (!addConfigToResponse(request, response, rule.getConfig(), exprContext)) {
                  valid = false;
                }
              }
            }
          }
        }
      }
    }
    if (config.getContextRules() != null) {
      for (RequestProcessingRule rule : config.getContextRules()) {
        Object exprOutput = ExprFactory.evalOutputExpression(rule.getMatchExpression(), exprContext);
        if ((exprOutput != null) && (exprOutput instanceof Boolean) && ((Boolean) exprOutput).booleanValue()) {
          if (!addConfigToResponse(request, response, rule.getConfig(), exprContext)) {
            valid = false;
          }
        }
      }
    }
    return valid;
  }

}
