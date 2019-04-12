package com.github.lancethomps.lava.common.testing;

import com.github.lancethomps.lava.common.lambda.ThrowingFunction;

public class SpeedTestFunction {

  private String id;
  private ThrowingFunction<Integer, Long> function;

  public SpeedTestFunction() {
    super();
  }

  public SpeedTestFunction(String id, ThrowingFunction<Integer, Long> function) {
    super();
    this.id = id;
    this.function = function;
  }

  public SpeedTestFunction(ThrowingFunction<Integer, Long> function) {
    super();
    this.function = function;
  }

  public SpeedTestFunction(ThrowingFunction<Integer, Long> function, String id) {
    super();
    this.function = function;
    this.id = id;
  }

  public ThrowingFunction<Integer, Long> getFunction() {
    return function;
  }

  public SpeedTestFunction setFunction(ThrowingFunction<Integer, Long> function) {
    this.function = function;
    return this;
  }

  public String getId() {
    return id;
  }

  public SpeedTestFunction setId(String id) {
    this.id = id;
    return this;
  }

}
