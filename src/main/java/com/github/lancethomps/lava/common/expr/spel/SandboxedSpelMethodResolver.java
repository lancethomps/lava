package com.github.lancethomps.lava.common.expr.spel;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;

import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class SandboxedSpelMethodResolver.
 */
public class SandboxedSpelMethodResolver extends ReflectiveMethodResolver {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(SandboxedContextConfig.class);

	/** The config. */
	private SandboxedContextConfig config;

	/**
	 * Instantiates a new sandboxed spel method resolver.
	 */
	public SandboxedSpelMethodResolver() {
		this(true);
	}

	/**
	 * Instantiates a new sandboxed spel method resolver.
	 *
	 * @param useDistance the use distance
	 */
	public SandboxedSpelMethodResolver(boolean useDistance) {
		this(useDistance, getDefaultConfig());
	}

	/**
	 * Instantiates a new sandboxed spel method resolver.
	 *
	 * @param useDistance the use distance
	 * @param config the config
	 */
	public SandboxedSpelMethodResolver(boolean useDistance, SandboxedContextConfig config) {
		super(useDistance);
		this.config = config;
	}

	/**
	 * Gets the default config.
	 *
	 * @return the default config
	 */
	public static SandboxedContextConfig getDefaultConfig() {
		return new SandboxedContextConfig()
			.addToBlackList("getClass")
			.addToTypesBlackList(System.class)
			.addToSuperTypesBlackList(Runtime.class)
			.addConfigForType(Class.class, new SandboxedContextConfig()
				.addToWhiteList("getName", "getSimpleName"));
	}

	/**
	 * Gets the config.
	 *
	 * @return the config
	 */
	public SandboxedContextConfig getConfig() {
		return config;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.expression.spel.support.ReflectiveMethodResolver#resolve(org.springframework.expression.EvaluationContext, java.lang.Object, java.lang.String,
	 * java.util.List)
	 */
	@Override
	public MethodExecutor resolve(EvaluationContext context, Object targetObject, String name, List<TypeDescriptor> argumentTypes) throws AccessException {
		Logs.logTrace(LOG, "Finding method on type [%s] for name [%s]", targetObject == null ? null : targetObject instanceof Class ? (Class<?>) targetObject : targetObject.getClass(), name);
		if (config != null) {
			config.checkAllowed(targetObject, name);
		}
		return super.resolve(context, targetObject, name, argumentTypes);
	}

	/**
	 * Sets the config.
	 *
	 * @param config the config to set
	 */
	public void setConfig(SandboxedContextConfig config) {
		this.config = config;
	}

}
