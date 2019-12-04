package com.lancethomps.lava.common.logging;

import static com.lancethomps.lava.common.time.Timing.NANO_2_MILLIS;
import static com.lancethomps.lava.common.time.Timing.formatMillis;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import org.apache.commons.collections4.EnumerationUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.Patterns;
import com.lancethomps.lava.common.format.Formatting;
import com.lancethomps.lava.common.string.StringUtil;
import com.lancethomps.lava.common.time.Stopwatch;
import com.sun.management.GarbageCollectionNotificationInfo;

@SuppressWarnings("restriction")
public final class Logs {

  public static final String ENABLE_TRACE_MSG = "<enable_TRACE_to_show>";

  public static final double LOG_INTERVAL = 0.01;

  public static final int MAX_ERROR_MESSAGES = 100;

  public static final Pattern SPLUNK_LOG_KEY_VALUE_EXTRACTOR = Pattern.compile(
    "[^\\w](\\w[a-zA-Z0-9_\\.]+)=(\"((?:(?<!\\\\)(?:\\\\{2})*\\\\\"|[^\"])+(?<!\\\\)(?:\\\\{2})*)\"|([^ ]+))"
  );

  private static final InheritableThreadLocal<Boolean> DISABLE_CUSTOM_SAVED_ERROR_HANDLER = new InheritableThreadLocal<>();

  private static final InheritableThreadLocal<Boolean> DISABLE_SAVED_ERRORS = new InheritableThreadLocal<>();

  private static final List<SavedErrorMessage> ERROR_MESSAGES = new ArrayList<>();

  private static final List<Pair<NotificationEmitter, NotificationListener>> GC_MONITORS = new ArrayList<>();
  private static final Logger LOG = Logger.getLogger(Logs.class);
  private static final Map<Logger, Level> LOGGER_RESET_MAP = new HashMap<>();
  private static final DecimalFormat MILLIS_FORMAT = new DecimalFormat("#,###");
  private static final String TIMER_MSG_SHELL = "TIMER - %s completed in %sms. %s";
  private static final DecimalFormat TIME_FORMAT = new DecimalFormat("#,###");
  private static boolean keepErrorMessages;
  private static boolean monitorGarbageCollectors;
  private static Function<SavedErrorMessage, Boolean> savedErrorMessageHandler;
  private static Set<String> skipErrorsForLoggers;
  private static List<Pattern> skipErrorsPatterns;
  private static int splunkKeyValueStringMaxLength = NumberUtils.toInt(System.getProperty("wtp.splunkKeyValueStringMaxLength"), 5000);
  private static String systemOutPrefix;
  private static Level tempAllLogLevel;

  public static void addErrorMessage(SavedErrorMessage savedError) {
    synchronized (ERROR_MESSAGES) {
      ERROR_MESSAGES.add(savedError);
    }
  }

  public static double asPctNumber(long numerator, long denominator) {
    return (double) (numerator * 100) / (double) denominator;
  }

  public static PrintStream createLoggingProxy(final PrintStream realPrintStream, final Logger logger, final Level level) {
    return new PrintStream(realPrintStream) {

      @Override
      public void print(final String string) {
        realPrintStream.print(string);
        if (level != null) {
          logger.log(level, string);
        } else {
          logger.info(string);
        }
      }
    };
  }

  public static void disableThreadSavedErrors() {
    DISABLE_SAVED_ERRORS.set(true);
  }

  public static void disableThreadSavedErrorsCustomHandler() {
    DISABLE_CUSTOM_SAVED_ERROR_HANDLER.set(true);
  }

  public static boolean doesSkipErrorsPatternMatch(@Nonnull final SavedErrorMessage msg) {
    final List<Pattern> skipErrorsPatterns = Logs.skipErrorsPatterns;
    if (skipErrorsPatterns == null) {
      return false;
    }
    return skipErrorsPatterns.stream().anyMatch(p -> p.matcher(msg.toString()).matches());
  }

  public static void enableThreadSavedErrors() {
    DISABLE_SAVED_ERRORS.remove();
  }

  public static void enableThreadSavedErrorsCustomHandler() {
    DISABLE_CUSTOM_SAVED_ERROR_HANDLER.remove();
  }

  public static Throwable findRootCause(Throwable e) {
    Throwable cause = e.getCause();
    if (cause != null) {
      return findRootCause(cause);
    }
    return e;
  }

  public static Map<String, String> findSplunkLogKeyValueMatches(@Nonnull String input) {
    return Patterns.findKeyValueMatches(
      SPLUNK_LOG_KEY_VALUE_EXTRACTOR,
      input,
      Arrays.asList(1),
      Arrays.asList(3, 4),
      val -> StringUtils.replace(val, "\\\"", "\"")
    );
  }

  public static String generateStackTrace(Throwable e) {
    if (e != null) {
      StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer, false));
      return writer.toString();
    }
    return null;
  }

  public static List<SavedErrorMessage> getErrorMessages() {
    List<SavedErrorMessage> messages = new ArrayList<>(ERROR_MESSAGES);
    synchronized (ERROR_MESSAGES) {
      ERROR_MESSAGES.clear();
    }
    return messages;
  }

  public static Function<SavedErrorMessage, Boolean> getSavedErrorMessageHandler() {
    return savedErrorMessageHandler;
  }

  public static void setSavedErrorMessageHandler(Function<SavedErrorMessage, Boolean> savedErrorMessageHandler) {
    Logs.savedErrorMessageHandler = savedErrorMessageHandler;
  }

  public static Set<String> getSkipErrorsForLoggers() {
    return skipErrorsForLoggers;
  }

  public static void setSkipErrorsForLoggers(Set<String> skipErrorsForLoggers) {
    Logs.skipErrorsForLoggers = skipErrorsForLoggers;
  }

  public static List<Pattern> getSkipErrorsPatterns() {
    return skipErrorsPatterns;
  }

  public static void setSkipErrorsPatterns(List<Pattern> skipErrorsPatterns) {
    Logs.skipErrorsPatterns = skipErrorsPatterns;
  }

  public static <K extends Object, V extends Object> String getSplunkKeyValueString(Map<K, V> data) {
    if (data == null) {
      return null;
    } else if (data.isEmpty()) {
      return "";
    }
    return data.entrySet().stream().map(e -> getSplunkKeyValueString(e.getKey(), e.getValue())).collect(Collectors.joining(" "));
  }

  public static String getSplunkKeyValueString(Object key, Object value) {
    return (key == null ? null : key.toString()) + '=' + getSplunkValueString(value);
  }

  public static int getSplunkKeyValueStringMaxLength() {
    return splunkKeyValueStringMaxLength;
  }

  public static void setSplunkKeyValueStringMaxLength(int splunkKeyValueStringMaxLength) {
    Logs.splunkKeyValueStringMaxLength = splunkKeyValueStringMaxLength;
  }

  public static String getSplunkValueString(Object value) {
    String valueStr = value == null ? null
      : splunkKeyValueStringMaxLength > 0 ?
      StringUtil.truncateStringIfNeeded(value.toString(), splunkKeyValueStringMaxLength, StringUtil.TRUNCATE_STRING_ELLIPSIS) : value.toString();
    if ((valueStr == null) || (!StringUtils.containsAny(valueStr, '"', '=') && !StringUtil.CONTAINS_SPACES_REGEX.matcher(valueStr).find())) {
      return valueStr;
    }
    return '"' + StringUtils.replace(valueStr, "\"", "\\\"") + '"';
  }

  public static String getSystemOutPrefix() {
    return systemOutPrefix;
  }

  public static void setSystemOutPrefix(String systemOutPrefix) {
    Logs.systemOutPrefix = systemOutPrefix;
  }

  public static Level getTempAllLogLevel() {
    return tempAllLogLevel;
  }

  public static boolean hasTempAllLogLevel() {
    return tempAllLogLevel != null;
  }

  public static void installGcMonitoring() {
    synchronized (GC_MONITORS) {
      uninstallGcMonitoring();
      logWarn(LOG, "Installing GC monitoring...");

      List<GarbageCollectorMXBean> gcbeans = ManagementFactory.getGarbageCollectorMXBeans();

      for (GarbageCollectorMXBean gcbean : gcbeans) {
        NotificationEmitter emitter = (NotificationEmitter) gcbean;
        AtomicLong totalGcDuration = new AtomicLong(0);

        NotificationListener listener = (Notification notification, Object handback) -> {

          if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {

            GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());

            long duration = info.getGcInfo().getDuration();
            String gctype = info.getGcAction();
            if ("end of minor GC".equals(gctype)) {
              gctype = "Young Gen GC";
            } else if ("end of major GC".equals(gctype)) {
              gctype = "Old Gen GC";
            }
            StringBuilder msg = new StringBuilder();
            msg.append(
              gctype + ": - " + info.getGcInfo().getId() + ' ' + info.getGcName() + " (from " + info.getGcCause() + ") " + duration +
                " milliseconds; start-end times " + info
                .getGcInfo()
                .getStartTime() + '-' + info.getGcInfo().getEndTime() + ' '
            );

            Map<String, MemoryUsage> memBefore = info.getGcInfo().getMemoryUsageBeforeGc();
            Map<String, MemoryUsage> mem = info.getGcInfo().getMemoryUsageAfterGc();
            long totalMemBefore = 0;
            long totalMemAfter = 0;
            for (Entry<String, MemoryUsage> entry : mem.entrySet()) {
              String name = entry.getKey();
              MemoryUsage memdetail = entry.getValue();
              long memCommitted = memdetail.getCommitted();
              long memMax = memdetail.getMax();
              long memUsed = memdetail.getUsed();
              totalMemAfter += memUsed;
              MemoryUsage before = memBefore.get(name);
              totalMemBefore += before.getUsed();
              long beforepercent = ((before.getUsed() * 1000L) / before.getCommitted());
              long percent = ((memUsed * 1000L) / before.getCommitted());

              msg.append(
                name + (memCommitted == memMax ? " (FULL) " : " (can expand) ") + "used: " + (beforepercent / 10) + '.' + (beforepercent % 10) +
                  "%->" + (percent / 10) + '.' + (percent
                  % 10) + "%(" + ((memUsed / 1048576) + 1) + "MB) / "
              );
            }
            totalGcDuration.addAndGet(info.getGcInfo().getDuration());
            long percent = (totalGcDuration.get() * 1000L) / info.getGcInfo().getEndTime();
            msg.append("GC cumulated overhead " + (percent / 10) + '.' + (percent % 10) + '%');
            BigDecimal reduction = Formatting.bytesToMegaBytes(totalMemBefore - totalMemAfter);
            BigDecimal reductionPct = BigDecimal
              .valueOf((double) (totalMemBefore - totalMemAfter) / (double) totalMemBefore)
              .movePointRight(2)
              .setScale(2, RoundingMode.HALF_DOWN);
            Logs.logDebug(
              LOG,
              "GC monitoring before [%s], after [%s], reduction [%s] - pct [%s]: %s",
              Formatting.bytesToMegaBytes(totalMemBefore),
              Formatting.bytesToMegaBytes(totalMemAfter),
              reduction,
              reductionPct,
              msg
            );
          }
        };

        emitter.addNotificationListener(listener, null, null);
        GC_MONITORS.add(Pair.of(emitter, listener));
      }
    }
  }

  public static boolean isKeepErrorMessages() {
    return keepErrorMessages;
  }

  public static void setKeepErrorMessages(boolean keepErrorMessages) {
    Logs.keepErrorMessages = keepErrorMessages;
  }

  public static boolean isMonitorGarbageCollectors() {
    return monitorGarbageCollectors;
  }

  public static void setMonitorGarbageCollectors(boolean monitorGarbageCollectors) {
    Logs.monitorGarbageCollectors = monitorGarbageCollectors;
    if (monitorGarbageCollectors) {
      installGcMonitoring();
    } else {
      uninstallGcMonitoring();
    }
  }

  public static void logDebug(final Logger logger, final String message, final Object... formatArgs) {
    logDebug(logger, null, message, formatArgs);
  }

  public static void logDebug(final Logger logger, final Throwable throwable, final String message, final Object... formatArgs) {
    if (logger.isDebugEnabled()) {
      logger.debug(Formatting.getMessage(message, formatArgs), throwable);
    }
  }

  public static void logEnd(final Logger logger, final Level level, final String message, final Object... formatArgs) {
    if (logger.isEnabledFor(level)) {
      logger.log(level, Formatting.getMessage("END - " + message, formatArgs));
    }
  }

  public static void logEnd(final Logger logger, final String message, final Object... formatArgs) {
    logStart(logger, Level.TRACE, message, formatArgs);
  }

  public static void logError(
    final Logger logger,
    final SavedErrorMessageSeverity severity,
    final Throwable t,
    final String message,
    final Object... formatArgs
  ) {
    String errorMessage = Formatting.getMessage(message, formatArgs);
    logger.error(errorMessage, t);
    saveErrorIfNeeded(logger, severity, t, errorMessage);
  }

  public static void logError(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
    logError(logger, null, t, message, formatArgs);
  }

  public static void logErrorHigh(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
    logError(logger, SavedErrorMessageSeverity.HIGH, t, message, formatArgs);
  }

  public static void logErrorLow(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
    logError(logger, SavedErrorMessageSeverity.LOW, t, message, formatArgs);
  }

  public static void logErrorMedium(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
    logError(logger, SavedErrorMessageSeverity.MEDIUM, t, message, formatArgs);
  }

  public static void logErrorWithoutKeeping(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
    String errorMessage = Formatting.getMessage(message, formatArgs);
    logger.error(errorMessage, t);
  }

  public static void logErrorWithoutSavedErrorHandler(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
    String errorMessage = Formatting.getMessage(message, formatArgs);
    logger.error(errorMessage, t);
    saveErrorIfNeeded(logger, SavedErrorMessageSeverity.HIGH, t, errorMessage, false);
  }

  public static void logFatal(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
    String errorMessage = Formatting.getMessage(message, formatArgs);
    logger.fatal(errorMessage, t);
    saveErrorIfNeeded(logger, SavedErrorMessageSeverity.HIGH, t, errorMessage);
  }

  public static void logForSplunk(
    @Nonnull final Logger logger,
    @Nonnull final Level level,
    @Nonnull final Object splunkId,
    @Nullable final String message,
    @Nullable final Object... formatArgs
  ) {
    logForSplunk(logger, level, null, splunkId, message, formatArgs);
  }

  public static void logForSplunk(
    @Nonnull final Logger logger,
    @Nonnull final Level level,
    @Nullable final Throwable t,
    @Nonnull final Object splunkId,
    @Nullable final String message,
    @Nullable final Object... formatArgs
  ) {
    if (level == Level.ERROR) {
      String msg = "SPLUNK@" + (Checks.isBlank(message) ? splunkId.toString() : (splunkId.toString() + '|' + message));
      logError(logger, t, msg, formatArgs);
    } else if (logger.isEnabledFor(level)) {
      String msg = "SPLUNK@" + Formatting.getMessage(Checks.isBlank(message) ? splunkId.toString() : (splunkId.toString() + '|' + message), formatArgs);
      logger.log(level, msg, t);
    }
  }

  public static void logForSplunk(
    @Nonnull final Logger logger,
    @Nonnull final Object splunkId,
    @Nullable final String message,
    @Nullable final Object... formatArgs
  ) {
    logForSplunk(logger, Level.WARN, splunkId, message, formatArgs);
  }

  public static void logForSplunk(
    @Nonnull final Logger logger,
    @Nullable final Throwable t,
    @Nonnull final Object splunkId,
    @Nullable final String message,
    @Nullable final Object... formatArgs
  ) {
    logForSplunk(logger, t == null ? Level.WARN : Level.ERROR, t, splunkId, message, formatArgs);
  }

  public static void logForSplunkWithKeyValArgs(
    @Nonnull final Logger logger,
    @Nonnull final Level level,
    @Nullable final Throwable t,
    @Nonnull final Object splunkId,
    @Nullable final String message,
    @Nullable final Object... formatArgs
  ) {
    logForSplunk(logger, level, t, splunkId, message, Formatting.createKeyValFormatArgs(formatArgs));
  }

  public static void logForSplunkWithKeyValArgs(
    @Nonnull final Logger logger,
    @Nonnull final Object splunkId,
    @Nullable final String message,
    @Nullable final Object... formatArgs
  ) {
    logForSplunkWithKeyValArgs(logger, Level.WARN, null, splunkId, message, formatArgs);
  }

  public static void logInfo(final Logger logger, final String message, final Object... formatArgs) {
    logInfo(logger, null, message, formatArgs);
  }

  public static void logInfo(final Logger logger, final Throwable throwable, final String message, final Object... formatArgs) {
    if (logger.isInfoEnabled()) {
      logger.info(Formatting.getMessage(message, formatArgs), throwable);
    }
  }

  public static double logInterval(
    final Logger logger,
    int count,
    int total,
    double nextLogPct,
    double logInterval,
    final String msg,
    final Stopwatch watch
  ) {
    double pct = (double) count / (double) total;
    if ((pct > nextLogPct) || (count == total)) {
      nextLogPct += logInterval;
      logIntervalMsg(logger, msg, count, total, watch);
    }
    return nextLogPct;
  }

  public static boolean logInterval(final Logger logger, final LogIntervalData data) {
    return logInterval(logger, data, 1);
  }

  public static boolean logInterval(final Logger logger, final LogIntervalData data, long add) {
    long count = data.getCount().addAndGet(add);
    double pct = (double) count / (double) data.getTotal();
    if ((pct > data.getNextLogPct().get()) || (count == data.getTotal())) {
      data.getNextLogPct().addAndGet(data.getLogInterval());
      logIntervalMsg(logger, data.getMsg(), count, data.getTotal(), data.getWatch());
      return true;
    }
    return false;
  }

  public static void logIntervalMsg(Logger logger, String msg, long count, long total, Stopwatch watch) {
    String timeMsg = "";
    if (watch != null) {
      long millis = watch.getTime();
      long estimatedTotal = Double.valueOf(millis * ((double) total / (double) count)).longValue();
      timeMsg = String.format("Elapsed: %12s\tRemaining: %12s", formatMillis(millis), formatMillis(estimatedTotal - millis));
    }
    String fullMsg = format("COMPLETE - %s - %6.2f%%\tCount: %6d / %d\t%s", msg, asPctNumber(count, total), count, total, timeMsg);
    logInfo(logger, fullMsg);
  }

  public static void logLevel(final Logger logger, final Level level, final Throwable t, final String message, final Object... formatArgs) {
    if (level == Level.ERROR) {
      logError(logger, t, message, formatArgs);
    } else if (logger.isEnabledFor(level)) {
      String msg = Formatting.getMessage(message, formatArgs);
      logger.log(level, msg, t);
    }
  }

  public static <T> void logNested(
    final Logger logger,
    final Collection<T> docs,
    final Function<T, Collection<T>> childrenGetter,
    final String tabs
  ) {
    docs
      .stream()
      .peek(doc -> logDebug(logger, "%s%s", tabs, doc))
      .map(doc -> childrenGetter == null ? null : childrenGetter.apply(doc))
      .filter(Checks::isNotEmpty)
      .forEach(
        children -> logNested(logger, children, childrenGetter, tabs + '\t')
      );
  }

  public static void logStart(final Logger logger, final Level level, final String message, final Object... formatArgs) {
    if (logger.isEnabledFor(level)) {
      logger.log(level, Formatting.getMessage("START - " + message, formatArgs));
    }
  }

  public static void logStart(final Logger logger, final String message, final Object... formatArgs) {
    logStart(logger, Level.TRACE, message, formatArgs);
  }

  public static void logTimer(final Logger logger, final long start, final String process) {
    logTimer(logger, start > 1000000000 ? new Date().getTime() - start : start, process, EMPTY);
  }

  public static void logTimer(final Logger logger, final long timeTaken, final String process, final String processInfo) {
    logTimer(logger, timeTaken, process, processInfo, null);
  }

  public static void logTimer(final Logger logger, final long timeTaken, final String process, final String processInfo, final Level level) {
    logTimerMillis(logger, timeTaken, process, processInfo, level, false);
  }

  public static void logTimer(
    final Logger logger,
    final long timeTakenNanos,
    final String process,
    final String processInfo,
    final Level level,
    final boolean withholdProcessInfo
  ) {
    if (level != null) {
      double millis = (double) timeTakenNanos / (double) NANO_2_MILLIS;
      String timeStr = MILLIS_FORMAT.format(millis);
      logger.log(level, format(TIMER_MSG_SHELL, process, timeStr, withholdProcessInfo ? EMPTY : defaultIfBlank(processInfo, EMPTY)));
    } else if (logger.isDebugEnabled()) {
      double millis = (double) timeTakenNanos / (double) NANO_2_MILLIS;
      String timeStr = MILLIS_FORMAT.format(millis);
      logger.debug(format(TIMER_MSG_SHELL, process, timeStr, withholdProcessInfo ? EMPTY : defaultIfBlank(processInfo, EMPTY)));
    }
  }

  public static void logTimer(final Logger logger, final Stopwatch watch, final String process) {
    logTimer(logger, watch, process, (Level) null);
  }

  public static void logTimer(final Logger logger, final Stopwatch watch, final String process, final Level level) {
    logTimer(logger, watch, process, null, level);
  }

  public static void logTimer(final Logger logger, final Stopwatch watch, final String process, final String processInfo) {
    logTimer(logger, watch, process, processInfo, null);
  }

  public static void logTimer(final Logger logger, final Stopwatch watch, final String process, final String processInfo, final Level level) {
    logTimer(logger, watch, process, processInfo, level, false);
  }

  public static void logTimer(
    final Logger logger,
    final Stopwatch watch,
    final String process,
    final String processInfo,
    final Level level,
    final boolean withholdProcessInfo
  ) {
    logTimer(logger, watch.getNanoTime(), process, processInfo, level, withholdProcessInfo);
  }

  public static void logTimerMillis(
    final Logger logger,
    final long timeTaken,
    final String process,
    final String processInfo,
    final Level level,
    final boolean withholdProcessInfo
  ) {
    if (level != null) {
      String timeStr = timeTaken > 9999 ? TIME_FORMAT.format(timeTaken) : String.valueOf(timeTaken);
      logger.log(level, format(TIMER_MSG_SHELL, process, timeStr, withholdProcessInfo ? EMPTY : defaultIfBlank(processInfo, EMPTY)));
    } else if (logger.isDebugEnabled()) {
      String timeStr = timeTaken > 9999 ? TIME_FORMAT.format(timeTaken) : String.valueOf(timeTaken);
      logger.debug(format(TIMER_MSG_SHELL, process, timeStr, withholdProcessInfo ? EMPTY : defaultIfBlank(processInfo, EMPTY)));
    }
  }

  public static void logTimerPerObject(final Logger logger, final Stopwatch watch, final String process, final int total) {
    long totalTime = watch.getTime();
    long perDoc = total > 0 ? ((long) Math.ceil((double) totalTime / (double) total)) : totalTime;
    logTimer(logger, perDoc, process + " Per Element (" + total + ')', String.format("Total: %sms", totalTime));
  }

  public static void logTrace(final Logger logger, final String message, final Object... formatArgs) {
    logTrace(logger, null, message, formatArgs);
  }

  public static void logTrace(final Logger logger, final Throwable throwable, final String message, final Object... formatArgs) {
    if (logger.isTraceEnabled()) {
      logger.trace(Formatting.getMessage(message, formatArgs), throwable);
    }
  }

  public static void logWarn(final Logger logger, final String message, final Object... formatArgs) {
    logWarn(logger, null, message, formatArgs);
  }

  public static void logWarn(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
    logger.warn(Formatting.getMessage(message, formatArgs), t);
  }

  // CHECKSTYLE.OFF: SystemOutLogging
  public static void printPercentComplete(final Object message, final double pct, final Object... formatArgs) {
    String progress = String.join("", Collections.nCopies((int) Math.floor(pct * 20), "#"));
    System.out.print(
      Formatting.getMessage(message == null ? "" : message.toString(), formatArgs) + String.format("[%1$-20s] %2$3.2f", progress, (pct * 100d)) + '%' +
        (pct >= 0.99999 ? '\n' : '\r')
    );
  }

  public static void printerr(final Object message, final Object... formatArgs) {
    printerr(null, message, formatArgs);
  }

  public static void printerr(final Throwable e, final Object message, final Object... formatArgs) {
    System.err.println(Formatting.getMessage(message.toString(), formatArgs));
    if (e != null) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }

  public static void println(final Object message, final Object... formatArgs) {
    System.out.println(Checks.defaultIfNull(systemOutPrefix, "") + Formatting.getMessage(message == null ? null : message.toString(), formatArgs));
  }
  // CHECKSTYLE.ON: SystemOutLogging

  public static void resetAllLogLevels() {
    synchronized (LOGGER_RESET_MAP) {
      if (!LOGGER_RESET_MAP.isEmpty()) {
        LOGGER_RESET_MAP.forEach((logger, level) -> logger.setLevel(level));
        LOGGER_RESET_MAP.clear();
      }
      tempAllLogLevel = null;
    }
  }

  public static void setAllLogLevels(Level newLevel) {
    setAllLogLevels(newLevel, false);
  }

  @SuppressWarnings("unchecked")
  public static void setAllLogLevels(Level newLevel, boolean temp) {
    if ((newLevel == null) || (newLevel == tempAllLogLevel)) {
      return;
    }
    synchronized (LOGGER_RESET_MAP) {
      if (temp && !LOGGER_RESET_MAP.isEmpty()) {
        LOGGER_RESET_MAP.forEach((logger, level) -> logger.setLevel(level));
        LOGGER_RESET_MAP.clear();
      }
      List<Logger> loggers = ListUtils
        .union(
          Arrays.asList(LogManager.getRootLogger()),
          EnumerationUtils.toList((Enumeration<Logger>) LogManager.getLoggerRepository().getCurrentLoggers())
        )
        .stream()
        .filter(logger -> logger.getLevel() != null)
        .collect(Collectors.toList());
      if (temp) {
        loggers.forEach(logger -> LOGGER_RESET_MAP.put(logger, logger.getLevel()));
        tempAllLogLevel = newLevel;
      }
      loggers.forEach(logger -> logger.setLevel(newLevel));
    }
  }

  public static LogIntervalData startLogInterval(final Logger logger, final long total, final String msg) {
    LogIntervalData data = new LogIntervalData(total, msg);
    return data;
  }

  public static void uninstallGcMonitoring() {
    synchronized (GC_MONITORS) {
      if (!GC_MONITORS.isEmpty()) {
        logWarn(LOG, "Uninstalling GC monitoring...");
      }
      GC_MONITORS.removeIf(pair -> {
        try {
          pair.getLeft().removeNotificationListener(pair.getRight());
          return true;
        } catch (Throwable e) {
          Logs.logError(LOG, e, "Issue removing GC monitor: %s", pair);
          return false;
        }
      });
    }
  }

  private static void saveErrorIfNeeded(final Logger logger, final SavedErrorMessageSeverity severity, final Throwable t, final String errorMessage) {
    Boolean allowHandler = DISABLE_CUSTOM_SAVED_ERROR_HANDLER.get();
    saveErrorIfNeeded(logger, severity, t, errorMessage, allowHandler == null || !allowHandler);
  }

  private static void saveErrorIfNeeded(
    final Logger logger,
    final SavedErrorMessageSeverity severity,
    final Throwable t,
    final String errorMessage,
    final boolean allowSavedErrorHandler
  ) {
    if ((DISABLE_SAVED_ERRORS.get() != null) && DISABLE_SAVED_ERRORS.get().booleanValue()) {
      return;
    }
    SavedErrorMessage savedError =
      new SavedErrorMessage(severity, t, errorMessage, substringAfterLast(logger.getName(), "."), logger.getName(), currentThread().getName());
    boolean keepErrors = ((savedErrorMessageHandler != null) && allowSavedErrorHandler && keepErrorMessages) ||
      (keepErrorMessages && (ERROR_MESSAGES.size() < MAX_ERROR_MESSAGES));
    if (keepErrors &&
      ((savedError.getLogger() == null) || (skipErrorsForLoggers == null) || !skipErrorsForLoggers.contains(savedError.getLogger())) &&
      !doesSkipErrorsPatternMatch(
        savedError
      )) {
      if (allowSavedErrorHandler && (savedErrorMessageHandler != null)) {
        Boolean handled = savedErrorMessageHandler.apply(savedError);
        if ((handled != null) && handled) {
          return;
        }
      }
      addErrorMessage(savedError);
    }
  }

}
