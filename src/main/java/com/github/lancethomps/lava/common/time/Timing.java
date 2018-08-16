package com.github.lancethomps.lava.common.time;

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * The Class TimeUtil.
 */
public class Timing {

	/** The Constant NANO_2_MILLIS. */
	public static final long NANO_2_MILLIS = 1000000L;

	/** The Constant ONE_DAY_MS. */
	public static final long ONE_DAY_MS = 1000 * 60 * 60 * 24;

	/** The Constant ONE_HOUR_MS. */
	public static final long ONE_HOUR_MS = 1000 * 60 * 60;

	/** The Constant ONE_MIN_MS. */
	public static final long ONE_MIN_MS = 1000 * 60;

	/**
	 * Estimate completion time.
	 *
	 * @param startTime the start time
	 * @param count the count
	 * @param total the total
	 * @return the local date time
	 */
	public static LocalDateTime estimateCompletionTime(@Nonnull LocalDateTime startTime, long count, long total) {
		long millis = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
		long estimatedTotal = Double.valueOf(millis * ((double) total / (double) count)).longValue();
		return startTime.plus(estimatedTotal, ChronoUnit.MILLIS);
	}

	/**
	 * Estimate time remaining millis.
	 *
	 * @param startTime the start time
	 * @param count the count
	 * @param total the total
	 * @return the long
	 */
	public static long estimateTimeRemainingMillis(@Nonnull LocalDateTime startTime, long count, long total) {
		return ChronoUnit.MILLIS.between(LocalDateTime.now(), estimateCompletionTime(startTime, count, total));
	}

	/**
	 * Estimate time remaining millis.
	 *
	 * @param watch the watch
	 * @param count the count
	 * @param total the total
	 * @return the long
	 */
	public static long estimateTimeRemainingMillis(@Nonnull Stopwatch watch, long count, long total) {
		long millis = watch.getTime();
		long estimatedTotal = Double.valueOf(millis * ((double) total / (double) count)).longValue();
		return estimatedTotal - millis;
	}

	/**
	 * Format interval.
	 *
	 * @param millis the millis
	 * @return the string
	 */
	public static String formatInterval(final long millis) {
		final long hr = TimeUnit.MILLISECONDS.toHours(millis);
		final long min = TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS.toSeconds(millis - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		final long ms = TimeUnit.MILLISECONDS.toMillis(millis - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
		return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
	}

	/**
	 * Format millis.
	 *
	 * @param millis the millis
	 * @return the string
	 */
	public static String formatMillis(long millis) {
		// String format = millis >= ONE_HOUR_MS ? "H:mm:ss.SSS" : "m:ss.SSS";
		String format = "H:mm:ss.SSS";
		return formatMillis(millis, format);
	}

	/**
	 * Format millis.
	 *
	 * @param millis the millis
	 * @param format the format
	 * @return the string
	 */
	public static String formatMillis(long millis, String format) {
		long nanos = Math.abs(millis * NANO_2_MILLIS);
		if (nanos > ChronoField.NANO_OF_DAY.range().getMaximum()) {
			return formatMillisStandard(millis);
		}
		return (millis < 0 ? '-' : "") + DateTimeFormatter.ofPattern(format).format(LocalTime.ofNanoOfDay(nanos));
	}

	/**
	 * Format millis standard.
	 *
	 * @param millis the millis
	 * @return the string
	 */
	public static String formatMillisStandard(long millis) {
		return (millis < 0 ? '-' : "") + String.format(
			"%d:%02d:%02d.%03d",
			TimeUnit.MILLISECONDS.toHours(millis),
			TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
			TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
			millis - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millis))
		);
	}

	/**
	 * Gets the elapsed time string.
	 *
	 * @param durationMillis the duration millis
	 * @return the elapsed time string
	 */
	public static String getElapsedTimeString(final long durationMillis) {
		return getElapsedTimeString(durationMillis, TimeUnit.MILLISECONDS);
	}

	/**
	 * Gets the elapsed time string.
	 *
	 * @param duration the duration
	 * @param unit the unit
	 * @return the elapsed time string
	 */
	public static String getElapsedTimeString(final long duration, @Nonnull final TimeUnit unit) {
		double x = unit.toMillis(duration) / 1000d;
		long seconds = (long) Math.floor(x % 60);
		x /= 60;
		long minutes = (long) Math.floor(x % 60);
		x /= 60;
		long hours = (long) Math.floor(x % 24);
		x /= 24;
		long days = (long) Math.floor(x);
		StringBuilder str = new StringBuilder();
		if (days > 0) {
			str.append(days).append(" days, ");
		}
		if (hours > 0) {
			str.append(hours).append(" hours, ");
		}
		if (minutes > 0) {
			str.append(minutes).append(" min, ");
		}
		str.append(seconds).append(" sec");
		return str.toString();
	}

	/**
	 * Gets the stop watch.
	 *
	 * @return the stop watch
	 */
	public static Stopwatch getStopwatch() {
		return getStopwatch(true);
	}

	/**
	 * Gets the stop watch.
	 *
	 * @param start the start
	 * @return the stop watch
	 */
	public static Stopwatch getStopwatch(boolean start) {
		return new Stopwatch(start);
	}

	/**
	 * Millis to seconds.
	 *
	 * @param millis the millis
	 * @return the double
	 */
	public static double millisToSeconds(Long millis) {
		return new BigDecimal(millis).movePointLeft(3).doubleValue();
	}

	/**
	 * Millis to seconds.
	 *
	 * @param millis the millis
	 * @param scale the scale
	 * @return the double
	 */
	public static double millisToSeconds(Long millis, int scale) {
		return new BigDecimal(millis).movePointLeft(3).setScale(scale, HALF_UP).doubleValue();
	}
}