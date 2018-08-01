package com.github.lancethomps.lava.common.web.ua;

import java.util.List;

import com.github.lancethomps.lava.common.lambda.Lambdas;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;

/**
 * The Class UserAgentParserConfig.
 *
 * @author lancethomps
 */
public class UserAgentParserConfig extends ExternalizableBean {

	/** The device parsers. */
	private List<DeviceParserRegex> deviceParsers;

	/** The os parsers. */
	private List<OsParserRegex> osParsers;

	/** The user agent parsers. */
	private List<UserAgentParserRegex> userAgentParsers;

	@Override
	public void afterDeserialization() {
		super.afterDeserialization();
		Lambdas.consumeIfNonNull(deviceParsers, parsers -> parsers.removeIf(parser -> parser.getParsedRegex() == null));
		Lambdas.consumeIfNonNull(osParsers, parsers -> parsers.removeIf(parser -> parser.getParsedRegex() == null));
		Lambdas.consumeIfNonNull(userAgentParsers, parsers -> parsers.removeIf(parser -> parser.getParsedRegex() == null));
	}

	/**
	 * @return the deviceParsers
	 */
	public List<DeviceParserRegex> getDeviceParsers() {
		return deviceParsers;
	}

	/**
	 * @return the osParsers
	 */
	public List<OsParserRegex> getOsParsers() {
		return osParsers;
	}

	/**
	 * @return the userAgentParsers
	 */
	public List<UserAgentParserRegex> getUserAgentParsers() {
		return userAgentParsers;
	}

	/**
	 * @param deviceParsers the deviceParsers to set
	 */
	public void setDeviceParsers(List<DeviceParserRegex> deviceParsers) {
		this.deviceParsers = deviceParsers;
	}

	/**
	 * @param osParsers the osParsers to set
	 */
	public void setOsParsers(List<OsParserRegex> osParsers) {
		this.osParsers = osParsers;
	}

	/**
	 * @param userAgentParsers the userAgentParsers to set
	 */
	public void setUserAgentParsers(List<UserAgentParserRegex> userAgentParsers) {
		this.userAgentParsers = userAgentParsers;
	}

}
