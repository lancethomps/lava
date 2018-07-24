package com.github.lancethomps.lava.common.ser.jackson.schema;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.github.lancethomps.lava.common.ser.Serializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

/**
 * The Class SchemaWithDefinitions.
 */
public class SchemaWithDefinitions {

	/** The definitions. */
	private Map<String, JsonSchema> definitions;

	/** The schema. */
	private ObjectSchema schema;

	/**
	 * Instantiates a new schema with definitions.
	 */
	public SchemaWithDefinitions() {
		super();
	}

	/**
	 * Instantiates a new schema with definitions.
	 *
	 * @param schema the schema
	 * @param definitions the definitions
	 */
	public SchemaWithDefinitions(ObjectSchema schema, Map<String, JsonSchema> definitions) {
		super();
		this.schema = schema;
		this.definitions = definitions;
	}

	/**
	 * Gets the definitions.
	 *
	 * @return the definitions
	 */
	public Map<String, JsonSchema> getDefinitions() {
		return definitions;
	}

	/**
	 * Gets the schema.
	 *
	 * @return the schema
	 */
	public ObjectSchema getSchema() {
		return schema;
	}

	/**
	 * Sets the definitions.
	 *
	 * @param definitions the definitions to set
	 */
	public void setDefinitions(Map<String, JsonSchema> definitions) {
		this.definitions = definitions;
	}

	/**
	 * Sets the schema.
	 *
	 * @param schema the schema to set
	 */
	public void setSchema(ObjectSchema schema) {
		this.schema = schema;
	}

	/**
	 * To properties.
	 *
	 * @return the map
	 */
	public Map<String, ObjectNode> toProperties() {
		return (schema == null) || (schema.getProperties() == null) ? null
			: schema.getProperties().entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> (ObjectNode) Serializer.toTree(e.getValue())));
	}
}
