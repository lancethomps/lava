package com.github.lancethomps.lava.common.web.requests;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.github.lancethomps.lava.common.Enums;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.github.lancethomps.lava.common.ser.OutputFormat;

/**
 * The Class AcceptHeaderMediaType.
 *
 * @author lancethomps
 */
public class AcceptHeaderMediaType extends ExternalizableBean implements Comparable<AcceptHeaderMediaType> {

	/** The Constant EXTRACTOR_REGEX. */
	public static final Pattern EXTRACTOR_REGEX = Pattern.compile("^\\s*([^,;\\s]+)(.*;\\s*q=([0-9\\.]+))?");

	/** The media range. */
	private final String mediaRange;

	/** The media subtype. */
	private final String mediaSubtype;

	/** The media type. */
	private final String mediaType;

	/** The output format. */
	private final OutputFormat outputFormat;

	/** The quality factor. */
	private final double qualityFactor;

	/**
	 * Instantiates a new accept header media type.
	 *
	 * @param mediaRange the media range
	 */
	public AcceptHeaderMediaType(String mediaRange) {
		this(mediaRange, 1.0, null);
	}

	/**
	 * Instantiates a new accept header media type.
	 *
	 * @param mediaRange the media range
	 * @param qualityFactor the quality factor
	 */
	public AcceptHeaderMediaType(String mediaRange, double qualityFactor) {
		this(mediaRange, qualityFactor, mediaRange == null ? null : Enums.fromString(OutputFormat.class, mediaRange));
	}

	/**
	 * Instantiates a new accept header media type.
	 *
	 * @param mediaRange the media range
	 * @param qualityFactor the quality factor
	 * @param outputFormat the output format
	 */
	public AcceptHeaderMediaType(String mediaRange, double qualityFactor, OutputFormat outputFormat) {
		this.mediaRange = mediaRange;
		mediaType = mediaRange == null ? null : trimToNull(StringUtils.substringBefore(mediaRange, "/"));
		mediaSubtype = mediaRange == null ? null : trimToNull(StringUtils.substringAfter(mediaRange, "/"));
		this.qualityFactor = qualityFactor;
		this.outputFormat = outputFormat;
	}

	/**
	 * From header part.
	 *
	 * @param headerPart the header part
	 * @return the accept header media type
	 */
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

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AcceptHeaderMediaType other) {
		if (other == null) {
			return 1;
		}
		int comparison = Double.compare(qualityFactor, other.getQualityFactor());
		return comparison == 0 ? toHeader().compareTo(other.toHeader()) : -comparison;
	}

	/**
	 * Gets the media range.
	 *
	 * @return the mediaRange
	 */
	public String getMediaRange() {
		return mediaRange;
	}

	/**
	 * @return the mediaSubtype
	 */
	public String getMediaSubtype() {
		return mediaSubtype;
	}

	/**
	 * @return the mediaType
	 */
	public String getMediaType() {
		return mediaType;
	}

	/**
	 * Gets the output format.
	 *
	 * @return the outputFormat
	 */
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}

	/**
	 * Gets the quality factor.
	 *
	 * @return the qualityFactor
	 */
	public double getQualityFactor() {
		return qualityFactor;
	}

	/**
	 * To header.
	 *
	 * @return the string
	 */
	public String toHeader() {
		return mediaRange + "; q=" + qualityFactor;
	}

}
