package com.lancethomps.lava.common;

import static com.lancethomps.lava.common.logging.Logs.logError;
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

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.collect.Sets;
import com.lancethomps.lava.common.date.Dates;
import com.lancethomps.lava.common.file.FileUtil;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.ser.Serializer;

public final class ContextUtil {

  public static final String CLASSPATH_PREFIX = "classpath:";
  public static final DateTimeFormatter INTRADAY_FORMAT = Dates.formatterFromPattern("yyyyMMdd-HH-mm-ss-SSS");
  public static final String SERVER_TYPE = "tomcat";
  public static final String UNKNOWN_USER = "Unknown";
  public static final String UNKNOWN_USER_THREAD_SUFFIX = String.format("_%s", UNKNOWN_USER);
  public static final boolean WILDFLY = SERVER_TYPE.equalsIgnoreCase("wildfly");
  private static final Logger LOG = Logger.getLogger(ContextUtil.class);
  private static Set<String> adminUserIds;
  private static String rootPath;
  private static Properties serverProperties;
  private static ServletContext servletContext;

  static {
    loadServerProperties();
  }

  public static void addAdminUsers(Collection<String> userIds) {
    Set<String> adminUserIds = Sets.newHashSet();
    if (Checks.isNotEmpty(userIds)) {
      adminUserIds.addAll(userIds);
    }
    if ((serverProperties != null) && serverProperties.containsKey("adminUserIds")) {
      Set<String> defaultUserIds = Sets.newHashSet(Collect.splitCsv(serverProperties.getProperty("adminUserIds")));
      if (Checks.isNotEmpty(defaultUserIds)) {
        adminUserIds.addAll(defaultUserIds);
      }
    }
    ContextUtil.adminUserIds = adminUserIds;
  }

  public static Set<String> getAdminUserIds() {
    return adminUserIds;
  }

  public static String getAndResetThreadName(Thread thread, String prefix) {
    if (thread == null) {
      thread = Thread.currentThread();
    }
    String name = prefix + '-' + thread.getId();
    thread.setName(name);
    return name;
  }

  public static String getAndSetCustomThreadName(Thread thread, String prefix, String suffix, String userId) {
    if (thread == null) {
      thread = Thread.currentThread();
    }
    String name = String.format(
      "%s-%s_%s_%s_%s",
      prefix,
      thread.getId(),
      INTRADAY_FORMAT.format(LocalDateTime.now(Dates.ZONE_PST)),
      suffix,
      defaultIfBlank(userId, UNKNOWN_USER)
    );
    thread.setName(name);
    return name;
  }

  public static InputStream getClassResource(String path) {
    return ContextUtil.class.getResourceAsStream(path);
  }

  public static File getConfigFile(String relativePath) {
    return getConfigFile(relativePath, null, null);
  }

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
      resolvedFileSystemDir =
        StringUtils.removeEnd(resolvedFileSystemDir, StringUtils.substringBeforeLast(resolvedRelativePath, separator) + separator);
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
        "Config file parameters resolved to - relativePath: [%s] => [%s] fileSystemDir: [%s] => [%s] classpathBaseDir: [%s] => [%s] classpathFile: " +
          "[%s] fileSystemFile: [%s] latestFile: [%s]",
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

  public static File getCpFile(String path) {
    return getFile(CLASSPATH_PREFIX + path);
  }

  public static File getFile(String path) {
    return getFile(path, false);
  }

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
          filePath = filePath.substring(1);
        }
        filePath = getRootPath() + filePath;
      }
      return ResourceUtils.getFile(filePath);
    } catch (FileNotFoundException e) {
      return null;
    }
  }

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

  public static void setRootPath(String rootPath) {
    if (isBlank(ContextUtil.rootPath)) {
      ContextUtil.rootPath = rootPath;
    }
  }

  public static Properties getServerProperties() {
    return serverProperties;
  }

  public static void setServerProperties(Properties serverProperties) {
    ContextUtil.serverProperties = serverProperties;
    if ((serverProperties != null) && serverProperties.containsKey("adminUserIds")) {
      adminUserIds = Sets.newHashSet(Collect.splitCsv(serverProperties.getProperty("adminUserIds")));
      Logs.logWarn(LOG, "Admin users ==> %s", adminUserIds);
    }
  }

  public static boolean getSystemBoolean(String key, boolean defaultValue) {
    String prop = getProperty(key);
    return isNotBlank(prop) ? BooleanUtils.toBoolean(prop) : defaultValue;
  }

  public static boolean getSystemBoolean(String key, String defaultValue) {
    return parseBoolean(getProperty(key, defaultValue));
  }

  public static String getThreadNameAndAddPrefix(@Nonnull String prefix) {
    return getThreadNameAndAddPrefix(prefix, Thread.currentThread());
  }

  public static String getThreadNameAndAddPrefix(@Nonnull String prefix, @Nonnull Thread thread) {
    String currentName = thread.getName();
    thread.setName(prefix + currentName);
    return currentName;
  }

  public static String getThreadNameAndAppendSuffix(@Nonnull String suffix) {
    return getThreadNameAndAppendSuffix(suffix, Thread.currentThread());
  }

  public static String getThreadNameAndAppendSuffix(@Nonnull String suffix, @Nonnull Thread thread) {
    String currentName = thread.getName();
    thread.setName(currentName + suffix);
    return currentName;
  }

  public static boolean isAdminUser(String userId) {
    return Checks.isNotEmpty(adminUserIds) && ((userId != null) && adminUserIds.contains(userId));
  }

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

  public static boolean parseBoolean(String bool) {
    return BooleanUtils.toBoolean(bool);
  }

  public static boolean parseBoolean(String bool, boolean defaultValue) {
    return bool != null ? BooleanUtils.toBoolean(bool) : defaultValue;
  }

  public static String resolvePossibleRootRelativePath(String path) {
    return FileUtil.isAbsolutePath(path) ? path : getRootPath() + path;
  }

  public static void setServletContext(ServletContext servletContext) {
    Logs.logTrace(LOG, "Setting servlet context...");
    ContextUtil.servletContext = servletContext;
    getRootPath();
    loadServerProperties();
  }

  public static void shutdownGracefully() {
    Runtime.getRuntime().halt(0);
  }

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

    System.exit(0);
  }

  public static void writeDebugJson(Object bean) {
    File debugDir = new File(getRootPath() + "debug");
    debugDir.mkdirs();
    File debugFile = new File(getRootPath() + "debug/" + System.currentTimeMillis() + ".json");
    try {
      FileUtil.writeFile(debugFile, Serializer.toPrettyJson(bean));
      Logs.logWarn(LOG, "Wrote debug JSON to file [%s]!", debugFile);
    } catch (Throwable e) {
      logError(LOG, e, "Could not write debug JSON to file [%s]. JSON is below...\n%s", FileUtil.fullPath(debugFile), Serializer.toPrettyJson(bean));
    }
  }

}
