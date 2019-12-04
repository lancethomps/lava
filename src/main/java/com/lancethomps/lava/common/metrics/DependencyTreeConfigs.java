package com.lancethomps.lava.common.metrics;

import java.util.Map;

import com.lancethomps.lava.common.ser.ExternalizableBean;

public class DependencyTreeConfigs extends ExternalizableBean {

  private Map<String, Map<String, Object>> healthCheckConfigs;

  private Map<String, HierarchyTree> services;

  public Map<String, Map<String, Object>> getHealthCheckConfigs() {
    return healthCheckConfigs;
  }

  public void setHealthCheckConfigs(Map<String, Map<String, Object>> healthCheckConfigs) {
    this.healthCheckConfigs = healthCheckConfigs;
  }

  public Map<String, HierarchyTree> getServices() {
    return services;
  }

  public void setServices(Map<String, HierarchyTree> services) {
    this.services = services;
  }

}
