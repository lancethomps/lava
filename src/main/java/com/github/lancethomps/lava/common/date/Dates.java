package com.github.lancethomps.lava.common.date;

import static com.github.lancethomps.lava.common.lambda.Lambdas.functionIfNonNull;
import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.Enums;
import com.github.lancethomps.lava.common.collections.FastHashMap;
import com.github.lancethomps.lava.common.lambda.Lambdas;
import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class DateUtil.
 */
public class Dates {

	/** The Constant ASC_DATE_COMPARATOR. */
	public static final Comparator<LocalDate> ASC_LOCAL_DATE_COMPARATOR = (d1, d2) -> d1.compareTo(d2);

	/** The Constant DATE_ENT_FORMAT. */
	public static final DateTimeFormatter DATE_ENT_FORMAT = formatterFromPattern("dd-MMM-yyyy");

	/** The Constant DD_MMM_YY_PATTERN. */
	public static final Pattern DD_MMM_YY_PATTERN = Pattern.compile("(\\d+)[-/]{0,1}([a-zA-Z]{3})[-/']{0,1}(\\d+)");

	/**
	 * The Constant DEBUG_FORMAT_LONG.
	 *
	 * @see java.time.format.DateTimeFormatter#RFC_1123_DATE_TIME
	 */
	public static final DateTimeFormatter DEBUG_FORMAT_LONG;

	/** The Constant DEFAULT_CAL_NAME. */
	public static final String DEFAULT_CAL_NAME = "US";

	/** The Constant DEFAULT_TZ. */
	public static final TimeZone DEFAULT_TZ = TimeZone.getTimeZone("GMT");

	/** The Constant DEFAULT_ZONE. */
	public static final ZoneOffset DEFAULT_ZONE = ZoneOffset.UTC;

	/** The Constant DESC_LOCAL_DATE_COMPARATOR. */
	public static final Comparator<LocalDate> DESC_LOCAL_DATE_COMPARATOR = (d1, d2) -> d2.compareTo(d1);

	/** The Constant EN_DOW. */
	public static final Map<Long, String> EN_DOW;

	/** The Constant EN_MOY. */
	public static final Map<Long, String> EN_MOY;

	/** The Constant END_OF_MONTH. */
	public static final TemporalAdjuster END_OF_MONTH = TemporalAdjusters.lastDayOfMonth();

	/** The Constant END_OF_NEXT_MONTH. */
	public static final TemporalAdjuster END_OF_NEXT_MONTH = (temporal) -> temporal.plus(1L, ChronoUnit.MONTHS).with(TemporalAdjusters.lastDayOfMonth());

	/** The Constant END_OF_PREV_MONTH. */
	public static final TemporalAdjuster END_OF_PREV_MONTH = (temporal) -> temporal.minus(-1L, ChronoUnit.MONTHS).with(TemporalAdjusters.lastDayOfMonth());

	/** The Constant ERROR_LOG. */
	public static final DateTimeFormatter ERROR_LOG_DATE_FORMAT = formatterFromPattern("dd-MMM-yyyy hh:mm:ss.SSS z");

	/** The Constant DASH_NUMS. */
	public static final DateTimeFormatter FORMAT_DASH_NUMS = formatterFromPattern("yyyy-MM-dd");

	/** The Constant DASH_TEXT. */
	public static final DateTimeFormatter FORMAT_DASH_TEXT = formatterFromPattern("dd-MMM-yyyy");

	/** The Constant INT_FORMAT. */
	public static final DateTimeFormatter INT_FORMAT = new DateTimeFormatterBuilder()
		.parseCaseInsensitive()
		.appendValue(YEAR, 4)
		.appendValue(MONTH_OF_YEAR, 2)
		.appendValue(DAY_OF_MONTH, 2)
		.optionalStart()
		.appendOffset("+HHMMss", "Z")
		.toFormatter();

	/** The Constant INTRADAY_DEBUG_FORMAT. */
	public static final DateTimeFormatter INTRADAY_DEBUG_FORMAT = new DateTimeFormatterBuilder()
		.parseCaseInsensitive()
		.appendPattern("dd-MMM-yyyy HH:mm:ss")
		.optionalStart()
		.appendPattern(".SSS")
		.optionalEnd()
		.optionalStart()
		.appendPattern(" z")
		.optionalEnd()
		.toFormatter();

	/** The Constant DEFAULT_INTRADAY_FORMAT. */
	public static final DateTimeFormatter INTRADAY_FORMAT = formatterFromPattern("yyyyMMdd_HH-mm-ss_SSS");

	/** The Constant DEFAULT_INTRADAY_DATE_FORMAT. */
	public static final DateTimeFormatter INTRADAY_NON_FILE_FORMAT = formatterFromPattern("dd-MMM-yyyy HH:mm:ss");

	/** The Constant INTRADAY_NUM_FORMAT. */
	public static final DateTimeFormatter INTRADAY_NUM_FORMAT = formatterFromPattern("yyyyMMddHHmmss");

	/** The Constant JULIAN_BASE. */
	public static final LocalDate JULIAN_BASE = LocalDate.of(1950, 01, 01);

	/** The Constant LOG. */
	public static final Logger LOG = Logger.getLogger(Dates.class);

	/** The Constant LOG4J_FORMAT. */
	public static final DateTimeFormatter LOG4J_FORMAT = formatterFromPattern("yyyy-MM-dd HH:mm:ss,SSS");

	/** The Constant MAX_DATE. */
	public static final LocalDate MAX_DATE = LocalDate.of(2222, 12, 31);

	/** The Constant MAX_DAY_INT. */
	public static final int MAX_DAY_INT = 22221231;

	/** The Constant MILLIS_PER_DAY. */
	public static final int MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

	/** The Constant RELATIVE_DATE_REGEX. */
	public static final Pattern RELATIVE_DATE_REGEX;

	/** The Constant RELATIVE_DATE_REGEX_PART. */
	public static final Pattern RELATIVE_DATE_REGEX_PART;

	/** The Constant SHORT_IDS. */
	public static final Map<String, String> SHORT_IDS = Collections.unmodifiableMap(
		MapUtils.putAll(
			new HashMap<>(ZoneId.SHORT_IDS),
			new String[] {
				"PDT", ZoneId.SHORT_IDS.get("PST"),
				"EDT", ZoneId.SHORT_IDS.get("EST")
			}
		)
	);

	/** The Constant SIMPLE_INTRADAY_FORMAT. */
	public static final DateTimeFormatter SIMPLE_INTRADAY_FORMAT = formatterFromPattern("yyyyMMdd_HH-mm-ss_z");

	/** The Constant SLASH_LONG. */
	public static final DateTimeFormatter SLASH_LONG = formatterFromPattern("MM/dd/yyyy");

	/** The Constant SLASH_LONG_INTL. */
	public static final DateTimeFormatter SLASH_LONG_INTL = formatterFromPattern("dd/MM/yyyy");

	/** The Constant SLASH_SHORT. */
	public static final DateTimeFormatter SLASH_SHORT = formatterFromPattern("M/d/yyyy");

	/** The Constant SLASH_VERY_SHORT. */
	public static final DateTimeFormatter SLASH_VERY_SHORT = formatterFromPattern("M/d/yy");

	/** The Constant SOLR_DATE_FORMAT. */
	public static final DateTimeFormatter SOLR_DATE_FORMAT = new DateTimeFormatterBuilder()
		.parseCaseInsensitive()
		.appendPattern("yyyy-MM-dd'T'HH:mm:ss")
		.optionalStart()
		.appendPattern(".SSS")
		.optionalEnd()
		.appendPattern("'Z'")
		.toFormatter();

	/** The Constant SOLR_DATE_TIMESTAMP_FORMAT. */
	public static final DateTimeFormatter SOLR_DATE_TIMESTAMP_FORMAT = new DateTimeFormatterBuilder()
		.parseCaseInsensitive()
		.appendPattern("yyyy-MM-dd'T'HH_mm_ss")
		.optionalStart()
		.appendPattern(".SSS")
		.optionalEnd()
		.appendPattern("'Z'")
		.toFormatter();

	/** The Constant SQL_FORMAT. */
	public static final DateTimeFormatter SQL_FORMAT = new DateTimeFormatterBuilder()
		.parseCaseInsensitive()
		.appendPattern("yyyy-MM-dd")
		.optionalStart()
		.appendPattern(" HH:mm:ss")
		.optionalEnd()
		.toFormatter();

	/** The Constant TIMESTAMP_FORMAT. */
	public static final DateTimeFormatter TIMESTAMP_FORMAT = formatterFromPattern("yyyy_MM_dd_HH_mm_ss_SS");

	/** The Constant PST. */
	public static final TimeZone TZ_PST = getTimeZoneByShortId("PST");

	/** The Constant UTC_FORMAT. */
	public static final DateTimeFormatter UTC_FORMAT = formatterFromPattern("yyyyMMdd");

	/** The Constant YYYYMM_FORMAT. */
	public static final DateTimeFormatter YYYYMM_FORMAT = formatterFromPattern("yyyyMM");

	/** The Constant ZONE_EST. */
	public static final ZoneOffset ZONE_EST = getZoneByShortId("EST");

	/** The Constant ZONE_PST. */
	public static final ZoneOffset ZONE_PST = getZoneByShortId("PST");

	/** The Constant DATE_CALCS. */
	private static final FastHashMap<String, HolidayDateCalc> DATE_CALCS = new FastHashMap<>(true);

	/** The holiday date calc creater. */
	private static Function<String, HolidayDateCalc> holidayDateCalcCreater = (calendarName) -> {
		return new JollydayHolidayDateCalc(calendarName);
	};

	static {
		TimeZone.setDefault(DEFAULT_TZ);

		String symbols = Stream.of(RelativeDateOperator.values()).map(RelativeDateOperator::getSymbol).sorted().map(Pattern::quote).collect(Collectors.joining());
		String dateMath = "(?:["
			+ symbols
			+ "]{1})(?:(?<=\\Q/\\E)|\\d+|[a-z]*(?=[a-z]{1}))(?:["
			+ Stream
				.of(RelativeDateType.values())
				.map(RelativeDateType::name)
				.flatMapToInt(String::chars)
				.distinct()
				.mapToObj(ch -> String.valueOf((char) ch))
				.sorted()
				.collect(Collectors.joining(""))
			+ "]{1,2})";
		RELATIVE_DATE_REGEX = Pattern.compile("(?i)^(?:T)((?:" + dateMath + ")*)$");
		RELATIVE_DATE_REGEX_PART = Pattern.compile("(?i)" + StringUtils.replace(dateMath, "?:", ""));
	}

	static {
		Map<Long, String> dow = new HashMap<>();
		dow.put(1L, "Mon");
		dow.put(2L, "Tue");
		dow.put(3L, "Wed");
		dow.put(4L, "Thu");
		dow.put(5L, "Fri");
		dow.put(6L, "Sat");
		dow.put(7L, "Sun");
		EN_DOW = Collections.unmodifiableMap(dow);
		Map<Long, String> moy = new HashMap<>();
		moy.put(1L, "Jan");
		moy.put(2L, "Feb");
		moy.put(3L, "Mar");
		moy.put(4L, "Apr");
		moy.put(5L, "May");
		moy.put(6L, "Jun");
		moy.put(7L, "Jul");
		moy.put(8L, "Aug");
		moy.put(9L, "Sep");
		moy.put(10L, "Oct");
		moy.put(11L, "Nov");
		moy.put(12L, "Dec");
		EN_MOY = Collections.unmodifiableMap(moy);
		DEBUG_FORMAT_LONG = new DateTimeFormatterBuilder()
			.parseCaseInsensitive()
			.parseLenient()
			.optionalStart()
			.appendText(ChronoField.DAY_OF_WEEK, dow)
			.appendLiteral(", ")
			.optionalEnd()
			.appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
			.appendLiteral(' ')
			.appendText(MONTH_OF_YEAR, moy)
			.appendLiteral(' ')
			.appendValue(YEAR, 4)
			.optionalStart()
			.appendLiteral(' ')
			.appendValue(ChronoField.HOUR_OF_DAY, 2)
			.optionalEnd()
			.optionalStart()
			.appendLiteral(':')
			.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
			.optionalEnd()
			.optionalStart()
			.appendLiteral(':')
			.appendValue(ChronoField.SECOND_OF_MINUTE, 2)
			.optionalEnd()
			.optionalStart()
			.appendLiteral('.')
			.appendValue(ChronoField.MILLI_OF_SECOND, 3)
			.optionalEnd()
			.optionalStart()
			.appendLiteral(' ')
			.appendOffset("+HHMM", "GMT")
			.optionalEnd()
			.toFormatter();
	}

	/**
	 * Business days between.
	 *
	 * @param earlierDate the earlier date
	 * @param laterDate the later date
	 * @return the int
	 */
	public static int businessDaysBetween(@Nonnull LocalDate earlierDate, @Nonnull LocalDate laterDate) {
		return businessDaysBetween(earlierDate, laterDate, DEFAULT_CAL_NAME);
	}

	/**
	 * Business days between.
	 *
	 * @param earlierDate the earlier date
	 * @param laterDate the later date
	 * @param calName the cal name
	 * @return the int
	 */
	public static int businessDaysBetween(@Nonnull LocalDate earlierDate, @Nonnull LocalDate laterDate, @Nonnull String calName) {
		return (int) getDateCalc(calName).businessDaysBetween(earlierDate, laterDate);
	}

	/**
	 * Change by business days.
	 *
	 * @param date the date
	 * @param amount the amount
	 * @return the local date
	 */
	public static LocalDate changeByBusinessDays(LocalDate date, int amount) {
		return changeByBusinessDays(date, amount, DEFAULT_CAL_NAME);
	}

	/**
	 * Change by business days.
	 *
	 * @param date the date
	 * @param amount the amount
	 * @param calName the cal name
	 * @return the local date
	 */
	public static LocalDate changeByBusinessDays(LocalDate date, int amount, String calName) {
		LocalDate changed = getDateCalc(calName).changeByBusinessDays(date, amount);
		Logs.logTrace(LOG, "Business day change result: amount=%,d calendarName=%s date=%s updatedDate=%s", amount, calName, date, changed);
		return changed;
	}

	/**
	 * Change relative date.
	 *
	 * @param date the date
	 * @param op the op
	 * @param type the type
	 * @param amount the amount
	 * @param calendar the calendar
	 * @return the local date time
	 */
	public static LocalDateTime changeRelativeDate(LocalDateTime date, RelativeDateOperator op, RelativeDateType type, long amount, String calendar) {
		ChronoUnit unit = type.getUnit();
		if (op == null) {
			Logs.logError(
				LOG,
				new IllegalArgumentException("RelativeDateOperator op cannot be null"),
				"RelativeDateOperator is null for arguments date [%s], type [%s], amount [%s] and calendar [%s]",
				date,
				type,
				amount,
				calendar
			);
			return date;
		}
		switch (op) {
		case ROUND:
			if (type == RelativeDateType.b) {
				HolidayDateCalc dc = getDateCalc(calendar);
				if (!dc.isBusinessDay(date.toLocalDate())) {
					date = dc.changeByBusinessDays(date.toLocalDate(), -1L).atTime(date.toLocalTime());
				}
			}
			switch (unit) {
			case YEARS:
				return LocalDateTime.of(LocalDate.of(date.get(YEAR), 1, 1), LocalTime.MIDNIGHT);
			case MONTHS:
				return LocalDateTime.of(LocalDate.of(date.get(YEAR), date.get(MONTH_OF_YEAR), 1), LocalTime.MIDNIGHT);
			case WEEKS:
				return LocalDateTime.of(date.with(DayOfWeek.MONDAY).toLocalDate(), LocalTime.MIDNIGHT);
			case DAYS:
				return LocalDateTime.of(date.toLocalDate(), LocalTime.MIDNIGHT);
			default:
				assert !unit.isDateBased();
				return date.truncatedTo(unit);
			}
		case PLUS:
		case MINUS:
			if (op == RelativeDateOperator.MINUS) {
				amount = -amount;
			}
			if (type == RelativeDateType.b) {
				return changeByBusinessDays(date.toLocalDate(), (int) amount).atTime(date.toLocalTime());
			}
			return date.plus(amount, unit);
		default:
			throw new IllegalArgumentException(String.format("RelativeDateOperator [%s] not supported!", op));
		}
	}

	/**
	 * Change to non weekend.
	 *
	 * @param date the date
	 * @return the local date
	 */
	public static LocalDate changeToNonWeekend(@Nonnull LocalDate date) {
		while ((date.getDayOfWeek() == DayOfWeek.SATURDAY) || (date.getDayOfWeek() == DayOfWeek.SUNDAY)) {
			date = date.plusDays(-1);
		}
		return date;
	}

	/**
	 * Count num of day of week between.
	 *
	 * @param earlierDate the earlier date
	 * @param laterDate the later date
	 * @param dayOfWeek the day of week
	 * @return the long
	 */
	public static long countNumOfDayOfWeekBetween(@Nonnull LocalDate earlierDate, @Nonnull LocalDate laterDate, @Nonnull DayOfWeek dayOfWeek) {
		LocalDate firstDayOfWeek = earlierDate.with(TemporalAdjusters.nextOrSame(dayOfWeek));
		LocalDate lastDayOfWeek = laterDate.with(TemporalAdjusters.previousOrSame(dayOfWeek));
		long weeksBetween = ChronoUnit.WEEKS.between(firstDayOfWeek, lastDayOfWeek);
		return weeksBetween + 1;
	}

	/**
	 * Days between.
	 *
	 * @param earlierDate the earlier date
	 * @param laterDate the later date
	 * @return the int
	 */
	public static int daysBetween(LocalDate earlierDate, LocalDate laterDate) {
		return (int) ChronoUnit.DAYS.between(earlierDate, laterDate);
	}

	/**
	 * Format date.
	 *
	 * @param format the format
	 * @return the string
	 */
	public static String formatDate(String format) {
		return formatDate(format, LocalDateTime.now());
	}

	/**
	 * Format date.
	 *
	 * @param format the format
	 * @param date the date
	 * @return the string
	 */
	public static String formatDate(String format, TemporalAccessor date) {
		return formatDate(format, date, null);
	}

	/**
	 * Format date.
	 *
	 * @param format the format
	 * @param date the date
	 * @param locale the locale
	 * @return the string
	 */
	public static String formatDate(String format, TemporalAccessor date, @Nullable Locale locale) {
		return formatterFromPattern(format, locale).format(date);
	}

	/**
	 * Formatter from pattern.
	 *
	 * @param pattern the pattern
	 * @return the date time formatter
	 */
	public static DateTimeFormatter formatterFromPattern(String pattern) {
		return formatterFromPattern(pattern, null);
	}

	/**
	 * Formatter from pattern.
	 *
	 * @param pattern the pattern
	 * @param locale the locale
	 * @return the date time formatter
	 */
	public static DateTimeFormatter formatterFromPattern(String pattern, @Nullable Locale locale) {
		DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(pattern);
		if (locale == null) {
			return builder.toFormatter();
		}
		return builder.toFormatter(locale);
	}

	/**
	 * From int.
	 *
	 * @param dateInt the date int
	 * @return the local date
	 */
	public static LocalDate fromInt(int dateInt) {
		return LocalDate.of(dateInt / 10000, (dateInt / 100) % 100, dateInt % 100);
	}

	/**
	 * From millis.
	 *
	 * @param millis the millis
	 * @return the local date time
	 */
	public static LocalDateTime fromMillis(long millis) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
	}

	/**
	 * From today.
	 *
	 * @param days the days
	 * @return the local date time
	 */
	public static LocalDateTime fromToday(long days) {
		return LocalDateTime.now().plusDays(days);
	}

	/**
	 * Gets the date calc.
	 *
	 * @param calName the cal name
	 * @return the date calc
	 */
	public static HolidayDateCalc getDateCalc(String calName) {
		return DATE_CALCS.computeIfAbsent(
			StringUtils.upperCase(calName),
			holidayDateCalcCreater
		);
	}

	/**
	 * Gets the earliest date.
	 *
	 * @param dates the dates
	 * @return the earliest date
	 */
	public static LocalDateTime getEarliestDate(LocalDateTime... dates) {
		if ((dates == null) || (dates.length == 0)) {
			return null;
		}
		LocalDateTime earliest = null;
		for (LocalDateTime date : dates) {
			if ((date != null) && ((earliest == null) || (earliest.compareTo(date) > 0))) {
				earliest = date;
			}
		}
		return earliest;
	}

	/**
	 * @return the holidayDateCalcCreater
	 */
	public static Function<String, HolidayDateCalc> getHolidayDateCalcCreater() {
		return holidayDateCalcCreater;
	}

	/**
	 * Gets the latest date.
	 *
	 * @param dates the dates
	 * @return the latest date
	 */
	public static LocalDateTime getLatestDate(LocalDateTime... dates) {
		if ((dates == null) || (dates.length == 0)) {
			return null;
		}
		LocalDateTime latest = null;
		for (LocalDateTime date : dates) {
			if ((date != null) && ((latest == null) || (latest.compareTo(date) < 0))) {
				latest = date;
			}
		}
		return latest;
	}

	/**
	 * Gets the month end business day.
	 *
	 * @param date the date
	 * @return the month end business day
	 */
	public static LocalDate getMonthEnd(LocalDate date) {
		if (!isMonthEnd(date)) {
			date = date.withDayOfMonth(date.lengthOfMonth());
		}
		return date;
	}

	/**
	 * Gets the month end business day.
	 *
	 * @param date the date
	 * @return the month end business day
	 */
	public static LocalDate getMonthEndBusinessDay(@Nonnull LocalDate date) {
		return getMonthEndBusinessDay(date, DEFAULT_CAL_NAME);
	}

	/**
	 * Gets the month end business day.
	 *
	 * @param date the date
	 * @param calName the cal name
	 * @return the month end business day
	 */
	public static LocalDate getMonthEndBusinessDay(@Nonnull LocalDate date, @Nullable String calName) {
		if (!isMonthEnd(date)) {
			date = date.with(END_OF_MONTH);
		}
		while (isWeekendOrHoliday(date, calName) && (date.getYear() > 1950)) {
			date = date.plusDays(-1);
		}
		return date;
	}

	/**
	 * Gets the month end non weekend.
	 *
	 * @param date the date
	 * @return the month end non weekend
	 */
	public static LocalDate getMonthEndNonWeekend(@Nonnull LocalDate date) {
		if (!isMonthEnd(date)) {
			date = date.with(END_OF_MONTH);
		}
		return changeToNonWeekend(date);
	}

	/**
	 * Gets the non weekend or holiday date.
	 *
	 * @param asOfDate the as of date
	 * @return the non weekend or holiday date
	 */
	public static LocalDate getNonWeekendOrHolidayDate(LocalDate asOfDate) {
		return getNonWeekendOrHolidayDate(asOfDate, DEFAULT_CAL_NAME);
	}

	/**
	 * Gets the non weekend or holiday date.
	 *
	 * @param asOfDate the as of date
	 * @param calName the cal name
	 * @return the non weekend or holiday date
	 */
	public static LocalDate getNonWeekendOrHolidayDate(LocalDate asOfDate, String calName) {
		calName = calName == null ? DEFAULT_CAL_NAME : calName;
		if ((asOfDate != null) && Dates.isWeekendOrHoliday(asOfDate, calName)) {
			int count = 0;
			while (Dates.isWeekendOrHoliday(asOfDate, calName) && (count++ < 50)) {
				asOfDate = asOfDate.plusDays(-1);
			}
		}
		return asOfDate;
	}

	/**
	 * Gets the or else.
	 *
	 * @param date the date
	 * @param field the field
	 * @return the or else
	 */
	public static int getOrElse(TemporalAccessor date, ChronoField field) {
		return getOrElse(date, field, 0);
	}

	/**
	 * Gets the or else.
	 *
	 * @param date the date
	 * @param field the field
	 * @param defaultValue the default value
	 * @return the or else
	 */
	public static int getOrElse(TemporalAccessor date, ChronoField field, int defaultValue) {
		return (date == null) || !date.isSupported(field) ? defaultValue : date.get(field);
	}

	/**
	 * Gets the previous business day.
	 *
	 * @return the previous business day
	 */
	public static LocalDate getPreviousBusinessDay() {
		return changeByBusinessDays(LocalDate.now(), -1);
	}

	/**
	 * Gets the previous month end.
	 *
	 * @param atLeastDays the at least days
	 * @return the previous month end
	 */
	public static LocalDate getPreviousMonthEnd(int atLeastDays) {
		return getPreviousMonthEnd(atLeastDays, false);
	}

	/**
	 * Gets the previous month end.
	 *
	 * @param atLeastDays the at least days
	 * @param businessDays the business days
	 * @return the previous month end
	 */
	public static LocalDate getPreviousMonthEnd(int atLeastDays, boolean businessDays) {
		return getPreviousMonthEnd(atLeastDays, businessDays, DEFAULT_CAL_NAME);
	}

	/**
	 * Gets the previous month end.
	 *
	 * @param atLeastDays the at least days
	 * @param businessDays the business days
	 * @param businessDaysCal the business days cal
	 * @return the previous month end
	 */
	public static LocalDate getPreviousMonthEnd(int atLeastDays, boolean businessDays, String businessDaysCal) {
		LocalDate date = businessDays ? changeByBusinessDays(LocalDate.now(), -atLeastDays, defaultIfBlank(businessDaysCal, DEFAULT_CAL_NAME)) : LocalDate.now();
		if (!businessDays) {
			date = date.plusDays(-atLeastDays);
		}
		if (!isMonthEnd(date)) {
			date = date.withDayOfMonth(1).plusDays(-1);
		}
		return date;
	}

	/**
	 * Gets the previous quarter end.
	 *
	 * @return the previous quarter end
	 */
	public static LocalDate getPreviousQuarterEnd() {
		return getPreviousQuarterEnd(15);
	}

	/**
	 * Gets the previous quarter end.
	 *
	 * @param atLeastDays the at least days
	 * @return the previous quarter end
	 */
	public static LocalDate getPreviousQuarterEnd(int atLeastDays) {
		return getPreviousQuarterEnd(atLeastDays, false);
	}

	/**
	 * Gets the previous quarter end.
	 *
	 * @param atLeastDays the at least days
	 * @param businessDays the business days
	 * @return the previous quarter end
	 */
	public static LocalDate getPreviousQuarterEnd(int atLeastDays, boolean businessDays) {
		return getPreviousQuarterEnd(atLeastDays, businessDays, DEFAULT_CAL_NAME);
	}

	/**
	 * Gets the previous quarter end.
	 *
	 * @param atLeastDays the at least days
	 * @param businessDays the business days
	 * @param businessDaysCal the business days cal
	 * @return the previous quarter end
	 */
	public static LocalDate getPreviousQuarterEnd(int atLeastDays, boolean businessDays, String businessDaysCal) {
		LocalDate date = getPreviousMonthEnd(atLeastDays, businessDays, businessDaysCal);
		int monthsOff = date.getMonthValue() % 3;
		if (monthsOff == 0) {
			return date;
		}
		return date.plusMonths(-1 * monthsOff);
	}

	/**
	 * Gets the previous year end.
	 *
	 * @param date the date
	 * @return the previous year end
	 */
	public static LocalDate getPreviousYearEnd(LocalDate date) {
		return LocalDate.of(date.getYear() - 1, 12, 31);
	}

	/**
	 * Gets the relative date.
	 *
	 * @param num the num
	 * @param type the type
	 * @return the relative date
	 */
	public static LocalDateTime getRelativeDate(int num, RelativeDateType type) {
		return getRelativeDate(LocalDateTime.now(), num, type);
	}

	/**
	 * Change date by relative amount.
	 *
	 * @param now the now
	 * @param num the num
	 * @param type the type
	 * @return the date
	 */
	public static LocalDateTime getRelativeDate(LocalDateTime now, int num, RelativeDateType type) {
		switch (type) {
		case b:
			return LocalDateTime.of(changeByBusinessDays(now.toLocalDate(), num), now.toLocalTime());
		default:
			return now.plus(num, type.getUnit());
		}
	}

	/**
	 * Gets the simple date format.
	 *
	 * @param pattern the pattern
	 * @param timezoneId the timezone id
	 * @return the simple date format
	 */
	public static SimpleDateFormat getSimpleDateFormat(String pattern, String timezoneId) {
		SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
		format.setCalendar(Calendar.getInstance(getTimeZoneByShortId(timezoneId)));
		return format;
	}

	/**
	 * Gets the start delay.
	 *
	 * @param start the start
	 * @return the start delay
	 */
	public static long getStartDelay(Date start) {
		LocalDateTime now = LocalDateTime.now();
		if (start.before(toOldDate(now))) {
			Calendar startPlusOne = new Calendar.Builder().setInstant(start).build();
			startPlusOne.add(Calendar.DATE, 1);
			start = startPlusOne.getTime();
		}
		return start.getTime() - toMillis(now);
	}

	/**
	 * Gets the time zone by short id.
	 *
	 * @param shortId the short id
	 * @return the time zone by short id
	 */
	public static TimeZone getTimeZoneByShortId(String shortId) {
		return TimeZone.getTimeZone(SHORT_IDS.get(shortId));
	}

	/**
	 * Gets the zone by short id.
	 *
	 * @param shortId the short id
	 * @return the zone by short id
	 */
	public static ZoneOffset getZoneByShortId(String shortId) {
		return ZonedDateTime.now(ZoneId.of(SHORT_IDS.get(shortId))).getOffset();
	}

	/**
	 * Increase day if before now.
	 *
	 * @param date the date
	 * @param increaseMillis the increase millis
	 * @return the date
	 */
	public static Date increaseDayIfBeforeNow(Date date, long increaseMillis) {
		Date now = toOldDate(LocalDateTime.now());
		while (date.before(now)) {
			Calendar startPlusOne = new Calendar.Builder().setInstant(date).build();
			startPlusOne.add(Calendar.MILLISECOND, (int) increaseMillis);
			date = startPlusOne.getTime();
		}
		return date;
	}

	/**
	 * Checks if firstDate is after or equal to secondDate.
	 *
	 * @param firstDate the first date
	 * @param secondDate the second date
	 * @return true, if is after or equal
	 */
	public static boolean isAfterOrEqual(LocalDate firstDate, LocalDate secondDate) {
		return daysBetween(firstDate, secondDate) <= 0;
	}

	/**
	 * Checks if firstDate is before or equal to secondDate.
	 *
	 * @param firstDate the first date
	 * @param secondDate the second date
	 * @return true, if is after or equal
	 */
	public static boolean isBeforeOrEqual(LocalDate firstDate, LocalDate secondDate) {
		return daysBetween(firstDate, secondDate) >= 0;
	}

	/**
	 * Checks if is month end.
	 *
	 * @param date the date
	 * @return true, if is month end
	 */
	public static boolean isMonthEnd(LocalDate date) {
		return (date != null) && (date.getDayOfMonth() == date.lengthOfMonth());
	}

	/**
	 * Checks if is quarter end.
	 *
	 * @param numberOfMonths the number of months
	 * @return true, if is quarter end
	 */
	public static boolean isQuarterEnd(int numberOfMonths) {
		if ((numberOfMonths % 3) == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if is valid.
	 *
	 * @param date the date
	 * @return true, if is valid
	 */
	public static boolean isValid(LocalDate date) {
		return !(date.isBefore(JULIAN_BASE) || (date.isAfter(MAX_DATE)));
	}

	/**
	 * Checks if is weekend.
	 *
	 * @param date the date
	 * @return true, if is weekend
	 */
	public static boolean isWeekend(LocalDate date) {
		return (date.getDayOfWeek() == DayOfWeek.SATURDAY) || (date.getDayOfWeek() == DayOfWeek.SUNDAY);
	}

	/**
	 * Checks if is weekend or holiday.
	 *
	 * @param date the date
	 * @return true, if is weekend or holiday
	 */
	public static boolean isWeekendOrHoliday(TemporalAccessor date) {
		return isWeekendOrHoliday(date, DEFAULT_CAL_NAME);
	}

	/**
	 * Checks if is weekend or holiday.
	 *
	 * @param date the date
	 * @param calName the cal name
	 * @return true, if is weekend or holiday
	 */
	public static boolean isWeekendOrHoliday(TemporalAccessor date, String calName) {
		LocalDate localDate = toDate(date);
		return isWeekend(localDate) || getDateCalc(Checks.defaultIfNull(calName, DEFAULT_CAL_NAME)).isHoliday(localDate);
	}

	/**
	 * Checks if is year end.
	 *
	 * @param numberOfMonths the number of months
	 * @return true, if is year end
	 */
	public static boolean isYearEnd(int numberOfMonths) {
		if ((numberOfMonths % 12) == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if is year end.
	 *
	 * @param date the date
	 * @return true, if is year end
	 */
	public static boolean isYearEnd(LocalDate date) {
		return date.getDayOfYear() == date.lengthOfYear();
	}

	/**
	 * Now ms.
	 *
	 * @return the long
	 */
	public static long nowMs() {
		return toMillis(LocalDateTime.now());
	}

	/**
	 * Parses the date.
	 *
	 * @param val the val
	 * @return the local date
	 */
	public static LocalDate parseDate(String val) {
		return parseDate(val, true);
	}

	/**
	 * Parses the date.
	 *
	 * @param val the val
	 * @param logEnabled the log enabled
	 * @return the local date
	 */
	public static LocalDate parseDate(String val, boolean logEnabled) {
		return functionIfNonNull(parseDateTime(val, logEnabled), LocalDateTime::toLocalDate).orElse(null);
	}

	/**
	 * Parses the date string.
	 *
	 * @param val the val
	 * @return the date
	 */
	public static Date parseDateString(String val) {
		return parseDateString(val, true);
	}

	/**
	 * Parses the date string.
	 *
	 * @param val the val
	 * @param logEnabled the log enabled
	 * @return the date
	 */
	public static Date parseDateString(String val, boolean logEnabled) {
		return functionIfNonNull(parseDateTime(val, logEnabled), date -> Date.from(date.atZone(Dates.DEFAULT_ZONE).toInstant())).orElse(null);
	}

	/**
	 * Parses the date.
	 *
	 * @param val the val
	 * @return the local date time
	 */
	public static LocalDateTime parseDateTime(String val) {
		return parseDateTime(val, true);
	}

	/**
	 * Parses the date time.
	 *
	 * @param val the val
	 * @param logEnabled the log enabled
	 * @return the local date time
	 */
	public static LocalDateTime parseDateTime(String val, boolean logEnabled) {
		try {
			return parseDateTimeUnsafe(val, logEnabled);
		} catch (Throwable e) {
			if (logEnabled) {
				Logs.logError(LOG, e, "Could not parse date from string [%s].", val);
			}
		}
		return null;
	}

	/**
	 * Parses the date time.
	 *
	 * @param val the val
	 * @param format the format
	 * @return the local date time
	 */
	public static LocalDateTime parseDateTime(String val, DateTimeFormatter format) {
		return parseDateTime(val, format, true);
	}

	/**
	 * Parses the date time.
	 *
	 * @param val the val
	 * @param format the format
	 * @param verbose the verbose
	 * @return the local date time
	 */
	public static LocalDateTime parseDateTime(String val, DateTimeFormatter format, boolean verbose) {
		try {
			return Optional.ofNullable(format.parse(val)).map(date -> date instanceof LocalDateTime ? (LocalDateTime) date : toDateTime(date)).orElse(null);
		} catch (Throwable e) {
			if (verbose) {
				Logs.logError(LOG, e, "Could not parse LocalDateTime for val [%s] and format [%s]", val, format);
			} else {
				Logs.logWarn(LOG, "Could not parse LocalDateTime for val [%s] and format [%s]. %s", val, format, e.getMessage());
			}
		}
		return null;
	}

	/**
	 * Parses the date time.
	 *
	 * @param val the val
	 * @param format the format
	 * @return the local date time
	 * @throws Exception the exception
	 */
	public static LocalDateTime parseDateTime(String val, String format) throws Exception {
		try {
			return parseDateTimeUnsafe(val, format);
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Could not parse LocalDateTime for val [%s] and format [%s]", val, format);
		}
		return null;
	}

	/**
	 * Parses the date string.
	 *
	 * @param val the val
	 * @param logEnabled the log enabled
	 * @return the date
	 * @throws Exception the exception
	 */
	public static LocalDateTime parseDateTimeUnsafe(String val, boolean logEnabled) throws Exception {
		val = trim(val);
		if (isBlank(val)) {
			return null;
		}
		long dateInt = NumberUtils.toLong(val, -1);
		if (dateInt != -1) {
			if ((dateInt <= MAX_DAY_INT) && (dateInt >= 19000101)) {
				return LocalDate.parse(val, BASIC_ISO_DATE).atStartOfDay();
			}
			return fromMillis(dateInt);
		} else if (val.startsWith("T") || val.startsWith("t")) {
			return parseRelativeDate(val);
		} else if (contains(val, "-")) {
			int len = val.length();
			if (len == 10) {
				return LocalDate.parse(val, DateTimeFormatter.ISO_DATE).atStartOfDay();
			} else if (len == 11) {
				return LocalDate.parse(val, DATE_ENT_FORMAT).atStartOfDay();
			} else if (len >= 19) {
				if (contains(val, "Z")) {
					return LocalDateTime.parse(val, SOLR_DATE_FORMAT);
				}
				return LocalDateTime.parse(val, SQL_FORMAT);
			}
		} else if (contains(val, "/")) {
			int len = val.length();
			if (len < 10) {
				return LocalDate.parse(val, SLASH_SHORT).atStartOfDay();
			}
			try {
				return LocalDate.parse(val, SLASH_LONG).atStartOfDay();
			} catch (DateTimeParseException e) {
				if (NumberUtils.toInt(val.substring(0, 2), -1) > 12) {
					Logs.logTrace(LOG, "Date possibly specified in international format (dd/MM/yyyy) - attempting to parse again: date=%s", val);
					return LocalDate.parse(val, SLASH_LONG_INTL).atStartOfDay();
				}
				throw e;
			}
		} else if ("null".equalsIgnoreCase(val)) {
			return null;
		}
		return LocalDateTime.parse(val, INTRADAY_NON_FILE_FORMAT);
	}

	/**
	 * Parses the date time.
	 *
	 * @param val the val
	 * @param format the format
	 * @return the local date time
	 * @throws Exception the exception
	 */
	public static LocalDateTime parseDateTimeUnsafe(String val, String format) throws Exception {
		if (StringUtils.isNotBlank(format)) {
			DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.parseCaseInsensitive()
				.parseLenient()
				.parseDefaulting(ChronoField.MILLI_OF_DAY, 0L)
				.appendPattern(format)
				.toFormatter();
			return Optional.ofNullable(formatter.parse(val)).map(date -> date instanceof LocalDateTime ? (LocalDateTime) date : toDateTime(date)).orElse(null);
		}
		return parseDateTimeUnsafe(val, true);
	}

	/**
	 * Parses the excel date.
	 *
	 * @param date the date
	 * @return the local date time
	 */
	public static LocalDateTime parseExcelDate(double date) {
		if (!(date > -Double.MIN_VALUE)) {
			return null;
		} else if (date >= 60) {
			date--;
		}
		long wholeDays = (long) Math.floor(date);
		long millisecondsInDay = (long) (((date - wholeDays) * MILLIS_PER_DAY) + 0.5);
		if (wholeDays < 60) {
			return LocalDateTime.of(1899, 12, 31, 0, 0).plusDays(wholeDays).plus(millisecondsInDay, ChronoUnit.MILLIS);
		}
		return LocalDateTime.of(1899, 12, 31, 0, 0).plusDays(wholeDays - 1).plus(millisecondsInDay, ChronoUnit.MILLIS);
	}

	/**
	 * Parses the excel date.
	 *
	 * @param date the date
	 * @return the local date time
	 */
	public static LocalDateTime parseExcelDate(String date) {
		return NumberUtils.isNumber(date) ? parseExcelDate(NumberUtils.toDouble(date)) : null;
	}

	/**
	 * Parses the relative date.
	 *
	 * @param val the val
	 * @return the local date time
	 */
	public static LocalDateTime parseRelativeDate(String val) {
		return parseRelativeDate(val, null);
	}

	/**
	 * Parses the relative date.
	 *
	 * @param val the val
	 * @param startingDate the starting date
	 * @return the local date time
	 */
	public static LocalDateTime parseRelativeDate(String val, @Nullable LocalDateTime startingDate) {
		try {
			return parseRelativeDateUnsafe(val, startingDate);
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Could not parse relative date string [%s].", val);
		}
		return null;
	}

	/**
	 * Parses the relative date string.
	 *
	 * @param val the val
	 * @return the date
	 */
	public static Date parseRelativeDateString(String val) {
		return toOldDate(parseRelativeDate(val));
	}

	/**
	 * Parses the relative date unsafe.
	 *
	 * @param val the val
	 * @return the local date time
	 */
	public static LocalDateTime parseRelativeDateUnsafe(String val) {
		return parseRelativeDateUnsafe(val, null);
	}

	/**
	 * Parses the relative date unsafe.
	 *
	 * @param val the val
	 * @param startingDate the starting date
	 * @return the local date time
	 */
	public static LocalDateTime parseRelativeDateUnsafe(String val, @Nullable LocalDateTime startingDate) {
		return parseRelativeDateUnsafe(val, false, startingDate).getLeft();
	}

	/**
	 * Parses the relative date unsafe with info.
	 *
	 * @param val the val
	 * @return the pair
	 */
	public static Pair<LocalDateTime, List<Triple<RelativeDateOperator, RelativeDateType, String>>> parseRelativeDateUnsafeWithInfo(String val) {
		return parseRelativeDateUnsafe(val, true);
	}

	/**
	 * Parses the zoned date time.
	 *
	 * @param val the val
	 * @return the zoned date time
	 */
	public static ZonedDateTime parseZonedDateTime(String val) {
		return functionIfNonNull(parseDateTime(val), date -> date.atZone(DEFAULT_ZONE)).orElse(null);
	}

	/**
	 * Parses the zone offset.
	 *
	 * @param zoneShortIdOrName the zone short id or name
	 * @return the zone offset
	 */
	public static ZoneOffset parseZoneOffset(String zoneShortIdOrName) {
		if (SHORT_IDS.containsKey(zoneShortIdOrName)) {
			return ZonedDateTime.now(ZoneId.of(SHORT_IDS.get(zoneShortIdOrName))).getOffset();
		}
		return ZonedDateTime.now(ZoneId.of(zoneShortIdOrName)).getOffset();
	}

	/**
	 * Sets the holiday date calc creater.
	 *
	 * @param holidayDateCalcCreater the holidayDateCalcCreater to set
	 */
	public static void setHolidayDateCalcCreater(Function<String, HolidayDateCalc> holidayDateCalcCreater) {
		if (Dates.holidayDateCalcCreater != holidayDateCalcCreater) {
			DATE_CALCS.clear();
		}
		Dates.holidayDateCalcCreater = holidayDateCalcCreater;
	}

	/**
	 * To local date.
	 *
	 * @param date the date
	 * @return the local date
	 */
	public static LocalDate toDate(Date date) {
		return date == null ? null : date instanceof java.sql.Date ? ((java.sql.Date) date).toLocalDate() : toDateTime(date).toLocalDate();
	}

	/**
	 * To date.
	 *
	 * @param temporal the temporal
	 * @return the local date
	 */
	public static LocalDate toDate(TemporalAccessor temporal) {
		return temporal == null ? null : LocalDate.from(temporal);
	}

	/**
	 * To java date.
	 *
	 * @param date the date
	 * @return the local date time
	 */
	public static LocalDateTime toDateTime(Date date) {
		return date == null ? null : date instanceof java.sql.Date ? ((java.sql.Date) date).toLocalDate().atStartOfDay() : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	/**
	 * To date time.
	 *
	 * @param temporal the temporal
	 * @return the local date time
	 */
	public static LocalDateTime toDateTime(TemporalAccessor temporal) {
		return temporal == null ? null : temporal instanceof LocalDate ? ((LocalDate) temporal).atStartOfDay() : LocalDateTime.from(temporal);
	}

	/**
	 * To int.
	 *
	 * @param temporal the temporal
	 * @return the int
	 */
	public static Integer toInt(TemporalAccessor temporal) {
		LocalDate date = temporal == null ? null : temporal instanceof LocalDate ? (LocalDate) temporal : LocalDate.from(temporal);
		return date == null ? null : ((date.getYear() * 10000) + (date.getMonthValue() * 100) + (date.getDayOfMonth()));
	}

	/**
	 * To int string.
	 *
	 * @param temporal the temporal
	 * @return the string
	 */
	public static String toIntString(TemporalAccessor temporal) {
		return Lambdas.functionIfNonNull(toInt(temporal), String::valueOf).orElse(null);
	}

	/**
	 * To json standard format.
	 *
	 * @param date the date
	 * @return the string
	 */
	public static String toJsonStandardFormat(TemporalAccessor date) {
		LocalDateTime dateTime = toDateTime(date);
		return dateTime == null ? null : dateTime.format(SOLR_DATE_FORMAT);
	}

	/**
	 * To millis.
	 *
	 * @param date the date
	 * @return the long
	 */
	public static long toMillis(TemporalAccessor date) {
		return (date instanceof LocalDate ? ((LocalDate) date).atStartOfDay(DEFAULT_ZONE).toInstant()
			: date instanceof ZonedDateTime ? ((ZonedDateTime) date).toInstant() : LocalDateTime.from(date).toInstant(DEFAULT_ZONE)).toEpochMilli();
	}

	/**
	 * To millis string.
	 *
	 * @param date the date
	 * @return the string
	 */
	public static String toMillisString(TemporalAccessor date) {
		return String.valueOf(toMillis(date));
	}

	/**
	 * To old java date.
	 *
	 * @param date the date
	 * @return the date
	 */
	public static Date toOldDate(TemporalAccessor date) {
		return Date.from(toDateTime(date).atZone(DEFAULT_ZONE).toInstant());
	}

	/**
	 * To solr date format.
	 *
	 * @param date the date
	 * @return the string
	 */
	public static String toSolrDateFormat(Date date) {
		return toJsonStandardFormat(toDateTime(date));
	}

	/**
	 * To solr date format.
	 *
	 * @param date the date
	 * @return the string
	 */
	public static String toSolrDateFormat(TemporalAccessor date) {
		return toJsonStandardFormat(date);
	}

	/**
	 * To zoned date time.
	 *
	 * @param date the date
	 * @return the zoned date time
	 */
	public static ZonedDateTime toZonedDateTime(TemporalAccessor date) {
		return toZonedDateTime(date, DEFAULT_ZONE);
	}

	/**
	 * To zoned date time.
	 *
	 * @param date the date
	 * @param zone the zone
	 * @return the zoned date time
	 */
	public static ZonedDateTime toZonedDateTime(TemporalAccessor date, ZoneId zone) {
		return ZonedDateTime.of(LocalDateTime.from(date), zone);
	}

	/**
	 * Years between.
	 *
	 * @param earlierDate the earlier date
	 * @param laterDate the later date
	 * @return the double
	 */
	public static double yearsBetween(LocalDate earlierDate, LocalDate laterDate) {
		int days = daysBetween(earlierDate, laterDate);
		LocalDate date = earlierDate;
		while ((date.getYear() <= laterDate.getYear())) {
			if (date.isLeapYear()) {
				LocalDate leapYearDate = LocalDate.of(date.getYear(), 02, 29);
				if ((isAfterOrEqual(leapYearDate, earlierDate)) && (isBeforeOrEqual(leapYearDate, laterDate))) {
					days--;
				}
			}
			date = date.plusYears(1).with(Dates.END_OF_MONTH);
		}
		return days / 365.0;
	}

	/**
	 * Parses the relative date unsafe.
	 *
	 * @param val the val
	 * @param includeInfo the include info
	 * @return the pair
	 */
	private static Pair<LocalDateTime, List<Triple<RelativeDateOperator, RelativeDateType, String>>> parseRelativeDateUnsafe(String val, boolean includeInfo) {
		return parseRelativeDateUnsafe(val, includeInfo, null);
	}

	/**
	 * Parses the relative date string.
	 *
	 * @param val the val
	 * @param includeInfo the include info
	 * @param date the date
	 * @return the date
	 */
	private static Pair<LocalDateTime, List<Triple<RelativeDateOperator, RelativeDateType, String>>> parseRelativeDateUnsafe(
		String val,
		boolean includeInfo,
		@Nullable LocalDateTime date
	) {
		List<Triple<RelativeDateOperator, RelativeDateType, String>> info = includeInfo ? new ArrayList<>() : null;
		Matcher matcher = RELATIVE_DATE_REGEX.matcher(val);
		if (matcher.matches()) {
			String calendar = DEFAULT_CAL_NAME;
			Matcher mathParts = RELATIVE_DATE_REGEX_PART.matcher(matcher.group(1));
			if (date == null) {
				date = LocalDateTime.now();
			}
			while (mathParts.find()) {
				RelativeDateOperator op = Enums.fromString(RelativeDateOperator.class, mathParts.group(1));
				RelativeDateType type = Enums.fromString(RelativeDateType.class, mathParts.group(3));
				String value = mathParts.group(2);
				if (op == RelativeDateOperator.SETTING) {
					if (type == RelativeDateType.z) {
						ZoneOffset zone = getZoneByShortId(value.toUpperCase());
						date = date.plusSeconds(zone.getTotalSeconds());
					} else if (type == RelativeDateType.c) {
						calendar = StringUtils.upperCase(value);
					} else {
						throw new IllegalArgumentException(String.format("Relative date setting %s is not supported", type));
					}
				} else {
					long amount = op == RelativeDateOperator.ROUND ? 0 : NumberUtils.toLong(value, 0);
					date = changeRelativeDate(date, op, type, amount, calendar);
				}
				if (info != null) {
					info.add(Triple.of(op, type, value));
				}
			}
			Logs.logDebug(LOG, "Successfully parsed relative date: date=%s", val);
		}
		return Pair.of(date, info);
	}
}
