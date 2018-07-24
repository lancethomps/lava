package com.github.lancethomps.lava.common.ser.jackson;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.std.MapProperty;

/**
 * The Class JacksonUtils.
 *
 */
public class JacksonUtils {

	/**
	 * Creates the type serializer.
	 *
	 * @param config the config
	 * @param baseType the base type
	 * @return the type serializer
	 */
	public static TypeSerializer createTypeSerializer(SerializationConfig config, JavaType baseType) {
		BeanDescription bean = config.introspectClassAnnotations(baseType.getRawClass());
		AnnotatedClass ac = bean.getClassInfo();
		AnnotationIntrospector ai = config.getAnnotationIntrospector();
		TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);
		/*
		 * Ok: if there is no explicit type info handler, we may want to use a default. If so, config object knows what to use.
		 */
		Collection<NamedType> subtypes = null;
		if (b == null) {
			b = config.getDefaultTyper(baseType);
		} else {
			subtypes = config.getSubtypeResolver().collectAndResolveSubtypesByClass(config, ac);
		}
		if (b == null) {
			return null;
		}
		// 10-Jun-2015, tatu: Since not created for Bean Property, no need for post-processing
		// wrt EXTERNAL_PROPERTY
		return b.buildTypeSerializer(config, baseType, subtypes);
	}

	/**
	 * Gets the property.
	 *
	 * @param <T> the generic type
	 * @param pojo the pojo
	 * @param writer the writer
	 * @return the property
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Object> T getProperty(@Nonnull Object pojo, @Nonnull PropertyWriter writer) throws Exception {
		if (writer instanceof BeanPropertyWriter) {
			return (T) ((BeanPropertyWriter) writer).get(pojo);
		} else if (writer instanceof MapProperty) {
			return (T) pojo;
		}
		return null;
	}
}
