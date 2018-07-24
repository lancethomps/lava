package com.github.lancethomps.lava.common.expr;

import java.util.ArrayList;
import java.util.List;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.github.lancethomps.lava.common.ser.OutputExpression;

/**
 * The Class ExpressionsMatchResult.
 */
public class ExpressionsMatchResult extends SimpleDomainObject {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The errors. */
	private List<String> errors;

	/** The matched. */
	private Boolean matched;

	/** The matched expression. */
	private OutputExpression matchedExpression;

	/**
	 * Adds the error.
	 *
	 * @param error the error
	 * @return the expressions match result
	 */
	public ExpressionsMatchResult addError(String error) {
		if (error != null) {
			if (errors == null) {
				errors = new ArrayList<>();
			}
			errors.add(error);
		}
		return this;
	}

	/**
	 * @return the errors
	 */
	public List<String> getErrors() {
		return errors;
	}

	/**
	 * Gets the matched.
	 *
	 * @return the matched
	 */
	public Boolean getMatched() {
		return matched;
	}

	/**
	 * Gets the matched expression.
	 *
	 * @return the matchedExpression
	 */
	public OutputExpression getMatchedExpression() {
		return matchedExpression;
	}

	/**
	 * Checks for errors.
	 *
	 * @return true, if successful
	 */
	public boolean hasErrors() {
		return Checks.isNotEmpty(errors);
	}

	/**
	 * Sets the errors.
	 *
	 * @param errors the errors to set
	 * @return the expressions match result
	 */
	public ExpressionsMatchResult setErrors(List<String> errors) {
		this.errors = errors;
		return this;
	}

	/**
	 * Sets the matched.
	 *
	 * @param matched the matched to set
	 * @return the expressions match result
	 */
	public ExpressionsMatchResult setMatched(Boolean matched) {
		this.matched = matched;
		return this;
	}

	/**
	 * Sets the matched expression.
	 *
	 * @param matchedExpression the matchedExpression to set
	 * @return the expressions match result
	 */
	public ExpressionsMatchResult setMatchedExpression(OutputExpression matchedExpression) {
		this.matchedExpression = matchedExpression;
		return this;
	}

	/**
	 * Test matched.
	 *
	 * @return true, if successful
	 */
	public boolean testMatched() {
		return (matched != null) && matched.booleanValue();
	}

	/**
	 * Test matched or has errors.
	 *
	 * @return true, if successful
	 */
	public boolean testMatchedOrHasErrors() {
		return testMatched() || hasErrors();
	}

}
