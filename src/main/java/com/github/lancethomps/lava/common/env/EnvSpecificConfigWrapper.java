package com.github.lancethomps.lava.common.env;

import java.util.List;

import com.github.lancethomps.lava.common.merge.MergeConfig;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;

/**
 * The Class EnvSpecificConfigWrapper.
 *
 * @param <T> the generic type
 */
public class EnvSpecificConfigWrapper<T> extends ExternalizableBean {

	/** The configs. */
	private List<EnvSpecificConfig<T>> configs;

	/** The merge config. */
	private MergeConfig mergeConfig;

	/**
	 * @return the configs
	 */
	public List<EnvSpecificConfig<T>> getConfigs() {
		return configs;
	}

	/**
	 * @return the mergeConfig
	 */
	public MergeConfig getMergeConfig() {
		return mergeConfig;
	}

	/**
	 * Sets the configs.
	 *
	 * @param configs the configs to set
	 * @return the env specific config wrapper
	 */
	public EnvSpecificConfigWrapper<T> setConfigs(List<EnvSpecificConfig<T>> configs) {
		this.configs = configs;
		return this;
	}

	/**
	 * Sets the merge config.
	 *
	 * @param mergeConfig the mergeConfig to set
	 * @return the env specific config wrapper
	 */
	public EnvSpecificConfigWrapper<T> setMergeConfig(MergeConfig mergeConfig) {
		this.mergeConfig = mergeConfig;
		return this;
	}

}
