package com.github.lancethomps.lava.common.string;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringStyle;

import com.github.lancethomps.lava.common.Checks;

public class OmittingToStringStyle extends ToStringStyle {

  public static final OmittingToStringStyle DEFAULT_INSTANCE = new OmittingToStringStyle(true, false, false, false, false);

  private static final long serialVersionUID = 1L;

  private boolean omitEmptyArrays;

  private boolean omitEmptyCollections;

  private boolean omitEmptyMaps;

  private boolean omitEmptyStrings;

  private boolean omitNulls = true;

  public OmittingToStringStyle() {
    super();
    setUseShortClassName(true);
    setUseIdentityHashCode(false);
  }

  public OmittingToStringStyle(
    boolean omitNulls,
    boolean omitEmptyArrays,
    boolean omitEmptyCollections,
    boolean omitEmptyMaps,
    boolean omitEmptyStrings
  ) {
    this();
    this.omitNulls = omitNulls;
    this.omitEmptyArrays = omitEmptyArrays;
    this.omitEmptyCollections = omitEmptyCollections;
    this.omitEmptyMaps = omitEmptyMaps;
    this.omitEmptyStrings = omitEmptyStrings;
  }

  @Override
  public void append(StringBuffer buffer, String fieldName, boolean[] array, Boolean fullDetail) {
    if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
      super.append(buffer, fieldName, array, fullDetail);
    }
  }

  @Override
  public void append(StringBuffer buffer, String fieldName, byte[] array, Boolean fullDetail) {
    if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
      super.append(buffer, fieldName, array, fullDetail);
    }
  }

  @Override
  public void append(StringBuffer buffer, String fieldName, char[] array, Boolean fullDetail) {
    if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
      super.append(buffer, fieldName, array, fullDetail);
    }
  }

  @Override
  public void append(StringBuffer buffer, String fieldName, double[] array, Boolean fullDetail) {
    if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
      super.append(buffer, fieldName, array, fullDetail);
    }
  }

  @Override
  public void append(StringBuffer buffer, String fieldName, float[] array, Boolean fullDetail) {
    if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
      super.append(buffer, fieldName, array, fullDetail);
    }
  }

  @Override
  public void append(StringBuffer buffer, String fieldName, int[] array, Boolean fullDetail) {
    if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
      super.append(buffer, fieldName, array, fullDetail);
    }
  }

  @Override
  public void append(StringBuffer buffer, String fieldName, long[] array, Boolean fullDetail) {
    if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
      super.append(buffer, fieldName, array, fullDetail);
    }
  }

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

  @Override
  public void append(StringBuffer buffer, String fieldName, Object[] array, Boolean fullDetail) {
    if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
      super.append(buffer, fieldName, array, fullDetail);
    }
  }

  @Override
  public void append(StringBuffer buffer, String fieldName, short[] array, Boolean fullDetail) {
    if ((!omitNulls || (array != null)) && (!omitEmptyArrays || Checks.isNotEmpty(array))) {
      super.append(buffer, fieldName, array, fullDetail);
    }
  }

  public boolean isOmitEmptyArrays() {
    return omitEmptyArrays;
  }

  public OmittingToStringStyle setOmitEmptyArrays(boolean omitEmptyArrays) {
    this.omitEmptyArrays = omitEmptyArrays;
    return this;
  }

  public boolean isOmitEmptyCollections() {
    return omitEmptyCollections;
  }

  public OmittingToStringStyle setOmitEmptyCollections(boolean omitEmptyCollections) {
    this.omitEmptyCollections = omitEmptyCollections;
    return this;
  }

  public boolean isOmitEmptyMaps() {
    return omitEmptyMaps;
  }

  public OmittingToStringStyle setOmitEmptyMaps(boolean omitEmptyMaps) {
    this.omitEmptyMaps = omitEmptyMaps;
    return this;
  }

  public boolean isOmitEmptyStrings() {
    return omitEmptyStrings;
  }

  public OmittingToStringStyle setOmitEmptyStrings(boolean omitEmptyStrings) {
    this.omitEmptyStrings = omitEmptyStrings;
    return this;
  }

  public boolean isOmitNulls() {
    return omitNulls;
  }

  public OmittingToStringStyle setOmitNulls(boolean omitNulls) {
    this.omitNulls = omitNulls;
    return this;
  }

}
