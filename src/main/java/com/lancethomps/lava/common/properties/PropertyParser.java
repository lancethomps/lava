package com.lancethomps.lava.common.properties;

import static com.lancethomps.lava.common.Checks.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.Collect;
import com.lancethomps.lava.common.ContextUtil;
import com.lancethomps.lava.common.date.Dates;
import com.lancethomps.lava.common.file.FileUtil;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.web.WebRequestContext;

@SuppressWarnings("rawtypes")
public class PropertyParser {

  public static final String CLIENT_AWARE_SUFFIX = "clientAware";
  public static final char DEFAULT_OVERRIDES_SEP = '.';
  public static final Logger LOG = Logger.getLogger(PropertyParser.class);
  public static final String PROP_ENV_OVERRIDES_SYSTEM_PROP_KEY = "com.lancethomps.lava.common.properties.PropertyParser.propEnvOverrides";
  private static final ConcurrentHashMap<String, BiFunction<String, PropertyResolverConfig, String>> PROPERTY_REPLACERS = new ConcurrentHashMap<>();
  private static PropertyParserHelper defaultPropertyParserHelper;
  private static List<String> propEnvOverrides = Optional
    .ofNullable(System.getProperty(PROP_ENV_OVERRIDES_SYSTEM_PROP_KEY))
    .filter(Checks::isNotBlank)
    .map(Collect::splitCsvAsList)
    .orElseGet(() -> Collections.emptyList());
  private static boolean propEnvOverridesSet;
  private static Pattern propReplaceRegex;

  static {
    registerPropertyReplacer(StandardPropertyType.bundle.name(), PropertyParser::resolvePropertyBundle);
    registerPropertyReplacer(StandardPropertyType.date.name(), PropertyParser::resolvePropertyDate);
    registerPropertyReplacer(StandardPropertyType.env.name(), PropertyParser::resolvePropertyEnv);
    registerPropertyReplacer(StandardPropertyType.expressions_file.name(), PropertyParser::resolvePropertyExpressionsFile);
    registerPropertyReplacer(StandardPropertyType.markdown.name(), PropertyParser::resolvePropertyMarkdown);
    registerPropertyReplacer(StandardPropertyType.param.name(), PropertyParser::resolvePropertyCustomParam);
    registerPropertyReplacer(StandardPropertyType.request_param.name(), PropertyParser::resolvePropertyRequestParam);
    registerPropertyReplacer(StandardPropertyType.system.name(), PropertyParser::resolvePropertySystem);
    try {

      PropertyParser.class.getClassLoader().loadClass("com.lancethomps.lava.common.EnvParser");
    } catch (ClassNotFoundException e) {
    }
  }

  public static boolean checkForEnabledEnvTag(Collection<String> enabledEnvTags) {
    logMissingPropEnvOverrides();
    return (enabledEnvTags != null) && !enabledEnvTags.isEmpty() && getPropEnvOverrides().stream().anyMatch(enabledEnvTags::contains);
  }

  public static boolean checkForEnabledEnvTag(String... enabledEnvTags) {
    return checkForEnabledEnvTag(enabledEnvTags == null ? null : Arrays.asList(enabledEnvTags));
  }

  public static Properties combineProperties(boolean expandValues, String... paths) {
    Properties props = null;
    if ((paths != null) && (paths.length > 0)) {
      for (String path : paths) {
        File file = new File(path);
        if (!file.isFile()) {
          continue;
        }
        if (props == null) {
          props = new Properties();
        }
        try (InputStream is = new FileInputStream(file)) {
          if (expandValues) {
            Properties fileProps = new Properties();
            fileProps.load(is);
            for (Entry<Object, Object> prop : fileProps.entrySet()) {
              props.put(
                parseAndReplaceWithProps(prop.getKey().toString()),
                prop.getValue() == null ? null : parseAndReplaceWithProps(prop.getValue().toString())
              );
            }
          } else {
            props.load(is);
          }
        } catch (IOException e) {
          Logs.logError(LOG, e, "Could not load properties file: path=%s resolvedPath=%s", path, FileUtil.fullPath(file));
        }
      }
    }
    return props;
  }

  public static BiFunction<String, PropertyResolverConfig, String> deregisterPropertyReplacer(@Nonnull String id) {
    BiFunction<String, PropertyResolverConfig, String> existing = PROPERTY_REPLACERS.remove(id);
    updatePropReplaceRegex();
    return existing;
  }

  public static PropertyParserHelper getDefaultPropertyParserHelper() {
    return defaultPropertyParserHelper;
  }

  public static void setDefaultPropertyParserHelper(PropertyParserHelper defaultPropertyParserHelper) {
    PropertyParser.defaultPropertyParserHelper = defaultPropertyParserHelper;
  }

  public static File getEnvSpecificFile(String parentPath, String fileName) throws FileNotFoundException {
    List<String> paths = getPropsKeysWithEnvOverrides(parentPath, '/').stream().map(path -> path + '/' + fileName).collect(Collectors.toList());
    File file = paths.stream().map(ContextUtil::getFile).filter(f -> (f != null) && f.isFile()).findFirst().orElse(null);
    if (file == null) {
      throw new FileNotFoundException(String.format("Could not find env specific file from possible paths %s", paths));
    }
    Logs.logInfo(LOG, "Found env specific file at parent path [%s] with name [%s] ==> [%s]", parentPath, fileName, file);
    return file;
  }

  public static String getEnvSpecificFilePath(String parentPath, String fileName) {
    List<String> paths = getPropsKeysWithEnvOverrides(parentPath, '/').stream().map(path -> path + '/' + fileName).collect(Collectors.toList());
    String loc = paths.stream().filter(path -> {
      File f = ContextUtil.getFile(path);
      return (f != null) && f.isFile();
    }).findFirst().orElse(null);
    if (loc == null) {
      Logs.logError(LOG, new FileNotFoundException(), "Could not find env specific file from possible paths %s", paths);
      loc = parentPath + '/' + fileName;
    } else {
      Logs.logInfo(LOG, "Found env specific file location at parent path [%s] with name [%s] ==> [%s]", parentPath, fileName, loc);
    }
    return loc;
  }

  public static String getEnvSpecificProp(Map props, String key) {
    return getEnvSpecificProp(props, key, DEFAULT_OVERRIDES_SEP);
  }

  public static String getEnvSpecificProp(Map props, String key, char sep) {
    try {
      List<String> overrides = getPropsKeysWithEnvOverrides(null, sep);
      Pair<String, String> resolvedKeyAndVal = getPropertyWithOverrides(props, key, overrides, sep);
      Logs.logDebug(
        LOG,
        "Property with env overrides: key=%s sep=%s resolvedKey=%s value=%s",
        key,
        sep,
        resolvedKeyAndVal.getLeft(),
        resolvedKeyAndVal.getRight()
      );
      return resolvedKeyAndVal.getRight();
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Issue getting property with key [%s].", key);
    }
    return null;
  }

  public static String getEnvSpecificProp(String bundle, String key) {
    return getEnvSpecificProp(FileUtil.loadProperties(ContextUtil.getCpFile("properties/" + bundle + ".properties")), key);
  }

  public static List<String> getKeyVariants(List<String> prefixes, List<String> variants, Character sep) {
    TreeMap<Integer, LinkedHashSet<String>> allKeys = new TreeMap<>();
    prefixes.forEach(keyPrefix -> {
      int index = variants.size();
      allKeys.computeIfAbsent(0, k -> new LinkedHashSet<>()).add(keyPrefix);
      while (index > 0) {
        List<String> subVars = variants.subList(0, index);
        String variant = variants.get(index - 1);
        allKeys.computeIfAbsent(index, k -> new LinkedHashSet<>()).add(keyPrefix + sep + StringUtils.join(subVars, sep));
        if (index > 1) {
          allKeys.computeIfAbsent(2, k -> new LinkedHashSet<>()).add(keyPrefix + sep + variant);
        }
        allKeys.get(0).add(variant);
        index--;
      }
    });
    return allKeys.descendingKeySet().stream().flatMap(key -> allKeys.get(key).stream()).collect(Collectors.toList());
  }

  public static List<String> getPropEnvOverrides() {
    return propEnvOverrides;
  }

  public static void setPropEnvOverrides(@Nonnull List<String> propEnvOverrides) {
    if (propEnvOverridesSet) {
      throw new IllegalArgumentException("propEnvOverrides may only be set once.");
    }
    propEnvOverridesSet = true;
    PropertyParser.propEnvOverrides = Collections.unmodifiableList(propEnvOverrides);
    Logs.logInfo(LOG, "Props key env overrides are set to: %s", propEnvOverrides);
  }

  public static Pattern getPropReplaceRegex() {
    return propReplaceRegex;
  }

  public static String getPropertyValue(String type, String propKey) {
    return getPropertyValue(type, propKey, null);
  }

  public static String getPropertyValue(String type, String propKey, String defaultValue) {
    return getPropertyValue(type, propKey, defaultValue, null, null, false);
  }

  public static String getPropertyValue(final String type, final String propKey, String defaultValue, PropertyResolverConfig config) {
    String resolved = null;
    if ((type != null) && (propKey != null)) {
      String previousRootDir = config.getRootDir();
      BiFunction<String, PropertyResolverConfig, String> resolver = PROPERTY_REPLACERS.get(type);
      if (resolver != null) {
        resolved = resolver.apply(propKey, config);
      }
      if (isBlank(resolved)) {
        if (defaultValue == null) {
          Logs.logWarn(LOG, new IllegalArgumentException(), "Could not find a match for property [%s] of type [%s].", propKey, type);
        } else {
          resolved = defaultValue;
        }
      } else if (config.isRecursively()) {
        resolved = parseAndReplaceWithProps(resolved, config);
      }
      config.setRootDir(previousRootDir);
    }
    return resolved == null ? EMPTY : resolved;
  }

  public static String getPropertyValue(
    String type,
    String propKey,
    String defaultValue,
    WebRequestContext context,
    PropertyParserHelper helper,
    boolean recursively
  ) {
    return getPropertyValue(
      type,
      propKey,
      defaultValue,
      new PropertyResolverConfig().setContext(context).setHelper(helper).setRecursively(recursively)
    );
  }

  public static String getPropertyWithOverride(Map props, String key, Object override) {
    return getPropertyWithOverrides(props, key, override != null ? Lists.newArrayList(override.toString()) : null);
  }

  public static String getPropertyWithOverrides(Map props, String key, List<String> overrides) {
    return getPropertyWithOverrides(props, key, overrides, DEFAULT_OVERRIDES_SEP).getRight();
  }

  public static Pair<String, String> getPropertyWithOverrides(Map props, String key, List<String> overrides, char sep) {
    List<String> keys = Lists.newArrayList();
    if (isNotEmpty(overrides)) {
      overrides.forEach(override -> keys.add(key + sep + override));
    }
    keys.add(key);
    for (String fullKey : keys) {
      String val = (String) props.get(fullKey);
      if (val != null) {
        return Pair.of(fullKey, parseAndReplaceWithProps(val));
      }
    }
    return Pair.of(null, null);
  }

  public static List<String> getPropsKeysWithEnvOverrides(String key, Character sep) {
    logMissingPropEnvOverrides();
    String keyPrefix = isNotBlank(key) ? (key + sep) : "";
    boolean needToReplace = (sep != null) && (sep.charValue() != DEFAULT_OVERRIDES_SEP);
    return Stream
      .concat(
        propEnvOverrides.stream().map(suffix -> keyPrefix + (needToReplace ? StringUtils.replaceChars(suffix, DEFAULT_OVERRIDES_SEP, sep) : suffix)),
        key != null ? Stream.of(key) : Stream.empty()
      )
      .collect(Collectors.toList());
  }

  public static String parseAndReplaceWithProps(String rawVal) {
    return parseAndReplaceWithProps(rawVal, null, null);
  }

  public static String parseAndReplaceWithProps(String rawVal, final PropertyResolverConfig config) {
    if (rawVal != null) {
      Matcher matcher = propReplaceRegex.matcher(rawVal);
      StringBuffer sb = new StringBuffer();
      while (matcher.find()) {
        String replacement;
        if ("\\".equalsIgnoreCase(matcher.group(1))) {
          replacement = matcher.group(0).substring(1);
        } else {
          String type = matcher.group(2);
          String propKey = matcher.group(3);
          replacement = getPropertyValue(type, propKey, matcher.group(4), config);
        }
        matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
      }
      return matcher.appendTail(sb).toString();
    }
    return rawVal;
  }

  public static String parseAndReplaceWithProps(String rawVal, WebRequestContext context, PropertyParserHelper helper) {
    return parseAndReplaceWithProps(rawVal, context, helper, false);
  }

  public static String parseAndReplaceWithProps(String rawVal, WebRequestContext context, PropertyParserHelper helper, boolean recursively) {
    return parseAndReplaceWithProps(rawVal, new PropertyResolverConfig().setContext(context).setHelper(helper).setRecursively(recursively));
  }

  public static PropertyParserKey parsePropertyKey(String rawVal) {
    if (rawVal != null) {
      Matcher matcher = propReplaceRegex.matcher(rawVal);
      while (matcher.find()) {
        String type = matcher.group(2);
        String propKey = matcher.group(3);
        String defaultValue = matcher.group(4);
        String bundle = null;
        if (StandardPropertyType.bundle.name().equalsIgnoreCase(type)) {
          if (StringUtils.contains(propKey, ":")) {
            bundle = StringUtils.substringBefore(propKey, ":");
            propKey = StringUtils.substringAfter(propKey, ":");
          }
        }
        return new PropertyParserKey().setBundle(bundle).setDefaultValue(defaultValue).setPropKey(propKey).setType(type);
      }
    }
    return null;
  }

  public static void registerPropertyReplacer(@Nonnull String id, @Nonnull BiFunction<String, PropertyResolverConfig, String> replacer) {
    if (PROPERTY_REPLACERS.containsKey(id)) {
      throw new IllegalArgumentException("A replacer with that ID has already been registered. Please deregister it first: id=" + id);
    }
    PROPERTY_REPLACERS.put(id, replacer);
    updatePropReplaceRegex();
  }

  private static void logMissingPropEnvOverrides() {
    if (propEnvOverrides.isEmpty()) {
      Logs.logWarn(LOG, new Exception(), "propEnvOverrides is empty - those should be set before calling this method.");
    }
  }

  private static String resolvePropertyBundle(@Nonnull String propKey, PropertyResolverConfig config) {
    if (config.getHelper() != null) {
      final Locale locale = config.getContext() == null ? null : config.getContext().getLocale();
      boolean hasSpecificBundle = StringUtils.contains(propKey, ":");
      String bundle = hasSpecificBundle ? StringUtils.substringBefore(propKey, ":") : PropertyParserHelper.DEFAULT_BUNDLE;
      String i18nKey = hasSpecificBundle ? StringUtils.substringAfter(propKey, ":") : propKey;
      String override = null;
      if ((config.getContext() != null) && (config.getContext().getOriginatingRequestUri() != null)) {
        String[] parts = StringUtils.split(config.getContext().getOriginatingRequestUri());
        override = parts.length > 2 ? parts[2] : parts.length > 1 ? parts[1] : null;
      }
      Pair<String, String> label = config.getHelper().getPropertyValue(
        config.getContext(),
        locale,
        bundle,
        EMPTY,
        i18nKey,
        override,
        null
      );
      return label != null ? label.getRight() : null;
    }
    return null;
  }

  private static String resolvePropertyCustomParam(@Nonnull String propKey, PropertyResolverConfig config) {
    return Optional.ofNullable(config.getCustomParams()).map(params -> params.get(propKey)).map(Object::toString).orElse(null);
  }

  private static String resolvePropertyDate(@Nonnull String propKey, PropertyResolverConfig config) {
    return Dates.toIntString(Dates.parseDate(propKey));
  }

  private static String resolvePropertyEnv(@Nonnull String propKey, PropertyResolverConfig config) {
    return System.getenv(propKey);
  }

  private static String resolvePropertyExpressionsFile(@Nonnull String propKey, PropertyResolverConfig config) {
    if (config.getHelper() != null) {
      Pair<String, File> pair = config.getHelper().getExpressionsFile(propKey);
      if (((pair == null) || (pair.getLeft() == null)) && (config.getRootDir() != null)) {
        pair = config.getHelper().getExpressionsFile(config.getRootDir() + File.separatorChar + propKey);
      }
      if ((pair != null) && (pair.getLeft() != null)) {
        config = config.copyWithNewRootDir(pair.getRight() == null ? config.getRootDir() : FileUtil.fullPath(pair.getRight().getParentFile()));
        return pair.getLeft();
      }
    }
    return null;
  }

  private static String resolvePropertyMarkdown(@Nonnull String propKey, PropertyResolverConfig config) {
    if (config.getHelper() != null) {
      final Locale locale = config.getContext() == null ? null : config.getContext().getLocale();
      Pair<String, File> md = config.getHelper().getMarkdown(propKey, locale);
      if (((md == null) || (md.getLeft() == null)) && (config.getRootDir() != null)) {
        md = config.getHelper().getMarkdown(config.getRootDir() + File.separatorChar + propKey, locale);
      }
      if ((md != null) && (md.getLeft() != null)) {
        config.setRootDir(md.getRight() == null ? config.getRootDir() : FileUtil.fullPath(md.getRight().getParentFile()));
        return md.getLeft();
      }
    }
    return null;
  }

  private static String resolvePropertyRequestParam(@Nonnull String propKey, PropertyResolverConfig config) {
    return Optional.ofNullable(config.getContext()).map(context -> context.getRequestParameter(propKey)).map(Object::toString).orElse(null);
  }

  private static String resolvePropertySystem(@Nonnull String propKey, PropertyResolverConfig config) {
    return System.getProperty(propKey);
  }

  private static void updatePropReplaceRegex() {
    Pattern updated = Pattern.compile(
      "(?i)(\\\\)?\\$\\{("
        + PROPERTY_REPLACERS.keySet().stream().sorted().map(Pattern::quote).collect(Collectors.joining("|"))
        + ")[\\.\\:]([a-z0-9\\.\\:\\/_-]+)\\?{0,1}'{0,1}(.*?)'{0,1}\\}"
    );
    propReplaceRegex = updated;
  }

}
