package com.github.lancethomps.lava.common.expr;

import java.util.List;

import com.github.lancethomps.lava.common.merge.MergeConfig;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.github.lancethomps.lava.common.ser.OutputExpression;

/**
 * The Class SavedPortfoliosExpressionMapping.
 *
 * @author amparikh
 * @param <T> the generic type
 */
public class ExpressionsDataMapping<T> extends ExternalizableBean {

	/** The break after match. */
	private Boolean breakAfterMatch;

	/** The expressions. */
	private List<OutputExpression> expressions;

	/** The id. */
	private String id;

	/** The tags. */
	private T mapping;

	/** The merge config. */
	private MergeConfig mergeConfig;

	/** The output expressions. */
	private List<OutputExpression> outputExpressions;

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.ser.PostConstructor#afterDeserialization()
	 */
	@Override
	public void afterDeserialization() {
		if (expressions != null) {
			ExprFactory.compileCreateExpressions(expressions, false, false);
		}
		if (outputExpressions != null) {
			ExprFactory.compileCreateExpressions(outputExpressions, false, false);
		}
	}

	/**
	 * Gets the break after match.
	 *
	 * @return the breakAfterMatch
	 */
	public Boolean getBreakAfterMatch() {
		return breakAfterMatch;
	}

	/**
	 * Gets the expressions.
	 *
	 * @return the expressions
	 */
	public List<OutputExpression> getExpressions() {
		return expressions;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the mapping.
	 *
	 * @return the mapping
	 */
	public T getMapping() {
		return mapping;
	}

	/**
	 * Gets the merge config.
	 *
	 * @return the mergeConfig
	 */
	public MergeConfig getMergeConfig() {
		return mergeConfig;
	}

	/**
	 * Gets the output expressions.
	 *
	 * @return the outputExpressions
	 */
	public List<OutputExpression> getOutputExpressions() {
		return outputExpressions;
	}

	/**
	 * Sets the break after match.
	 *
	 * @param breakAfterMatch the breakAfterMatch to set
	 * @return ExpressionsDataMapping the expressions data mapping
	 */
	public ExpressionsDataMapping<T> setBreakAfterMatch(Boolean breakAfterMatch) {
		this.breakAfterMatch = breakAfterMatch;
		return this;
	}

	/**
	 * Sets the expressions.
	 *
	 * @param expressions the expressions to set
	 * @return ExpressionsDataMapping the expressions data mapping
	 */
	public ExpressionsDataMapping<T> setExpressions(List<OutputExpression> expressions) {
		this.expressions = expressions;
		return this;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the id to set
	 * @return the expressions data mapping
	 */
	public ExpressionsDataMapping<T> setId(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Sets the mapping.
	 *
	 * @param mapping the mapping to set
	 * @return ExpressionsDataMapping the expressions data mapping
	 */
	public ExpressionsDataMapping<T> setMapping(T mapping) {
		this.mapping = mapping;
		return this;
	}

	/**
	 * Sets the merge config.
	 *
	 * @param mergeConfig the mergeConfig to set
	 * @return ExpressionsDataMapping the expressions data mapping
	 */
	public ExpressionsDataMapping<T> setMergeConfig(MergeConfig mergeConfig) {
		this.mergeConfig = mergeConfig;
		return this;
	}

	/**
	 * Sets the output expressions.
	 *
	 * @param outputExpressions the outputExpressions to set
	 * @return ExpressionsDataMapping the expressions data mapping
	 */
	public ExpressionsDataMapping<T> setOutputExpressions(List<OutputExpression> outputExpressions) {
		this.outputExpressions = outputExpressions;
		return this;
	}

	/**
	 * Test break after match.
	 *
	 * @return true, if successful
	 */
	public boolean testBreakAfterMatch() {
		return (breakAfterMatch != null) && breakAfterMatch.booleanValue();
	}

}
