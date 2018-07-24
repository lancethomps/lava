package com.github.lancethomps.lava.common.ser.jackson.deserialize;

import java.io.IOException;
import java.time.ZoneOffset;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.date.Dates;
import com.github.lancethomps.lava.common.logging.Logs;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

/**
 * The Class ZoneOffsetDeserializer.
 */
@SuppressWarnings("serial")
public class ZoneOffsetDeserializer extends StdScalarDeserializer<ZoneOffset> {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(ZoneOffsetDeserializer.class);

	/**
	 * Instantiates a new zone offset deserializer.
	 */
	public ZoneOffsetDeserializer() {
		super(ZoneOffset.class);
	}

	@Override
	public ZoneOffset deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
		String zone = jsonParser.getValueAsString();
		if (Checks.isNotBlank(zone)) {
			try {
				return Dates.parseZoneOffset(zone);
			} catch (Throwable e) {
				Logs.logWarn(LOG, e, "Error parsing time zone into ZoneOffset for value [%s]", zone);
			}
		}
		return null;
	}
}