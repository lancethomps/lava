package com.github.lancethomps.lava.common.logging;

import static com.github.lancethomps.lava.common.format.Formatting.bytesToMegaBytes;
import static com.github.lancethomps.lava.common.format.Formatting.getMessage;
import static com.github.lancethomps.lava.common.time.Timing.NANO_2_MILLIS;
import static com.github.lancethomps.lava.common.time.Timing.formatMillis;
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

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.Patterns;
import com.github.lancethomps.lava.common.format.Formatting;
import com.github.lancethomps.lava.common.string.StringUtil;
import com.github.lancethomps.lava.common.time.Stopwatch;
import com.sun.management.GarbageCollectionNotificationInfo;

/**
 * Utility class for Log4j logging to avoid string evaluation when logging is disabled at the
 * DEBUG/INFO level.
 *
 * <p>
 * Proper usage: {@code Logs.logDebug(LOG, exception, "There was an error processing ID %s", id);}
 * </p>
 *
 * <p>
 * Improper usage: {@code Logs.logDebug(LOG, exception, "There was an error processing ID " + id);}
 * </p>
 */
@SuppressWarnings("restriction")
public final class Logs {

	/** The Constant ENABLE_TRACE_MSG. */
	public static final String ENABLE_TRACE_MSG = "<enable_TRACE_to_show>";

	/** The Constant LOG_INTERVAL. */
	public static final double LOG_INTERVAL = 0.01;

	/** The Constant MAX_ERROR_MESSAGES. */
	public static final int MAX_ERROR_MESSAGES = 100;

	/** The Constant SPLUNK_LOG_KEY_VALUE_EXTRACTOR. */
	// [^\w](\w[a-zA-Z0-9_\.]+)=("((?:(?<!\\)(?:\\{2})*\\"|[^"])+(?<!\\)(?:\\{2})*)"|([^ ]+))
	// [^\w](\w[a-zA-Z0-9_\.]+)=("((?:\\"|[^"])+(?<!\\))"|([^ ]+))
	public static final Pattern SPLUNK_LOG_KEY_VALUE_EXTRACTOR = Pattern.compile(
		"[^\\w](\\w[a-zA-Z0-9_\\.]+)=(\"((?:(?<!\\\\)(?:\\\\{2})*\\\\\"|[^\"])+(?<!\\\\)(?:\\\\{2})*)\"|([^ ]+))"
	);

	/** The Constant DISABLE_CUSTOM_SAVED_ERROR_HANDLER. */
	private static final InheritableThreadLocal<Boolean> DISABLE_CUSTOM_SAVED_ERROR_HANDLER = new InheritableThreadLocal<>();

	/** The Constant DISABLE_SAVED_ERRORS. */
	private static final InheritableThreadLocal<Boolean> DISABLE_SAVED_ERRORS = new InheritableThreadLocal<>();

	/** The error messages. */
	private static final List<SavedErrorMessage> ERROR_MESSAGES = new ArrayList<>();

	/** The Constant GC_MONITORS. */
	private static final List<Pair<NotificationEmitter, NotificationListener>> GC_MONITORS = new ArrayList<>();

	/** The keep error messages. */
	private static boolean keepErrorMessages;

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(Logs.class);

	/** The Constant LOGGER_RESET_MAP. */
	private static final Map<Logger, Level> LOGGER_RESET_MAP = new HashMap<>();

	/** The Constant MILLIS_FORMAT. */
	private static final DecimalFormat MILLIS_FORMAT = new DecimalFormat("#,###");

	/** The monitor garbage collectors. */
	private static boolean monitorGarbageCollectors;

	/** The saved error message handler. */
	private static Function<SavedErrorMessage, Boolean> savedErrorMessageHandler;

	/** The skip errors for loggers. */
	private static Set<String> skipErrorsForLoggers;

	/** The skip errors patterns. */
	private static List<Pattern> skipErrorsPatterns;

	/** The splunk key value string max length. */
	private static int splunkKeyValueStringMaxLength = NumberUtils.toInt(System.getProperty("wtp.splunkKeyValueStringMaxLength"), 5000);

	/** The system out prefix. */
	private static String systemOutPrefix;

	/** The temp all log level. */
	private static Level tempAllLogLevel;

	/** The Constant TIME_FORMAT. */
	private static final DecimalFormat TIME_FORMAT = new DecimalFormat("#,###");

	/** The Constant TIMER_MSG_SHELL. */
	private static final String TIMER_MSG_SHELL = "TIMER - %s completed in %sms. %s";

	/**
	 * Adds the error message.
	 *
	 * @param savedError the saved error
	 */
	public static void addErrorMessage(SavedErrorMessage savedError) {
		synchronized (ERROR_MESSAGES) {
			ERROR_MESSAGES.add(savedError);
		}
	}

	/**
	 * As pct number.
	 *
	 * @param numerator the numerator
	 * @param denominator the denominator
	 * @return the double
	 */
	public static double asPctNumber(long numerator, long denominator) {
		return (double) (numerator * 100) / (double) denominator;
	}

	/**
	 * Creates the logging proxy.
	 *
	 * @param realPrintStream the real print stream
	 * @param logger the logger
	 * @param level the level
	 * @return the prints the stream
	 */
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

	/**
	 * Disable thread saved errors.
	 */
	public static void disableThreadSavedErrors() {
		DISABLE_SAVED_ERRORS.set(true);
	}

	/**
	 * Disable thread saved errors.
	 */
	public static void disableThreadSavedErrorsCustomHandler() {
		DISABLE_CUSTOM_SAVED_ERROR_HANDLER.set(true);
	}

	/**
	 * Does skip errors pattern match.
	 *
	 * @param msg the msg
	 * @return true, if successful
	 */
	public static boolean doesSkipErrorsPatternMatch(@Nonnull final SavedErrorMessage msg) {
		final List<Pattern> skipErrorsPatterns = Logs.skipErrorsPatterns;
		if (skipErrorsPatterns == null) {
			return false;
		}
		return skipErrorsPatterns.stream().anyMatch(p -> p.matcher(msg.toString()).matches());
	}

	/**
	 * Enable thread saved errors.
	 */
	public static void enableThreadSavedErrors() {
		DISABLE_SAVED_ERRORS.remove();
	}

	/**
	 * Enable thread saved errors.
	 */
	public static void enableThreadSavedErrorsCustomHandler() {
		DISABLE_CUSTOM_SAVED_ERROR_HANDLER.remove();
	}

	/**
	 * Find root cause.
	 *
	 * @param e the e
	 * @return the throwable
	 */
	public static Throwable findRootCause(Throwable e) {
		Throwable cause = e.getCause();
		if (cause != null) {
			return findRootCause(cause);
		}
		return e;
	}

	/**
	 * Find splunk log key value matches.
	 *
	 * @param input the input
	 * @return the map
	 */
	public static Map<String, String> findSplunkLogKeyValueMatches(@Nonnull String input) {
		return Patterns.findKeyValueMatches(SPLUNK_LOG_KEY_VALUE_EXTRACTOR, input, Arrays.asList(1), Arrays.asList(3, 4), val -> StringUtils.replace(val, "\\\"", "\""));
	}

	/**
	 * Generate stack trace.
	 *
	 * @param e the e
	 * @return the string
	 */
	public static String generateStackTrace(Throwable e) {
		if (e != null) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer, false));
			return writer.toString();
		}
		return null;
	}

	/**
	 * Gets the error messages.
	 *
	 * @return the error messages
	 */
	public static List<SavedErrorMessage> getErrorMessages() {
		List<SavedErrorMessage> messages = new ArrayList<>(ERROR_MESSAGES);
		synchronized (ERROR_MESSAGES) {
			ERROR_MESSAGES.clear();
		}
		return messages;
	}

	/**
	 * @return the savedErrorMessageHandler
	 */
	public static Function<SavedErrorMessage, Boolean> getSavedErrorMessageHandler() {
		return savedErrorMessageHandler;
	}

	/**
	 * @return the skipErrorsForLoggers
	 */
	public static Set<String> getSkipErrorsForLoggers() {
		return skipErrorsForLoggers;
	}

	/**
	 * @return the skipErrorsPatterns
	 */
	public static List<Pattern> getSkipErrorsPatterns() {
		return skipErrorsPatterns;
	}

	/**
	 * Gets the splunk key value string.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param data the data
	 * @return the splunk key value string
	 */
	public static <K extends Object, V extends Object> String getSplunkKeyValueString(Map<K, V> data) {
		if (data == null) {
			return null;
		} else if (data.isEmpty()) {
			return "";
		}
		return data.entrySet().stream().map(e -> getSplunkKeyValueString(e.getKey(), e.getValue())).collect(Collectors.joining(" "));
	}

	/**
	 * Gets the splunk key value string.
	 *
	 * @param key the key
	 * @param value the value
	 * @return the splunk key value string
	 */
	public static String getSplunkKeyValueString(Object key, Object value) {
		return (key == null ? null : key.toString()) + '=' + getSplunkValueString(value);
	}

	/**
	 * @return the splunkKeyValueStringMaxLength
	 */
	public static int getSplunkKeyValueStringMaxLength() {
		return splunkKeyValueStringMaxLength;
	}

	/**
	 * Gets the splunk value string.
	 *
	 * @param value the value
	 * @return the splunk value string
	 */
	public static String getSplunkValueString(Object value) {
		String valueStr = value == null ? null
			: splunkKeyValueStringMaxLength > 0 ? StringUtil.truncateStringIfNeeded(value.toString(), splunkKeyValueStringMaxLength, StringUtil.TRUNCATE_STRING_ELLIPSIS) : value.toString();
		if ((valueStr == null) || (!StringUtils.containsAny(valueStr, '"', '=') && !StringUtil.CONTAINS_SPACES_REGEX.matcher(valueStr).find())) {
			return valueStr;
		}
		return '"' + StringUtils.replace(valueStr, "\"", "\\\"") + '"';
	}

	/**
	 * @return the systemOutPrefix
	 */
	public static String getSystemOutPrefix() {
		return systemOutPrefix;
	}

	/**
	 * @return the tempAllLogLevel
	 */
	public static Level getTempAllLogLevel() {
		return tempAllLogLevel;
	}

	/**
	 * Checks for temp all log level.
	 *
	 * @return true, if successful
	 */
	public static boolean hasTempAllLogLevel() {
		return tempAllLogLevel != null;
	}

	/**
	 * Install GC monitoring.
	 */
	public static void installGcMonitoring() {
		synchronized (GC_MONITORS) {
			uninstallGcMonitoring();
			logWarn(LOG, "Installing GC monitoring...");
			// get all the GarbageCollectorMXBeans - there's one for each heap generation
			// so probably two - the old generation and young generation
			List<GarbageCollectorMXBean> gcbeans = ManagementFactory.getGarbageCollectorMXBeans();
			// Install a notifcation handler for each bean
			for (GarbageCollectorMXBean gcbean : gcbeans) {
				NotificationEmitter emitter = (NotificationEmitter) gcbean;
				AtomicLong totalGcDuration = new AtomicLong(0);
				// use an anonymously generated listener for this example
				// - proper code should really use a named class
				NotificationListener listener = (Notification notification, Object handback) -> {
					// we only handle GARBAGE_COLLECTION_NOTIFICATION notifications here
					if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
						// get the information associated with this notification
						GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
						// get all the info and pretty print it
						long duration = info.getGcInfo().getDuration();
						String gctype = info.getGcAction();
						if ("end of minor GC".equals(gctype)) {
							gctype = "Young Gen GC";
						} else if ("end of major GC".equals(gctype)) {
							gctype = "Old Gen GC";
						}
						StringBuilder msg = new StringBuilder();
						msg.append(
							gctype + ": - " + info.getGcInfo().getId() + ' ' + info.getGcName() + " (from " + info.getGcCause() + ") " + duration + " milliseconds; start-end times " + info
								.getGcInfo()
								.getStartTime() + '-' + info.getGcInfo().getEndTime() + ' '
						);

						// Get the information about each memory space, and pretty print it
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
							long percent = ((memUsed * 1000L) / before.getCommitted()); // >100% when it gets expanded

							msg.append(
								name + (memCommitted == memMax ? " (FULL) " : " (can expand) ") + "used: " + (beforepercent / 10) + '.' + (beforepercent % 10) + "%->" + (percent / 10) + '.' + (percent
									% 10) + "%(" + ((memUsed / 1048576) + 1) + "MB) / "
							);
						}
						totalGcDuration.addAndGet(info.getGcInfo().getDuration());
						long percent = (totalGcDuration.get() * 1000L) / info.getGcInfo().getEndTime();
						msg.append("GC cumulated overhead " + (percent / 10) + '.' + (percent % 10) + '%');
						BigDecimal reduction = bytesToMegaBytes(totalMemBefore - totalMemAfter);
						BigDecimal reductionPct = BigDecimal.valueOf((double) (totalMemBefore - totalMemAfter) / (double) totalMemBefore).movePointRight(2).setScale(2, RoundingMode.HALF_DOWN);
						Logs.logDebug(
							LOG,
							"GC monitoring before [%s], after [%s], reduction [%s] - pct [%s]: %s",
							bytesToMegaBytes(totalMemBefore),
							bytesToMegaBytes(totalMemAfter),
							reduction,
							reductionPct,
							msg
						);
					}
				};

				// Add the listener
				emitter.addNotificationListener(listener, null, null);
				GC_MONITORS.add(Pair.of(emitter, listener));
			}
		}
	}

	/**
	 * @return the keepErrorMessages
	 */
	public static boolean isKeepErrorMessages() {
		return keepErrorMessages;
	}

	/**
	 * @return the monitorGarbageCollectors
	 */
	public static boolean isMonitorGarbageCollectors() {
		return monitorGarbageCollectors;
	}

	/**
	 * Log debug.
	 *
	 * @param logger the logger
	 * @param message the message
	 * @param formatArgs the formatArgs
	 */
	public static void logDebug(final Logger logger, final String message, final Object... formatArgs) {
		logDebug(logger, null, message, formatArgs);
	}

	/**
	 * Log debug.
	 *
	 * @param logger the logger
	 * @param throwable the throwable
	 * @param message the message
	 * @param formatArgs the formatArgs
	 */
	public static void logDebug(final Logger logger, final Throwable throwable, final String message, final Object... formatArgs) {
		if (logger.isDebugEnabled()) {
			logger.debug(getMessage(message, formatArgs), throwable);
		}
	}

	/**
	 * Log end.
	 *
	 * @param logger the logger
	 * @param level the level
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logEnd(final Logger logger, final Level level, final String message, final Object... formatArgs) {
		if (logger.isEnabledFor(level)) {
			logger.log(level, getMessage("END - " + message, formatArgs));
		}
	}

	/**
	 * Log end.
	 *
	 * @param logger the logger
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logEnd(final Logger logger, final String message, final Object... formatArgs) {
		logStart(logger, Level.TRACE, message, formatArgs);
	}

	/**
	 * Log error.
	 *
	 * @param logger the logger
	 * @param severity the severity
	 * @param t the t
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logError(final Logger logger, final SavedErrorMessageSeverity severity, final Throwable t, final String message, final Object... formatArgs) {
		String errorMessage = getMessage(message, formatArgs);
		logger.error(errorMessage, t);
		saveErrorIfNeeded(logger, severity, t, errorMessage);
	}

	/**
	 * Log error.
	 *
	 * @param logger the logger
	 * @param t the t
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logError(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
		logError(logger, null, t, message, formatArgs);
	}

	/**
	 * Log error high.
	 *
	 * @param logger the logger
	 * @param t the t
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logErrorHigh(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
		logError(logger, SavedErrorMessageSeverity.HIGH, t, message, formatArgs);
	}

	/**
	 * Log error low.
	 *
	 * @param logger the logger
	 * @param t the t
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logErrorLow(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
		logError(logger, SavedErrorMessageSeverity.LOW, t, message, formatArgs);
	}

	/**
	 * Log error medium.
	 *
	 * @param logger the logger
	 * @param t the t
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logErrorMedium(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
		logError(logger, SavedErrorMessageSeverity.MEDIUM, t, message, formatArgs);
	}

	/**
	 * Log error without keeping.
	 *
	 * @param logger the logger
	 * @param t the t
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logErrorWithoutKeeping(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
		String errorMessage = getMessage(message, formatArgs);
		logger.error(errorMessage, t);
	}

	/**
	 * Log error without saved error handler.
	 *
	 * @param logger the logger
	 * @param t the t
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logErrorWithoutSavedErrorHandler(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
		String errorMessage = getMessage(message, formatArgs);
		logger.error(errorMessage, t);
		saveErrorIfNeeded(logger, SavedErrorMessageSeverity.HIGH, t, errorMessage, false);
	}

	/**
	 * Log fatal.
	 *
	 * @param logger the logger
	 * @param t the t
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logFatal(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
		String errorMessage = getMessage(message, formatArgs);
		logger.fatal(errorMessage, t);
		saveErrorIfNeeded(logger, SavedErrorMessageSeverity.HIGH, t, errorMessage);
	}

	/**
	 * Log for splunk.
	 *
	 * @param logger the logger
	 * @param level the level
	 * @param splunkId the splunk id
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logForSplunk(
		@Nonnull final Logger logger,
		@Nonnull final Level level,
		@Nonnull final Object splunkId,
		@Nullable final String message,
		@Nullable final Object... formatArgs
	) {
		logForSplunk(logger, level, null, splunkId, message, formatArgs);
	}

	/**
	 * Log for splunk.
	 *
	 * @param logger the logger
	 * @param level the level
	 * @param t the t
	 * @param splunkId the splunk id
	 * @param message the message
	 * @param formatArgs the format args
	 */
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
			String msg = "SPLUNK@" + getMessage(Checks.isBlank(message) ? splunkId.toString() : (splunkId.toString() + '|' + message), formatArgs);
			logger.log(level, msg, t);
		}
	}

	/**
	 * Log for splunk.
	 *
	 * @param logger the logger
	 * @param splunkId the splunk id
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logForSplunk(@Nonnull final Logger logger, @Nonnull final Object splunkId, @Nullable final String message, @Nullable final Object... formatArgs) {
		logForSplunk(logger, Level.WARN, splunkId, message, formatArgs);
	}

	/**
	 * Log for splunk.
	 *
	 * @param logger the logger
	 * @param t the t
	 * @param splunkId the splunk id
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logForSplunk(
		@Nonnull final Logger logger,
		@Nullable final Throwable t,
		@Nonnull final Object splunkId,
		@Nullable final String message,
		@Nullable final Object... formatArgs
	) {
		logForSplunk(logger, t == null ? Level.WARN : Level.ERROR, t, splunkId, message, formatArgs);
	}

	/**
	 * Log for splunk with key val args.
	 *
	 * @param logger the logger
	 * @param level the level
	 * @param t the t
	 * @param splunkId the splunk id
	 * @param message the message
	 * @param formatArgs the format args
	 */
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

	/**
	 * Log for splunk with key val args.
	 *
	 * @param logger the logger
	 * @param splunkId the splunk id
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logForSplunkWithKeyValArgs(
		@Nonnull final Logger logger,
		@Nonnull final Object splunkId,
		@Nullable final String message,
		@Nullable final Object... formatArgs
	) {
		logForSplunkWithKeyValArgs(logger, Level.WARN, null, splunkId, message, formatArgs);
	}

	/**
	 * Log info.
	 *
	 * @param logger the logger
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logInfo(final Logger logger, final String message, final Object... formatArgs) {
		logInfo(logger, null, message, formatArgs);
	}

	/**
	 * Log info.
	 *
	 * @param logger the logger
	 * @param throwable the throwable
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logInfo(final Logger logger, final Throwable throwable, final String message, final Object... formatArgs) {
		if (logger.isInfoEnabled()) {
			logger.info(getMessage(message, formatArgs), throwable);
		}
	}

	/**
	 * Log interval.
	 *
	 * @param logger the logger
	 * @param count the count
	 * @param total the total
	 * @param nextLogPct the next log pct
	 * @param logInterval the log interval
	 * @param msg the msg
	 * @param watch the watch
	 * @return the double
	 */
	public static double logInterval(final Logger logger, int count, int total, double nextLogPct, double logInterval, final String msg, final Stopwatch watch) {
		double pct = (double) count / (double) total;
		if ((pct > nextLogPct) || (count == total)) {
			nextLogPct += logInterval;
			logIntervalMsg(logger, msg, count, total, watch);
		}
		return nextLogPct;
	}

	/**
	 * Log interval.
	 *
	 * @param logger the logger
	 * @param data the data
	 * @return true, if successful
	 */
	public static boolean logInterval(final Logger logger, final LogIntervalData data) {
		return logInterval(logger, data, 1);
	}

	/**
	 * Log interval.
	 *
	 * @param logger the logger
	 * @param data the data
	 * @param add the add
	 * @return true, if successful
	 */
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

	/**
	 * Log interval msg.
	 *
	 * @param logger the logger
	 * @param msg the msg
	 * @param count the count
	 * @param total the total
	 * @param watch the watch
	 */
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

	/**
	 * Log level.
	 *
	 * @param logger the logger
	 * @param level the level
	 * @param t the t
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logLevel(final Logger logger, final Level level, final Throwable t, final String message, final Object... formatArgs) {
		if (level == Level.ERROR) {
			logError(logger, t, message, formatArgs);
		} else if (logger.isEnabledFor(level)) {
			String msg = getMessage(message, formatArgs);
			logger.log(level, msg, t);
		}
	}

	/**
	 * Log nested.
	 *
	 * @param <T> the generic type
	 * @param logger the logger
	 * @param docs the docs
	 * @param childrenGetter the children getter
	 * @param tabs the tabs
	 */
	public static <T> void logNested(final Logger logger, final Collection<T> docs, final Function<T, Collection<T>> childrenGetter, final String tabs) {
		docs.stream().peek(doc -> logDebug(logger, "%s%s", tabs, doc)).map(doc -> childrenGetter == null ? null : childrenGetter.apply(doc)).filter(Checks::isNotEmpty).forEach(
			children -> logNested(logger, children, childrenGetter, tabs + '\t')
		);
	}

	/**
	 * Log start.
	 *
	 * @param logger the logger
	 * @param level the level
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logStart(final Logger logger, final Level level, final String message, final Object... formatArgs) {
		if (logger.isEnabledFor(level)) {
			logger.log(level, getMessage("START - " + message, formatArgs));
		}
	}

	/**
	 * Log start.
	 *
	 * @param logger the logger
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logStart(final Logger logger, final String message, final Object... formatArgs) {
		logStart(logger, Level.TRACE, message, formatArgs);
	}

	/**
	 * Log timer.
	 *
	 * @param logger the logger
	 * @param start the start
	 * @param process the process
	 */
	public static void logTimer(final Logger logger, final long start, final String process) {
		logTimer(logger, start > 1000000000 ? new Date().getTime() - start : start, process, EMPTY);
	}

	/**
	 * Log timer.
	 *
	 * @param logger the logger
	 * @param timeTaken the time taken
	 * @param process the process
	 * @param processInfo the process info
	 */
	public static void logTimer(final Logger logger, final long timeTaken, final String process, final String processInfo) {
		logTimer(logger, timeTaken, process, processInfo, null);
	}

	/**
	 * Log timer.
	 *
	 * @param logger the logger
	 * @param timeTaken the time taken
	 * @param process the process
	 * @param processInfo the process info
	 * @param level the level
	 */
	public static void logTimer(final Logger logger, final long timeTaken, final String process, final String processInfo, final Level level) {
		logTimerMillis(logger, timeTaken, process, processInfo, level, false);
	}

	/**
	 * Log timer.
	 *
	 * @param logger the logger
	 * @param timeTakenNanos the time taken
	 * @param process the process
	 * @param processInfo the process info
	 * @param level the level
	 * @param withholdProcessInfo the withhold process info
	 */
	public static void logTimer(final Logger logger, final long timeTakenNanos, final String process, final String processInfo, final Level level, final boolean withholdProcessInfo) {
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

	/**
	 * Log timer.
	 *
	 * @param logger the logger
	 * @param watch the watch
	 * @param process the process
	 */
	public static void logTimer(final Logger logger, final Stopwatch watch, final String process) {
		logTimer(logger, watch, process, (Level) null);
	}

	/**
	 * Log timer.
	 *
	 * @param logger the logger
	 * @param watch the watch
	 * @param process the process
	 * @param level the level
	 */
	public static void logTimer(final Logger logger, final Stopwatch watch, final String process, final Level level) {
		logTimer(logger, watch, process, null, level);
	}

	/**
	 * Log timer.
	 *
	 * @param logger the logger
	 * @param watch the watch
	 * @param process the process
	 * @param processInfo the process info
	 */
	public static void logTimer(final Logger logger, final Stopwatch watch, final String process, final String processInfo) {
		logTimer(logger, watch, process, processInfo, null);
	}

	/**
	 * Log timer.
	 *
	 * @param logger the logger
	 * @param watch the watch
	 * @param process the process
	 * @param processInfo the process info
	 * @param level the level
	 */
	public static void logTimer(final Logger logger, final Stopwatch watch, final String process, final String processInfo, final Level level) {
		logTimer(logger, watch, process, processInfo, level, false);
	}

	/**
	 * Log timer.
	 *
	 * @param logger the logger
	 * @param watch the watch
	 * @param process the process
	 * @param processInfo the process info
	 * @param level the level
	 * @param withholdProcessInfo the withhold process info
	 */
	public static void logTimer(final Logger logger, final Stopwatch watch, final String process, final String processInfo, final Level level, final boolean withholdProcessInfo) {
		logTimer(logger, watch.getNanoTime(), process, processInfo, level, withholdProcessInfo);
	}

	/**
	 * Log timer.
	 *
	 * @param logger the logger
	 * @param timeTaken the time taken
	 * @param process the process
	 * @param processInfo the process info
	 * @param level the level
	 * @param withholdProcessInfo the withhold process info
	 */
	public static void logTimerMillis(final Logger logger, final long timeTaken, final String process, final String processInfo, final Level level, final boolean withholdProcessInfo) {
		if (level != null) {
			String timeStr = timeTaken > 9999 ? TIME_FORMAT.format(timeTaken) : String.valueOf(timeTaken);
			logger.log(level, format(TIMER_MSG_SHELL, process, timeStr, withholdProcessInfo ? EMPTY : defaultIfBlank(processInfo, EMPTY)));
		} else if (logger.isDebugEnabled()) {
			String timeStr = timeTaken > 9999 ? TIME_FORMAT.format(timeTaken) : String.valueOf(timeTaken);
			logger.debug(format(TIMER_MSG_SHELL, process, timeStr, withholdProcessInfo ? EMPTY : defaultIfBlank(processInfo, EMPTY)));
		}
	}

	/**
	 * Log timer per doc.
	 *
	 * @param logger the logger
	 * @param watch the watch
	 * @param process the process
	 * @param total the total
	 */
	public static void logTimerPerObject(final Logger logger, final Stopwatch watch, final String process, final int total) {
		long totalTime = watch.getTime();
		long perDoc = total > 0 ? ((long) Math.ceil((double) totalTime / (double) total)) : totalTime;
		logTimer(logger, perDoc, process + " Per Element (" + total + ')', String.format("Total: %sms", totalTime));
	}

	/**
	 * Log trace.
	 *
	 * @param logger the logger
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logTrace(final Logger logger, final String message, final Object... formatArgs) {
		logTrace(logger, null, message, formatArgs);
	}

	/**
	 * Log trace.
	 *
	 * @param logger the logger
	 * @param throwable the throwable
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logTrace(final Logger logger, final Throwable throwable, final String message, final Object... formatArgs) {
		if (logger.isTraceEnabled()) {
			logger.trace(getMessage(message, formatArgs), throwable);
		}
	}

	/**
	 * Log warn.
	 *
	 * @param logger the logger
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logWarn(final Logger logger, final String message, final Object... formatArgs) {
		logWarn(logger, null, message, formatArgs);
	}

	/**
	 * Log warn.
	 *
	 * @param logger the logger
	 * @param t the t
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void logWarn(final Logger logger, final Throwable t, final String message, final Object... formatArgs) {
		logger.warn(getMessage(message, formatArgs), t);
	}

	/**
	 * Printerr.
	 *
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void printerr(final Object message, final Object... formatArgs) {
		printerr(null, message, formatArgs);
	}

	/**
	 * Printerr.
	 *
	 * @param e the e
	 * @param message the message
	 * @param formatArgs the format args
	 */
	// CHECKSTYLE.OFF: SystemOutLogging
	public static void printerr(final Throwable e, final Object message, final Object... formatArgs) {
		System.err.println(getMessage(message.toString(), formatArgs));
		if (e != null) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Println.
	 *
	 * @param message the message
	 * @param formatArgs the format args
	 */
	public static void println(final Object message, final Object... formatArgs) {
		System.out.println(Checks.defaultIfNull(systemOutPrefix, "") + getMessage(message == null ? null : message.toString(), formatArgs));
	}

	/**
	 * Prints the percent complete.
	 *
	 * @param message the message
	 * @param pct the pct
	 * @param formatArgs the format args
	 */
	public static void printPercentComplete(final Object message, final double pct, final Object... formatArgs) {
		String progress = String.join("", Collections.nCopies((int) Math.floor(pct * 20), "#"));
		System.out.print(
			getMessage(message == null ? "" : message.toString(), formatArgs) + String.format("[%1$-20s] %2$3.2f", progress, (pct * 100d)) + '%' + (pct >= 0.99999 ? '\n' : '\r')
		);
	}
	// CHECKSTYLE.ON: SystemOutLogging

	/**
	 * Reset all log levels.
	 */
	public static void resetAllLogLevels() {
		synchronized (LOGGER_RESET_MAP) {
			if (!LOGGER_RESET_MAP.isEmpty()) {
				LOGGER_RESET_MAP.forEach((logger, level) -> logger.setLevel(level));
				LOGGER_RESET_MAP.clear();
			}
			tempAllLogLevel = null;
		}
	}

	/**
	 * Sets the all log levels.
	 *
	 * @param newLevel the new all log levels
	 */
	public static void setAllLogLevels(Level newLevel) {
		setAllLogLevels(newLevel, false);
	}

	/**
	 * Sets the all log levels.
	 *
	 * @param newLevel the new level
	 * @param temp the temp
	 */
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
				.union(Arrays.asList(LogManager.getRootLogger()), EnumerationUtils.toList((Enumeration<Logger>) LogManager.getLoggerRepository().getCurrentLoggers()))
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

	/**
	 * @param keepErrorMessages the keepErrorMessages to set
	 */
	public static void setKeepErrorMessages(boolean keepErrorMessages) {
		Logs.keepErrorMessages = keepErrorMessages;
	}

	/**
	 * @param monitorGarbageCollectors the monitorGarbageCollectors to set
	 */
	public static void setMonitorGarbageCollectors(boolean monitorGarbageCollectors) {
		Logs.monitorGarbageCollectors = monitorGarbageCollectors;
		if (monitorGarbageCollectors) {
			installGcMonitoring();
		} else {
			uninstallGcMonitoring();
		}
	}

	/**
	 * @param savedErrorMessageHandler the savedErrorMessageHandler to set
	 */
	public static void setSavedErrorMessageHandler(Function<SavedErrorMessage, Boolean> savedErrorMessageHandler) {
		Logs.savedErrorMessageHandler = savedErrorMessageHandler;
	}

	/**
	 * Sets the skip errors for loggers.
	 *
	 * @param skipErrorsForLoggers the new skip errors for loggers
	 */
	public static void setSkipErrorsForLoggers(Set<String> skipErrorsForLoggers) {
		Logs.skipErrorsForLoggers = skipErrorsForLoggers;
	}

	/**
	 * @param skipErrorsPatterns the skipErrorsPatterns to set
	 */
	public static void setSkipErrorsPatterns(List<Pattern> skipErrorsPatterns) {
		Logs.skipErrorsPatterns = skipErrorsPatterns;
	}

	/**
	 * @param splunkKeyValueStringMaxLength the splunkKeyValueStringMaxLength to set
	 */
	public static void setSplunkKeyValueStringMaxLength(int splunkKeyValueStringMaxLength) {
		Logs.splunkKeyValueStringMaxLength = splunkKeyValueStringMaxLength;
	}

	/**
	 * @param systemOutPrefix the systemOutPrefix to set
	 */
	public static void setSystemOutPrefix(String systemOutPrefix) {
		Logs.systemOutPrefix = systemOutPrefix;
	}

	/**
	 * Start log interval.
	 *
	 * @param logger the logger
	 * @param total the total
	 * @param msg the msg
	 * @return the log interval data
	 */
	public static LogIntervalData startLogInterval(final Logger logger, final long total, final String msg) {
		LogIntervalData data = new LogIntervalData(total, msg);
		return data;
	}

	/**
	 * Uninstall gc monitoring.
	 */
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

	/**
	 * Save error if needed.
	 *
	 * @param logger the logger
	 * @param severity the severity
	 * @param t the t
	 * @param errorMessage the error message
	 */
	private static void saveErrorIfNeeded(final Logger logger, final SavedErrorMessageSeverity severity, final Throwable t, final String errorMessage) {
		Boolean allowHandler = DISABLE_CUSTOM_SAVED_ERROR_HANDLER.get();
		saveErrorIfNeeded(logger, severity, t, errorMessage, allowHandler != null ? !allowHandler : true);
	}

	/**
	 * Save error if needed.
	 *
	 * @param logger the logger
	 * @param severity the severity
	 * @param t the t
	 * @param errorMessage the error message
	 * @param allowSavedErrorHandler the allow saved error handler
	 */
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
		SavedErrorMessage savedError = new SavedErrorMessage(severity, t, errorMessage, substringAfterLast(logger.getName(), "."), logger.getName(), currentThread().getName());
		boolean keepErrors = ((savedErrorMessageHandler != null) && allowSavedErrorHandler && keepErrorMessages) || (keepErrorMessages && (ERROR_MESSAGES.size() < MAX_ERROR_MESSAGES));
		if (keepErrors && ((savedError.getLogger() == null) || (skipErrorsForLoggers == null) || !skipErrorsForLoggers.contains(savedError.getLogger())) && !doesSkipErrorsPatternMatch(
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
