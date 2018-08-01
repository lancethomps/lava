package com.github.lancethomps.lava.common.ser;

import java.util.List;
import java.util.Map;

import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.github.lancethomps.lava.common.expr.ExprParser;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class OutputExpression.
 *
 * @author lancethomps
 */
@SuppressWarnings("serial")
public class OutputExpression extends SimpleDomainObject {

	/** The compile. */
	private Boolean compile;

	/** The compiled expression. */
	@JsonIgnore
	private Object compiledExpression;

	/** The description. */
	private String description;

	/** The expression. */
	private String expression;

	/** The global variables. */
	private List<OutputExpression> globalVariables;

	/** The global variables resolved. */
	@JsonIgnore
	private Map<String, Object> globalVariablesResolved;

	/** The output path. */
	private String path;

	/** The returns path key map. */
	private Boolean returnsPathKeyMap;

	/** The type. */
	private ExprParser type;

	/**
	 * @return the compile
	 */
	public Boolean getCompile() {
		return compile;
	}

	/**
	 * Gets the compiled expression.
	 *
	 * @return the compiledExpression
	 */
	public Object getCompiledExpression() {
		return compiledExpression;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the expression.
	 *
	 * @return the expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @return the globalVariables
	 */
	public List<OutputExpression> getGlobalVariables() {
		return globalVariables;
	}

	/**
	 * @return the globalVariablesResolved
	 */
	public Map<String, Object> getGlobalVariablesResolved() {
		return globalVariablesResolved;
	}

	/**
	 * Gets the path.
	 *
	 * @return the outputPath
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the returnsPathKeyMap
	 */
	public Boolean getReturnsPathKeyMap() {
		return returnsPathKeyMap;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public ExprParser getType() {
		return type;
	}

	/**
	 * Sets the compile.
	 *
	 * @param compile the compile to set
	 * @return the output expression
	 */
	public OutputExpression setCompile(Boolean compile) {
		this.compile = compile;
		return this;
	}

	/**
	 * Sets the compiled expression.
	 *
	 * @param compiledExpression the compiledExpression to set
	 * @return the output expression
	 */
	public OutputExpression setCompiledExpression(Object compiledExpression) {
		this.compiledExpression = compiledExpression;
		return this;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the description to set
	 * @return the output expression
	 */
	public OutputExpression setDescription(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Sets the expression.
	 *
	 * @param expression the expression to set
	 * @return the output expression
	 */
	public OutputExpression setExpression(String expression) {
		this.expression = expression;
		return this;
	}

	/**
	 * Sets the global variables.
	 *
	 * @param globalVariables the globalVariables to set
	 * @return the output expression
	 */
	public OutputExpression setGlobalVariables(List<OutputExpression> globalVariables) {
		this.globalVariables = globalVariables;
		return this;
	}

	/**
	 * Sets the global variables resolved.
	 *
	 * @param globalVariablesResolved the globalVariablesResolved to set
	 * @return the output expression
	 */
	public OutputExpression setGlobalVariablesResolved(Map<String, Object> globalVariablesResolved) {
		this.globalVariablesResolved = globalVariablesResolved;
		return this;
	}

	/**
	 * Sets the path.
	 *
	 * @param path the path
	 * @return the output expression
	 */
	public OutputExpression setPath(String path) {
		this.path = path;
		return this;
	}

	/**
	 * Sets the returns path key map.
	 *
	 * @param returnsPathKeyMap the returnsPathKeyMap to set
	 * @return the output expression
	 */
	public OutputExpression setReturnsPathKeyMap(Boolean returnsPathKeyMap) {
		this.returnsPathKeyMap = returnsPathKeyMap;
		return this;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type to set
	 * @return the output expression
	 */
	public OutputExpression setType(ExprParser type) {
		this.type = type;
		return this;
	}

	/**
	 * Test compile.
	 *
	 * @return true, if successful
	 */
	public boolean testCompile() {
		return (compile != null) && compile.booleanValue();
	}

	/**
	 * Test returns path key map.
	 *
	 * @return true, if successful
	 */
	public boolean testReturnsPathKeyMap() {
		return (returnsPathKeyMap != null) && returnsPathKeyMap.booleanValue();
	}

}
