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

/**
 * The Class JollydayHolidayDateCalc.
 */
public class JollydayHolidayDateCalc implements HolidayDateCalc {

	/** The calendar args. */
	private final String[] calendarArgs;

	/** The calendar name. */
	private final String calendarName;

	/** The manager. */
	private final HolidayManager manager;

	/**
	 * Instantiates a new jollyday holiday date calc.
	 */
	public JollydayHolidayDateCalc() {
		this(Locale.getDefault().toString());
	}

	/**
	 * Instantiates a new jollyday holiday date calc.
	 *
	 * @param calendarName the calendar name
	 */
	public JollydayHolidayDateCalc(@Nonnull String calendarName) {
		super();
		this.calendarName = calendarName;
		if ("us".equalsIgnoreCase(calendarName)) {
			manager = HolidayManager.getInstance(
				ManagerParameters.create(
					getClass().getClassLoader().getResource(StringUtils.replace(StringUtils.substringBeforeLast(getClass().getName(), "."), ".", "/") + "/Holidays_usmarket.xml")
				)
			);
			calendarArgs = new String[0];
		} else {
			manager = HolidayManager.getInstance(ManagerParameters.create(calendarName));
			calendarArgs = new String[0];
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.date.HolidayDateCalc#businessDaysBetween(java.time.LocalDate,
	 * java.time.LocalDate)
	 */
	@Override
	public long businessDaysBetween(@Nonnull LocalDate earlierDate, @Nonnull LocalDate laterDate) {
		long total = ChronoUnit.DAYS.between(earlierDate, laterDate) + 1;
		long weekends = Dates.countNumOfDayOfWeekBetween(earlierDate, laterDate, DayOfWeek.SATURDAY) + Dates.countNumOfDayOfWeekBetween(earlierDate, laterDate, DayOfWeek.SUNDAY);
		long holidays = getHolidaysNonWeekend(earlierDate, laterDate).size();
		return total - weekends - holidays - 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.date.HolidayDateCalc#changeByBusinessDays(java.time.LocalDate, long)
	 */
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

	/**
	 * Gets the calendar name.
	 *
	 * @return the calendarName
	 */
	public String getCalendarName() {
		return calendarName;
	}

	/**
	 * Gets the holidays non weekend.
	 *
	 * @param earlierDate the earlier date
	 * @param laterDate the later date
	 * @return the holidays non weekend
	 */
	public Set<Holiday> getHolidaysNonWeekend(@Nonnull LocalDate earlierDate, @Nonnull LocalDate laterDate) {
		return manager.getHolidays(earlierDate, laterDate, calendarArgs).stream().filter(hol -> !Dates.isWeekend(hol.getDate())).collect(Collectors.toSet());
	}

	/**
	 * Gets the manager.
	 *
	 * @return the manager
	 */
	public HolidayManager getManager() {
		return manager;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.date.HolidayDateCalc#isHoliday(java.time.LocalDate)
	 */
	@Override
	public boolean isHoliday(@Nonnull LocalDate date) {
		return manager.isHoliday(date, calendarArgs);
	}

}
