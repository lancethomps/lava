package com.lancethomps.lava.common.env;

import java.util.Set;

import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.properties.PropertyParser;
import com.lancethomps.lava.common.ser.ExternalizableBean;

public class EnvSpecificConfig<T> extends ExternalizableBean {

  private Set<String> envs;

  private T value;

  public boolean checkEnvConfigsMatch() {
    return Checks.isNotEmpty(envs) && PropertyParser.checkForEnabledEnvTag(envs);
  }

  public boolean checkIsDefault() {
    return Checks.isEmpty(envs) || envs.contains("*");
  }

  public boolean checkValidForCurrentEnv() {
    return checkIsDefault() || checkEnvConfigsMatch();
  }

  public Set<String> getEnvs() {
    return envs;
  }

  public EnvSpecificConfig<T> setEnvs(Set<String> envs) {
    this.envs = envs;
    return this;
  }

  public T getValue() {
    return value;
  }

  public EnvSpecificConfig<T> setValue(T value) {
    this.value = value;
    return this;
  }

}
