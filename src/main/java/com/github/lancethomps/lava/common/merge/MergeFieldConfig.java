package com.github.lancethomps.lava.common.merge;

import static com.github.lancethomps.lava.common.Exceptions.throwIfTrue;
import static com.github.lancethomps.lava.common.expr.ExprFactory.parseAndConsumeExpressions;

import java.util.List;

import org.apache.commons.lang3.SerializationException;

import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.lambda.Lambdas;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ognl.Node;

/**
 * The Class MergeFieldConfig.
 *
 * @author lancethomps
 */
public class MergeFieldConfig extends MergeConfig {

	/** The enabled. */
	@JsonIgnore
	private List<Node> enabled;

	/** The enabled expressions. */
	private List<String> enabledExpressions;

	/** The name. */
	private String name;

	/** The post process. */
	@JsonIgnore
	private Node postProcess;

	/** The post process expression. */
	private String postProcessExpression;

	/** The result. */
	@JsonIgnore
	private List<Node> result;

	/** The result expressions. */
	private List<String> resultExpressions;

	/** The result keys. */
	private List<String> resultKeys;

	/** The value. */
	@JsonIgnore
	private List<Node> value;

	/** The value expressions. */
	private List<String> valueExpressions;

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.ser.PostConstructor#afterDeserialization()
	 */
	@Override
	public void afterDeserialization() {
		super.afterDeserialization();
		String msg = "Could not parse expressions!";
		throwIfTrue(!parseAndConsumeExpressions(enabledExpressions, this::setEnabled), SerializationException.class, msg);
		throwIfTrue(!parseAndConsumeExpressions(resultExpressions, this::setResult), SerializationException.class, msg);
		throwIfTrue(!parseAndConsumeExpressions(valueExpressions, this::setValue), SerializationException.class, msg);
		postProcess = Lambdas.functionIfNonNull(postProcessExpression, ExprFactory::createOgnlExpression).orElse(null);
	}

	/**
	 * Gets the enabled.
	 *
	 * @return the enabled
	 */
	public List<Node> getEnabled() {
		return enabled;
	}

	/**
	 * Gets the enabled expressions.
	 *
	 * @return the enabledExpressions
	 */
	public List<String> getEnabledExpressions() {
		return enabledExpressions;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the post process.
	 *
	 * @return the postProcess
	 */
	public Node getPostProcess() {
		return postProcess;
	}

	/**
	 * Gets the post process expression.
	 *
	 * @return the postProcessExpression
	 */
	public String getPostProcessExpression() {
		return postProcessExpression;
	}

	/**
	 * Gets the result.
	 *
	 * @return the result
	 */
	public List<Node> getResult() {
		return result;
	}

	/**
	 * Gets the result expressions.
	 *
	 * @return the resultExpressions
	 */
	public List<String> getResultExpressions() {
		return resultExpressions;
	}

	/**
	 * Gets the result keys.
	 *
	 * @return the resultKeys
	 */
	public List<String> getResultKeys() {
		return resultKeys;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public List<Node> getValue() {
		return value;
	}

	/**
	 * Gets the value expressions.
	 *
	 * @return the valueExpressions
	 */
	public List<String> getValueExpressions() {
		return valueExpressions;
	}

	/**
	 * Sets the enabled.
	 *
	 * @param enabled the enabled to set
	 * @return the merge field config
	 */
	public MergeFieldConfig setEnabled(List<Node> enabled) {
		checkModificationsDisabled();
		this.enabled = enabled;
		return this;
	}

	/**
	 * Sets the enabled expressions.
	 *
	 * @param enabledExpressions the enabledExpressions to set
	 * @return the merge field config
	 */
	public MergeFieldConfig setEnabledExpressions(List<String> enabledExpressions) {
		checkModificationsDisabled();
		this.enabledExpressions = enabledExpressions;
		return this;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the name to set
	 * @return the merge field config
	 */
	public MergeFieldConfig setName(String name) {
		checkModificationsDisabled();
		this.name = name;
		return this;
	}

	/**
	 * Sets the post process.
	 *
	 * @param postProcess the postProcess to set
	 * @return the merge field config
	 */
	public MergeFieldConfig setPostProcess(Node postProcess) {
		checkModificationsDisabled();
		this.postProcess = postProcess;
		return this;
	}

	/**
	 * Sets the post process expression.
	 *
	 * @param postProcessExpression the postProcessExpression to set
	 * @return the merge field config
	 */
	public MergeFieldConfig setPostProcessExpression(String postProcessExpression) {
		checkModificationsDisabled();
		this.postProcessExpression = postProcessExpression;
		return this;
	}

	/**
	 * Sets the result.
	 *
	 * @param result the result to set
	 * @return the merge field config
	 */
	public MergeFieldConfig setResult(List<Node> result) {
		checkModificationsDisabled();
		this.result = result;
		return this;
	}

	/**
	 * Sets the result expressions.
	 *
	 * @param resultExpressions the resultExpressions to set
	 * @return the merge field config
	 */
	public MergeFieldConfig setResultExpressions(List<String> resultExpressions) {
		checkModificationsDisabled();
		this.resultExpressions = resultExpressions;
		return this;
	}

	/**
	 * Sets the result keys.
	 *
	 * @param resultKeys the resultKeys to set
	 * @return the merge field config
	 */
	public MergeFieldConfig setResultKeys(List<String> resultKeys) {
		checkModificationsDisabled();
		this.resultKeys = resultKeys;
		return this;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the value to set
	 * @return the merge field config
	 */
	public MergeFieldConfig setValue(List<Node> value) {
		checkModificationsDisabled();
		this.value = value;
		return this;
	}

	/**
	 * Sets the value expressions.
	 *
	 * @param valueExpressions the valueExpressions to set
	 * @return the merge field config
	 */
	public MergeFieldConfig setValueExpressions(List<String> valueExpressions) {
		checkModificationsDisabled();
		this.valueExpressions = valueExpressions;
		return this;
	}

}
