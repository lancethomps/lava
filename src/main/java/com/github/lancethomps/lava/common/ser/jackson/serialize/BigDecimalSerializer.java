package com.github.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * The Class BigDecimalSerializer.
 */
public final class BigDecimalSerializer extends StdScalarSerializer<BigDecimal> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6615409322856141058L;

	/** The sig figs. */
	private final int sigFigs;

	/**
	 * Instantiates a new big decimal serializer.
	 *
	 * @param sigFigs the sig figs
	 */
	public BigDecimalSerializer(int sigFigs) {
		super(BigDecimal.class);
		this.sigFigs = sigFigs;
	}

	@Override
	public void serialize(BigDecimal val, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
		BigDecimal finalVal = val == null ? val : (val.scale() > sigFigs) ? val.setScale(sigFigs, RoundingMode.HALF_UP).stripTrailingZeros() : val;
		jsonGenerator.writeNumber(finalVal);
	}
}
