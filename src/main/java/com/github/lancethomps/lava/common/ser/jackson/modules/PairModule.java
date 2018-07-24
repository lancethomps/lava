/*
 *
 */
package com.github.lancethomps.lava.common.ser.jackson.modules;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.github.lancethomps.lava.common.Checks;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;
import com.fasterxml.jackson.databind.ser.Serializers;

/**
 * The Class PairModule.
 *
 * @author lathomps
 */
public class PairModule extends Module {

	/*
	 * (non-Javadoc)
	 *
	 * @see com.fasterxml.jackson.databind.Module#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return "WTP PairModule";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.fasterxml.jackson.databind.Module#setupModule(com.fasterxml.jackson.databind.Module.SetupContext)
	 */
	@Override
	public void setupModule(SetupContext context) {
		context.addSerializers(new PairsSerializerFinder());
		context.addDeserializers(new PairDeserializerFinder());
		context.addKeySerializers(new PairsKeySerializerFinder());
		context.addKeyDeserializers(new PairsKeyDeserializerFinder());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.fasterxml.jackson.databind.Module#version()
	 */
	@Override
	public Version version() {
		return Version.unknownVersion();
	}

	/**
	 * The Class PairDeserializer.
	 *
	 * @author lathomps
	 */
	public static class PairDeserializer extends JsonDeserializer<Pair<?, ?>> {

		/** The type. */
		private final JavaType type;

		/**
		 * Instantiates a new pair deserializer.
		 *
		 * @param type the type
		 */
		public PairDeserializer(JavaType type) {
			this.type = type;
		}

		/**
		 * Expect.
		 *
		 * @param actual the actual
		 * @param expected the expected
		 * @param jp the jp
		 * @param ctxt the ctxt
		 * @throws JsonMappingException the json mapping exception
		 */
		private static void expect(JsonToken actual, JsonToken expected, JsonParser jp, DeserializationContext ctxt) throws JsonMappingException {
			if (actual != expected) {
				throw ctxt.wrongTokenException(jp, expected, "Wrong token");
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		public Pair<?, ?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
			JsonDeserializer<Object> deserK = ctxt.findContextualValueDeserializer(type.containedType(0), null);
			JsonDeserializer<Object> deserV = ctxt.findContextualValueDeserializer(type.containedType(1), null);

			Object k = null, v = null;

			if (!(jp.hasCurrentToken() && (jp.getCurrentToken() == JsonToken.START_OBJECT))) {
				expect(jp.nextToken(), JsonToken.START_OBJECT, jp, ctxt);
			}

			while (true) {
				JsonToken nextToken = jp.nextToken();
				if (nextToken == JsonToken.END_OBJECT) {
					break;
				}
				expect(nextToken, JsonToken.FIELD_NAME, jp, ctxt);
				jp.nextToken();
				if (PairSerializer.KEY_FIELD.equals(jp.getCurrentName())) {
					if (k != null) {
						throw new JsonMappingException("Multiple key properties for Pair", jp.getCurrentLocation());
					}
					k = deserK.deserialize(jp, ctxt);
				} else if (PairSerializer.VAL_FIELD.equals(jp.getCurrentName())) {
					if (v != null) {
						throw new JsonMappingException("Multiple value properties for Pair", jp.getCurrentLocation());
					}
					v = deserV.deserialize(jp, ctxt);
				} else if ((k == null) && (v == null)) {
					KeyDeserializer deserKeyK = ctxt.findKeyDeserializer(type.containedType(0), null);
					k = deserKeyK.deserializeKey(jp.getCurrentName(), ctxt);
					v = deserV.deserialize(jp, ctxt);
				} else {
					if (!ctxt.handleUnknownProperty(jp, this, Map.Entry.class, jp.getCurrentName())) {
						throw new JsonMappingException("Unknown Pair property " + jp.getCurrentName(), jp.getCurrentLocation());
					}
				}
			}

			return Pair.of(k, v);
		}
	}

	/**
	 * The Class PairDeserializerFinder.
	 *
	 * @author lathomps
	 */
	public static class PairDeserializerFinder extends Deserializers.Base {

		/*
		 * (non-Javadoc)
		 *
		 * @see com.fasterxml.jackson.databind.deser.Deserializers.Base#findBeanDeserializer(com.fasterxml.jackson.databind.JavaType,
		 * com.fasterxml.jackson.databind.DeserializationConfig, com.fasterxml.jackson.databind.BeanDescription)
		 */
		@Override
		public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
			if (type.hasRawClass(Pair.class)) {
				return new PairDeserializer(type);
			}
			return null;
		}
	}

	/**
	 * The Class PairKeyDeserializer.
	 *
	 * @author lathomps
	 */
	public static class PairKeyDeserializer extends KeyDeserializer {

		/** The sep value. */
		private final String sepValue;

		/** The type. */
		private final JavaType type;

		/**
		 * Instantiates a new pair key deserializer.
		 *
		 * @param type the type
		 */
		public PairKeyDeserializer(JavaType type) {
			this(type, null);
		}

		/**
		 * Instantiates a new pair deserializer.
		 *
		 * @param type the type
		 * @param sepValue the sep value
		 */
		public PairKeyDeserializer(JavaType type, String sepValue) {
			super();
			this.type = type;
			this.sepValue = Checks.defaultIfNull(sepValue, ":");
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.fasterxml.jackson.databind.KeyDeserializer#deserializeKey(java.lang.String, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			KeyDeserializer deserK = ctxt.findKeyDeserializer(type.containedType(0), null);
			KeyDeserializer deserV = ctxt.findKeyDeserializer(type.containedType(1), null);
			String left = StringUtils.substringBefore(key, sepValue);
			String right = StringUtils.substringAfter(key, sepValue);
			return Pair.of(Checks.isBlank(left) ? null : deserK.deserializeKey(left, ctxt), Checks.isBlank(right) ? null : deserV.deserializeKey(right, ctxt));
		}
	}

	/**
	 * The Class PairKeySerializer.
	 *
	 * @author lathomps
	 */
	public static class PairKeySerializer extends JsonSerializer<Pair<?, ?>> {

		/** The sep value. */
		private final String sepValue;

		/** The type. */
		private final JavaType type;

		/**
		 * Instantiates a new pair key serializer.
		 *
		 * @param config the config
		 * @param type the type
		 */
		public PairKeySerializer(SerializationConfig config, JavaType type) {
			this(config, type, null);
		}

		/**
		 * Instantiates a new pair serializer.
		 *
		 * @param config the config
		 * @param type the type
		 * @param sepValue the sep value
		 */
		public PairKeySerializer(SerializationConfig config, JavaType type, String sepValue) {
			super();
			this.type = type;
			this.sepValue = Checks.defaultIfNull(sepValue, ":");
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		public void serialize(Pair<?, ?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			// TODO: use the field type serializers to get the string values
			jgen.writeFieldName(value == null ? "null"
				: StringUtils.defaultString(value.getKey() == null ? null : value.getKey().toString()) + sepValue
					+ StringUtils.defaultString(value.getValue() == null ? null : value.getValue().toString()));
		}
	}

	/**
	 * The Class PairSerializer.
	 *
	 * @author lathomps
	 */
	public static class PairSerializer extends JsonSerializer<Pair<?, ?>> {

		/** The Constant KEY_FIELD. */
		public static final String KEY_FIELD = "left";

		/** The Constant VAL_FIELD. */
		public static final String VAL_FIELD = "right";

		/**
		 * Instantiates a new pair serializer.
		 *
		 * @param config the config
		 */
		public PairSerializer(SerializationConfig config) {
			super();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		public void serialize(Pair<?, ?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			jgen.writeStartObject();
			jgen.writeObjectField(KEY_FIELD, value.getKey());
			jgen.writeObjectField(VAL_FIELD, value.getValue());
			jgen.writeEndObject();
		}
	}

	/**
	 * The Class PairsKeyDeserializerFinder.
	 *
	 * @author lathomps
	 */
	public static class PairsKeyDeserializerFinder implements KeyDeserializers {

		/*
		 * (non-Javadoc)
		 *
		 * @see com.fasterxml.jackson.databind.deser.KeyDeserializers#findKeyDeserializer(com.fasterxml.jackson.databind.JavaType, com.fasterxml.jackson.databind.DeserializationConfig,
		 * com.fasterxml.jackson.databind.BeanDescription)
		 */
		@Override
		public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
			if (Pair.class.isAssignableFrom(type.getRawClass())) {
				return new PairKeyDeserializer(type);
			}
			return null;
		}

	}

	/**
	 * The Class PairsKeySerializerFinder.
	 *
	 * @author lathomps
	 */
	public static class PairsKeySerializerFinder extends Serializers.Base {

		/*
		 * (non-Javadoc)
		 *
		 * @see com.fasterxml.jackson.databind.ser.Serializers.Base#findSerializer(com.fasterxml.jackson.databind.SerializationConfig, com.fasterxml.jackson.databind.JavaType,
		 * com.fasterxml.jackson.databind.BeanDescription)
		 */
		@Override
		public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
			if (Pair.class.isAssignableFrom(type.getRawClass())) {
				return new PairKeySerializer(config, type);
			}
			return null;
		}
	}

	/**
	 * The Class PairSserializerFinder.
	 *
	 * @author lathomps
	 */
	public static class PairsSerializerFinder extends Serializers.Base {

		/*
		 * (non-Javadoc)
		 *
		 * @see com.fasterxml.jackson.databind.ser.Serializers.Base#findSerializer(com.fasterxml.jackson.databind.SerializationConfig, com.fasterxml.jackson.databind.JavaType,
		 * com.fasterxml.jackson.databind.BeanDescription)
		 */
		@Override
		public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
			if (Pair.class.isAssignableFrom(type.getRawClass())) {
				return new PairSerializer(config);
			}
			return null;
		}
	}

}
