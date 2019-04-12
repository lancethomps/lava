package com.github.lancethomps.lava.common.expr;

import com.github.lancethomps.lava.common.format.Formatting;
import com.github.lancethomps.lava.common.logging.Logs;

public class ExpressionEvalException extends Exception {

  private static final long serialVersionUID = 1L;

  private final ExprParser type;

  public ExpressionEvalException() {
    this(null);
  }

  public ExpressionEvalException(ExprParser type) {
    this(type, (String) null);
  }

  public ExpressionEvalException(ExprParser type, String message, Object... formatArgs) {
    this(type, (Throwable) null, Formatting.getMessage(message, formatArgs));
  }

  public ExpressionEvalException(ExprParser type, Throwable cause) {
    this(type, cause, null);
  }

  public ExpressionEvalException(ExprParser type, Throwable cause, String message, Object... formatArgs) {
    super(Formatting.getMessage(message, formatArgs), cause);
    this.type = type;
  }

  @Override
  public String getMessage() {
    final StringBuilder msg = new StringBuilder(String.format("Expression evaluation error: exprType=%s", type));
    if (super.getMessage() != null) {
      msg.append(' ').append(super.getMessage());
    }
    if (getCause() != null) {
      msg.append(" cause=").append(Logs.getSplunkValueString(getCause().getMessage()));
    }
    return msg.toString();
  }

  public ExprParser getType() {
    return type;
  }

}
