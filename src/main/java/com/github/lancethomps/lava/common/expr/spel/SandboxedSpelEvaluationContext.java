package com.github.lancethomps.lava.common.expr.spel;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.github.lancethomps.lava.common.Checks;

public class SandboxedSpelEvaluationContext extends StandardEvaluationContext {

  private final SandboxedSpelConstructorResolver constructorResolver;

  private final SandboxedSpelMethodResolver methodResolver;

  private final SandboxedSpelPropertyAccessor propertyAccessor;

  private final SandboxedSpelTypeLocator typeLocator;

  public SandboxedSpelEvaluationContext() {
    this(null);
  }

  public SandboxedSpelEvaluationContext(Object rootObject) {
    this(rootObject, null, null, null, null);
  }

  public SandboxedSpelEvaluationContext(
    Object rootObject, SandboxedSpelConstructorResolver constructorResolver, SandboxedSpelMethodResolver methodResolver,
    SandboxedSpelPropertyAccessor propertyAccessor, SandboxedSpelTypeLocator typeLocator
  ) {
    super(rootObject);
    this.constructorResolver = Checks.defaultIfNull(constructorResolver, SandboxedSpelConstructorResolver::new);
    this.methodResolver = Checks.defaultIfNull(methodResolver, SandboxedSpelMethodResolver::new);
    this.propertyAccessor = Checks.defaultIfNull(propertyAccessor, SandboxedSpelPropertyAccessor::new);
    this.typeLocator = Checks.defaultIfNull(typeLocator, SandboxedSpelTypeLocator::new);
    init();
  }

  public SandboxedSpelEvaluationContext(
    SandboxedSpelConstructorResolver constructorResolver, SandboxedSpelMethodResolver methodResolver, SandboxedSpelPropertyAccessor propertyAccessor,
    SandboxedSpelTypeLocator typeLocator
  ) {
    this(null, constructorResolver, methodResolver, propertyAccessor, typeLocator);
  }

  public SandboxedSpelConstructorResolver getConstructorResolver() {
    return constructorResolver;
  }

  public SandboxedSpelMethodResolver getMethodResolver() {
    return methodResolver;
  }

  public SandboxedSpelPropertyAccessor getPropertyAccessor() {
    return propertyAccessor;
  }

  @Override
  public SandboxedSpelTypeLocator getTypeLocator() {
    return typeLocator;
  }

  public void init() {
    setConstructorResolvers(new ArrayList<>(Arrays.asList(constructorResolver)));
    setMethodResolvers(new ArrayList<>(Arrays.asList(methodResolver)));
    setPropertyAccessors(new ArrayList<>(Arrays.asList(propertyAccessor)));
    setTypeLocator(typeLocator);
  }

}
