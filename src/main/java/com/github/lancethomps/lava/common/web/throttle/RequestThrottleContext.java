package com.github.lancethomps.lava.common.web.throttle;

public interface RequestThrottleContext {

  String getResourceIdentifier();

  String getSenderIdentifier();

}
