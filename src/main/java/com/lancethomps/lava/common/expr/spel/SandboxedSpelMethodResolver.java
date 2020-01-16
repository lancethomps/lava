package com.lancethomps.lava.common.expr.spel;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;

import com.lancethomps.lava.common.logging.Logs;

public class SandboxedSpelMethodResolver extends ReflectiveMethodResolver {

  private static final Logger LOG = LogManager.getLogger(SandboxedContextConfig.class);

  private SandboxedContextConfig config;

  public SandboxedSpelMethodResolver() {
    this(true);
  }

  public SandboxedSpelMethodResolver(boolean useDistance) {
    this(useDistance, getDefaultConfig());
  }

  public SandboxedSpelMethodResolver(boolean useDistance, SandboxedContextConfig config) {
    super(useDistance);
    this.config = config;
  }

  public static SandboxedContextConfig getDefaultConfig() {
    return new SandboxedContextConfig()
      .addToBlackList("getClass")
      .addToTypesBlackList(System.class)
      .addToSuperTypesBlackList(Runtime.class)
      .addConfigForType(Class.class, new SandboxedContextConfig()
        .addToWhiteList("getName", "getSimpleName"));
  }

  public SandboxedContextConfig getConfig() {
    return config;
  }

  public void setConfig(SandboxedContextConfig config) {
    this.config = config;
  }

  @Override
  public MethodExecutor resolve(EvaluationContext context, Object targetObject, String name, List<TypeDescriptor> argumentTypes)
    throws AccessException {
    Logs.logTrace(
      LOG,
      "Finding method on type [%s] for name [%s]",
      targetObject == null ? null : targetObject instanceof Class ? (Class<?>) targetObject : targetObject.getClass(),
      name
    );
    if (config != null) {
      config.checkAllowed(targetObject, name);
    }
    return super.resolve(context, targetObject, name, argumentTypes);
  }

}
