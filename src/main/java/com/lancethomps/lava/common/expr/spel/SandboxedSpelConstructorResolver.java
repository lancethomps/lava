package com.lancethomps.lava.common.expr.spel;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.ConstructorExecutor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.ReflectiveConstructorResolver;

import com.lancethomps.lava.common.logging.Logs;

public class SandboxedSpelConstructorResolver extends ReflectiveConstructorResolver {

  private static final Logger LOG = LogManager.getLogger(SandboxedContextConfig.class);

  private SandboxedContextConfig config;

  public SandboxedSpelConstructorResolver() {
    this(null);
  }

  public SandboxedSpelConstructorResolver(SandboxedContextConfig config) {
    super();
    this.config = config;
  }

  public SandboxedContextConfig getConfig() {
    return config;
  }

  public void setConfig(SandboxedContextConfig config) {
    this.config = config;
  }

  @Override
  public ConstructorExecutor resolve(EvaluationContext context, String typeName, List<TypeDescriptor> argumentTypes) throws AccessException {
    Class<?> type = context.getTypeLocator().findType(typeName);
    Logs.logTrace(LOG, "Finding constructor for type [%s] with arguments %s", type, argumentTypes);
    if (config != null) {
      config.checkAllowed(type, typeName);
    }
    return super.resolve(context, typeName, argumentTypes);
  }

}
