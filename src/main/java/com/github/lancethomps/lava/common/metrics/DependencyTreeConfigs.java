package com.github.lancethomps.lava.common.metrics;

import java.util.Map;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;

/**
 * The Class DependecyTreeConfigs.
 */
public class DependencyTreeConfigs extends ExternalizableBean {

	/** The health check configs. */
	private Map<String, Map<String, Object>> healthCheckConfigs;

	/** The dependencies. */
	private Map<String, HierarchyTree> services;

	/**
	 * @return the healthCheckConfigs
	 */
	public Map<String, Map<String, Object>> getHealthCheckConfigs() {
		return healthCheckConfigs;
	}

	/**
	 * Gets the dependencies.
	 *
	 * @return the dependencies
	 */
	public Map<String, HierarchyTree> getServices() {
		return services;
	}

	/**
	 * @param healthCheckConfigs the healthCheckConfigs to set
	 */
	public void setHealthCheckConfigs(Map<String, Map<String, Object>> healthCheckConfigs) {
		this.healthCheckConfigs = healthCheckConfigs;
	}

	/**
	 * Sets the dependencies.
	 *
	 * @param services the dependencies
	 */
	public void setServices(Map<String, HierarchyTree> services) {
		this.services = services;
	}
}
