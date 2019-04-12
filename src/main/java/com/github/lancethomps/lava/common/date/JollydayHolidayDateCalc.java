package com.github.lancethomps.lava.common.date;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameters;

public class JollydayHolidayDateCalc implements HolidayDateCalc {

  private final String[] calendarArgs;

  private final String calendarName;

  private final HolidayManager manager;

  public JollydayHolidayDateCalc() {
    this(Locale.getDefault().toString());
  }

  public JollydayHolidayDateCalc(@Nonnull String calendarName) {
    super();
    this.calendarName = calendarName;
    if ("us".equalsIgnoreCase(calendarName)) {
      manager = HolidayManager.getInstance(
        ManagerParameters.create(
          getClass()
            .getClassLoader()
            .getResource(StringUtils.replace(StringUtils.substringBeforeLast(getClass().getName(), "."), ".", "/") + "/Holidays_usmarket.xml")
        )
      );
      calendarArgs = new String[0];
    } else {
      manager = HolidayManager.getInstance(ManagerParameters.create(calendarName));
      calendarArgs = new String[0];
    }
  }

  @Override
  public long businessDaysBetween(@Nonnull LocalDate earlierDate, @Nonnull LocalDate laterDate) {
    long total = ChronoUnit.DAYS.between(earlierDate, laterDate) + 1;
    long weekends = Dates.countNumOfDayOfWeekBetween(earlierDate, laterDate, DayOfWeek.SATURDAY) +
      Dates.countNumOfDayOfWeekBetween(earlierDate, laterDate, DayOfWeek.SUNDAY);
    long holidays = getHolidaysNonWeekend(earlierDate, laterDate).size();
    return total - weekends - holidays - 1;
  }

  @Override
  public LocalDate changeByBusinessDays(@Nonnull LocalDate date, long count) {
    long absCount = Math.abs(count);
    long daysToAdd = count > 0 ? 1 : -1;
    for (long idx = 0; idx < absCount; idx++) {
      while (Dates.isWeekend(date) || isHoliday(date)) {
        date = date.plusDays(daysToAdd);
      }
    }
    return date;
  }

  public String getCalendarName() {
    return calendarName;
  }

  public Set<Holiday> getHolidaysNonWeekend(@Nonnull LocalDate earlierDate, @Nonnull LocalDate laterDate) {
    return manager
      .getHolidays(earlierDate, laterDate, calendarArgs)
      .stream()
      .filter(hol -> !Dates.isWeekend(hol.getDate()))
      .collect(Collectors.toSet());
  }

  public HolidayManager getManager() {
    return manager;
  }

  @Override
  public boolean isHoliday(@Nonnull LocalDate date) {
    return manager.isHoliday(date, calendarArgs);
  }

}
