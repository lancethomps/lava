package com.github.lancethomps.lava.common.ser.jackson.filter;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * The Class JacksonAnnotationIntrospectorSingleFilter.
 *
 * @author lathomps
 */
public class JacksonAnnotationIntrospectorSingleFilter extends JacksonAnnotationIntrospector {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7063429226703685319L;

	/** The filter id. */
	private String filterId;

	/**
	 * Instantiates a new jackson exclude fields filter inspector.
	 */
	public JacksonAnnotationIntrospectorSingleFilter() {
		this(null);
	}

	/**
	 * Instantiates a new jackson exclude fields filter inspector.
	 *
	 * @param filterId the filter id
	 */
	public JacksonAnnotationIntrospectorSingleFilter(String filterId) {
		super();
		this.filterId = filterId;
	}

	@Override
	public Object findFilterId(Annotated a) {
		return filterId;
	}

}
