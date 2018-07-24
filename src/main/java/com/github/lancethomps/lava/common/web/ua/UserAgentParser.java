package com.github.lancethomps.lava.common.web.ua;

import static com.github.lancethomps.lava.common.Checks.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.Patterns;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.web.WebRequests;

/**
 * The Class UserAgentParser.
 *
 * @author lathomps
 */
public class UserAgentParser {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(UserAgentParser.class);

	/** The Constant DEFAULT_VALUE. */
	private static final String DEFAULT_VALUE = "Other";

	/** The config. */
	private UserAgentParserConfig config;

	/**
	 * Instantiates a new user agent parser.
	 */
	public UserAgentParser() {
		this(
			Serializer.fromYaml(
				WebRequests.findUaRegexesYaml(),
				UserAgentParserConfig.class
			)
		);
	}

	/**
	 * Instantiates a new user agent parser.
	 *
	 * @param config the config
	 */
	public UserAgentParser(UserAgentParserConfig config) {
		super();
		this.config = config;
	}

	/**
	 * Gets the config.
	 *
	 * @return the config
	 */
	public UserAgentParserConfig getConfig() {
		return config;
	}

	/**
	 * Parses the.
	 *
	 * @param agentString the agent string
	 * @return the user agent
	 */
	public UserAgent parse(String agentString) {
		UserAgent ua = new UserAgent().setBrowser(DEFAULT_VALUE);
		try {
			for (UserAgentParserRegex parser : config.getUserAgentParsers()) {
				Matcher matcher = parser.getParsedRegex().matcher(agentString);

				if (matcher.find()) {
					ua.setBrowser(defaultIfBlank(Patterns.replace(matcher, defaultIfNull(parser.getFamilyReplacement(), "$1")), DEFAULT_VALUE));
					ua.setBrowserVersionMajor(StringUtils.trimToNull(Patterns.replace(matcher, Checks.defaultIfNull(parser.getV1Replacement(), "$2"))));
					ua.setBrowserVersionMinor(StringUtils.trimToNull(Patterns.replace(matcher, Checks.defaultIfNull(parser.getV2Replacement(), "$3"))));
					ua.setBrowserVersionPatch(StringUtils.trimToNull(Patterns.replace(matcher, Checks.defaultIfNull(parser.getV3Replacement(), "$4"))));
					break;
				}
			}

			for (OsParserRegex parser : config.getOsParsers()) {
				Matcher matcher = parser.getParsedRegex().matcher(agentString);

				if (matcher.find()) {
					ua.setOs(StringUtils.defaultIfBlank(Patterns.replace(matcher, Checks.defaultIfNull(parser.getOsReplacement(), "$1")), DEFAULT_VALUE));
					ua.setOsVersionMajor(StringUtils.trimToNull(Patterns.replace(matcher, Checks.defaultIfNull(parser.getOsV1Replacement(), "$2"))));
					ua.setOsVersionMinor(StringUtils.trimToNull(Patterns.replace(matcher, Checks.defaultIfNull(parser.getOsV2Replacement(), "$3"))));
					ua.setOsVersionPatch(StringUtils.trimToNull(Patterns.replace(matcher, Checks.defaultIfNull(parser.getOsV3Replacement(), "$4"))));
					ua.setOsVersionPatchMinor(StringUtils.trimToNull(Patterns.replace(matcher, Checks.defaultIfNull(parser.getOsV4Replacement(), "$5"))));
					break;
				}
			}

			for (DeviceParserRegex parser : config.getDeviceParsers()) {
				Matcher matcher = parser.getParsedRegex().matcher(agentString);

				if (matcher.find()) {
					ua.setDeviceFamily(StringUtils.defaultIfBlank(Patterns.replace(matcher, Checks.defaultIfNull(parser.getDeviceReplacement(), "$1")), DEFAULT_VALUE));
					if (parser.getBrandReplacement() != null) {
						ua.setDeviceBrand(StringUtils.trimToNull(Patterns.replace(matcher, parser.getBrandReplacement())));
					}
					ua.setDeviceModel(StringUtils.trimToNull(Patterns.replace(matcher, Checks.defaultIfNull(parser.getModelReplacement(), "$1"))));
					break;
				}
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue while parsing User-Agent [%s]", agentString);
		}
		return ua;
	}

	/**
	 * Sets the config.
	 *
	 * @param config the config to set
	 */
	public void setConfig(UserAgentParserConfig config) {
		this.config = config;
	}

}
