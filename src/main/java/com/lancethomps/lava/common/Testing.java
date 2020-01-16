package com.lancethomps.lava.common;

import static java.io.File.separator;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lancethomps.lava.common.collections.MapUtil;
import com.lancethomps.lava.common.compare.Compare;
import com.lancethomps.lava.common.date.Dates;
import com.lancethomps.lava.common.diff.DiffToHtml;
import com.lancethomps.lava.common.diff.domain.DiffFile;
import com.lancethomps.lava.common.file.FileUtil;
import com.lancethomps.lava.common.format.Formatting;
import com.lancethomps.lava.common.lambda.ThrowingFunction;
import com.lancethomps.lava.common.lambda.ThrowingIntConsumer;
import com.lancethomps.lava.common.lambda.ThrowingRunnable;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.merge.Merges;
import com.lancethomps.lava.common.os.OsUtil;
import com.lancethomps.lava.common.properties.PropertyParserHelper;
import com.lancethomps.lava.common.ser.OutputFormat;
import com.lancethomps.lava.common.ser.OutputParams;
import com.lancethomps.lava.common.ser.Serializer;
import com.lancethomps.lava.common.testing.SpeedTest;
import com.lancethomps.lava.common.web.WebRequestContext;

public class Testing {

  public static final OutputParams DEFAULT_DIFF_AS_JSON_PARAMS =
    new OutputParams().setOutputFormat(OutputFormat.json).setPrettifyJson(true).setOrderKeys(true).disableModifications();

  public static final long DEFAULT_SPEED_ITERATIONS = 100_000;

  public static final long DEFAULT_SPEED_WARMUP = 10_000;

  public static final OutputParams PRETTY_JSON_PARAMS =
    new OutputParams().setOutputFormat(OutputFormat.json).setPrettifyJson(true).setOrderKeys(true).setDatesAsStrings(true).disableModifications();

  public static final String PRINT_SEP = Stream.iterate(0, idx -> idx + 1).limit(50).map(idx -> "*").collect(Collectors.joining());

  public static final Pattern PROPERTIES_REGEX = Pattern.compile("^(\\w+)=(.*)$", Pattern.MULTILINE);

  public static final String TMP = defaultIfBlank(
    getenv("TMP"),
    defaultIfBlank(getenv("TEMP"), defaultIfBlank(new File("/tmp").exists() ? "/tmp" : null, getProperty("java.io.tmpdir")))
  );

  public static final String USER = getProperty("user.name");

  public static final String USER_HOME = getenv("HOME");

  public static final String WTP_TESTS_USER = "wtptests";

  private static Properties hostProperties;

  private static String projRootPath;

  public static <T extends Comparable<?>> void assertCollectionElementsEqual(String message, Collection<T> expected, Collection<T> actual) {
    Object[] expectedArray = expected == null ? null : Collect.sort(new ArrayList<>(expected)).toArray();
    Object[] actualArray = actual == null ? null : Collect.sort(new ArrayList<>(actual)).toArray();
    Assert.assertArrayEquals(message, expectedArray, actualArray);
  }

  public static DiffFile compare(Object base, Object other, OutputParams outputParams) {
    return compareAndWriteFiles(base, other, outputParams, null);
  }

  public static DiffFile compareAndWriteFiles(Object base, Object other) {
    return compareAndWriteFiles(base, other, null);
  }

  public static DiffFile compareAndWriteFiles(Object base, Object other, OutputParams outputParams) {
    return compareAndWriteFiles(base, other, outputParams, getHomeFile("Documents/Documents-WTP/compare"));
  }

  public static DiffFile compareAndWriteFiles(Object base, Object other, OutputParams outputParams, @Nullable File baseDir) {
    if (baseDir != null) {
      FileUtil.writeFile(new File(baseDir, "base.json"), Serializer.output(base, outputParams == null ? DEFAULT_DIFF_AS_JSON_PARAMS : outputParams));
      FileUtil.writeFile(
        new File(baseDir, "other.json"),
        Serializer.output(other, outputParams == null ? DEFAULT_DIFF_AS_JSON_PARAMS : outputParams)
      );
    }
    String diff = Compare.diffSerialized(base, other, outputParams == null ? DEFAULT_DIFF_AS_JSON_PARAMS : outputParams, 5);
    DiffFile diffFile = null;
    try {
      DiffToHtml d2h = new DiffToHtml().setDiffInput(diff).generateHtml();
      String compare = d2h.getHtml();
      if (baseDir != null) {
        FileUtil.writeFile(new File(baseDir, "compare.html"), compare);
      }
      if (Checks.isNotEmpty(d2h.getDiffFiles())) {
        diffFile = d2h.getDiffFiles().get(0);
        if (baseDir != null) {
          ObjectNode objectDiffs = Compare.generateDifferences(base, other);
          FileUtil.writeFile(
            new File(baseDir, "compare.json"),
            Serializer.output(objectDiffs, outputParams == null ? DEFAULT_DIFF_AS_JSON_PARAMS : outputParams)
          );
        }
      }
      return diffFile;
    } catch (Throwable e) {
      Logs.logError(LogManager.getLogger(Testing.class), e, "Error");
      return diffFile;
    }
  }

  public static MockHttpServletRequest createMockHttpServletRequest(String method, String uri, String queryString) {
    MockHttpServletRequest mock = new MockHttpServletRequest(Checks.defaultIfNull(method, ""), Checks.defaultIfNull(uri, ""));
    if (Checks.isNotBlank(queryString)) {
      mock.addParameters(MapUtil.createFromQueryString(queryString));
    }
    return mock;
  }

  public static Map<String, String> getBashEnv() {
    Map<String, String> bashEnv = Maps.newTreeMap();
    try {
      String bashLoc = new File("/usr/local/bin/bash").isFile() ? "/usr/local/bin/bash" : "/bin/bash";
      Process process = OsUtil.run(Lists.newArrayList(bashLoc, "-l", "-c", "source ~/.bash_profile && env"), true);
      String output = OsUtil.getProcessOutput(process);
      Matcher matcher = Testing.PROPERTIES_REGEX.matcher(output);
      while (matcher.find()) {
        bashEnv.put(matcher.group(1), matcher.group(2));
      }
    } catch (Throwable e) {
      Logs.logError(LogManager.getLogger(Testing.class), e, "Issue getting bash env!");
    }
    return bashEnv;
  }

  public static File getHomeFile(String path) {
    String fullPath = USER_HOME + separator + path;
    return new File(fullPath);
  }

  public static <T> T getHomeJson(Class<T> clazz) throws Exception {
    return Serializer.fromJson(getHomeJsonFile(), clazz);
  }

  public static File getHomeJsonFile() throws Exception {
    return getHomeFile("test.json");
  }

  public static File getHomeWtpFile(String path) {
    String fullPath = USER_HOME + "/Documents/Documents-WTP/" + path;
    return new File(fullPath);
  }

  public static String getHost(String shortName) {
    if (shortName == null) {
      return null;
    }
    if (hostProperties == null) {
      File hostFile = Testing.getProjFile("scripts/common/env-vars/hosts.properties");
      hostProperties = (hostFile == null) || !hostFile.isFile() ? new Properties() : FileUtil.loadProperties(hostFile);
      if (hostProperties == null) {
        hostProperties = new Properties();
      }
    }
    return hostProperties.getProperty(shortName, shortName.length() > 6 ? shortName : null);
  }

  public static String getJson(Object obj) {
    return getJson(obj, null);
  }

  public static String getJson(Object obj, @Nullable OutputParams addParams) {
    return Serializer.output(
      obj,
      addParams == null ? PRETTY_JSON_PARAMS : Merges.deepMerge(addParams, PRETTY_JSON_PARAMS.copy(), Merges.OVERWRITE_MERGE_CONFIG)
    );
  }

  public static File getProjFile(String relativePath) {
    String projRoot;
    if (Checks.isNotBlank(Testing.projRootPath)) {
      projRoot = Testing.projRootPath;
    } else if (Checks.isNotBlank(System.getenv("PROJ_DIR")) && new File(System.getenv("PROJ_DIR")).isDirectory()) {
      projRoot = System.getenv("PROJ_DIR") + File.separatorChar;
      Logs.logDebug(LogManager.getLogger(Testing.class), "Using PROJ_DIR env variable for project root: %s", projRoot);
      setProjRootPath(projRoot);
    } else {
      projRoot = getProjRootPath();
    }
    return new File(projRoot, relativePath);
  }

  public static String getProjRootPath() {
    if (Testing.projRootPath == null) {
      try {
        URI rootUri = Testing.class.getResource("/").toURI();
        File root = new File(rootUri).getParentFile().getParentFile().getParentFile();
        String projRoot = root.getPath() + File.separatorChar;
        Logs.logDebug(LogManager.getLogger(Testing.class), "Guessed project root from Testing.class: %s", projRoot);
        Testing.setProjRootPath(projRoot);
      } catch (Throwable e) {
      }
    }
    return Testing.projRootPath;
  }

  public static void setProjRootPath(String projRootPath) {
    Testing.projRootPath = projRootPath;
    System.setProperty("myserver.rootPath", FileUtil.fullPath(projRootPath));
  }

  public static File getProjWebResourceFile(String relativePath) {
    return new File(Testing.getProjRootPath() + "src/main/webapp/shared/resources/" + relativePath);
  }

  public static PropertyParserHelper getPropertyParserHelper() {
    return new PropertyParserHelper() {

      @Override
      public Pair<String, File> getMarkdown(String path, Locale locale) {
        File file = Testing.getProjFile(String.format(
          "src/main/resources/shared/templatedata/readme/data/%s/%s",
          locale == null ? "en" : locale.getLanguage(),
          path
        ));
        if (((file == null) || !file.isFile()) && new File(path).isAbsolute()) {
          file = new File(path);
        }
        return (file == null) || !file.isFile() ? null : Pair.of(FileUtil.readFile(file), file);
      }

      @Override
      public Pair<String, String> getPropertyValue(
        WebRequestContext context,
        Locale locale,
        String bundle,
        String defaultValue,
        String labelKey,
        String override,
        String fallbackKey
      ) {
        return null;
      }
    };
  }

  public static <T> T getTestJson(Class<T> type) {
    return getTestJson(Serializer.constructType(type));
  }

  public static <T> T getTestJson(JavaType type) {
    return Serializer.fromJson(getHomeWtpFile("test.json"), type);
  }

  public static File getTmpFile(String path) {
    String fullPath = TMP + separator + path;
    return new File(fullPath);
  }

  public static void logWithSeparator(@Nonnull final Logger logger, final Object message, final Object... formatArgs) {
    Logs.logInfo(logger, PRINT_SEP);
    Logs.logInfo(logger, message.toString(), formatArgs);
    Logs.logInfo(logger, PRINT_SEP);
  }

  public static String loginBash() {
    String bashLoc = new File("/usr/local/bin/bash").isFile() ? "/usr/local/bin/bash" : "/bin/bash";
    try {
      return OsUtil.runAndGetOutput(Lists.newArrayList("exec", bashLoc, "--login"), true);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void overwriteFieldValue(Object val, String fieldName, Object fieldVal) throws Exception {
    Field field = Reflections.getField(val.getClass(), fieldName);
    field.setAccessible(true);
    field.set(val, fieldVal);
  }

  public static <T> T overwriteStandardFields(T bean, Class<?> testingClass, String... ignoreFields) {
    final Set<String> ignoreFieldsFinal = Checks.isEmpty(ignoreFields) ? null : Sets.newHashSet(ignoreFields);
    for (Field field : Reflections.getFieldsWithAnnotation(bean.getClass(), Autowired.class)) {
      String name = field.getName();
      if ((ignoreFieldsFinal != null) && ignoreFieldsFinal.contains(name)) {
        continue;
      }
      try {
        if (!field.isAccessible()) {
          field.setAccessible(true);
        }
        Object currentVal = field.get(bean);
        if (currentVal != null) {
          continue;
        }
        Class<?> beanFieldType = field.getType();
        Field staticField = Stream.of(testingClass.getDeclaredFields()).filter(f -> f.getName().equals(name)).findFirst().orElse(null);
        if (staticField == null) {
          staticField = Stream.of(testingClass.getDeclaredFields()).filter(f -> beanFieldType.isAssignableFrom(f.getType())).findFirst().orElse(null);
        }
        if (staticField != null) {
          Class<?> staticFieldType = staticField.getType();
          if (!beanFieldType.isAssignableFrom(staticFieldType)) {
            Logs.logWarn(
              LogManager.getLogger(Testing.class),
              "Types not compatible: field=%s testingClass=%s beanClass=%s beanFieldType=%s staticFieldType=%s",
              name,
              testingClass.getName(),
              bean.getClass().getName(),
              beanFieldType.getName(),
              staticFieldType.getName()
            );
            continue;
          }
          Method method = testingClass.getDeclaredMethod("get" + StringUtils.capitalize(staticField.getName()));
          if (method != null) {
            Object val = method.invoke(null);
            if (val != null) {
              overwriteFieldValue(bean, name, val);
            }
          }
        }
      } catch (NoSuchFieldException | NoSuchMethodException e) {
        Logs.logTrace(
          LogManager.getLogger(Testing.class),
          e,
          "Standard field/method missing: name=%s testingClass=%s beanClass=%s",
          name,
          testingClass.getName(),
          bean.getClass().getName()
        );
      } catch (Exception e) {
        Logs.logError(
          LogManager.getLogger(Testing.class),
          e,
          "Issue overwriting standard field: name=%s testingClass=%s beanClass=%s",
          name,
          testingClass.getName(),
          bean.getClass().getName()
        );
      }
    }
    return bean;
  }

  public static void printFields(Class<?> type, Class<? extends Annotation> optionalAnnotationType) {
    printFields(type, optionalAnnotationType, null);
  }

  public static void printFields(Class<?> type, Class<? extends Annotation> optionalAnnotationType, Function<Field, String> addInfo) {
    Logs.println(createFieldsInfo(type, optionalAnnotationType, addInfo));
  }

  public static String createFieldsInfo(Class<?> type, Class<? extends Annotation> optionalAnnotationType, Function<Field, String> addInfo) {
    List<String> infos = new ArrayList<>();
    (optionalAnnotationType == null ? Reflections.getFields(type) : Reflections.getFieldsWithAnnotation(type, optionalAnnotationType)).stream()
        .sorted((f1, f2) -> f1.getName().compareTo(f2.getName()))
        .forEach(field -> {
          String info = addInfo == null ? "" : addInfo.apply(field);
          String fmt = addInfo == null ? "%-50s %s%s" : "%-50s %-30s %s";
          infos.add(Formatting.getMessage(fmt, Reflections.getFieldDisplay(field, type), field.getName(), info));
        });
    return StringUtils.join(infos, "\n");
  }

  public static void printJson(Object obj) {
    printJson(obj, (OutputParams) null);
  }

  public static void printYaml(Object obj) {
    printSerialized(obj, new OutputParams().setOutputFormat(OutputFormat.yaml));
  }

  public static void printJson(Object obj, File writeToFile, OutputParams addParams) {
    String json = getJson(obj, addParams);
    if (writeToFile != null) {
      FileUtil.writeFile(writeToFile, json);
    }
    Logs.println(json);
  }

  public static void printJson(Object obj, OutputParams addParams) {
    printJson(obj, addParams, null);
  }

  public static void printJson(Object obj, OutputParams addParams, String logHeader, Object... formatArgs) {
    if (logHeader != null) {
      Testing.printlnWithSeparator(logHeader, formatArgs);
    }
    printJson(obj, false, addParams);
  }

  public static void printJson(Object obj, String logHeader, Object... formatArgs) {
    printJson(obj, null, logHeader, formatArgs);
  }

  public static void printJsonAndWriteToFile(Object obj, File file) {
    printJson(obj, file);
  }

  public static void printJsonAndWriteToTempFile(Object obj) {
    printJson(obj, true);
  }

  public static void printJsonAndWriteToTempFile(Object obj, String fileName) {
    printJson(obj, new File(TMP, defaultIfBlank(fileName, "out.json")));
  }

  public static void printJsonAndWriteToWtpFile(Object obj, String fileName) {
    printJson(obj, getHomeWtpFile(fileName));
  }

  public static void printSerialized(Object obj, OutputParams params) {
    printSerialized(obj, params, false);
  }

  public static void printSerialized(Object obj, OutputParams params, boolean writeToFile) {
    if (params.getPrettifyJson() == null) {
      params.setPrettifyJson(true);
    }
    String serialized = Serializer.output(obj, params);
    Logs.println(serialized);
    if (writeToFile) {
      FileUtil.writeFile(new File(TMP, "out." + (params.getOutputFormat() != null ? params.getOutputFormat().name() : "json")), serialized);
    }
  }

  public static void printSystemProperties() {
    List<String> keys = System.getProperties().keySet().stream().map(Object::toString).sorted().collect(Collectors.toList());
    int padding = keys.stream().mapToInt(String::length).max().getAsInt() + 2;
    keys.stream().forEach(key -> {
      Logs.println("%-" + padding + "s ==> %s", key, System.getProperty(key));
    });
  }

  public static void printTest(Class<?> parent, String test) {
    Logs.println("Running test %s.%s...", parent.getSimpleName(), test);
  }

  public static void printlnWithSeparator(final Object message, final Object... formatArgs) {
    Logs.println(Testing.PRINT_SEP);
    Logs.println(message, formatArgs);
    Logs.println(Testing.PRINT_SEP);
  }

  public static String readHomeFileToString(String path) {
    File file = getHomeFile(path);
    try {
      return FileUtil.readFile(file);
    } catch (Throwable e) {
      Logger log = LogManager.getLogger(Testing.class);
      Logs.logError(log, e, "Could not read file [%s] to string!", file);
    }
    return null;
  }

  public static <T> boolean runAllTests(@Nonnull Class<T> type) {
    return runAllTests(type, true);
  }

  public static <T> boolean runAllTests(@Nonnull Class<T> type, boolean failFast) {
    T tests = ClassUtil.createInstance(type, true);
    List<Method> methods = Stream
      .of(type.getDeclaredMethods())
      .filter(method -> method.isAnnotationPresent(Test.class))
      .filter(method -> method.getParameterCount() == 0)
      .collect(
        Collectors.toList()
      );
    boolean allSuccess = true;
    for (Method method : methods) {
      try {
        method.invoke(tests);
      } catch (Throwable e) {
        if (failFast) {
          Exceptions.sneakyThrow(e);
        }
        allSuccess = false;
        Logs.logError(LogManager.getLogger(Testing.class), e, "Error running test method [%s]", method);
      }
    }
    return allSuccess;
  }

  public static void setBashEnv() {
    Map<String, String> bashEnv = getBashEnv();
    if (Checks.isEmpty(bashEnv)) {
      return;
    }
    try {
      Class<?> processEnv = Class.forName("java.lang.ProcessEnvironment");
      Field theEnvironmentField = processEnv.getDeclaredField("theEnvironment");
      theEnvironmentField.setAccessible(true);
      Map<?, ?> envMap = (Map<?, ?>) theEnvironmentField.get(null);
      Class<?> envVar = Class.forName("java.lang.ProcessEnvironment$Variable");
      Method envVarInit = envVar.getDeclaredMethod("valueOf", byte[].class);
      envVarInit.setAccessible(true);
      Class<?> envVal = Class.forName("java.lang.ProcessEnvironment$Value");
      Method envValInit = envVal.getDeclaredMethod("valueOf", byte[].class);
      envValInit.setAccessible(true);
      Method putMethod = Stream.of(HashMap.class.getDeclaredMethods()).filter(m -> "put".equalsIgnoreCase(m.getName())).findAny().get();
      bashEnv.forEach((k, v) -> {
        try {
          Object key = envVarInit.invoke(null, k.getBytes());
          Object val = envValInit.invoke(null, v.getBytes());
          putMethod.invoke(envMap, key, val);
        } catch (Exception e) {
          Logs.logError(LogManager.getLogger(Testing.class), e, "Issue setting env var with key [%s] and val [%s]!", k, v);
        }
      });
    } catch (Throwable e) {
      Logs.logError(LogManager.getLogger(Testing.class), e, "Issue setting bash env!");
    }
  }

  @SafeVarargs
  public static <T, R> void testFunctionAndPrintJson(Function<T, R> function, T... values) {
    Arrays.stream(values).forEach(val -> printJson(function.apply(val)));
  }

  public static void testRun(Runnable run) {
    run.run();
  }

  public static SpeedTest testSpeed(List<ThrowingIntConsumer> functions) throws Exception {
    return testSpeed(functions, false);
  }

  public static SpeedTest testSpeed(List<ThrowingIntConsumer> functions, boolean reverse) throws Exception {
    return testSpeed(functions, DEFAULT_SPEED_WARMUP, DEFAULT_SPEED_ITERATIONS, reverse);
  }

  public static SpeedTest testSpeed(List<ThrowingIntConsumer> functions, Integer repeatCount) throws Exception {
    return testSpeed(functions, null, DEFAULT_SPEED_WARMUP, DEFAULT_SPEED_ITERATIONS, false, repeatCount);
  }

  public static SpeedTest testSpeed(List<ThrowingIntConsumer> functions, long warmup, long iterations) throws Exception {
    return testSpeed(functions, null, warmup, iterations);
  }

  public static SpeedTest testSpeed(List<ThrowingIntConsumer> functions, long warmup, long iterations, boolean reverse) throws Exception {
    return testSpeed(functions, null, warmup, iterations, reverse);
  }

  public static SpeedTest testSpeed(List<ThrowingIntConsumer> functions, ThrowingIntConsumer betweenFunction, long warmup, long iterations)
    throws Exception {
    return testSpeed(functions, betweenFunction, warmup, iterations, false);
  }

  public static SpeedTest testSpeed(
    List<ThrowingIntConsumer> functions,
    ThrowingIntConsumer betweenFunction,
    long warmup,
    long iterations,
    boolean reverse
  )
    throws Exception {
    return testSpeed(functions, betweenFunction, warmup, iterations, reverse, null);
  }

  @SuppressWarnings("unchecked")
  public static SpeedTest testSpeed(
    @Nonnull List<ThrowingIntConsumer> functions,
    @Nullable ThrowingIntConsumer betweenFunction,
    long warmup,
    long iterations,
    boolean reverse,
    @Nullable Integer repeatCount
  ) throws Exception {
    return testSpeedWithCustomTimer(betweenFunction, warmup, iterations, reverse, repeatCount, functions.stream().map(cons -> {
      ThrowingFunction<Integer, Long> wrapper = (idx) -> {
        cons.accept(idx);
        return null;
      };
      return wrapper;
    }).collect(Collectors.toList()).toArray(new ThrowingFunction[0]));
  }

  public static SpeedTest testSpeed(long warmup, long iterations, @Nonnull ThrowingRunnable... functions) throws Exception {
    return testSpeed(Arrays.asList(functions).stream().map(function -> {
      ThrowingIntConsumer consumer = i -> function.run();
      return consumer;
    }).collect(Collectors.toList()), warmup, iterations);
  }

  public static SpeedTest testSpeed(ThrowingFunction<Integer, Long> function, long warmup, long iterations) throws Exception {
    return testSpeedWithCustomTimer(warmup, iterations, function);
  }

  public static SpeedTest testSpeed(ThrowingIntConsumer function) throws Exception {
    return testSpeed(Arrays.asList(function));
  }

  public static SpeedTest testSpeed(ThrowingIntConsumer function, long warmup, long iterations) throws Exception {
    return testSpeed(Arrays.asList(function), warmup, iterations);
  }

  public static SpeedTest testSpeed(@Nonnull ThrowingRunnable... functions) throws Exception {
    return testSpeed(DEFAULT_SPEED_WARMUP, DEFAULT_SPEED_ITERATIONS, functions);
  }

  @SafeVarargs
  public static SpeedTest testSpeedWithCustomTimer(long warmup, long iterations, @Nonnull ThrowingFunction<Integer, Long>... functions)
    throws Exception {
    return testSpeedWithCustomTimer(null, warmup, iterations, false, null, functions);
  }

  @SafeVarargs
  public static SpeedTest testSpeedWithCustomTimer(@Nonnull ThrowingFunction<Integer, Long>... functions) throws Exception {
    return testSpeedWithCustomTimer(DEFAULT_SPEED_WARMUP, DEFAULT_SPEED_ITERATIONS, functions);
  }

  @SafeVarargs
  public static SpeedTest testSpeedWithCustomTimer(
    @Nullable ThrowingIntConsumer betweenFunction,
    long warmup,
    long iterations,
    boolean reverse,
    @Nullable Integer repeatCount,
    @Nonnull ThrowingFunction<Integer, Long>... functions
  ) throws Exception {
    return new SpeedTest()
      .setBetweenFunction(betweenFunction)
      .setIterations(iterations)
      .setRepeatCount(repeatCount)
      .setReverse(reverse)
      .setWarmup(warmup)
      .addTests(functions)
      .run();
  }

  public static void watchFile(@Nonnull File file, long stopAfter, @Nonnull ThrowingRunnable action) throws Exception {
    long startTime = System.currentTimeMillis();
    long fileTimestamp = file.lastModified();
    while (stopAfter >= (System.currentTimeMillis() - startTime)) {
      if (file.lastModified() != fileTimestamp) {
        Logs.logWarn(
          LogManager.getLogger(Testing.class),
          "File modified! previous=%s updated=%s",
          Dates.fromMillis(fileTimestamp),
          Dates.fromMillis(file.lastModified())
        );
        fileTimestamp = file.lastModified();
        action.run();
      }
      // CHECKSTYLE.OFF: ThreadSleep
      Thread.sleep(1000);
      // CHECKSTYLE.ON: ThreadSleep
    }
  }

  public static void watchFile(@Nonnull File file, @Nonnull ThrowingRunnable action) throws Exception {
    watchFile(file, Long.MAX_VALUE, action);
  }

  public static void writeJsonToHomeWtpFile(Object data, String path) {
    writeToHomeWtpFile(path, getJson(data));
  }

  public static void writeTempJson(Object obj) {
    writeTempJson(obj, null);
  }

  public static void writeTempJson(Object obj, @Nullable String fileName) {
    writeTempJson(obj, fileName, null);
  }

  public static void writeTempJson(Object obj, @Nullable String fileName, @Nullable OutputParams addParams) {
    writeJson(obj, new File(TMP, Checks.defaultIfBlank(fileName, "test.json")), addParams);
  }

  public static void writeJson(Object obj, File file) {
    writeJson(obj, file, null);
  }

  public static void writeJson(Object obj, File file, @Nullable OutputParams addParams) {
    FileUtil.writeFile(file, obj instanceof String ? obj.toString() : getJson(obj, addParams));
  }

  public static void writeToHomeFile(String path, String output) {
    File file = getHomeFile(path);
    FileUtil.writeFile(file, output);
  }

  public static void writeToHomeWtpFile(String path, String output) {
    File file = getHomeWtpFile(path);
    FileUtil.writeFile(file, output);
  }

  private static void printJson(Object obj, boolean writeToFile) {
    printJson(obj, writeToFile, null);
  }

  private static void printJson(Object obj, boolean writeToFile, OutputParams addParams) {
    printJson(obj, writeToFile ? new File(TMP, "out.json") : null, addParams);
  }

  private static void printJson(Object obj, File writeToFile) {
    printJson(obj, writeToFile, null);
  }

}
