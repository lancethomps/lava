package com.github.lancethomps.lava.common.ser.jackson;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.github.lancethomps.lava.common.Collect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.deser.std.StringCollectionDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * The Class CustomStringCollectionDeserializer.
 */
public class CustomStringCollectionDeserializer extends ContainerDeserializerBase<Collection<String>> implements ContextualDeserializer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5496096135069140655L;

	/** The Constant DEFAULT_FUNCTION. */
	private static final Function<String, Collection<String>> DEFAULT_FUNCTION = Collect::splitCsvAsList;

	/** The collection type. */
	private final JavaType collectionType;

	/** The function. */
	private final Function<String, Collection<String>> function;

	/** The original. */
	private final StringCollectionDeserializer original;

	/**
	 * Instantiates a new custom string collection deserializer.
	 */
	public CustomStringCollectionDeserializer() {
		this(TypeFactory.defaultInstance().constructCollectionType(List.class, String.class), null, DEFAULT_FUNCTION);
	}

	/**
	 * Instantiates a new custom string collection deserializer.
	 *
	 * @param collectionType the collection type
	 * @param original the original
	 * @param function the function
	 */
	public CustomStringCollectionDeserializer(JavaType collectionType, StringCollectionDeserializer original, Function<String, Collection<String>> function) {
		super(collectionType);
		this.collectionType = collectionType;
		this.original = original;
		this.function = function;
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		if (original == null) {
			JsonDeserializer<?> root = ctxt.findRootValueDeserializer(property.getType());
			Function<String, Collection<String>> contextualFunction = function;
			if ((function == DEFAULT_FUNCTION) && Set.class.isAssignableFrom(property.getType().getRawClass())) {
				contextualFunction = val -> new LinkedHashSet<>(Arrays.asList(Collect.splitCsv(val)));
			}
			return new CustomStringCollectionDeserializer(property.getType(), (StringCollectionDeserializer) root, contextualFunction);
		}
		StringCollectionDeserializer created = (StringCollectionDeserializer) original.createContextual(ctxt, property);
		if (created == original) {
			return this;
		}
		return new CustomStringCollectionDeserializer(collectionType, original, function);
	}

	@Override
	public Collection<String> deserialize(JsonParser p, DeserializationContext ctxt)
		throws IOException {
		if (!p.isExpectedStartArrayToken() && (function != null)) {
			return function.apply(p.getText());
		}
		return original.deserialize(p, ctxt);
	}

	@Override
	public Collection<String> deserialize(JsonParser p, DeserializationContext ctxt, Collection<String> result) throws IOException {
		return original.deserialize(p, ctxt, result);
	}

	@Override
	public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
		return typeDeserializer.deserializeTypedFromArray(p, ctxt);
	}

	@Override
	public JsonDeserializer<Object> getContentDeserializer() {
		return original.getContentDeserializer();
	}

	@Override
	public JavaType getContentType() {
		return original.getContentType();
	}

	@Override
	public boolean isCachable() {
		return original.isCachable();
	}

}
