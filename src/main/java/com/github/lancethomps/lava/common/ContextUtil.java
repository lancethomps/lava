package com.github.lancethomps.lava.common;

import static com.github.lancethomps.lava.common.Checks.isNotEmpty;
import static com.github.lancethomps.lava.common.Collect.splitCsv;
import static com.github.lancethomps.lava.common.date.Dates.ZONE_PST;
import static com.github.lancethomps.lava.common.logging.Logs.logError;
import static com.github.lancethomps.lava.common.ser.Serializer.toPrettyJson;
import static com.google.common.collect.Sets.newHashSet;
import static java.io.File.separator;
import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.github.lancethomps.lava.common.date.Dates;
import com.github.lancethomps.lava.common.file.FileUtil;
import com.github.lancethomps.lava.common.logging.Logs;
import com.google.common.collect.Sets;

/**
 * The Class ContextUtil.
 */
public final class ContextUtil {

	/** The Constant CLASSPATH_PREFIX. */
	public static final String CLASSPATH_PREFIX = "classpath:";

	/** The Constant DEFAULT_INTRADAY_FORMAT. */
	public static final DateTimeFormatter INTRADAY_FORMAT = Dates.formatterFromPattern("yyyyMMdd-HH-mm-ss-SSS");

	/** The Constant SERVER_TYPE. */
	public static final String SERVER_TYPE = getProperty("webtools.serverType", isNotBlank(System.getenv("CATALINA_BASE")) ? "tomcat" : "wildfly");

	/** The Constant UNKNOWN_USER. */
	public static final String UNKNOWN_USER = "Unknown";

	/** The Constant UNKNOWN_USER_THREAD_SUFFIX. */
	public static final String UNKNOWN_USER_THREAD_SUFFIX = String.format("_%s", UNKNOWN_USER);

	/** The Constant IS_WILDFLY. */
	public static final boolean WILDFLY = SERVER_TYPE.equalsIgnoreCase("wildfly");

	/** The admin user ids. */
	private static Set<String> adminUserIds;

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(ContextUtil.class);

	/** The root path. */
	private static String rootPath;

	/** The server properties. */
	private static Properties serverProperties;

	/** The servlet context. */
	private static ServletContext servletContext;

	static {
		loadServerProperties();
	}

	/**
	 * Adds the admin users.
	 *
	 * @param userIds the user ids
	 */
	public static void addAdminUsers(Collection<String> userIds) {
		Set<String> adminUserIds = Sets.newHashSet();
		if (isNotEmpty(userIds)) {
			adminUserIds.addAll(userIds);
		}
		if ((serverProperties != null) && serverProperties.containsKey("adminUserIds")) {
			Set<String> defaultUserIds = newHashSet(splitCsv(serverProperties.getProperty("adminUserIds")));
			if (isNotEmpty(defaultUserIds)) {
				adminUserIds.addAll(defaultUserIds);
			}
		}
		ContextUtil.adminUserIds = adminUserIds;
	}

	/**
	 * Append and get original thread name.
	 *
	 * @param threadNameSuffix the thread name suffix
	 * @return the string
	 */
	public static String appendSuffixAndGetOriginalThreadName(String threadNameSuffix) {
		String original = Thread.currentThread().getName();
		if ((threadNameSuffix != null) && !original.equals(threadNameSuffix)) {
			Thread.currentThread().setName(original + '#' + threadNameSuffix);
		}
		return original;
	}

	/**
	 * @return the adminUserIds
	 */
	public static Set<String> getAdminUserIds() {
		return adminUserIds;
	}

	/**
	 * Gets the and reset thread name.
	 *
	 * @param thread the thread
	 * @param prefix the prefix
	 * @return the and reset thread name
	 */
	public static String getAndResetThreadName(Thread thread, String prefix) {
		if (thread == null) {
			thread = Thread.currentThread();
		}
		String name = prefix + '-' + thread.getId();
		thread.setName(name);
		return name;
	}

	/**
	 * Gets the and set custom thread name.
	 *
	 * @param thread the thread
	 * @param prefix the prefix
	 * @param suffix the suffix
	 * @param userId the user id
	 * @return the and set custom thread name
	 */
	public static String getAndSetCustomThreadName(Thread thread, String prefix, String suffix, String userId) {
		if (thread == null) {
			thread = Thread.currentThread();
		}
		String name = String.format("%s-%s_%s_%s_%s", prefix, thread.getId(), INTRADAY_FORMAT.format(LocalDateTime.now(ZONE_PST)), suffix, defaultIfBlank(userId, UNKNOWN_USER));
		thread.setName(name);
		return name;
	}

	/**
	 * Gets the class resource.
	 *
	 * @param path the path
	 * @return the class resource
	 */
	public static InputStream getClassResource(String path) {
		return ContextUtil.class.getResourceAsStream(path);
	}

	/**
	 * Gets the config file.
	 *
	 * @param relativePath the relative path
	 * @return the config file
	 */
	public static File getConfigFile(String relativePath) {
		return getConfigFile(relativePath, null, null);
	}

	/**
	 * Gets the config file.
	 *
	 * @param relativePath the relative path
	 * @param fileSystemDir the file system dir
	 * @param classpathBaseDir the classpath base dir
	 * @param otherFiles the other files
	 * @return the config file
	 */
	public static File getConfigFile(String relativePath, String fileSystemDir, String classpathBaseDir, File... otherFiles) {
		String resolvedRelativePath = FileUtil.useCorrectFileSeps(relativePath);
		String resolvedClasspathBaseDir = classpathBaseDir;
		if (resolvedClasspathBaseDir != null) {
			resolvedClasspathBaseDir = FileUtil.useCorrectFileSeps(resolvedClasspathBaseDir);
			if (!(resolvedClasspathBaseDir.endsWith(separator))) {
				resolvedClasspathBaseDir += separator;
			}
		}
		String resolvedFileSystemDir = fileSystemDir;
		if (resolvedFileSystemDir != null) {
			resolvedFileSystemDir = FileUtil.useCorrectFileSeps(resolvedFileSystemDir);
			if (!(resolvedFileSystemDir.endsWith(separator))) {
				resolvedFileSystemDir += separator;
			}
			resolvedFileSystemDir = StringUtils.removeEnd(resolvedFileSystemDir, StringUtils.substringBeforeLast(resolvedRelativePath, separator) + separator);
			if (!(resolvedFileSystemDir.endsWith(separator))) {
				resolvedFileSystemDir += separator;
			}
		}
		File classpathFile = ContextUtil.getFile(CLASSPATH_PREFIX + StringUtils.defaultString(resolvedClasspathBaseDir) + resolvedRelativePath);
		File fileSystemFile = new File(resolvedFileSystemDir + resolvedRelativePath);
		File latestFile;
		if (resolvedFileSystemDir == null) {
			latestFile = classpathFile;
		} else {
			latestFile = FileUtil.getLatestFile(classpathFile, fileSystemFile);
		}
		if (LOG.isTraceEnabled()) {
			Logs.logTrace(
				LOG,
				"Config file parameters resolved to - relativePath: [%s] => [%s] fileSystemDir: [%s] => [%s] classpathBaseDir: [%s] => [%s] classpathFile: [%s] fileSystemFile: [%s] latestFile: [%s]",
				relativePath,
				resolvedRelativePath,
				fileSystemDir,
				resolvedFileSystemDir,
				classpathBaseDir,
				resolvedClasspathBaseDir,
				FileUtil.fullPath(classpathFile),
				FileUtil.fullPath(fileSystemFile),
				FileUtil.fullPath(latestFile)
			);
		}
		return latestFile;
	}

	/**
	 * Gets the cp file.
	 *
	 * @param path the path
	 * @return the cp file
	 */
	public static File getCpFile(String path) {
		return getFile(CLASSPATH_PREFIX + path);
	}

	/**
	 * Gets the file.
	 *
	 * @param path the path
	 * @return the file
	 */
	public static File getFile(String path) {
		return getFile(path, false);
	}

	/**
	 * Gets the file.
	 *
	 * @param path the path
	 * @param addRoot the add root
	 * @return the file
	 */
	public static File getFile(String path, boolean addRoot) {
		if (isBlank(path)) {
			return null;
		}
		String filePath = path;
		try {
			if (filePath.startsWith(CLASSPATH_PREFIX)) {
				if (WILDFLY && isNotBlank(getRootPath())) {
					filePath = path.replace(CLASSPATH_PREFIX, getRootPath() + "WEB-INF/classes/");
					return ResourceUtils.getFile(filePath);
				}
				return ResourceUtils.getFile(filePath);
			}
			if (addRoot) {
				if (filePath.startsWith("/") || filePath.startsWith("\\")) {
					filePath = filePath.substring(1, filePath.length());
				}
				filePath = getRootPath() + filePath;
			}
			return ResourceUtils.getFile(filePath);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/**
	 * Gets the first resolved property.
	 *
	 * @param keys the keys
	 * @return the first resolved property
	 */
	public static String getFirstResolvedProperty(String... keys) {
		if (keys != null) {
			for (String key : keys) {
				String prop = getProperty(key);
				if (isNotBlank(prop)) {
					return prop;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the root path.
	 *
	 * @return the root path
	 */
	public static String getRootPath() {
		if (isBlank(rootPath) && (servletContext != null)) {
			String root = FileUtil.fullPath(servletContext.getRealPath("/"));
			if (!endsWith(root, "/") && !endsWith(root, "\\")) {
				root = root + File.separatorChar;
			}
			rootPath = root;
			System.setProperty("myserver.rootPath", FileUtil.fullPath(new File(rootPath)));
			Logs.logTrace(LOG, "Servlet root path set to [%s].", rootPath);
		}
		return rootPath;
	}

	/**
	 * @return the serverProperties
	 */
	public static Properties getServerProperties() {
		return serverProperties;
	}

	/**
	 * Gets the system boolean.
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the system boolean
	 */
	public static boolean getSystemBoolean(String key, boolean defaultValue) {
		String prop = getProperty(key);
		return isNotBlank(prop) ? BooleanUtils.toBoolean(prop) : defaultValue;
	}

	/**
	 * Gets the system boolean.
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the system boolean
	 */
	public static boolean getSystemBoolean(String key, String defaultValue) {
		return parseBoolean(getProperty(key, defaultValue));
	}

	/**
	 * Checks if is admin user.
	 *
	 * @param userId the user id
	 * @return true, if is admin user
	 */
	public static boolean isAdminUser(String userId) {
		return Checks.isNotEmpty(adminUserIds) && ((userId != null) && adminUserIds.contains(userId));
	}

	/**
	 * Load server properties.
	 */
	public static void loadServerProperties() {
		try {
			File propsFile = getFile(CLASSPATH_PREFIX + "properties/server.properties", false);
			if ((propsFile != null) && propsFile.exists()) {
				setServerProperties(FileUtil.loadProperties(propsFile));
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue while trying to get server.properties file!");
		}
	}

	/**
	 * Parses the boolean.
	 *
	 * @param bool the bool
	 * @return true, if successful
	 */
	public static boolean parseBoolean(String bool) {
		return BooleanUtils.toBoolean(bool);
	}

	/**
	 * Parses the boolean.
	 *
	 * @param bool the bool
	 * @param defaultValue the default value
	 * @return true, if successful
	 */
	public static boolean parseBoolean(String bool, boolean defaultValue) {
		return bool != null ? BooleanUtils.toBoolean(bool) : defaultValue;
	}

	/**
	 * Resolve possible root relative path.
	 *
	 * @param path the path
	 * @return the string
	 */
	public static String resolvePossibleRootRelativePath(String path) {
		return FileUtil.isAbsolutePath(path) ? path : getRootPath() + path;
	}

	/**
	 * @param rootPath the rootPath to set
	 */
	public static void setRootPath(String rootPath) {
		if (isBlank(ContextUtil.rootPath)) {
			ContextUtil.rootPath = rootPath;
		}
	}

	/**
	 * @param serverProperties the serverProperties to set
	 */
	public static void setServerProperties(Properties serverProperties) {
		ContextUtil.serverProperties = serverProperties;
		if ((serverProperties != null) && serverProperties.containsKey("adminUserIds")) {
			adminUserIds = newHashSet(splitCsv(serverProperties.getProperty("adminUserIds")));
			Logs.logWarn(LOG, "Admin users ==> %s", adminUserIds);
		}
	}

	/**
	 * Sets the servlet context.
	 *
	 * @param servletContext the new servlet context
	 */
	public static void setServletContext(ServletContext servletContext) {
		Logs.logTrace(LOG, "Setting servlet context...");
		ContextUtil.servletContext = servletContext;
		getRootPath();
		loadServerProperties();
	}

	/**
	 * Shutdown gracefully.
	 */
	public static void shutdownGracefully() {
		Runtime.getRuntime().halt(0);
	}

	/**
	 * Shutdown gracefully.
	 *
	 * @param contextLoader the context loader
	 * @param wac the wac
	 */
	public static void shutdownGracefully(ContextLoader contextLoader, WebApplicationContext wac) {
		Logs.logWarn(LOG, "Shutting down server...");
		ServletContext sc = wac == null ? null : wac.getServletContext();
		if (sc != null) {
			try {
				Enumeration<String> attrNames = sc.getAttributeNames();
				while (attrNames.hasMoreElements()) {
					String attrName = attrNames.nextElement();
					if (attrName.startsWith("org.springframework.")) {
						Object attrValue = sc.getAttribute(attrName);
						if (attrValue instanceof DisposableBean) {
							try {
								((DisposableBean) attrValue).destroy();
							} catch (Throwable e) {
								Logs.printerr(e, "Couldn't invoke destroy method of attribute with name '%s'", attrName);
							}
						}
					}
				}
			} catch (Throwable e) {
				Logs.printerr(e, "Exception while shutting down context.");
			}
		}
		try {
			if (contextLoader != null) {
				contextLoader.closeWebApplicationContext(sc);
			}
		} catch (Throwable e) {
			Logs.printerr(e, "Exception while shutting down context.");
		}
		try {
			Enumeration<Driver> drivers = DriverManager.getDrivers();
			if ((drivers != null) && (contextLoader != null)) {
				while (drivers.hasMoreElements()) {
					Driver driver = drivers.nextElement();
					ClassLoader driverclassLoader = driver.getClass().getClassLoader();
					ClassLoader thisClassLoader = contextLoader.getClass().getClassLoader();
					if ((driverclassLoader != null) && (thisClassLoader != null) && driverclassLoader.equals(thisClassLoader)) {
						try {
							Logs.println("Deregistering [%s]", driver);
							DriverManager.deregisterDriver(driver);
						} catch (SQLException e) {
							Logs.printerr(e, "Exception while deregistering driver [%s]", driver);
						}
					}
				}
			}
		} catch (Throwable e) {
			Logs.printerr(e, "Exception while shutting down context.");
		}
		// Runtime.getRuntime().halt(0);
		System.exit(0);
	}

	/**
	 * Write debug json.
	 *
	 * @param bean the bean
	 */
	public static void writeDebugJson(Object bean) {
		File debugDir = new File(getRootPath() + "debug");
		debugDir.mkdirs();
		File debugFile = new File(getRootPath() + "debug/" + System.currentTimeMillis() + ".json");
		try {
			FileUtil.writeFile(debugFile, toPrettyJson(bean));
			Logs.logWarn(LOG, "Wrote debug JSON to file [%s]!", debugFile);
		} catch (Throwable e) {
			logError(LOG, e, "Could not write debug JSON to file [%s]. JSON is below...\n%s", FileUtil.fullPath(debugFile), toPrettyJson(bean));
		}
	}

}
