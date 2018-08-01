package com.github.lancethomps.lava.common.ser.jackson.filter;

import com.fasterxml.jackson.annotation.JsonFilter;

import net.jcip.annotations.ThreadSafe;

/**
 * The Class FieldsFilterMixIn.
 *
 * @author lancethomps
 */
@ThreadSafe
@JsonFilter(FieldsFilter.FIELDS_FILTER_ID)
public class FieldsFilterMixIn {
}
