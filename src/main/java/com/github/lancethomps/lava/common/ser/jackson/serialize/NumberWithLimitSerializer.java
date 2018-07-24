package com.github.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.lambda.ThrowingTriConsumer;
import com.github.lancethomps.lava.common.logging.Logs;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * The Class NumberWithLimitSerializer.
 *
 * @param <T> the generic type
 */
public class NumberWithLimitSerializer<T> extends StdScalarSerializer<T> implements ContextualSerializer {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(NumberWithLimitSerializer.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -9123969819834800797L;

	/** The number type. */
	private final JsonParser.NumberType numberType;

	/** The sig figs. */
	private final Integer sigFigs;

	/** The writer. */
	private final ThrowingTriConsumer<JsonGenerator, Integer, T> writer;

	/**
	 * Instantiates a new double serializer.
	 *
	 * @param type the type
	 * @param numberType the number type
	 * @param writer the writer
	 * @param sigFigs the sig figs
	 */
	public NumberWithLimitSerializer(final Class<T> type, final JsonParser.NumberType numberType, final ThrowingTriConsumer<JsonGenerator, Integer, T> writer, final int sigFigs) {
		super(type, false);
		this.numberType = numberType;
		this.writer = writer;
		this.sigFigs = sigFigs;
	}

	@Override
	public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor,
		JavaType typeHint) throws JsonMappingException {
		visitFloatFormat(visitor, typeHint, numberType);
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov,
		BeanProperty property) throws JsonMappingException {
		if (property != null) {
			AnnotatedMember m = property.getMember();
			if (m != null) {
				JsonFormat.Value format = prov.getAnnotationIntrospector()
					.findFormat(m);
				if (format != null) {
					switch (format.getShape()) {
					case STRING:
						return ToStringSerializer.instance;
					default:
					}
				}
			}
		}
		return this;
	}

	@Override
	public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
		return createSchemaNode("number", true);
	}

	@Override
	public void serialize(T val, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
		if (val != null) {
			try {
				writer.accept(jsonGenerator, sigFigs, val);
			} catch (Exception e) {
				Logs.logError(LOG, e, "Error writing number [%s] for class [%s] with sigFigs limit [%s]", val, _handledType, sigFigs);
			}
		} else {
			jsonGenerator.writeNull();
		}
	}
}
