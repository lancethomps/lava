package com.github.lancethomps.lava.common.web.ua;

import static java.util.Optional.ofNullable;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.github.lancethomps.lava.common.web.WebRequests;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.Version;
import ua_parser.Client;

/**
 * The Class UserAgent.
 *
 * @author lathomps
 */
public class UserAgent extends ExternalizableBean {

	/** The browser. */
	private String browser;

	/** The browser manufacturer. */
	private String browserManufacturer;

	/** The browser rendering engine. */
	private String browserRenderingEngine;

	/** The browser type. */
	private String browserType;

	/** The browser version. */
	private String browserVersion;

	/** The browser version major. */
	private String browserVersionMajor;

	/** The browser version minor. */
	private String browserVersionMinor;

	/** The browser version patch. */
	private String browserVersionPatch;

	/** The device brand. */
	private String deviceBrand;

	/** The device family. */
	private String deviceFamily;

	/** The device model. */
	private String deviceModel;

	/** The os. */
	private String os;

	/** The os device type. */
	private String osDeviceType;

	/** The os manufacturer. */
	private String osManufacturer;

	/** The os version major. */
	private String osVersionMajor;

	/** The os version minor. */
	private String osVersionMinor;

	/** The os version patch. */
	private String osVersionPatch;

	/** The os version patch minor. */
	private String osVersionPatchMinor;

	/** The user agent string. */
	private String userAgent;

	/**
	 * Instantiates a new user agent.
	 */
	public UserAgent() {
		super();
	}

	/**
	 * From ua parser.
	 *
	 * @param client the client
	 * @return the user agent
	 */
	public static UserAgent fromUaParser(Client client) {
		return fromUaParser(client, null);
	}

	/**
	 * From ua parser.
	 *
	 * @param client the client
	 * @param userAgent the user agent
	 * @return the user agent
	 */
	public static UserAgent fromUaParser(Client client, String userAgent) {
		UserAgent ua = new UserAgent();
		ua.setUserAgent(userAgent);
		return ua;
	}

	/**
	 * From user agent utils.
	 *
	 * @param other the other
	 * @return the user agent
	 */
	public static UserAgent fromUserAgentUtils(eu.bitwalker.useragentutils.UserAgent other) {
		return fromUserAgentUtils(other, null);
	}

	/**
	 * From user agent utils.
	 *
	 * @param other the other
	 * @param userAgent the user agent
	 * @return the user agent
	 */
	public static UserAgent fromUserAgentUtils(eu.bitwalker.useragentutils.UserAgent other, String userAgent) {
		UserAgent ua = new UserAgent();
		ua.setUserAgent(userAgent != null ? userAgent : WebRequests.getUserAgentString(other));
		Browser browser = other.getBrowser();
		if (browser != null) {
			ua.setBrowser(browser.name())
				.setBrowserManufacturer(ofNullable(browser.getManufacturer()).map(Enum::name).orElse(null))
				.setBrowserRenderingEngine(ofNullable(browser.getRenderingEngine()).map(Enum::name).orElse(null))
				.setBrowserType(ofNullable(browser.getBrowserType()).map(Enum::name).orElse(null));
		}
		Version vers = other.getBrowserVersion();
		if (vers != null) {
			ua.setBrowserVersion(vers.toString())
				.setBrowserVersionMajor(vers.getMajorVersion())
				.setBrowserVersionMinor(vers.getMinorVersion());
		}
		OperatingSystem os = other.getOperatingSystem();
		if (os != null) {
			ua.setOs(os.name())
				.setOsDeviceType(ofNullable(os.getDeviceType()).map(Enum::name).orElse(null))
				.setOsManufacturer(ofNullable(os.getManufacturer()).map(Enum::name).orElse(null));
		}
		return ua;
	}

	/**
	 * Gets the browser.
	 *
	 * @return the browser
	 */
	public String getBrowser() {
		return browser;
	}

	/**
	 * Gets the browser manufacturer.
	 *
	 * @return the browserManufacturer
	 */
	public String getBrowserManufacturer() {
		return browserManufacturer;
	}

	/**
	 * Gets the browser rendering engine.
	 *
	 * @return the browserRenderingEngine
	 */
	public String getBrowserRenderingEngine() {
		return browserRenderingEngine;
	}

	/**
	 * Gets the browser type.
	 *
	 * @return the browserType
	 */
	public String getBrowserType() {
		return browserType;
	}

	/**
	 * Gets the browser version.
	 *
	 * @return the browserVersion
	 */
	public String getBrowserVersion() {
		return browserVersion;
	}

	/**
	 * Gets the browser version major.
	 *
	 * @return the browserVersionMajor
	 */
	public String getBrowserVersionMajor() {
		return browserVersionMajor;
	}

	/**
	 * Gets the browser version minor.
	 *
	 * @return the browserVersionMinor
	 */
	public String getBrowserVersionMinor() {
		return browserVersionMinor;
	}

	/**
	 * @return the browserVersionPatch
	 */
	public String getBrowserVersionPatch() {
		return browserVersionPatch;
	}

	/**
	 * @return the deviceBrand
	 */
	public String getDeviceBrand() {
		return deviceBrand;
	}

	/**
	 * @return the deviceFamily
	 */
	public String getDeviceFamily() {
		return deviceFamily;
	}

	/**
	 * @return the deviceModel
	 */
	public String getDeviceModel() {
		return deviceModel;
	}

	/**
	 * Gets the os.
	 *
	 * @return the os
	 */
	public String getOs() {
		return os;
	}

	/**
	 * Gets the os device type.
	 *
	 * @return the osDeviceType
	 */
	public String getOsDeviceType() {
		return osDeviceType;
	}

	/**
	 * Gets the os manufacturer.
	 *
	 * @return the osManufacturer
	 */
	public String getOsManufacturer() {
		return osManufacturer;
	}

	/**
	 * @return the osVersionMajor
	 */
	public String getOsVersionMajor() {
		return osVersionMajor;
	}

	/**
	 * @return the osVersionMinor
	 */
	public String getOsVersionMinor() {
		return osVersionMinor;
	}

	/**
	 * @return the osVersionPatch
	 */
	public String getOsVersionPatch() {
		return osVersionPatch;
	}

	/**
	 * @return the osVersionPatchMinor
	 */
	public String getOsVersionPatchMinor() {
		return osVersionPatchMinor;
	}

	/**
	 * Gets the user agent string.
	 *
	 * @return the userAgentString
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * Sets the browser.
	 *
	 * @param browser the browser to set
	 * @return the user agent
	 */
	public UserAgent setBrowser(String browser) {
		this.browser = browser;
		return this;
	}

	/**
	 * Sets the browser manufacturer.
	 *
	 * @param browserManufacturer the browserManufacturer to set
	 * @return the user agent
	 */
	public UserAgent setBrowserManufacturer(String browserManufacturer) {
		this.browserManufacturer = browserManufacturer;
		return this;
	}

	/**
	 * Sets the browser rendering engine.
	 *
	 * @param browserRenderingEngine the browserRenderingEngine to set
	 * @return the user agent
	 */
	public UserAgent setBrowserRenderingEngine(String browserRenderingEngine) {
		this.browserRenderingEngine = browserRenderingEngine;
		return this;
	}

	/**
	 * Sets the browser type.
	 *
	 * @param browserType the browserType to set
	 * @return the user agent
	 */
	public UserAgent setBrowserType(String browserType) {
		this.browserType = browserType;
		return this;
	}

	/**
	 * Sets the browser version.
	 *
	 * @param browserVersion the browserVersion to set
	 * @return the user agent
	 */
	public UserAgent setBrowserVersion(String browserVersion) {
		this.browserVersion = browserVersion;
		return this;
	}

	/**
	 * Sets the browser version major.
	 *
	 * @param browserVersionMajor the browserVersionMajor to set
	 * @return the user agent
	 */
	public UserAgent setBrowserVersionMajor(String browserVersionMajor) {
		this.browserVersionMajor = browserVersionMajor;
		return this;
	}

	/**
	 * Sets the browser version minor.
	 *
	 * @param browserVersionMinor the browserVersionMinor to set
	 * @return the user agent
	 */
	public UserAgent setBrowserVersionMinor(String browserVersionMinor) {
		this.browserVersionMinor = browserVersionMinor;
		return this;
	}

	/**
	 * Sets the browser version patch.
	 *
	 * @param browserVersionPatch the browserVersionPatch to set
	 * @return the user agent
	 */
	public UserAgent setBrowserVersionPatch(String browserVersionPatch) {
		this.browserVersionPatch = browserVersionPatch;
		return this;
	}

	/**
	 * @param deviceBrand the deviceBrand to set
	 */
	public void setDeviceBrand(String deviceBrand) {
		this.deviceBrand = deviceBrand;
	}

	/**
	 * @param deviceFamily the deviceFamily to set
	 */
	public void setDeviceFamily(String deviceFamily) {
		this.deviceFamily = deviceFamily;
	}

	/**
	 * @param deviceModel the deviceModel to set
	 */
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	/**
	 * Sets the os.
	 *
	 * @param os the os to set
	 * @return the user agent
	 */
	public UserAgent setOs(String os) {
		this.os = os;
		return this;
	}

	/**
	 * Sets the os device type.
	 *
	 * @param osDeviceType the osDeviceType to set
	 * @return the user agent
	 */
	public UserAgent setOsDeviceType(String osDeviceType) {
		this.osDeviceType = osDeviceType;
		return this;
	}

	/**
	 * Sets the os manufacturer.
	 *
	 * @param osManufacturer the osManufacturer to set
	 * @return the user agent
	 */
	public UserAgent setOsManufacturer(String osManufacturer) {
		this.osManufacturer = osManufacturer;
		return this;
	}

	/**
	 * @param osVersionMajor the osVersionMajor to set
	 */
	public void setOsVersionMajor(String osVersionMajor) {
		this.osVersionMajor = osVersionMajor;
	}

	/**
	 * @param osVersionMinor the osVersionMinor to set
	 */
	public void setOsVersionMinor(String osVersionMinor) {
		this.osVersionMinor = osVersionMinor;
	}

	/**
	 * @param osVersionPatch the osVersionPatch to set
	 */
	public void setOsVersionPatch(String osVersionPatch) {
		this.osVersionPatch = osVersionPatch;
	}

	/**
	 * @param osVersionPatchMinor the osVersionPatchMinor to set
	 */
	public void setOsVersionPatchMinor(String osVersionPatchMinor) {
		this.osVersionPatchMinor = osVersionPatchMinor;
	}

	/**
	 * Sets the user agent string.
	 *
	 * @param userAgent the user agent
	 * @return the user agent
	 */
	public UserAgent setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		return this;
	}

}
