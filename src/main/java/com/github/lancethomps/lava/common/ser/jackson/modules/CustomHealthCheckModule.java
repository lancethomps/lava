package com.github.lancethomps.lava.common.ser.jackson.modules;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import io.dropwizard.metrics5.health.HealthCheck;

/**
 * The Class CustomHealthCheckModule.
 */
public class CustomHealthCheckModule extends Module {

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.Module#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return "healthchecks";
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.Module#setupModule(com.fasterxml.jackson.databind.Module.
	 * SetupContext)
	 */
	@Override
	public void setupModule(SetupContext context) {
		context.addSerializers(
			new SimpleSerializers(
				Arrays.<JsonSerializer<?>> asList(
					new HealthCheckResultSerializer()
				)
			)
		);
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.Module#version()
	 */
	@Override
	public Version version() {
		return new Version(5, 0, 0, "", "io.dropwizard.metrics5", "metrics-json");
	}

	/**
	 * The Class HealthCheckResultSerializer.
	 */
	@SuppressWarnings("serial")
	private static final class HealthCheckResultSerializer extends StdSerializer<HealthCheck.Result> {

		/**
		 * Instantiates a new health check result serializer.
		 */
		private HealthCheckResultSerializer() {
			super(HealthCheck.Result.class);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object,
		 * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		public void serialize(
			HealthCheck.Result result,
			JsonGenerator json,
			SerializerProvider provider
		) throws IOException {
			json.writeStartObject();
			json.writeBooleanField("healthy", result.isHealthy());

			final String message = result.getMessage();
			if (message != null) {
				json.writeStringField("message", message);
			}
			final String timestamp = result.getTimestamp();
			if (timestamp != null) {
				json.writeStringField("timestamp", timestamp);
			}

			serializeThrowable(json, result.getError(), "error");

			Map<String, Object> details = result.getDetails();
			if ((details != null) && !details.isEmpty()) {
				json.writeObjectFieldStart("details");
				Iterator<Map.Entry<String, Object>> it = details.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, Object> e = it.next();
					json.writeObjectField(e.getKey(), e.getValue());
				}
				json.writeEndObject();
			}

			json.writeEndObject();
		}

		/**
		 * Serialize throwable.
		 *
		 * @param json the json
		 * @param error the error
		 * @param name the name
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		private void serializeThrowable(JsonGenerator json, Throwable error, String name) throws IOException {
			if (error != null) {
				json.writeObjectFieldStart(name);
				json.writeStringField("message", error.getMessage());
				json.writeArrayFieldStart("stack");
				for (StackTraceElement element : error.getStackTrace()) {
					json.writeString(element.toString());
				}
				json.writeEndArray();

				if (error.getCause() != null) {
					serializeThrowable(json, error.getCause(), "cause");
				}

				json.writeEndObject();
			}
		}
	}
}
