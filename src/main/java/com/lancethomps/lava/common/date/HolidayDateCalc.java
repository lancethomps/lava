package com.lancethomps.lava.common.date;

import java.time.LocalDate;

import javax.annotation.Nonnull;

public interface HolidayDateCalc {

  long businessDaysBetween(@Nonnull LocalDate earlierDate, @Nonnull LocalDate laterDate);

  LocalDate changeByBusinessDays(@Nonnull LocalDate date, long count);

  default boolean isBusinessDay(@Nonnull LocalDate date) {
    return !Dates.isWeekend(date) && !isHoliday(date);
  }

  boolean isHoliday(@Nonnull LocalDate date);

}
