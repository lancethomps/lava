package com.lancethomps.lava.common.expr.spel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;

import com.lancethomps.lava.common.logging.Logs;

public class SandboxedSpelPropertyAccessor implements PropertyAccessor {

  private static final Logger LOG = LogManager.getLogger(SandboxedContextConfig.class);

  private SandboxedContextConfig config;

  private ReflectivePropertyAccessor delegate;

  public SandboxedSpelPropertyAccessor() {
    this(getDefaultConfig());
  }

  public SandboxedSpelPropertyAccessor(SandboxedContextConfig config) {
    super();
    this.config = config;
    delegate = new ReflectivePropertyAccessor();
  }

  public static SandboxedContextConfig getDefaultConfig() {
    return new SandboxedContextConfig()
      .addToBlackList("class")
      .addToTypesBlackList(System.class, Class.class)
      .addToSuperTypesBlackList(Runtime.class);
  }

  @Override
  public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
    checkAllowed(target, name);
    return delegate.canRead(context, target, name);
  }

  @Override
  public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
    checkAllowed(target, name);
    return delegate.canWrite(context, target, name);
  }

  public SandboxedContextConfig getConfig() {
    return config;
  }

  public void setConfig(SandboxedContextConfig config) {
    this.config = config;
    delegate = new ReflectivePropertyAccessor();
  }

  @Override
  public Class<?>[] getSpecificTargetClasses() {
    return delegate.getSpecificTargetClasses();
  }

  @Override
  public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
    checkAllowed(target, name);
    return delegate.read(context, target, name);
  }

  @Override
  public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
    checkAllowed(target, name);
    delegate.write(context, target, name, newValue);
  }

  private void checkAllowed(Object target, String name) throws AccessException {
    Logs.logTrace(
      LOG,
      "Finding property on type [%s] for name [%s]",
      target == null ? null : target instanceof Class ? (Class<?>) target : target.getClass(),
      name
    );
    if (config != null) {
      config.checkAllowed(target, name);
    }
  }

}
