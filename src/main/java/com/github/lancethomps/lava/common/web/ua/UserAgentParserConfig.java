package com.github.lancethomps.lava.common.web.ua;

import java.util.List;

import com.github.lancethomps.lava.common.lambda.Lambdas;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;

public class UserAgentParserConfig extends ExternalizableBean {

  private List<DeviceParserRegex> deviceParsers;

  private List<OsParserRegex> osParsers;

  private List<UserAgentParserRegex> userAgentParsers;

  @Override
  public void afterDeserialization() {
    super.afterDeserialization();
    Lambdas.consumeIfNonNull(deviceParsers, parsers -> parsers.removeIf(parser -> parser.getParsedRegex() == null));
    Lambdas.consumeIfNonNull(osParsers, parsers -> parsers.removeIf(parser -> parser.getParsedRegex() == null));
    Lambdas.consumeIfNonNull(userAgentParsers, parsers -> parsers.removeIf(parser -> parser.getParsedRegex() == null));
  }

  public List<DeviceParserRegex> getDeviceParsers() {
    return deviceParsers;
  }

  public void setDeviceParsers(List<DeviceParserRegex> deviceParsers) {
    this.deviceParsers = deviceParsers;
  }

  public List<OsParserRegex> getOsParsers() {
    return osParsers;
  }

  public void setOsParsers(List<OsParserRegex> osParsers) {
    this.osParsers = osParsers;
  }

  public List<UserAgentParserRegex> getUserAgentParsers() {
    return userAgentParsers;
  }

  public void setUserAgentParsers(List<UserAgentParserRegex> userAgentParsers) {
    this.userAgentParsers = userAgentParsers;
  }

}
