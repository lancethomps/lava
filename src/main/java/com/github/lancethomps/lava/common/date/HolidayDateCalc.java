package com.github.lancethomps.lava.common.date;

import java.time.LocalDate;

import javax.annotation.Nonnull;

/**
 * The Class HolidayDateCalc.
 */
public interface HolidayDateCalc {

	/**
	 * Business days between.
	 *
	 * @param earlierDate the earlier date
	 * @param laterDate the later date
	 * @return the long
	 */
	long businessDaysBetween(@Nonnull LocalDate earlierDate, @Nonnull LocalDate laterDate);

	/**
	 * Change by business days.
	 *
	 * @param date the date
	 * @param count the count
	 * @return the local date
	 */
	LocalDate changeByBusinessDays(@Nonnull LocalDate date, long count);

	/**
	 * Checks if is business day.
	 *
	 * @param date the date
	 * @return true, if is business day
	 */
	default boolean isBusinessDay(@Nonnull LocalDate date) {
		return !Dates.isWeekend(date) && !isHoliday(date);
	}

	/**
	 * Checks if is holiday.
	 *
	 * @param date the date
	 * @return true, if is holiday
	 */
	boolean isHoliday(@Nonnull LocalDate date);

}
