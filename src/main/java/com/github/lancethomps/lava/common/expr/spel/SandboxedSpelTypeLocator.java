package com.github.lancethomps.lava.common.expr.spel;

import org.apache.log4j.Logger;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.support.StandardTypeLocator;

import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class SandboxedSpelTypeLocator.
 */
public class SandboxedSpelTypeLocator extends StandardTypeLocator {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(SandboxedContextConfig.class);

	/** The config. */
	private SandboxedContextConfig config;

	/**
	 * Instantiates a new sandboxed spel type locator.
	 */
	public SandboxedSpelTypeLocator() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.expression.spel.support.StandardTypeLocator#findType(java.lang.String)
	 */
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

	/**
	 * @return the config
	 */
	public SandboxedContextConfig getConfig() {
		return config;
	}

	/**
	 * @param config the config to set
	 */
	public void setConfig(SandboxedContextConfig config) {
		this.config = config;
	}
}
