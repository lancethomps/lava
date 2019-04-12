package com.github.lancethomps.lava.common.os;

import com.github.lancethomps.lava.common.SimpleDomainObject;

public class ProcessResult extends SimpleDomainObject {

  private final String errorOutput;

  private final Exception exception;

  private final Integer exitValue;

  private final String output;

  private final Long timeTaken;

  public ProcessResult(String errorOutput, Exception exception, Integer exitValue, String output, Long timeTaken) {
    super();
    this.errorOutput = errorOutput;
    this.exception = exception;
    this.exitValue = exitValue;
    this.output = output;
    this.timeTaken = timeTaken;
  }

  public String getErrorOutput() {
    return errorOutput;
  }

  public Exception getException() {
    return exception;
  }

  public Integer getExitValue() {
    return exitValue;
  }

  public String getOutput() {
    return output;
  }

  public Long getTimeTaken() {
    return timeTaken;
  }

  public boolean wasSuccessful() {
    return (exitValue != null) && (exitValue.intValue() == 0);
  }

}
