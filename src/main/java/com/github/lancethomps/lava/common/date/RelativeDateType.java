package com.github.lancethomps.lava.common.date;

import java.time.temporal.ChronoUnit;

/**
 * The Enum RelativeDateType.
 */
public enum RelativeDateType {

	/** The b. */
	b(ChronoUnit.DAYS),

	/** The holiday calendar. */
	c(null),

	/** The d. */
	d(ChronoUnit.DAYS),

	/** The h. */
	h(ChronoUnit.HOURS),

	/** The m. */
	m(ChronoUnit.MONTHS),

	/** The mi. */
	mi(ChronoUnit.MICROS),

	/** The mm. */
	mm(ChronoUnit.MINUTES),

	/** The ms. */
	ms(ChronoUnit.MILLIS),

	/** The n. */
	n(ChronoUnit.NANOS),

	/** The s. */
	s(ChronoUnit.SECONDS),

	/** The w. */
	w(ChronoUnit.WEEKS),

	/** The y. */
	y(ChronoUnit.YEARS),

	/** The time zone. */
	z(null);

	/** The unit. */
	private final ChronoUnit unit;

	/**
	 * Instantiates a new relative date type.
	 *
	 * @param unit the unit
	 */
	RelativeDateType(ChronoUnit unit) {
		this.unit = unit;
	}

	/**
	 * @return the unit
	 */
	public ChronoUnit getUnit() {
		return unit;
	}
}
