package com.github.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;

import com.github.lancethomps.lava.common.math.Numbers;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * The Class DoubleSerializer.
 */
public class DoubleSerializer extends JsonSerializer<Double> {

	/** The sig figs. */
	private final int sigFigs;

	/**
	 * Instantiates a new double serializer.
	 *
	 * @param sigFigs the sig figs
	 */
	public DoubleSerializer(int sigFigs) {
		this.sigFigs = sigFigs;
	}

	@Override
	public void serialize(Double val, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
		if (val != null) {
			jsonGenerator.writeNumber(Numbers.round(val, sigFigs));
		} else {
			jsonGenerator.writeNull();
		}
	}
}
