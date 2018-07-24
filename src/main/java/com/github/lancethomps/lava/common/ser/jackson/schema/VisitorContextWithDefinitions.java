package com.github.lancethomps.lava.common.ser.jackson.schema;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;

/**
 * The Class VisitorContextWithDefinitions.
 */
public class VisitorContextWithDefinitions extends VisitorContext {

	/** The definitions. */
	private final Map<String, JsonSchema> definitions = new LinkedHashMap<>();

	/** The seen schemas. */
	private final HashSet<JavaType> seenSchemas = new HashSet<JavaType>();

	/**
	 * Adds the definition.
	 *
	 * @param id the id
	 * @param schema the schema
	 */
	public void addDefinition(String id, JsonSchema schema) {
		definitions.put(id, schema);
	}

	@Override
	public String addSeenSchemaUri(JavaType aSeenSchema) {
		if ((aSeenSchema != null) && !aSeenSchema.isPrimitive()) {
			seenSchemas.add(aSeenSchema);
			return javaTypeToUrn(aSeenSchema);
		}
		return null;
	}

	/**
	 * @return the definitions
	 */
	public Map<String, JsonSchema> getDefinitions() {
		return definitions;
	}

	@Override
	public String getSeenSchemaUri(JavaType aSeenSchema) {
		return (seenSchemas.contains(aSeenSchema)) ? javaTypeToUrn(aSeenSchema) : null;
	}

	@Override
	public String javaTypeToUrn(JavaType jt) {
		return "java:" + jt.toCanonical();
	}
}
