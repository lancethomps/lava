package com.github.lancethomps.lava.common.expr.spel;

import org.apache.log4j.Logger;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.support.StandardTypeLocator;

import com.github.lancethomps.lava.common.logging.Logs;

public class SandboxedSpelTypeLocator extends StandardTypeLocator {

  private static final Logger LOG = Logger.getLogger(SandboxedContextConfig.class);

  private SandboxedContextConfig config;

  public SandboxedSpelTypeLocator() {
    super();
  }

  @Override
  public Class<?> findType(String typeName) throws EvaluationException {
    Logs.logTrace(LOG, "Finding type for name [%s]", typeName);
    Class<?> type = super.findType(typeName);
    if (config != null) {
      try {
        config.checkAllowed(type, typeName);
      } catch (AccessException e) {
        throw new EvaluationException("Access Error", e);
      }
    }
    return type;
  }

  public SandboxedContextConfig getConfig() {
    return config;
  }

  public void setConfig(SandboxedContextConfig config) {
    this.config = config;
  }

}
