package com.github.lancethomps.lava.common.web.requests;

import java.util.List;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.format.Formatting;

public class RequestDefaultsDisallowedParametersException extends Exception {

  private static final long serialVersionUID = 7015802387098897856L;

  private final String additionalMessage;

  private final List<String> parameters;

  public RequestDefaultsDisallowedParametersException() {
    this(null);
  }

  public RequestDefaultsDisallowedParametersException(List<String> parameters) {
    this(parameters, null);
  }

  public RequestDefaultsDisallowedParametersException(List<String> parameters, String additionalMessage, Object... formatArgs) {
    super(additionalMessage == null ? "" : Formatting.getMessage(additionalMessage, formatArgs));
    this.additionalMessage = Formatting.getMessage(additionalMessage, formatArgs);
    this.parameters = parameters;
  }

  public RequestDefaultsDisallowedParametersException(String additionalMessage, Object... formatArgs) {
    this(null, additionalMessage, formatArgs);
  }

  public String getAdditionalMessage() {
    return additionalMessage;
  }

  @Override
  public String getMessage() {
    return (Checks.isBlank(additionalMessage) ? "" : (additionalMessage + ' ')) + "Found disallowed parameters in request" +
      ((parameters == null) || parameters.isEmpty() ? '.'
        : (": " + parameters.toString()));
  }

  public List<String> getParameters() {
    return parameters;
  }

}
