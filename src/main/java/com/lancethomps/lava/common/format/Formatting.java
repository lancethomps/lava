package com.lancethomps.lava.common.format;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.logging.Logs;

public final class Formatting {

  public static final BigDecimal BYTES_MULTIPLIER = BigDecimal.valueOf(1024d);

  private static final DecimalFormat PCT_FORMAT = new DecimalFormat("#.##");

  private Formatting() throws InstantiationException {
    throw new InstantiationException("Don't instantitate this class");
  }

  public static double asLongPct(int numerator, int denominator) {
    double pct = (double) (numerator) / (double) denominator;
    return pct;
  }

  public static String asPct(int numerator, int denominator) {
    double pct = (double) (numerator * 100) / (double) denominator;
    return PCT_FORMAT.format(pct) + '%';
  }

  public static double asPctNumber(int numerator, int denominator) {
    return (double) (numerator * 100) / (double) denominator;
  }

  public static BigDecimal bytesToKiloBytes(long byteCount) {
    return bytesToKiloBytes(byteCount, 3);
  }

  public static BigDecimal bytesToKiloBytes(long byteCount, int sigFigs) {
    return BigDecimal.valueOf(byteCount / 1024d).setScale(sigFigs, RoundingMode.HALF_UP);
  }

  public static BigDecimal bytesToMegaBytes(long byteCount) {
    return Formatting.bytesToMegaBytes(byteCount, 3);
  }

  public static BigDecimal bytesToMegaBytes(long byteCount, int sigFigs) {
    return BigDecimal.valueOf(byteCount / 1024d / 1024d).setScale(sigFigs, RoundingMode.HALF_UP);
  }

  public static Object[] createKeyValFormatArgs(@Nullable final Object... formatArgs) {
    return Checks.isEmpty(formatArgs) ? formatArgs : Stream.of(formatArgs).map(Logs::getSplunkValueString).toArray();
  }

  public static String getMessage(final String message, final Object... formatArgs) {
    return (message == null) || (formatArgs == null) || (formatArgs.length == 0) ? message : String.format(message, formatArgs);
  }

  public static String getMessageWithKeyValArgs(final String message, final Object... formatArgs) {
    return (message == null) || (formatArgs == null) || (formatArgs.length == 0) ? message :
      String.format(message, createKeyValFormatArgs(formatArgs));
  }

  public static String humanReadableByteCount(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) {
      return bytes + " B";
    }
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

  public static long megaBytesToBytes(BigDecimal mb) {
    return mb.multiply(BYTES_MULTIPLIER, MathContext.DECIMAL128).multiply(BYTES_MULTIPLIER, MathContext.DECIMAL128).longValue();
  }

}
