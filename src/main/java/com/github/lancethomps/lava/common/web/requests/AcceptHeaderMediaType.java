package com.github.lancethomps.lava.common.web.requests;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.github.lancethomps.lava.common.Enums;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.github.lancethomps.lava.common.ser.OutputFormat;

public class AcceptHeaderMediaType extends ExternalizableBean implements Comparable<AcceptHeaderMediaType> {

  public static final Pattern EXTRACTOR_REGEX = Pattern.compile("^\\s*([^,;\\s]+)(.*;\\s*q=([0-9\\.]+))?");

  private final String mediaRange;

  private final String mediaSubtype;

  private final String mediaType;

  private final OutputFormat outputFormat;

  private final double qualityFactor;

  public AcceptHeaderMediaType(String mediaRange) {
    this(mediaRange, 1.0, null);
  }

  public AcceptHeaderMediaType(String mediaRange, double qualityFactor) {
    this(mediaRange, qualityFactor, mediaRange == null ? null : Enums.fromString(OutputFormat.class, mediaRange));
  }

  public AcceptHeaderMediaType(String mediaRange, double qualityFactor, OutputFormat outputFormat) {
    this.mediaRange = mediaRange;
    mediaType = mediaRange == null ? null : trimToNull(StringUtils.substringBefore(mediaRange, "/"));
    mediaSubtype = mediaRange == null ? null : trimToNull(StringUtils.substringAfter(mediaRange, "/"));
    this.qualityFactor = qualityFactor;
    this.outputFormat = outputFormat;
  }

  public static AcceptHeaderMediaType fromHeaderPart(String headerPart) {
    if (headerPart == null) {
      return null;
    }
    Matcher matcher = EXTRACTOR_REGEX.matcher(headerPart);
    if (!matcher.matches()) {
      return null;
    }
    return new AcceptHeaderMediaType(matcher.group(1), NumberUtils.toDouble(matcher.group(3), 1.0));
  }

  public String getMediaRange() {
    return mediaRange;
  }

  public String getMediaSubtype() {
    return mediaSubtype;
  }

  public String getMediaType() {
    return mediaType;
  }

  public OutputFormat getOutputFormat() {
    return outputFormat;
  }

  public double getQualityFactor() {
    return qualityFactor;
  }

  public String toHeader() {
    return mediaRange + "; q=" + qualityFactor;
  }

  @Override
  public int compareTo(AcceptHeaderMediaType other) {
    if (other == null) {
      return 1;
    }
    int comparison = Double.compare(qualityFactor, other.getQualityFactor());
    return comparison == 0 ? toHeader().compareTo(other.toHeader()) : -comparison;
  }

}
