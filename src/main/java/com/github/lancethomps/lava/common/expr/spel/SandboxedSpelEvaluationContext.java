package com.github.lancethomps.lava.common.expr.spel;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.github.lancethomps.lava.common.Checks;

/**
 * The Class SandboxedSpelEvaluationContext.
 */
public class SandboxedSpelEvaluationContext extends StandardEvaluationContext {

	/** The constructor resolver. */
	private final SandboxedSpelConstructorResolver constructorResolver;

	/** The method resolver. */
	private final SandboxedSpelMethodResolver methodResolver;

	/** The property accessor. */
	private final SandboxedSpelPropertyAccessor propertyAccessor;

	/** The type locator. */
	private final SandboxedSpelTypeLocator typeLocator;

	/**
	 * Instantiates a new sandboxed spel evaluation context.
	 */
	public SandboxedSpelEvaluationContext() {
		this(null);
	}

	/**
	 * Instantiates a new sandboxed spel evaluation context.
	 *
	 * @param rootObject the root object
	 */
	public SandboxedSpelEvaluationContext(Object rootObject) {
		this(rootObject, null, null, null, null);
	}

	/**
	 * Instantiates a new sandboxed spel evaluation context.
	 *
	 * @param rootObject the root object
	 * @param constructorResolver the constructor resolver
	 * @param methodResolver the method resolver
	 * @param propertyAccessor the property accessor
	 * @param typeLocator the type locator
	 */
	public SandboxedSpelEvaluationContext(Object rootObject, SandboxedSpelConstructorResolver constructorResolver, SandboxedSpelMethodResolver methodResolver,
		SandboxedSpelPropertyAccessor propertyAccessor, SandboxedSpelTypeLocator typeLocator) {
		super(rootObject);
		this.constructorResolver = Checks.defaultIfNull(constructorResolver, SandboxedSpelConstructorResolver::new);
		this.methodResolver = Checks.defaultIfNull(methodResolver, SandboxedSpelMethodResolver::new);
		this.propertyAccessor = Checks.defaultIfNull(propertyAccessor, SandboxedSpelPropertyAccessor::new);
		this.typeLocator = Checks.defaultIfNull(typeLocator, SandboxedSpelTypeLocator::new);
		init();
	}

	/**
	 * Instantiates a new sandboxed spel evaluation context.
	 *
	 * @param constructorResolver the constructor resolver
	 * @param methodResolver the method resolver
	 * @param propertyAccessor the property accessor
	 * @param typeLocator the type locator
	 */
	public SandboxedSpelEvaluationContext(SandboxedSpelConstructorResolver constructorResolver, SandboxedSpelMethodResolver methodResolver, SandboxedSpelPropertyAccessor propertyAccessor,
		SandboxedSpelTypeLocator typeLocator) {
		this(null, constructorResolver, methodResolver, propertyAccessor, typeLocator);
	}

	/**
	 * Gets the constructor resolver.
	 *
	 * @return the constructorResolver
	 */
	public SandboxedSpelConstructorResolver getConstructorResolver() {
		return constructorResolver;
	}

	/**
	 * Gets the method resolver.
	 *
	 * @return the methodResolver
	 */
	public SandboxedSpelMethodResolver getMethodResolver() {
		return methodResolver;
	}

	/**
	 * Gets the property accessor.
	 *
	 * @return the propertyAccessor
	 */
	public SandboxedSpelPropertyAccessor getPropertyAccessor() {
		return propertyAccessor;
	}

	/**
	 * Gets the type locator.
	 *
	 * @return the typeLocator
	 */
	@Override
	public SandboxedSpelTypeLocator getTypeLocator() {
		return typeLocator;
	}

	/**
	 * Inits the.
	 */
	public void init() {
		setConstructorResolvers(new ArrayList<>(Arrays.asList(constructorResolver)));
		setMethodResolvers(new ArrayList<>(Arrays.asList(methodResolver)));
		setPropertyAccessors(new ArrayList<>(Arrays.asList(propertyAccessor)));
		setTypeLocator(typeLocator);
	}

}
