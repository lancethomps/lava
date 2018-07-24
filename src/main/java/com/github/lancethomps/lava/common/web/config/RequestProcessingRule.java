package com.github.lancethomps.lava.common.web.config;

import java.util.List;
import java.util.regex.Pattern;

import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.ser.OutputExpression;

/**
 * The Class RequestProcessingRule.
 */
@SuppressWarnings("serial")
public class RequestProcessingRule extends SimpleDomainObject {

	/** The black list. */
	private List<Pattern> blackList;

	/** The config. */
	private RequestProcessingConfig config;

	/** The match expression. */
	private OutputExpression matchExpression;

	/** The white list. */
	private List<Pattern> whiteList;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.github.lancethomps.lava.common.ser.PostConstructor#afterDeserialization()
	 */
	@Override
	public void afterDeserialization() {
		if (matchExpression != null) {
			ExprFactory.compileCreateExpressions(false, false, true, matchExpression);
		}
	}

	/**
	 * Gets the black list.
	 *
	 * @return the blackList
	 */
	public List<Pattern> getBlackList() {
		return blackList;
	}

	/**
	 * Gets the config.
	 *
	 * @return the config
	 */
	public RequestProcessingConfig getConfig() {
		return config;
	}

	/**
	 * Gets the match expression.
	 *
	 * @return the expression
	 */
	public OutputExpression getMatchExpression() {
		return matchExpression;
	}

	/**
	 * Gets the white list.
	 *
	 * @return the whiteList
	 */
	public List<Pattern> getWhiteList() {
		return whiteList;
	}

	/**
	 * Sets the black list.
	 *
	 * @param blackList the blackList to set
	 * @return the request processing rule
	 */
	public RequestProcessingRule setBlackList(List<Pattern> blackList) {
		this.blackList = blackList;
		return this;
	}

	/**
	 * Sets the config.
	 *
	 * @param config the config to set
	 * @return the request processing rule
	 */
	public RequestProcessingRule setConfig(RequestProcessingConfig config) {
		this.config = config;
		return this;
	}

	/**
	 * Sets the expression.
	 *
	 * @param matchExpression the match expression
	 * @return the request processing rule
	 */
	public RequestProcessingRule setMatchExpression(OutputExpression matchExpression) {
		this.matchExpression = matchExpression;
		return this;
	}

	/**
	 * Sets the white list.
	 *
	 * @param whiteList the whiteList to set
	 * @return the request processing rule
	 */
	public RequestProcessingRule setWhiteList(List<Pattern> whiteList) {
		this.whiteList = whiteList;
		return this;
	}

}
