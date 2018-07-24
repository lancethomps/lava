package com.github.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * The Class StringSerializerWithHtmlEncoding.
 */
public class StringSerializerWithHtmlEncoding extends StdScalarSerializer<Object> {

	/** The Constant HTML_ENCODER. */
	private static final Encoder HTML_ENCODER = ESAPI.encoder();

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new string serializer with html encoding.
	 */
	public StringSerializerWithHtmlEncoding() {
		super(String.class, false);
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object,
	 * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
	 */
	@Override
	public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeString(HTML_ENCODER.encodeForHTML((String) value));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.fasterxml.jackson.databind.ser.std.StdScalarSerializer#serializeWithType(java.lang.Object,
	 * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider,
	 * com.fasterxml.jackson.databind.jsontype.TypeSerializer)
	 */
	@Override
	public final void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
		gen.writeString(HTML_ENCODER.encodeForHTML((String) value));
	}

}
