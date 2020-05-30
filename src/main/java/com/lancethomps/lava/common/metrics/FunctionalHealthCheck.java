package com.lancethomps.lava.common.metrics;

import static com.lancethomps.lava.common.metrics.StatusMonitor.getHealthCheckConfig;

import java.util.Map;

import com.lancethomps.lava.common.lambda.ThrowingFunction;

import io.dropwizard.metrics5.health.HealthCheck;

public class FunctionalHealthCheck extends HealthCheck {

  private final String name;
  private final ThrowingFunction<Map<String, Object>, Result> checker;

  public FunctionalHealthCheck(
      String name,
      ThrowingFunction<Map<String, Object>, Result> checker
  ) {
    this.name = name;
    this.checker = checker;
  }

  @Override
  protected Result check() throws Exception {
    return checker.apply(getHealthCheckConfig(name));
  }

}
