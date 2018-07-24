package com.github.lancethomps.lava.common.expr.spel;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.ConstructorExecutor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.ReflectiveConstructorResolver;

import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class SandboxedSpelConstructorResolver.
 */
public class SandboxedSpelConstructorResolver extends ReflectiveConstructorResolver {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(SandboxedContextConfig.class);

	/** The config. */
	private SandboxedContextConfig config;

	/**
	 * Instantiates a new sandboxed spel constructor resolver.
	 */
	public SandboxedSpelConstructorResolver() {
		this(null);
	}

	/**
	 * Instantiates a new sandboxed spel constructor resolver.
	 *
	 * @param config the config
	 */
	public SandboxedSpelConstructorResolver(SandboxedContextConfig config) {
		super();
		this.config = config;
	}

	/**
	 * @return the config
	 */
	public SandboxedContextConfig getConfig() {
		return config;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.expression.spel.support.ReflectiveConstructorResolver#resolve(org.springframework.expression.EvaluationContext, java.lang.String, java.util.List)
	 */
	@Override
	public ConstructorExecutor resolve(EvaluationContext context, String typeName, List<TypeDescriptor> argumentTypes) throws AccessException {
		Class<?> type = context.getTypeLocator().findType(typeName);
		Logs.logTrace(LOG, "Finding constructor for type [%s] with arguments %s", type, argumentTypes);
		if (config != null) {
			config.checkAllowed(type, typeName);
		}
		return super.resolve(context, typeName, argumentTypes);
	}

	/**
	 * @param config the config to set
	 */
	public void setConfig(SandboxedContextConfig config) {
		this.config = config;
	}

}
