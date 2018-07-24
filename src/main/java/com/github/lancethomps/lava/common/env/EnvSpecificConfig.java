package com.github.lancethomps.lava.common.env;

import java.util.Set;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.properties.PropertyParser;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;

/**
 * The Class EnvSpecificConfig.
 *
 * @param <T> the generic type
 */
public class EnvSpecificConfig<T> extends ExternalizableBean {

	/** The envs. */
	private Set<String> envs;

	/** The value. */
	private T value;

	/**
	 * Check env configs match.
	 *
	 * @return true, if successful
	 */
	public boolean checkEnvConfigsMatch() {
		return Checks.isNotEmpty(envs) && PropertyParser.checkForEnabledEnvTag(envs);
	}

	/**
	 * Check is default.
	 *
	 * @return true, if successful
	 */
	public boolean checkIsDefault() {
		return Checks.isEmpty(envs) || envs.contains("*");
	}

	/**
	 * Check valid for current env.
	 *
	 * @return true, if successful
	 */
	public boolean checkValidForCurrentEnv() {
		return checkIsDefault() || checkEnvConfigsMatch();
	}

	/**
	 * Gets the envs.
	 *
	 * @return the envs
	 */
	public Set<String> getEnvs() {
		return envs;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Sets the envs.
	 *
	 * @param envs the envs to set
	 * @return the env specific config
	 */
	public EnvSpecificConfig<T> setEnvs(Set<String> envs) {
		this.envs = envs;
		return this;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the value to set
	 * @return the env specific config
	 */
	public EnvSpecificConfig<T> setValue(T value) {
		this.value = value;
		return this;
	}

}
