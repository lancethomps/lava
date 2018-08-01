package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

/**
 * The Class PatternDeserializer.
 *
 * @author lancethomps
 */
@SuppressWarnings("serial")
public class PatternDeserializer extends StdScalarDeserializer<Pattern> {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(PatternDeserializer.class);

	/**
	 * Instantiates a new pattern deserializer.
	 */
	public PatternDeserializer() {
		super(Pattern.class);
	}

	@Override
	public Pattern deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
		String regex = jsonParser.getValueAsString();
		if (regex != null) {
			try {
				return Pattern.compile(regex);
			} catch (Throwable e) {
				Logs.logWarn(LOG, e, "Pattern deserialization issue for [%s].", regex);
			}
		}
		return null;
	}
}
