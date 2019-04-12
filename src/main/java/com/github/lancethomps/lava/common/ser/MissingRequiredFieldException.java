package com.github.lancethomps.lava.common.ser;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.format.Formatting;

public class MissingRequiredFieldException extends Exception {

  private static final long serialVersionUID = 1L;

  private final String fieldName;

  public MissingRequiredFieldException() {
    this(null);
  }

  public MissingRequiredFieldException(String fieldName) {
    this(null, fieldName);
  }

  public MissingRequiredFieldException(Throwable cause, String fieldName) {
    this(cause, fieldName, null);
  }

  public MissingRequiredFieldException(Throwable cause, String fieldName, String message, Object... formatArgs) {
    super(Formatting.getMessage(message, formatArgs), cause);
    this.fieldName = fieldName;
  }

  @Override
  public String getMessage() {
    return (fieldName == null ? "" : String.format("The `%s` field is required.", fieldName)) + Checks.defaultIfNull(super.getMessage(), "");
  }

}
