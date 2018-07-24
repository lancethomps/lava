package com.github.lancethomps.lava.common.ser.csv;

import com.github.lancethomps.lava.common.ser.SerializerFactory;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.util.NameTransformer;

/**
 * The Class CsvAnnotationIntrospector.
 */
public class CsvAnnotationIntrospector extends JacksonAnnotationIntrospector {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7645118621906559253L;

	/** The use default unwrapping. */
	private static boolean useDefaultUnwrapping;

	@Override
	public Object findFilterId(Annotated a) {
		return SerializerFactory.CSV_FILTER;
	}

	@Override
	public NameTransformer findUnwrappingNameTransformer(AnnotatedMember member) {
		if (useDefaultUnwrapping) {
			return super.findUnwrappingNameTransformer(member);
		}
		return null;
	}
}
