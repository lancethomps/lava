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

public class Dates {

  public static final Comparator<LocalDate> ASC_LOCAL_DATE_COMPARATOR = (d1, d2) -> d1.compareTo(d2);
  public static final DateTimeFormatter DATE_ENT_FORMAT = formatterFromPattern("dd-MMM-yyyy");
  public static final Pattern DD_MMM_YY_PATTERN = Pattern.compile("(\\d+)[-/]{0,1}([a-zA-Z]{3})[-/']{0,1}(\\d+)");
  public static final DateTimeFormatter DEBUG_FORMAT_LONG;
  public static final String DEFAULT_CAL_NAME = "US";
  public static final TimeZone DEFAULT_TZ = TimeZone.getTimeZone("GMT");
  public static final ZoneOffset DEFAULT_ZONE = ZoneOffset.UTC;
  public static final Comparator<LocalDate> DESC_LOCAL_DATE_COMPARATOR = (d1, d2) -> d2.compareTo(d1);
  public static final TemporalAdjuster END_OF_MONTH = TemporalAdjusters.lastDayOfMonth();

  public static final TemporalAdjuster END_OF_NEXT_MONTH =
    (temporal) -> temporal.plus(1L, ChronoUnit.MONTHS).with(TemporalAdjusters.lastDayOfMonth());

  public static final TemporalAdjuster END_OF_PREV_MONTH =
    (temporal) -> temporal.minus(-1L, ChronoUnit.MONTHS).with(TemporalAdjusters.lastDayOfMonth());
  public static final Map<Long, String> EN_DOW;
  public static final Map<Long, String> EN_MOY;
  public static final DateTimeFormatter ERROR_LOG_DATE_FORMAT = formatterFromPattern("dd-MMM-yyyy hh:mm:ss.SSS z");
  public static final DateTimeFormatter FORMAT_DASH_NUMS = formatterFromPattern("yyyy-MM-dd");
  public static final DateTimeFormatter FORMAT_DASH_TEXT = formatterFromPattern("dd-MMM-yyyy");
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
  public static final DateTimeFormatter INTRADAY_FORMAT = formatterFromPattern("yyyyMMdd_HH-mm-ss_SSS");
  public static final DateTimeFormatter INTRADAY_NON_FILE_FORMAT = formatterFromPattern("dd-MMM-yyyy HH:mm:ss");
  public static final DateTimeFormatter INTRADAY_NUM_FORMAT = formatterFromPattern("yyyyMMddHHmmss");
  public static final DateTimeFormatter INT_FORMAT = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendValue(YEAR, 4)
    .appendValue(MONTH_OF_YEAR, 2)
    .appendValue(DAY_OF_MONTH, 2)
    .optionalStart()
    .appendOffset("+HHMMss", "Z")
    .toFormatter();
  public static final LocalDate JULIAN_BASE = LocalDate.of(1950, 01, 01);
  public static final Logger LOG = Logger.getLogger(Dates.class);
  public static final DateTimeFormatter LOG4J_FORMAT = formatterFromPattern("yyyy-MM-dd HH:mm:ss,SSS");
  public static final LocalDate MAX_DATE = LocalDate.of(2222, 12, 31);
  public static final int MAX_DAY_INT = 22221231;
  public static final int MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
  public static final Pattern RELATIVE_DATE_REGEX;
  public static final Pattern RELATIVE_DATE_REGEX_PART;
  public static final Map<String, String> SHORT_IDS = Collections.unmodifiableMap(
    MapUtils.putAll(
      new HashMap<>(ZoneId.SHORT_IDS),
      new String[]{
        "PDT", ZoneId.SHORT_IDS.get("PST"),
        "EDT", ZoneId.SHORT_IDS.get("EST")
      }
    )
  );
  public static final DateTimeFormatter SIMPLE_INTRADAY_FORMAT = formatterFromPattern("yyyyMMdd_HH-mm-ss_z");
  public static final DateTimeFormatter SLASH_LONG = formatterFromPattern("MM/dd/yyyy");
  public static final DateTimeFormatter SLASH_LONG_INTL = formatterFromPattern("dd/MM/yyyy");
  public static final DateTimeFormatter SLASH_SHORT = formatterFromPattern("M/d/yyyy");
  public static final DateTimeFormatter SLASH_VERY_SHORT = formatterFromPattern("M/d/yy");
  public static final DateTimeFormatter SOLR_DATE_FORMAT = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
    .optionalStart()
    .appendPattern(".SSS")
    .optionalEnd()
    .appendPattern("'Z'")
    .toFormatter();
  public static final DateTimeFormatter SOLR_DATE_TIMESTAMP_FORMAT = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendPattern("yyyy-MM-dd'T'HH_mm_ss")
    .optionalStart()
    .appendPattern(".SSS")
    .optionalEnd()
    .appendPattern("'Z'")
    .toFormatter();
  public static final DateTimeFormatter SQL_FORMAT = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendPattern("yyyy-MM-dd")
    .optionalStart()
    .appendPattern(" HH:mm:ss")
    .optionalEnd()
    .optionalStart()
    .appendPattern(".SSS")
    .optionalEnd()
    .toFormatter();
  public static final TemporalAdjuster START_OF_CURRENT_QUARTER = (temporal) -> temporal
    .with(ChronoField.MONTH_OF_YEAR, ((int) Math.floor(((temporal.get(ChronoField.MONTH_OF_YEAR) - 1) / 3)) * 3) + 1)
    .with(ChronoField.DAY_OF_MONTH, 1);
  public static final DateTimeFormatter TIMESTAMP_FORMAT = formatterFromPattern("yyyy_MM_dd_HH_mm_ss_SS");
  public static final TimeZone TZ_PST = getTimeZoneByShortId("PST");
  public static final DateTimeFormatter UTC_FORMAT = formatterFromPattern("yyyyMMdd");
  public static final DateTimeFormatter YYYYMM_FORMAT = formatterFromPattern("yyyyMM");
  public static final ZoneOffset ZONE_EST = getZoneByShortId("EST");
  public static final ZoneOffset ZONE_PST = getZoneByShortId("PST");
  private static final FastHashMap<String, HolidayDateCalc> DATE_CALCS = new FastHashMap<>(true);
  private static Function<String, HolidayDateCalc> holidayDateCalcCreater = (calendarName) -> {
    return new JollydayHolidayDateCalc(calendarName);
  };

  static {
    TimeZone.setDefault(DEFAULT_TZ);

    String symbols =
      Stream.of(RelativeDateOperator.values()).map(RelativeDateOperator::getSymbol).sorted().map(Pattern::quote).collect(Collectors.joining());
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

  public static int businessDaysBetween(@Nonnull LocalDate earlierDate, @Nonnull LocalDate laterDate) {
    return businessDaysBetween(earlierDate, laterDate, DEFAULT_CAL_NAME);
  }

  public static int businessDaysBetween(@Nonnull LocalDate earlierDate, @Nonnull LocalDate laterDate, @Nonnull String calName) {
    return (int) getDateCalc(calName).businessDaysBetween(earlierDate, laterDate);
  }

  public static LocalDate changeByBusinessDays(LocalDate date, int amount) {
    return changeByBusinessDays(date, amount, DEFAULT_CAL_NAME);
  }

  public static LocalDate changeByBusinessDays(LocalDate date, int amount, String calName) {
    LocalDate changed = getDateCalc(calName).changeByBusinessDays(date, amount);
    Logs.logTrace(LOG, "Business day change result: amount=%,d calendarName=%s date=%s updatedDate=%s", amount, calName, date, changed);
    return changed;
  }

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
        } else if (type == RelativeDateType.q) {
          return date.with(START_OF_CURRENT_QUARTER);
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
        } else if (type == RelativeDateType.q) {
          amount *= 3;
          unit = ChronoUnit.MONTHS;
        }
        return date.plus(amount, unit);
      default:
        throw new IllegalArgumentException(String.format("RelativeDateOperator [%s] not supported!", op));
    }
  }

  public static LocalDate changeToNonWeekend(@Nonnull LocalDate date) {
    while ((date.getDayOfWeek() == DayOfWeek.SATURDAY) || (date.getDayOfWeek() == DayOfWeek.SUNDAY)) {
      date = date.plusDays(-1);
    }
    return date;
  }

  public static long countNumOfDayOfWeekBetween(@Nonnull LocalDate earlierDate, @Nonnull LocalDate laterDate, @Nonnull DayOfWeek dayOfWeek) {
    LocalDate firstDayOfWeek = earlierDate.with(TemporalAdjusters.nextOrSame(dayOfWeek));
    LocalDate lastDayOfWeek = laterDate.with(TemporalAdjusters.previousOrSame(dayOfWeek));
    long weeksBetween = ChronoUnit.WEEKS.between(firstDayOfWeek, lastDayOfWeek);
    return weeksBetween + 1;
  }

  public static int daysBetween(LocalDate earlierDate, LocalDate laterDate) {
    return (int) ChronoUnit.DAYS.between(earlierDate, laterDate);
  }

  public static String formatDate(String format) {
    return formatDate(format, LocalDateTime.now());
  }

  public static String formatDate(String format, TemporalAccessor date) {
    return formatDate(format, date, null);
  }

  public static String formatDate(String format, TemporalAccessor date, @Nullable Locale locale) {
    return formatterFromPattern(format, locale).format(date);
  }

  public static DateTimeFormatter formatterFromPattern(String pattern) {
    return formatterFromPattern(pattern, null);
  }

  public static DateTimeFormatter formatterFromPattern(String pattern, @Nullable Locale locale) {
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(pattern);
    if (locale == null) {
      return builder.toFormatter();
    }
    return builder.toFormatter(locale);
  }

  public static LocalDate fromInt(int dateInt) {
    return LocalDate.of(dateInt / 10000, (dateInt / 100) % 100, dateInt % 100);
  }

  public static LocalDateTime fromMillis(long millis) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
  }

  public static LocalDateTime fromToday(long days) {
    return LocalDateTime.now().plusDays(days);
  }

  public static HolidayDateCalc getDateCalc(String calName) {
    return DATE_CALCS.computeIfAbsent(
      StringUtils.upperCase(calName),
      holidayDateCalcCreater
    );
  }

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

  public static Function<String, HolidayDateCalc> getHolidayDateCalcCreater() {
    return holidayDateCalcCreater;
  }

  public static void setHolidayDateCalcCreater(Function<String, HolidayDateCalc> holidayDateCalcCreater) {
    if (Dates.holidayDateCalcCreater != holidayDateCalcCreater) {
      DATE_CALCS.clear();
    }
    Dates.holidayDateCalcCreater = holidayDateCalcCreater;
  }

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

  public static LocalDate getMonthEnd(LocalDate date) {
    if (!isMonthEnd(date)) {
      date = date.withDayOfMonth(date.lengthOfMonth());
    }
    return date;
  }

  public static LocalDate getMonthEndBusinessDay(@Nonnull LocalDate date) {
    return getMonthEndBusinessDay(date, DEFAULT_CAL_NAME);
  }

  public static LocalDate getMonthEndBusinessDay(@Nonnull LocalDate date, @Nullable String calName) {
    if (!isMonthEnd(date)) {
      date = date.with(END_OF_MONTH);
    }
    while (isWeekendOrHoliday(date, calName) && (date.getYear() > 1950)) {
      date = date.plusDays(-1);
    }
    return date;
  }

  public static LocalDate getMonthEndNonWeekend(@Nonnull LocalDate date) {
    if (!isMonthEnd(date)) {
      date = date.with(END_OF_MONTH);
    }
    return changeToNonWeekend(date);
  }

  public static LocalDate getNonWeekendOrHolidayDate(LocalDate asOfDate) {
    return getNonWeekendOrHolidayDate(asOfDate, DEFAULT_CAL_NAME);
  }

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

  public static int getOrElse(TemporalAccessor date, ChronoField field) {
    return getOrElse(date, field, 0);
  }

  public static int getOrElse(TemporalAccessor date, ChronoField field, int defaultValue) {
    return (date == null) || !date.isSupported(field) ? defaultValue : date.get(field);
  }

  public static LocalDate getPreviousBusinessDay() {
    return changeByBusinessDays(LocalDate.now(), -1);
  }

  public static LocalDate getPreviousMonthEnd(int atLeastDays) {
    return getPreviousMonthEnd(atLeastDays, false);
  }

  public static LocalDate getPreviousMonthEnd(int atLeastDays, boolean businessDays) {
    return getPreviousMonthEnd(atLeastDays, businessDays, DEFAULT_CAL_NAME);
  }

  public static LocalDate getPreviousMonthEnd(int atLeastDays, boolean businessDays, String businessDaysCal) {
    LocalDate date =
      businessDays ? changeByBusinessDays(LocalDate.now(), -atLeastDays, defaultIfBlank(businessDaysCal, DEFAULT_CAL_NAME)) : LocalDate.now();
    if (!businessDays) {
      date = date.plusDays(-atLeastDays);
    }
    if (!isMonthEnd(date)) {
      date = date.withDayOfMonth(1).plusDays(-1);
    }
    return date;
  }

  public static LocalDate getPreviousQuarterEnd() {
    return getPreviousQuarterEnd(15);
  }

  public static LocalDate getPreviousQuarterEnd(int atLeastDays) {
    return getPreviousQuarterEnd(atLeastDays, false);
  }

  public static LocalDate getPreviousQuarterEnd(int atLeastDays, boolean businessDays) {
    return getPreviousQuarterEnd(atLeastDays, businessDays, DEFAULT_CAL_NAME);
  }

  public static LocalDate getPreviousQuarterEnd(int atLeastDays, boolean businessDays, String businessDaysCal) {
    LocalDate date = getPreviousMonthEnd(atLeastDays, businessDays, businessDaysCal);
    int monthsOff = date.getMonthValue() % 3;
    if (monthsOff == 0) {
      return date;
    }
    return date.plusMonths(-1 * monthsOff);
  }

  public static LocalDate getPreviousYearEnd(LocalDate date) {
    return LocalDate.of(date.getYear() - 1, 12, 31);
  }

  public static LocalDateTime getRelativeDate(int num, RelativeDateType type) {
    return getRelativeDate(LocalDateTime.now(), num, type);
  }

  public static LocalDateTime getRelativeDate(LocalDateTime now, int num, RelativeDateType type) {
    switch (type) {
      case b:
        return LocalDateTime.of(changeByBusinessDays(now.toLocalDate(), num), now.toLocalTime());
      default:
        return now.plus(num, type.getUnit());
    }
  }

  public static SimpleDateFormat getSimpleDateFormat(String pattern, String timezoneId) {
    SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
    format.setCalendar(Calendar.getInstance(getTimeZoneByShortId(timezoneId)));
    return format;
  }

  public static long getStartDelay(Date start) {
    LocalDateTime now = LocalDateTime.now();
    if (start.before(toOldDate(now))) {
      Calendar startPlusOne = new Calendar.Builder().setInstant(start).build();
      startPlusOne.add(Calendar.DATE, 1);
      start = startPlusOne.getTime();
    }
    return start.getTime() - toMillis(now);
  }

  public static TimeZone getTimeZoneByShortId(String shortId) {
    return TimeZone.getTimeZone(SHORT_IDS.get(shortId));
  }

  public static ZoneOffset getZoneByShortId(String shortId) {
    return ZonedDateTime.now(ZoneId.of(SHORT_IDS.get(shortId))).getOffset();
  }

  public static Date increaseDayIfBeforeNow(Date date, long increaseMillis) {
    Date now = toOldDate(LocalDateTime.now());
    while (date.before(now)) {
      Calendar startPlusOne = new Calendar.Builder().setInstant(date).build();
      startPlusOne.add(Calendar.MILLISECOND, (int) increaseMillis);
      date = startPlusOne.getTime();
    }
    return date;
  }

  public static boolean isAfterOrEqual(LocalDate firstDate, LocalDate secondDate) {
    return daysBetween(firstDate, secondDate) <= 0;
  }

  public static boolean isBeforeOrEqual(LocalDate firstDate, LocalDate secondDate) {
    return daysBetween(firstDate, secondDate) >= 0;
  }

  public static boolean isMonthEnd(LocalDate date) {
    return (date != null) && (date.getDayOfMonth() == date.lengthOfMonth());
  }

  public static boolean isQuarterEnd(int numberOfMonths) {
    return (numberOfMonths % 3) == 0;
  }

  public static boolean isValid(LocalDate date) {
    return !(date.isBefore(JULIAN_BASE) || (date.isAfter(MAX_DATE)));
  }

  public static boolean isWeekend(LocalDate date) {
    return (date.getDayOfWeek() == DayOfWeek.SATURDAY) || (date.getDayOfWeek() == DayOfWeek.SUNDAY);
  }

  public static boolean isWeekendOrHoliday(TemporalAccessor date) {
    return isWeekendOrHoliday(date, DEFAULT_CAL_NAME);
  }

  public static boolean isWeekendOrHoliday(TemporalAccessor date, String calName) {
    LocalDate localDate = toDate(date);
    return isWeekend(localDate) || getDateCalc(Checks.defaultIfNull(calName, DEFAULT_CAL_NAME)).isHoliday(localDate);
  }

  public static boolean isYearEnd(int numberOfMonths) {
    return (numberOfMonths % 12) == 0;
  }

  public static boolean isYearEnd(LocalDate date) {
    return date.getDayOfYear() == date.lengthOfYear();
  }

  public static long nowMs() {
    return toMillis(LocalDateTime.now());
  }

  public static LocalDate parseDate(String val) {
    return parseDate(val, true);
  }

  public static LocalDate parseDate(String val, boolean logEnabled) {
    return functionIfNonNull(parseDateTime(val, logEnabled), LocalDateTime::toLocalDate).orElse(null);
  }

  public static Date parseDateString(String val) {
    return parseDateString(val, true);
  }

  public static Date parseDateString(String val, boolean logEnabled) {
    return functionIfNonNull(parseDateTime(val, logEnabled), date -> Date.from(date.atZone(Dates.DEFAULT_ZONE).toInstant())).orElse(null);
  }

  public static LocalDateTime parseDateTime(String val) {
    return parseDateTime(val, true);
  }

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

  public static LocalDateTime parseDateTime(String val, DateTimeFormatter format) {
    return parseDateTime(val, format, true);
  }

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

  public static LocalDateTime parseDateTime(String val, String format) throws Exception {
    try {
      return parseDateTimeUnsafe(val, format);
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Could not parse LocalDateTime for val [%s] and format [%s]", val, format);
    }
    return null;
  }

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

  public static LocalDateTime parseDateTimeUnsafe(String val, String format) throws Exception {
    if (StringUtils.isNotBlank(format)) {
      DateTimeFormatter formatter = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .parseLenient()
        .parseDefaulting(ChronoField.MILLI_OF_DAY, 0L)
        .appendPattern(format)
        .toFormatter();
      return Optional
        .ofNullable(formatter.parse(val))
        .map(date -> date instanceof LocalDateTime ? (LocalDateTime) date : toDateTime(date))
        .orElse(null);
    }
    return parseDateTimeUnsafe(val, true);
  }

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

  public static LocalDateTime parseExcelDate(String date) {
    return NumberUtils.isNumber(date) ? parseExcelDate(NumberUtils.toDouble(date)) : null;
  }

  public static LocalDateTime parseRelativeDate(String val) {
    return parseRelativeDate(val, null);
  }

  public static LocalDateTime parseRelativeDate(String val, @Nullable LocalDateTime startingDate) {
    try {
      return parseRelativeDateUnsafe(val, startingDate);
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Could not parse relative date string [%s].", val);
    }
    return null;
  }

  public static Date parseRelativeDateString(String val) {
    return toOldDate(parseRelativeDate(val));
  }

  public static LocalDateTime parseRelativeDateUnsafe(String val) {
    return parseRelativeDateUnsafe(val, null);
  }

  public static LocalDateTime parseRelativeDateUnsafe(String val, @Nullable LocalDateTime startingDate) {
    return parseRelativeDateUnsafe(val, false, startingDate).getLeft();
  }

  public static Pair<LocalDateTime, List<Triple<RelativeDateOperator, RelativeDateType, String>>> parseRelativeDateUnsafeWithInfo(String val) {
    return parseRelativeDateUnsafe(val, true);
  }

  public static ZoneOffset parseZoneOffset(String zoneShortIdOrName) {
    if (SHORT_IDS.containsKey(zoneShortIdOrName)) {
      return ZonedDateTime.now(ZoneId.of(SHORT_IDS.get(zoneShortIdOrName))).getOffset();
    }
    return ZonedDateTime.now(ZoneId.of(zoneShortIdOrName)).getOffset();
  }

  public static ZonedDateTime parseZonedDateTime(String val) {
    return functionIfNonNull(parseDateTime(val), date -> date.atZone(DEFAULT_ZONE)).orElse(null);
  }

  public static LocalDate toDate(Date date) {
    return date == null ? null : date instanceof java.sql.Date ? ((java.sql.Date) date).toLocalDate() : toDateTime(date).toLocalDate();
  }

  public static LocalDate toDate(TemporalAccessor temporal) {
    return temporal == null ? null : LocalDate.from(temporal);
  }

  public static LocalDateTime toDateTime(Date date) {
    return date == null ? null : date instanceof java.sql.Date ? ((java.sql.Date) date).toLocalDate().atStartOfDay() :
      LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }

  public static LocalDateTime toDateTime(TemporalAccessor temporal) {
    return temporal == null ? null : temporal instanceof LocalDate ? ((LocalDate) temporal).atStartOfDay() : LocalDateTime.from(temporal);
  }

  public static Integer toInt(TemporalAccessor temporal) {
    LocalDate date = temporal == null ? null : temporal instanceof LocalDate ? (LocalDate) temporal : LocalDate.from(temporal);
    return date == null ? null : ((date.getYear() * 10000) + (date.getMonthValue() * 100) + (date.getDayOfMonth()));
  }

  public static String toIntString(TemporalAccessor temporal) {
    return Lambdas.functionIfNonNull(toInt(temporal), String::valueOf).orElse(null);
  }

  public static String toJsonStandardFormat(TemporalAccessor date) {
    LocalDateTime dateTime = toDateTime(date);
    return dateTime == null ? null : dateTime.format(SOLR_DATE_FORMAT);
  }

  public static long toMillis(TemporalAccessor date) {
    return (date instanceof LocalDate ? ((LocalDate) date).atStartOfDay(DEFAULT_ZONE).toInstant()
      : date instanceof ZonedDateTime ? ((ZonedDateTime) date).toInstant() : LocalDateTime.from(date).toInstant(DEFAULT_ZONE)).toEpochMilli();
  }

  public static String toMillisString(TemporalAccessor date) {
    return String.valueOf(toMillis(date));
  }

  public static Date toOldDate(TemporalAccessor date) {
    return Date.from(toDateTime(date).atZone(DEFAULT_ZONE).toInstant());
  }

  public static String toSolrDateFormat(Date date) {
    return toJsonStandardFormat(toDateTime(date));
  }

  public static String toSolrDateFormat(TemporalAccessor date) {
    return toJsonStandardFormat(date);
  }

  public static ZonedDateTime toZonedDateTime(TemporalAccessor date) {
    return toZonedDateTime(date, DEFAULT_ZONE);
  }

  public static ZonedDateTime toZonedDateTime(TemporalAccessor date, ZoneId zone) {
    return ZonedDateTime.of(LocalDateTime.from(date), zone);
  }

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

  private static Pair<LocalDateTime, List<Triple<RelativeDateOperator, RelativeDateType, String>>> parseRelativeDateUnsafe(
    String val,
    boolean includeInfo
  ) {
    return parseRelativeDateUnsafe(val, includeInfo, null);
  }

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
