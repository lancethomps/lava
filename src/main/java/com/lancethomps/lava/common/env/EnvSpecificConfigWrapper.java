package com.lancethomps.lava.common.env;

import java.util.List;

import com.lancethomps.lava.common.merge.MergeConfig;
import com.lancethomps.lava.common.ser.ExternalizableBean;

public class EnvSpecificConfigWrapper<T> extends ExternalizableBean {

  private List<EnvSpecificConfig<T>> configs;

  private MergeConfig mergeConfig;

  public List<EnvSpecificConfig<T>> getConfigs() {
    return configs;
  }

  public EnvSpecificConfigWrapper<T> setConfigs(List<EnvSpecificConfig<T>> configs) {
    this.configs = configs;
    return this;
  }

  public MergeConfig getMergeConfig() {
    return mergeConfig;
  }

  public EnvSpecificConfigWrapper<T> setMergeConfig(MergeConfig mergeConfig) {
    this.mergeConfig = mergeConfig;
    return this;
  }

}
