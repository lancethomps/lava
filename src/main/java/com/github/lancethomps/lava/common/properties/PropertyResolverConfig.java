package com.github.lancethomps.lava.common.properties;

import java.util.Map;

import com.github.lancethomps.lava.common.web.WebRequestContext;

/**
 * The Class PropertyResolverConfig.
 */
public class PropertyResolverConfig {

	/** The context. */
	private WebRequestContext context;

	/** The custom params. */
	private Map<String, Object> customParams;

	/** The helper. */
	private PropertyParserHelper helper;

	/** The recursively. */
	private boolean recursively = true;

	/** The root dir. */
	private String rootDir;

	/**
	 * Copy.
	 *
	 * @return the property resolver config
	 */
	public PropertyResolverConfig copy() {
		return copyWithNewRootDir(rootDir);
	}

	/**
	 * Copy with new root dir.
	 *
	 * @param rootDir the root dir
	 * @return the property resolver config
	 */
	public PropertyResolverConfig copyWithNewRootDir(String rootDir) {
		return new PropertyResolverConfig().setContext(context).setHelper(helper).setRecursively(recursively).setRootDir(rootDir);
	}

	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	public WebRequestContext getContext() {
		return context;
	}

	/**
	 * @return the customParams
	 */
	public Map<String, Object> getCustomParams() {
		return customParams;
	}

	/**
	 * Gets the helper.
	 *
	 * @return the helper
	 */
	public PropertyParserHelper getHelper() {
		return helper;
	}

	/**
	 * Gets the root dir.
	 *
	 * @return the rootDir
	 */
	public String getRootDir() {
		return rootDir;
	}

	/**
	 * Checks if is recursively.
	 *
	 * @return the recursively
	 */
	public boolean isRecursively() {
		return recursively;
	}

	/**
	 * Sets the context.
	 *
	 * @param context the context to set
	 * @return the property resolver config
	 */
	public PropertyResolverConfig setContext(WebRequestContext context) {
		this.context = context;
		return this;
	}

	/**
	 * Sets the custom params.
	 *
	 * @param customParams the customParams to set
	 * @return the property resolver config
	 */
	public PropertyResolverConfig setCustomParams(Map<String, Object> customParams) {
		this.customParams = customParams;
		return this;
	}

	/**
	 * Sets the helper.
	 *
	 * @param helper the helper to set
	 * @return the property resolver config
	 */
	public PropertyResolverConfig setHelper(PropertyParserHelper helper) {
		this.helper = helper;
		return this;
	}

	/**
	 * Sets the recursively.
	 *
	 * @param recursively the recursively to set
	 * @return the property resolver config
	 */
	public PropertyResolverConfig setRecursively(boolean recursively) {
		this.recursively = recursively;
		return this;
	}

	/**
	 * Sets the root dir.
	 *
	 * @param rootDir the rootDir to set
	 * @return the property resolver config
	 */
	public PropertyResolverConfig setRootDir(String rootDir) {
		this.rootDir = rootDir;
		return this;
	}

}
