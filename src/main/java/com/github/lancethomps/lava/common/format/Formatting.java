package com.github.lancethomps.lava.common.format;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class FormatUtil.
 */
public final class Formatting {

	/** The Constant BYTES_MULTIPLIER. */
	public static final BigDecimal BYTES_MULTIPLIER = BigDecimal.valueOf(1024d);

	/** The Constant PCT_FORMAT. */
	private static final DecimalFormat PCT_FORMAT = new DecimalFormat("#.##");

	/**
	 * Instantiates a new format utils.
	 *
	 * @throws InstantiationException the instantiation exception
	 */
	private Formatting() throws InstantiationException {
		throw new InstantiationException("Don't instantitate this class");
	}

	/**
	 * As long pct.
	 *
	 * @param numerator the numerator
	 * @param denominator the denominator
	 * @return the double
	 */
	public static double asLongPct(int numerator, int denominator) {
		double pct = (double) (numerator) / (double) denominator;
		return pct;
	}

	/**
	 * As pct.
	 *
	 * @param numerator the numerator
	 * @param denominator the denominator
	 * @return the int
	 */
	public static String asPct(int numerator, int denominator) {
		double pct = (double) (numerator * 100) / (double) denominator;
		return PCT_FORMAT.format(pct) + '%';
	}

	/**
	 * As pct number.
	 *
	 * @param numerator the numerator
	 * @param denominator the denominator
	 * @return the double
	 */
	public static double asPctNumber(int numerator, int denominator) {
		return (double) (numerator * 100) / (double) denominator;
	}

	/**
	 * Bytes to kilo bytes.
	 *
	 * @param byteCount the byte count
	 * @return the big decimal
	 */
	public static BigDecimal bytesToKiloBytes(long byteCount) {
		return bytesToKiloBytes(byteCount, 3);
	}

	/**
	 * Bytes to kilo bytes.
	 *
	 * @param byteCount the byte count
	 * @param sigFigs the sig figs
	 * @return the big decimal
	 */
	public static BigDecimal bytesToKiloBytes(long byteCount, int sigFigs) {
		return BigDecimal.valueOf(byteCount / 1024d).setScale(sigFigs, RoundingMode.HALF_UP);
	}

	/**
	 * Bytes to mega bytes.
	 *
	 * @param byteCount the byte count
	 * @return the big decimal
	 */
	public static BigDecimal bytesToMegaBytes(long byteCount) {
		return Formatting.bytesToMegaBytes(byteCount, 3);
	}

	/**
	 * Bytes to mega bytes.
	 *
	 * @param byteCount the byte count
	 * @param sigFigs the sig figs
	 * @return the big decimal
	 */
	public static BigDecimal bytesToMegaBytes(long byteCount, int sigFigs) {
		return BigDecimal.valueOf(byteCount / 1024d / 1024d).setScale(sigFigs, RoundingMode.HALF_UP);
	}

	/**
	 * Creates the key val format args.
	 *
	 * @param formatArgs the format args
	 * @return the object[]
	 */
	public static Object[] createKeyValFormatArgs(@Nullable final Object... formatArgs) {
		return Checks.isEmpty(formatArgs) ? formatArgs : Stream.of(formatArgs).map(Logs::getSplunkValueString).toArray();
	}

	/**
	 * Gets the message.
	 *
	 * @param message the message
	 * @param formatArgs the format args
	 * @return the message
	 */
	public static String getMessage(final String message, final Object... formatArgs) {
		return (message == null) || (formatArgs == null) || (formatArgs.length == 0) ? message : String.format(message, formatArgs);
	}

	/**
	 * Gets the message with key val args.
	 *
	 * @param message the message
	 * @param formatArgs the format args
	 * @return the message with key val args
	 */
	public static String getMessageWithKeyValArgs(final String message, final Object... formatArgs) {
		return (message == null) || (formatArgs == null) || (formatArgs.length == 0) ? message : String.format(message, createKeyValFormatArgs(formatArgs));
	}

	/**
	 * Human readable byte count.
	 *
	 * @param bytes the bytes
	 * @param si the si
	 * @return the string
	 */
	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) {
			return bytes + " B";
		}
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * Mega bytes to bytes.
	 *
	 * @param mb the mb
	 * @return the long
	 */
	public static long megaBytesToBytes(BigDecimal mb) {
		return mb.multiply(BYTES_MULTIPLIER, MathContext.DECIMAL128).multiply(BYTES_MULTIPLIER, MathContext.DECIMAL128).longValue();
	}
}
