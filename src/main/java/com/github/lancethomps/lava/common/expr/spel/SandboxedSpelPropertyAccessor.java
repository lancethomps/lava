package com.github.lancethomps.lava.common.expr.spel;

import org.apache.log4j.Logger;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;

import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class SandboxedSpelPropertyAccessor.
 */
public class SandboxedSpelPropertyAccessor implements PropertyAccessor {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(SandboxedContextConfig.class);

	/** The config. */
	private SandboxedContextConfig config;

	/** The delegate. */
	private ReflectivePropertyAccessor delegate;

	/**
	 * Instantiates a new sandboxed spel property accessor.
	 */
	public SandboxedSpelPropertyAccessor() {
		this(getDefaultConfig());
	}

	/**
	 * Instantiates a new sandboxed spel property accessor.
	 *
	 * @param config the config
	 */
	public SandboxedSpelPropertyAccessor(SandboxedContextConfig config) {
		super();
		this.config = config;
		delegate = new ReflectivePropertyAccessor();
	}

	/**
	 * Gets the default config.
	 *
	 * @return the default config
	 */
	public static SandboxedContextConfig getDefaultConfig() {
		return new SandboxedContextConfig()
			.addToBlackList("class")
			.addToTypesBlackList(System.class, Class.class)
			.addToSuperTypesBlackList(Runtime.class);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.expression.spel.support.ReflectivePropertyAccessor#canRead(org.springframework.expression.EvaluationContext, java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		checkAllowed(target, name);
		return delegate.canRead(context, target, name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.expression.spel.support.ReflectivePropertyAccessor#canWrite(org.springframework.expression.EvaluationContext, java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		checkAllowed(target, name);
		return delegate.canWrite(context, target, name);
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
	 * @see org.springframework.expression.spel.support.ReflectivePropertyAccessor#getSpecificTargetClasses()
	 */
	@Override
	public Class<?>[] getSpecificTargetClasses() {
		return delegate.getSpecificTargetClasses();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.expression.spel.support.ReflectivePropertyAccessor#read(org.springframework.expression.EvaluationContext, java.lang.Object, java.lang.String)
	 */
	@Override
	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		checkAllowed(target, name);
		return delegate.read(context, target, name);
	}

	/**
	 * Sets the config.
	 *
	 * @param config the config to set
	 */
	public void setConfig(SandboxedContextConfig config) {
		this.config = config;
		delegate = new ReflectivePropertyAccessor();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.expression.spel.support.ReflectivePropertyAccessor#write(org.springframework.expression.EvaluationContext, java.lang.Object, java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
		checkAllowed(target, name);
		delegate.write(context, target, name, newValue);
	}

	/**
	 * Check allowed.
	 *
	 * @param target the target
	 * @param name the name
	 * @throws AccessException the access exception
	 */
	private void checkAllowed(Object target, String name) throws AccessException {
		Logs.logTrace(LOG, "Finding property on type [%s] for name [%s]", target == null ? null : target instanceof Class ? (Class<?>) target : target.getClass(), name);
		if (config != null) {
			config.checkAllowed(target, name);
		}
	}

}
