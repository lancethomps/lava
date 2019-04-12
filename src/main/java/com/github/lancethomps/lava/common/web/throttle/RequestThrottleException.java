package com.github.lancethomps.lava.common.web.throttle;

import static java.lang.String.format;

import com.github.lancethomps.lava.common.format.Formatting;

public class RequestThrottleException extends Exception {

  private static final long serialVersionUID = -4598261915410167423L;

  private Integer maxRequests;

  private String user;

  public RequestThrottleException(String message) {
    super(message);
  }

  public RequestThrottleException(String user, Integer maxRequests) {
    super(format("Maximum number of requests - [%s] - for user [%s] reached!", maxRequests, user));
    this.user = user;
    this.maxRequests = maxRequests;
  }

  public RequestThrottleException(String message, Throwable cause) {
    super(message, cause);
  }

  public RequestThrottleException(Throwable cause) {
    super(cause);
  }

  public RequestThrottleException(Throwable cause, String message, Object... formatArgs) {
    super(Formatting.getMessage(message, formatArgs), cause);
  }

  public static void throwIfGreater(int openRequests, int maxRequests, String user) throws RequestThrottleException {
    if (openRequests > maxRequests) {
      throw new RequestThrottleException(user, maxRequests);
    }
  }

  public Integer getMaxRequests() {
    return maxRequests;
  }

  public RequestThrottleException setMaxRequests(Integer maxRequests) {
    this.maxRequests = maxRequests;
    return this;
  }

  public String getUser() {
    return user;
  }

  public RequestThrottleException setUser(String user) {
    this.user = user;
    return this;
  }

}
