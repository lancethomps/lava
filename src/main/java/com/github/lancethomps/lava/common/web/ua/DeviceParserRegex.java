package com.github.lancethomps.lava.common.web.ua;

/**
 * The Class DeviceRegex.
 *
 * @author lancethomps
 */
public class DeviceParserRegex extends AbstractUserAgentParserRegex {

	/** The brand replacement. */
	private String brandReplacement;

	/** The device replacement. */
	private String deviceReplacement;

	/** The model replacement. */
	private String modelReplacement;

	/**
	 * @return the brandReplacement
	 */
	public String getBrandReplacement() {
		return brandReplacement;
	}

	/**
	 * @return the deviceReplacement
	 */
	public String getDeviceReplacement() {
		return deviceReplacement;
	}

	/**
	 * @return the modelReplacement
	 */
	public String getModelReplacement() {
		return modelReplacement;
	}

	/**
	 * @param brandReplacement the brandReplacement to set
	 */
	public void setBrandReplacement(String brandReplacement) {
		this.brandReplacement = brandReplacement;
	}

	/**
	 * @param deviceReplacement the deviceReplacement to set
	 */
	public void setDeviceReplacement(String deviceReplacement) {
		this.deviceReplacement = deviceReplacement;
	}

	/**
	 * @param modelReplacement the modelReplacement to set
	 */
	public void setModelReplacement(String modelReplacement) {
		this.modelReplacement = modelReplacement;
	}
}
