package com.github.lancethomps.lava.common.web;

import java.util.List;

import javax.servlet.ServletException;

import org.owasp.esapi.errors.ValidationException;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.format.Formatting;

public class ValidationErrorsException extends ServletException {

  public static final String REQUEST_ATTRIBUTE = "_zzValidationErrorsException";

  private static final long serialVersionUID = 1L;

  private List<ValidationException> validationErrors;

  public ValidationErrorsException(List<ValidationException> validationErrors) {
    this(null, null, validationErrors);
  }

  public ValidationErrorsException(String message, Object... formatArgs) {
    this(Formatting.getMessage(message, formatArgs), (Throwable) null);
  }

  public ValidationErrorsException(String message, Throwable cause) {
    this(message, cause, null);
  }

  public ValidationErrorsException(String message, Throwable cause, List<ValidationException> validationErrors) {
    super(message, cause);
    this.validationErrors = validationErrors;
  }

  public ValidationErrorsException(Throwable cause) {
    this(null, cause);
  }

  @Override
  public String getMessage() {
    return super.getMessage() +
      (Checks.isEmpty(validationErrors) ? "" : validationErrors.stream().map(err -> err.getLogMessage()).reduce(String::concat).orElse(""));
  }

  public String getUserMessage() {
    return Checks.isEmpty(validationErrors) ? "" : validationErrors.stream().map(err -> err.getMessage()).reduce(String::concat).orElse("");
  }

  public List<ValidationException> getValidationErrors() {
    return validationErrors;
  }

  public void setValidationErrors(List<ValidationException> validationErrors) {
    this.validationErrors = validationErrors;
  }

}
