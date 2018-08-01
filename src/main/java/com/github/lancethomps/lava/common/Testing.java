package com.github.lancethomps.lava.common;

import static com.github.lancethomps.lava.common.logging.Logs.println;
import static com.github.lancethomps.lava.common.ser.OutputFormat.json;
import static java.io.File.separator;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.io.File;
import java.io.IOException;
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
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.lancethomps.lava.common.collections.MapUtil;
import com.github.lancethomps.lava.common.compare.Compare;
import com.github.lancethomps.lava.common.date.Dates;
import com.github.lancethomps.lava.common.diff.DiffToHtml;
import com.github.lancethomps.lava.common.diff.domain.DiffFile;
import com.github.lancethomps.lava.common.file.FileUtil;
import com.github.lancethomps.lava.common.lambda.ThrowingFunction;
import com.github.lancethomps.lava.common.lambda.ThrowingIntConsumer;
import com.github.lancethomps.lava.common.lambda.ThrowingRunnable;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.merge.Merges;
import com.github.lancethomps.lava.common.os.OsUtil;
import com.github.lancethomps.lava.common.properties.PropertyParserHelper;
import com.github.lancethomps.lava.common.ser.OutputParams;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.testing.SpeedTest;
import com.github.lancethomps.lava.common.web.WebRequestContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The Class TestingUtil.
 *
 * @author lancethomps
 */
public class Testing {

	/** The Constant DEFAULT_DIFF_AS_JSON_PARAMS. */
	public static final OutputParams DEFAULT_DIFF_AS_JSON_PARAMS = new OutputParams().setOutputFormat(json).setPrettifyJson(true).setOrderKeys(true).disableModifications();

	/** The Constant DEFAULT_SPEED_ITERATIONS. */
	public static final long DEFAULT_SPEED_ITERATIONS = 100000;

	/** The Constant DEFAULT_SPEED_WARMUP. */
	public static final long DEFAULT_SPEED_WARMUP = 10000;

	/** The Constant PRETTY_JSON_PARAMS. */
	public static final OutputParams PRETTY_JSON_PARAMS = new OutputParams().setOutputFormat(json).setPrettifyJson(true).setOrderKeys(true).setDatesAsStrings(true).disableModifications();

	/** The Constant PRINT_SEP. */
	public static final String PRINT_SEP = Stream.iterate(0, idx -> idx + 1).limit(50).map(idx -> "*").collect(Collectors.joining());

	/** The Constant PROPERTIES_REGEX. */
	public static final Pattern PROPERTIES_REGEX = Pattern.compile("^(\\w+)=(.*)$", Pattern.MULTILINE);

	/** The Constant TMP. */
	public static final String TMP = defaultIfBlank(
		getenv("TMP"),
		defaultIfBlank(getenv("TEMP"), defaultIfBlank(new File("/tmp").exists() ? "/tmp" : null, getProperty("java.io.tmpdir")))
	);

	/** The Constant USER. */
	public static final String USER = getProperty("user.name");

	/** The Constant USER_HOME. */
	public static final String USER_HOME = getenv("HOME");

	/** The Constant WTP_TESTS_USER. */
	public static final String WTP_TESTS_USER = "wtptests";

	/** The host properties. */
	private static Properties hostProperties;

	/** The proj root path. */
	private static String projRootPath;

	/**
	 * Assert collection elements equal.
	 *
	 * @param <T> the generic type
	 * @param message the message
	 * @param expected the expected
	 * @param actual the actual
	 */
	public static <T extends Comparable<?>> void assertCollectionElementsEqual(String message, Collection<T> expected, Collection<T> actual) {
		Object[] expectedArray = expected == null ? null : Collect.sort(new ArrayList<>(expected)).toArray();
		Object[] actualArray = actual == null ? null : Collect.sort(new ArrayList<>(actual)).toArray();
		Assert.assertArrayEquals(message, expectedArray, actualArray);
	}

	/**
	 * Compare.
	 *
	 * @param base the base
	 * @param other the other
	 * @param outputParams the output params
	 * @return the diff file
	 */
	public static DiffFile compare(Object base, Object other, OutputParams outputParams) {
		return compareAndWriteFiles(base, other, outputParams, null);
	}

	/**
	 * Compare and write files.
	 *
	 * @param base the base
	 * @param other the other
	 * @return true, if successful
	 */
	public static DiffFile compareAndWriteFiles(Object base, Object other) {
		return compareAndWriteFiles(base, other, null);
	}

	/**
	 * Compare and write files.
	 *
	 * @param base the base
	 * @param other the other
	 * @param outputParams the output params
	 * @return true, if successful
	 */
	public static DiffFile compareAndWriteFiles(Object base, Object other, OutputParams outputParams) {
		return compareAndWriteFiles(base, other, outputParams, getHomeFile("Documents/Documents-WTP/compare"));
	}

	/**
	 * Compare and write files.
	 *
	 * @param base the base
	 * @param other the other
	 * @param outputParams the output params
	 * @param baseDir the base dir
	 * @return true, if successful
	 */
	public static DiffFile compareAndWriteFiles(Object base, Object other, OutputParams outputParams, @Nullable File baseDir) {
		if (baseDir != null) {
			FileUtil.writeFile(new File(baseDir, "base.json"), Serializer.output(base, outputParams == null ? DEFAULT_DIFF_AS_JSON_PARAMS : outputParams));
			FileUtil.writeFile(new File(baseDir, "other.json"), Serializer.output(other, outputParams == null ? DEFAULT_DIFF_AS_JSON_PARAMS : outputParams));
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
					FileUtil.writeFile(new File(baseDir, "compare.json"), Serializer.output(objectDiffs, outputParams == null ? DEFAULT_DIFF_AS_JSON_PARAMS : outputParams));
				}
			}
			return diffFile;
		} catch (Throwable e) {
			Logs.logError(Logger.getLogger(Testing.class), e, "Error");
			return diffFile;
		}
	}

	/**
	 * Creates the mock http servlet request.
	 *
	 * @param method the method
	 * @param uri the uri
	 * @param queryString the query string
	 * @return the mock http servlet request
	 */
	public static MockHttpServletRequest createMockHttpServletRequest(String method, String uri, String queryString) {
		MockHttpServletRequest mock = new MockHttpServletRequest(Checks.defaultIfNull(method, ""), Checks.defaultIfNull(uri, ""));
		if (Checks.isNotBlank(queryString)) {
			mock.addParameters(MapUtil.createFromQueryString(queryString));
		}
		return mock;
	}

	/**
	 * Gets the bash env.
	 *
	 * @return the bash env
	 */
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
			Logs.logError(Logger.getLogger(Testing.class), e, "Issue getting bash env!");
		}
		return bashEnv;
	}

	/**
	 * Gets the home file.
	 *
	 * @param path the path
	 * @return the home file
	 */
	public static File getHomeFile(String path) {
		String fullPath = USER_HOME + separator + path;
		return new File(fullPath);
	}

	/**
	 * Reads the file at $HOME/test.json.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @return the home json
	 * @throws Exception the exception
	 */
	public static <T> T getHomeJson(Class<T> clazz) throws Exception {
		return Serializer.fromJson(getHomeJsonFile(), clazz);
	}

	/**
	 * Gets the file at $HOME/test.json.
	 *
	 * @return the home json file
	 * @throws Exception the exception
	 */
	public static File getHomeJsonFile() throws Exception {
		return getHomeFile("test.json");
	}

	/**
	 * Gets the home wtp file.
	 *
	 * @param path the path
	 * @return the home wtp file
	 */
	public static File getHomeWtpFile(String path) {
		String fullPath = USER_HOME + "/Documents/Documents-WTP/" + path;
		return new File(fullPath);
	}

	/**
	 * Gets the host.
	 *
	 * @param shortName the short name
	 * @return the host
	 */
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

	/**
	 * Gets the json.
	 *
	 * @param obj the obj
	 * @return the json
	 */
	public static String getJson(Object obj) {
		return getJson(obj, null);
	}

	/**
	 * Gets the json.
	 *
	 * @param obj the obj
	 * @param addParams the add params
	 * @return the json
	 */
	public static String getJson(Object obj, @Nullable OutputParams addParams) {
		return Serializer.output(obj, addParams == null ? PRETTY_JSON_PARAMS : Merges.deepMerge(addParams, PRETTY_JSON_PARAMS.copy(), Merges.OVERWRITE_MERGE_CONFIG));
	}

	/**
	 * Gets the proj file.
	 *
	 * @param relativePath the relative path
	 * @return the proj file
	 */
	public static File getProjFile(String relativePath) {
		return new File(StringUtils.defaultIfBlank(Testing.getProjRootPath(), System.getenv("PROJ_DIR") + File.separatorChar) + relativePath);
	}

	/**
	 * Gets the proj root path.
	 *
	 * @return the proj root path
	 */
	public static String getProjRootPath() {
		if (Testing.projRootPath == null) {
			try {
				URI rootUri = Testing.class.getResource("/").toURI();
				File root = new File(rootUri).getParentFile().getParentFile().getParentFile().getParentFile();
				Testing.setProjRootPath(root.getPath() + File.separatorChar);
			} catch (Throwable e) {
				;
			}
		}
		return Testing.projRootPath;
	}

	/**
	 * Gets the proj web resource file.
	 *
	 * @param relativePath the relative path
	 * @return the proj web resource file
	 */
	public static File getProjWebResourceFile(String relativePath) {
		return new File(Testing.getProjRootPath() + "src/main/webapp/shared/resources/" + relativePath);
	}

	/**
	 * Gets the property parser helper.
	 *
	 * @return the property parser helper
	 */
	public static PropertyParserHelper getPropertyParserHelper() {
		return new PropertyParserHelper() {

			@Override
			public Pair<String, File> getMarkdown(String path, Locale locale) {
				File file = Testing.getProjFile(String.format("src/main/resources/shared/templatedata/readme/data/%s/%s", locale == null ? "en" : locale.getLanguage(), path));
				if (((file == null) || !file.isFile()) && new File(path).isAbsolute()) {
					file = new File(path);
				}
				return (file == null) || !file.isFile() ? null : Pair.of(FileUtil.readFile(file), file);
			}

			@Override
			public Pair<String, String> getPropertyValue(WebRequestContext context, Locale locale, String bundle, String defaultValue, String labelKey, String override, String fallbackKey) {
				return null;
			}
		};
	}

	/**
	 * Gets the test json.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @return the test json
	 */
	public static <T> T getTestJson(Class<T> type) {
		return getTestJson(Serializer.constructType(type));
	}

	/**
	 * Gets the test json.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @return the test json
	 */
	public static <T> T getTestJson(JavaType type) {
		return Serializer.fromJson(getHomeWtpFile("test.json"), type);
	}

	/**
	 * Gets the tmp file.
	 *
	 * @param path the path
	 * @return the tmp file
	 */
	public static File getTmpFile(String path) {
		String fullPath = TMP + separator + path;
		return new File(fullPath);
	}

	/**
	 * Login bash.
	 *
	 * @return the string
	 */
	public static String loginBash() {
		String bashLoc = new File("/usr/local/bin/bash").isFile() ? "/usr/local/bin/bash" : "/bin/bash";
		try {
			return OsUtil.runAndGetOutput(Lists.newArrayList("exec", bashLoc, "--login"), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Log with separator.
	 *
	 * @param logger the logger
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logWithSeparator(@Nonnull final Logger logger, final Object message, final Object... formatArgs) {
		Logs.logInfo(logger, PRINT_SEP);
		Logs.logInfo(logger, message.toString(), formatArgs);
		Logs.logInfo(logger, PRINT_SEP);
	}

	/**
	 * Overwrite field value.
	 *
	 * @param val the val
	 * @param fieldName the field name
	 * @param fieldVal the field val
	 * @throws Exception the exception
	 */
	public static void overwriteFieldValue(Object val, String fieldName, Object fieldVal) throws Exception {
		Field field = Reflections.getField(val.getClass(), fieldName);
		field.setAccessible(true);
		field.set(val, fieldVal);
	}

	/**
	 * Overwrite standard fields.
	 *
	 * @param <T> the generic type
	 * @param bean the bean
	 * @param testingClass the testing class
	 * @param ignoreFields the ignore fields
	 * @return the t
	 */
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
							Logger.getLogger(Testing.class),
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
					Logger.getLogger(Testing.class),
					e,
					"Standard field/method missing: name=%s testingClass=%s beanClass=%s",
					name,
					testingClass.getName(),
					bean.getClass().getName()
				);
			} catch (Exception e) {
				Logs.logError(
					Logger.getLogger(Testing.class),
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

	/**
	 * Prints the json.
	 *
	 * @param obj the obj
	 */
	public static void printJson(Object obj) {
		printJson(obj, (OutputParams) null);
	}

	/**
	 * Prints the json.
	 *
	 * @param obj the obj
	 * @param addParams the add params
	 */
	public static void printJson(Object obj, OutputParams addParams) {
		printJson(obj, addParams, null);
	}

	/**
	 * Prints the json.
	 *
	 * @param obj the obj
	 * @param addParams the add params
	 * @param logHeader the log header
	 * @param formatArgs the format args
	 */
	public static void printJson(Object obj, OutputParams addParams, String logHeader, Object... formatArgs) {
		if (logHeader != null) {
			Testing.printlnWithSeparator(logHeader, formatArgs);
		}
		printJson(obj, false, addParams);
	}

	/**
	 * Prints the json.
	 *
	 * @param obj the obj
	 * @param logHeader the log header
	 * @param formatArgs the format args
	 */
	public static void printJson(Object obj, String logHeader, Object... formatArgs) {
		printJson(obj, (OutputParams) null, logHeader, formatArgs);
	}

	/**
	 * Prints the json and write to file.
	 *
	 * @param obj the obj
	 * @param file the file
	 */
	public static void printJsonAndWriteToFile(Object obj, File file) {
		printJson(obj, file);
	}

	/**
	 * Prints the json and write to temp file.
	 *
	 * @param obj the obj
	 */
	public static void printJsonAndWriteToTempFile(Object obj) {
		printJson(obj, true);
	}

	/**
	 * Prints the json and write to temp file.
	 *
	 * @param obj the obj
	 * @param fileName the file name
	 */
	public static void printJsonAndWriteToTempFile(Object obj, String fileName) {
		printJson(obj, new File(TMP, defaultIfBlank(fileName, "out.json")));
	}

	/**
	 * Prints the json and write to wtp file.
	 *
	 * @param obj the obj
	 * @param fileName the file name
	 */
	public static void printJsonAndWriteToWtpFile(Object obj, String fileName) {
		printJson(obj, getHomeWtpFile(fileName));
	}

	/**
	 * Println with separator.
	 *
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void printlnWithSeparator(final Object message, final Object... formatArgs) {
		Logs.println(Testing.PRINT_SEP);
		Logs.println(message, formatArgs);
		Logs.println(Testing.PRINT_SEP);
	}

	/**
	 * Prints the serializaed.
	 *
	 * @param obj the obj
	 * @param params the params
	 */
	public static void printSerialized(Object obj, OutputParams params) {
		printSerialized(obj, params, false);
	}

	/**
	 * Prints the serializaed.
	 *
	 * @param obj the obj
	 * @param params the params
	 * @param writeToFile the write to file
	 */
	public static void printSerialized(Object obj, OutputParams params, boolean writeToFile) {
		if (params.getPrettifyJson() == null) {
			params.setPrettifyJson(true);
		}
		String serialized = Serializer.output(obj, params);
		println(serialized);
		if (writeToFile) {
			FileUtil.writeFile(new File(TMP, "out." + (params.getOutputFormat() != null ? params.getOutputFormat().name() : "json")), serialized);
		}
	}

	/**
	 * Prints the properties.
	 */
	public static void printSystemProperties() {
		List<String> keys = System.getProperties().keySet().stream().map(Object::toString).sorted().collect(Collectors.toList());
		int padding = keys.stream().mapToInt(String::length).max().getAsInt() + 2;
		keys.stream().forEach(key -> {
			Logs.println("%-" + padding + "s ==> %s", key, System.getProperty(key));
		});
	}

	/**
	 * Prints the test.
	 *
	 * @param parent the parent
	 * @param test the test
	 */
	public static void printTest(Class<?> parent, String test) {
		Logs.println("Running test %s.%s...", parent.getSimpleName(), test);
	}

	/**
	 * Read home file to string.
	 *
	 * @param path the path
	 * @return the string
	 */
	public static String readHomeFileToString(String path) {
		File file = getHomeFile(path);
		try {
			return FileUtil.readFile(file);
		} catch (Throwable e) {
			Logger log = Logger.getLogger(Testing.class);
			Logs.logError(log, e, "Could not read file [%s] to string!", file);
		}
		return null;
	}

	/**
	 * Run all tests.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @return true, if successful
	 */
	public static <T> boolean runAllTests(@Nonnull Class<T> type) {
		return runAllTests(type, true);
	}

	/**
	 * Run all tests.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param failFast the fail fast
	 * @return true, if successful
	 */
	public static <T> boolean runAllTests(@Nonnull Class<T> type, boolean failFast) {
		T tests = ClassUtil.createInstance(type, true);
		List<Method> methods = Stream.of(type.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(Test.class)).filter(method -> method.getParameterCount() == 0).collect(
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
				Logs.logError(Logger.getLogger(Testing.class), e, "Error running test method [%s]", method);
			}
		}
		return allSuccess;
	}

	/**
	 * Sets the bash env.
	 */
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
					Logs.logError(Logger.getLogger(Testing.class), e, "Issue setting env var with key [%s] and val [%s]!", k, v);
				}
			});
		} catch (Throwable e) {
			Logs.logError(Logger.getLogger(Testing.class), e, "Issue setting bash env!");
		}
	}

	/**
	 * Sets the proj root path.
	 *
	 * @param projRootPath the projRootPath to set
	 */
	public static void setProjRootPath(String projRootPath) {
		Testing.projRootPath = projRootPath;
		System.setProperty("myserver.rootPath", FileUtil.fullPath(projRootPath));
	}

	/**
	 * Test consumer.
	 *
	 * @param <T> the generic type
	 * @param <R> the generic type
	 * @param function the function
	 * @param values the values
	 */
	@SafeVarargs
	public static <T, R> void testFunctionAndPrintJson(Function<T, R> function, T... values) {
		Arrays.stream(values).forEach(val -> printJson(function.apply(val)));
	}

	/**
	 * Test run.
	 *
	 * @param run the run
	 */
	public static void testRun(Runnable run) {
		run.run();
	}

	/**
	 * Test speed.
	 *
	 * @param functions the functions
	 * @return the map
	 * @throws Exception the exception
	 */
	public static SpeedTest testSpeed(List<ThrowingIntConsumer> functions) throws Exception {
		return testSpeed(functions, false);
	}

	/**
	 * Test speed.
	 *
	 * @param functions the functions
	 * @param reverse the reverse
	 * @return the map
	 * @throws Exception the exception
	 */
	public static SpeedTest testSpeed(List<ThrowingIntConsumer> functions, boolean reverse) throws Exception {
		return testSpeed(functions, DEFAULT_SPEED_WARMUP, DEFAULT_SPEED_ITERATIONS, reverse);
	}

	/**
	 * Test speed.
	 *
	 * @param functions the functions
	 * @param repeatCount the repeat count
	 * @return the map
	 * @throws Exception the exception
	 */
	public static SpeedTest testSpeed(List<ThrowingIntConsumer> functions, Integer repeatCount) throws Exception {
		return testSpeed(functions, null, DEFAULT_SPEED_WARMUP, DEFAULT_SPEED_ITERATIONS, false, repeatCount);
	}

	/**
	 * Test speed.
	 *
	 * @param functions the functions
	 * @param warmup the warmup
	 * @param iterations the iterations
	 * @return the map
	 * @throws Exception the exception
	 */
	public static SpeedTest testSpeed(List<ThrowingIntConsumer> functions, long warmup, long iterations) throws Exception {
		return testSpeed(functions, null, warmup, iterations);
	}

	/**
	 * Test speed.
	 *
	 * @param functions the functions
	 * @param warmup the warmup
	 * @param iterations the iterations
	 * @param reverse the reverse
	 * @return the map
	 * @throws Exception the exception
	 */
	public static SpeedTest testSpeed(List<ThrowingIntConsumer> functions, long warmup, long iterations, boolean reverse) throws Exception {
		return testSpeed(functions, null, warmup, iterations, reverse);
	}

	/**
	 * Test speed.
	 *
	 * @param functions the functions
	 * @param betweenFunction the between function
	 * @param warmup the warmup
	 * @param iterations the iterations
	 * @return the map
	 * @throws Exception the exception
	 */
	public static SpeedTest testSpeed(List<ThrowingIntConsumer> functions, ThrowingIntConsumer betweenFunction, long warmup, long iterations) throws Exception {
		return testSpeed(functions, betweenFunction, warmup, iterations, false);
	}

	/**
	 * Test speed.
	 *
	 * @param functions the functions
	 * @param betweenFunction the between function
	 * @param warmup the warmup
	 * @param iterations the iterations
	 * @param reverse the reverse
	 * @return the map
	 * @throws Exception the exception
	 */
	public static SpeedTest testSpeed(List<ThrowingIntConsumer> functions, ThrowingIntConsumer betweenFunction, long warmup, long iterations, boolean reverse)
		throws Exception {
		return testSpeed(functions, betweenFunction, warmup, iterations, reverse, null);
	}

	/**
	 * Test speed.
	 *
	 * @param functions the functions
	 * @param betweenFunction the between function
	 * @param warmup the warmup
	 * @param iterations the iterations
	 * @param reverse the reverse
	 * @param repeatCount the repeat count
	 * @return the map
	 * @throws Exception the exception
	 */
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

	/**
	 * Test speed.
	 *
	 * @param warmup the warmup
	 * @param iterations the iterations
	 * @param functions the functions
	 * @return the map
	 * @throws Exception the exception
	 */
	public static SpeedTest testSpeed(long warmup, long iterations, @Nonnull ThrowingRunnable... functions) throws Exception {
		return testSpeed(Arrays.asList(functions).stream().map(function -> {
			ThrowingIntConsumer consumer = i -> function.run();
			return consumer;
		}).collect(Collectors.toList()), warmup, iterations);
	}

	/**
	 * Test speed.
	 *
	 * @param function the function
	 * @param warmup the warmup
	 * @param iterations the iterations
	 * @return the map
	 * @throws Exception the exception
	 */
	public static SpeedTest testSpeed(ThrowingFunction<Integer, Long> function, long warmup, long iterations) throws Exception {
		return testSpeedWithCustomTimer(warmup, iterations, function);
	}

	/**
	 * Test speed.
	 *
	 * @param function the function
	 * @return the map
	 * @throws Exception the exception
	 */
	public static SpeedTest testSpeed(ThrowingIntConsumer function) throws Exception {
		return testSpeed(Arrays.asList(function));
	}

	/**
	 * Test speed.
	 *
	 * @param function the function
	 * @param warmup the warmup
	 * @param iterations the iterations
	 * @return the map
	 * @throws Exception the exception
	 */
	public static SpeedTest testSpeed(ThrowingIntConsumer function, long warmup, long iterations) throws Exception {
		return testSpeed(Arrays.asList(function), warmup, iterations);
	}

	/**
	 * Test speed.
	 *
	 * @param functions the functions
	 * @return the map
	 * @throws Exception the exception
	 */
	public static SpeedTest testSpeed(@Nonnull ThrowingRunnable... functions) throws Exception {
		return testSpeed(DEFAULT_SPEED_WARMUP, DEFAULT_SPEED_ITERATIONS, functions);
	}

	/**
	 * Test speed with custom timer.
	 *
	 * @param warmup the warmup
	 * @param iterations the iterations
	 * @param functions the functions
	 * @return the map
	 * @throws Exception the exception
	 */
	@SafeVarargs
	public static SpeedTest testSpeedWithCustomTimer(long warmup, long iterations, @Nonnull ThrowingFunction<Integer, Long>... functions) throws Exception {
		return testSpeedWithCustomTimer(null, warmup, iterations, false, null, functions);
	}

	/**
	 * Test speed with custom timer.
	 *
	 * @param functions the functions
	 * @return the map
	 * @throws Exception the exception
	 */
	@SafeVarargs
	public static SpeedTest testSpeedWithCustomTimer(@Nonnull ThrowingFunction<Integer, Long>... functions) throws Exception {
		return testSpeedWithCustomTimer(DEFAULT_SPEED_WARMUP, DEFAULT_SPEED_ITERATIONS, functions);
	}

	/**
	 * Test speed with custom timer.
	 *
	 * @param betweenFunction the between function
	 * @param warmup the warmup
	 * @param iterations the iterations
	 * @param reverse the reverse
	 * @param repeatCount the repeat count
	 * @param functions the functions
	 * @return the map
	 * @throws Exception the exception
	 */
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

	/**
	 * Watch file.
	 *
	 * @param file the file
	 * @param stopAfter the stop after
	 * @param action the action
	 * @throws Exception the exception
	 */
	public static void watchFile(@Nonnull File file, long stopAfter, @Nonnull ThrowingRunnable action) throws Exception {
		long startTime = System.currentTimeMillis();
		long fileTimestamp = file.lastModified();
		while (stopAfter >= (System.currentTimeMillis() - startTime)) {
			if (file.lastModified() != fileTimestamp) {
				Logs.logWarn(Logger.getLogger(Testing.class), "File modified! previous=%s updated=%s", Dates.fromMillis(fileTimestamp), Dates.fromMillis(file.lastModified()));
				fileTimestamp = file.lastModified();
				action.run();
			}
			// CHECKSTYLE.OFF: ThreadSleep
			Thread.sleep(1000);
			// CHECKSTYLE.ON: ThreadSleep
		}
	}

	/**
	 * Watch file.
	 *
	 * @param file the file
	 * @param action the action
	 * @throws Exception the exception
	 */
	public static void watchFile(@Nonnull File file, @Nonnull ThrowingRunnable action) throws Exception {
		watchFile(file, Long.MAX_VALUE, action);
	}

	/**
	 * Write json to home wtp file.
	 *
	 * @param data the data
	 * @param path the path
	 */
	public static void writeJsonToHomeWtpFile(Object data, String path) {
		writeToHomeWtpFile(path, getJson(data));
	}

	/**
	 * Write temp json.
	 *
	 * @param obj the obj
	 */
	public static void writeTempJson(Object obj) {
		writeTempJson(obj, null);
	}

	/**
	 * Write temp json.
	 *
	 * @param obj the obj
	 * @param fileName the file name
	 */
	public static void writeTempJson(Object obj, @Nullable String fileName) {
		writeTempJson(obj, fileName, null);
	}

	/**
	 * Write temp json.
	 *
	 * @param obj the obj
	 * @param fileName the file name
	 * @param addParams the add params
	 */
	public static void writeTempJson(Object obj, @Nullable String fileName, @Nullable OutputParams addParams) {
		FileUtil.writeFile(new File(TMP, Checks.defaultIfBlank(fileName, "test.json")), obj instanceof String ? obj.toString() : getJson(obj, addParams));
	}

	/**
	 * Write to home file.
	 *
	 * @param path the path
	 * @param output the output
	 */
	public static void writeToHomeFile(String path, String output) {
		File file = getHomeFile(path);
		FileUtil.writeFile(file, output);
	}

	/**
	 * Write to home wtp file.
	 *
	 * @param path the path
	 * @param output the output
	 */
	public static void writeToHomeWtpFile(String path, String output) {
		File file = getHomeWtpFile(path);
		FileUtil.writeFile(file, output);
	}

	/**
	 * Prints the json.
	 *
	 * @param obj the obj
	 * @param writeToFile the write to file
	 */
	private static void printJson(Object obj, boolean writeToFile) {
		printJson(obj, writeToFile, null);
	}

	/**
	 * Prints the json.
	 *
	 * @param obj the obj
	 * @param writeToFile the write to file
	 * @param addParams the add params
	 */
	private static void printJson(Object obj, boolean writeToFile, OutputParams addParams) {
		printJson(obj, writeToFile ? new File(TMP, "out.json") : null, addParams);
	}

	/**
	 * Prints the json.
	 *
	 * @param obj the obj
	 * @param writeToFile the write to file
	 */
	private static void printJson(Object obj, File writeToFile) {
		printJson(obj, writeToFile, null);
	}

	/**
	 * Prints the json.
	 *
	 * @param obj the obj
	 * @param writeToFile the write to file
	 * @param addParams the add params
	 */
	private static void printJson(Object obj, File writeToFile, OutputParams addParams) {
		String json = getJson(obj, addParams);
		if (writeToFile != null) {
			FileUtil.writeFile(writeToFile, json);
		}
		println(json);
	}
}
