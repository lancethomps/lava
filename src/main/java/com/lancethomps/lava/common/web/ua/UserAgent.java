package com.lancethomps.lava.common.web.ua;

import static java.util.Optional.ofNullable;

import com.lancethomps.lava.common.ser.ExternalizableBean;
import com.lancethomps.lava.common.web.WebRequests;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.Version;
import ua_parser.Client;

public class UserAgent extends ExternalizableBean {

  private String browser;

  private String browserManufacturer;

  private String browserRenderingEngine;

  private String browserType;

  private String browserVersion;

  private String browserVersionMajor;

  private String browserVersionMinor;

  private String browserVersionPatch;

  private String deviceBrand;

  private String deviceFamily;

  private String deviceModel;

  private String os;

  private String osDeviceType;

  private String osManufacturer;

  private String osVersionMajor;

  private String osVersionMinor;

  private String osVersionPatch;

  private String osVersionPatchMinor;

  private String userAgent;

  public UserAgent() {
    super();
  }

  public static UserAgent fromUaParser(Client client) {
    return fromUaParser(client, null);
  }

  public static UserAgent fromUaParser(Client client, String userAgent) {
    UserAgent ua = new UserAgent();
    ua.setUserAgent(userAgent);
    return ua;
  }

  public static UserAgent fromUserAgentUtils(eu.bitwalker.useragentutils.UserAgent other) {
    return fromUserAgentUtils(other, null);
  }

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

  public String getBrowser() {
    return browser;
  }

  public UserAgent setBrowser(String browser) {
    this.browser = browser;
    return this;
  }

  public String getBrowserManufacturer() {
    return browserManufacturer;
  }

  public UserAgent setBrowserManufacturer(String browserManufacturer) {
    this.browserManufacturer = browserManufacturer;
    return this;
  }

  public String getBrowserRenderingEngine() {
    return browserRenderingEngine;
  }

  public UserAgent setBrowserRenderingEngine(String browserRenderingEngine) {
    this.browserRenderingEngine = browserRenderingEngine;
    return this;
  }

  public String getBrowserType() {
    return browserType;
  }

  public UserAgent setBrowserType(String browserType) {
    this.browserType = browserType;
    return this;
  }

  public String getBrowserVersion() {
    return browserVersion;
  }

  public UserAgent setBrowserVersion(String browserVersion) {
    this.browserVersion = browserVersion;
    return this;
  }

  public String getBrowserVersionMajor() {
    return browserVersionMajor;
  }

  public UserAgent setBrowserVersionMajor(String browserVersionMajor) {
    this.browserVersionMajor = browserVersionMajor;
    return this;
  }

  public String getBrowserVersionMinor() {
    return browserVersionMinor;
  }

  public UserAgent setBrowserVersionMinor(String browserVersionMinor) {
    this.browserVersionMinor = browserVersionMinor;
    return this;
  }

  public String getBrowserVersionPatch() {
    return browserVersionPatch;
  }

  public UserAgent setBrowserVersionPatch(String browserVersionPatch) {
    this.browserVersionPatch = browserVersionPatch;
    return this;
  }

  public String getDeviceBrand() {
    return deviceBrand;
  }

  public void setDeviceBrand(String deviceBrand) {
    this.deviceBrand = deviceBrand;
  }

  public String getDeviceFamily() {
    return deviceFamily;
  }

  public void setDeviceFamily(String deviceFamily) {
    this.deviceFamily = deviceFamily;
  }

  public String getDeviceModel() {
    return deviceModel;
  }

  public void setDeviceModel(String deviceModel) {
    this.deviceModel = deviceModel;
  }

  public String getOs() {
    return os;
  }

  public UserAgent setOs(String os) {
    this.os = os;
    return this;
  }

  public String getOsDeviceType() {
    return osDeviceType;
  }

  public UserAgent setOsDeviceType(String osDeviceType) {
    this.osDeviceType = osDeviceType;
    return this;
  }

  public String getOsManufacturer() {
    return osManufacturer;
  }

  public UserAgent setOsManufacturer(String osManufacturer) {
    this.osManufacturer = osManufacturer;
    return this;
  }

  public String getOsVersionMajor() {
    return osVersionMajor;
  }

  public void setOsVersionMajor(String osVersionMajor) {
    this.osVersionMajor = osVersionMajor;
  }

  public String getOsVersionMinor() {
    return osVersionMinor;
  }

  public void setOsVersionMinor(String osVersionMinor) {
    this.osVersionMinor = osVersionMinor;
  }

  public String getOsVersionPatch() {
    return osVersionPatch;
  }

  public void setOsVersionPatch(String osVersionPatch) {
    this.osVersionPatch = osVersionPatch;
  }

  public String getOsVersionPatchMinor() {
    return osVersionPatchMinor;
  }

  public void setOsVersionPatchMinor(String osVersionPatchMinor) {
    this.osVersionPatchMinor = osVersionPatchMinor;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public UserAgent setUserAgent(String userAgent) {
    this.userAgent = userAgent;
    return this;
  }

}
