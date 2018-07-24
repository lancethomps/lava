package com.github.lancethomps.lava.common.string;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringStyle;

import com.github.lancethomps.lava.common.Checks;

/**
 * The Class OmittingToStringStyle.
 */
public class OmittingToStringStyle extends ToStringStyle {

	/** The Constant DEFAULT_INSTANCE. */
	public static final OmittingToStringStyle DEFAULT_INSTANCE = new OmittingToStringStyle(true, false, false, false, false);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The omit empty arrays. */
	private boolean omitEmptyArrays;

	/** The omit empty collections. */
	private boolean omitEmptyCollections;

	/** The omit empty maps. */
	private boolean omitEmptyMaps;

	/** The omit empty strings. */
	private boolean omitEmptyStrings;

	/** The omit nulls. */
	private boolean omitNulls = true;

	/**
	 * Instantiates a new omitting to string style.
	 */
	public OmittingToStringStyle() {
		super();
		setUseShortClassName(true);
		setUseIdentityHashCode(false);
	}

	/**
	 * Instantiates a new omitting to string style.
	 *
	 * @param omitNulls the omit nulls
	 * @param omitEmptyArrays the omit empty arrays
	 * @param omitEmptyCollections the omit empty collections
	 * @param omitEmptyMaps the omit empty maps
	 * @param omitEmptyStrings the omit empty strings
	 */
	public OmittingToStringStyle(boolean omitNulls, boolean omitEmptyArrays, boolean omitEmptyCollections, boolean omitEmptyMaps, boolean omitEmptyStrings) {
		this();
		this.omitNulls = omitNulls;
		this.omitEmptyArrays = omitEmptyArrays;
		this.omitEmptyCollections = omitEmptyCollections;
		this.omitEmptyMaps = omitEmptyMaps;
		this.omitEmptyStrings = omitEmptyStrings;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.lang3.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, boolean[], java.lang.Boolean)
	 */
	@Override
	public void append(StringBuffer buffer, String fieldName, boolean[] array, Boolean fullDetail) {
		if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
			super.append(buffer, fieldName, array, fullDetail);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.lang3.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, byte[], java.lang.Boolean)
	 */
	@Override
	public void append(StringBuffer buffer, String fieldName, byte[] array, Boolean fullDetail) {
		if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
			super.append(buffer, fieldName, array, fullDetail);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.lang3.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, char[], java.lang.Boolean)
	 */
	@Override
	public void append(StringBuffer buffer, String fieldName, char[] array, Boolean fullDetail) {
		if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
			super.append(buffer, fieldName, array, fullDetail);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.lang3.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, double[], java.lang.Boolean)
	 */
	@Override
	public void append(StringBuffer buffer, String fieldName, double[] array, Boolean fullDetail) {
		if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
			super.append(buffer, fieldName, array, fullDetail);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.lang3.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, float[], java.lang.Boolean)
	 */
	@Override
	public void append(StringBuffer buffer, String fieldName, float[] array, Boolean fullDetail) {
		if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
			super.append(buffer, fieldName, array, fullDetail);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.lang3.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, int[], java.lang.Boolean)
	 */
	@Override
	public void append(StringBuffer buffer, String fieldName, int[] array, Boolean fullDetail) {
		if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
			super.append(buffer, fieldName, array, fullDetail);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.lang3.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, long[], java.lang.Boolean)
	 */
	@Override
	public void append(StringBuffer buffer, String fieldName, long[] array, Boolean fullDetail) {
		if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
			super.append(buffer, fieldName, array, fullDetail);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.lang3.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, java.lang.Object, java.lang.Boolean)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
		if (!omitNulls || (value != null)) {
			if (omitEmptyCollections && (value instanceof Collection)) {
				if (((Collection) value).isEmpty()) {
					return;
				}
			} else if (omitEmptyMaps && (value instanceof Map)) {
				if (((Map) value).isEmpty()) {
					return;
				}
			} else if (omitEmptyStrings && (value instanceof String)) {
				if (Checks.isBlank(((String) value))) {
					return;
				}
			}
			super.append(buffer, fieldName, value, fullDetail);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.lang3.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, java.lang.Object[], java.lang.Boolean)
	 */
	@Override
	public void append(StringBuffer buffer, String fieldName, Object[] array, Boolean fullDetail) {
		if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
			super.append(buffer, fieldName, array, fullDetail);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.lang3.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, short[], java.lang.Boolean)
	 */
	@Override
	public void append(StringBuffer buffer, String fieldName, short[] array, Boolean fullDetail) {
		if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
			super.append(buffer, fieldName, array, fullDetail);
		}
	}

	/**
	 * @return the omitEmptyArrays
	 */
	public boolean isOmitEmptyArrays() {
		return omitEmptyArrays;
	}

	/**
	 * Checks if is omit empty collections.
	 *
	 * @return the omitEmptyCollections
	 */
	public boolean isOmitEmptyCollections() {
		return omitEmptyCollections;
	}

	/**
	 * Checks if is omit empty maps.
	 *
	 * @return the omitEmptyMaps
	 */
	public boolean isOmitEmptyMaps() {
		return omitEmptyMaps;
	}

	/**
	 * Checks if is omit empty strings.
	 *
	 * @return the omitEmptyStrings
	 */
	public boolean isOmitEmptyStrings() {
		return omitEmptyStrings;
	}

	/**
	 * @return the omitNulls
	 */
	public boolean isOmitNulls() {
		return omitNulls;
	}

	/**
	 * Sets the omit empty arrays.
	 *
	 * @param omitEmptyArrays the omitEmptyArrays to set
	 * @return the omitting to string style
	 */
	public OmittingToStringStyle setOmitEmptyArrays(boolean omitEmptyArrays) {
		this.omitEmptyArrays = omitEmptyArrays;
		return this;
	}

	/**
	 * Sets the omit empty collections.
	 *
	 * @param omitEmptyCollections the omitEmptyCollections to set
	 * @return the omitting to string style
	 */
	public OmittingToStringStyle setOmitEmptyCollections(boolean omitEmptyCollections) {
		this.omitEmptyCollections = omitEmptyCollections;
		return this;
	}

	/**
	 * Sets the omit empty maps.
	 *
	 * @param omitEmptyMaps the omitEmptyMaps to set
	 * @return the omitting to string style
	 */
	public OmittingToStringStyle setOmitEmptyMaps(boolean omitEmptyMaps) {
		this.omitEmptyMaps = omitEmptyMaps;
		return this;
	}

	/**
	 * Sets the omit empty strings.
	 *
	 * @param omitEmptyStrings the omitEmptyStrings to set
	 * @return the omitting to string style
	 */
	public OmittingToStringStyle setOmitEmptyStrings(boolean omitEmptyStrings) {
		this.omitEmptyStrings = omitEmptyStrings;
		return this;
	}

	/**
	 * Sets the omit nulls.
	 *
	 * @param omitNulls the omitNulls to set
	 * @return the omitting to string style
	 */
	public OmittingToStringStyle setOmitNulls(boolean omitNulls) {
		this.omitNulls = omitNulls;
		return this;
	}
}
