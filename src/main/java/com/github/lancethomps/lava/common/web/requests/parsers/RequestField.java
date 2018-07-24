package com.github.lancethomps.lava.common.web.requests.parsers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The Interface RequestField.
 */
@Target({ FIELD, TYPE })
@Retention(RUNTIME)
public @interface RequestField {

	/** The Constant DEFAULT. */
	String DEFAULT = "#default";

	/**
	 * Additional parameter names.
	 *
	 * @return the string[]
	 */
	String[] additionalParameterNames() default "";

	/**
	 * Admin only.
	 *
	 * @return true, if successful
	 */
	boolean adminOnly() default false;

	/**
	 * Enabled.
	 *
	 * @return true, if successful
	 */
	boolean enabled() default true;

	/**
	 * Include type fields.
	 *
	 * @return true, if successful
	 */
	boolean includeTypeFields() default false;

	/**
	 * Internal only.
	 *
	 * @return true, if successful
	 */
	boolean internalOnly() default false;

	/**
	 * Post process.
	 *
	 * @return true, if successful
	 */
	boolean postProcess() default false;

	/**
	 * Prefix.
	 *
	 * @return the string
	 */
	String prefix() default "";

	/**
	 * Prefix only.
	 *
	 * @return true, if successful
	 */
	boolean prefixOnly() default false;

	/**
	 * Prefix or json.
	 *
	 * @return true, if successful
	 */
	boolean prefixOrJson() default false;

	/**
	 * Process using.
	 *
	 * @return the class
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends RequestParameterParser> processUsing() default DefaultRequestParameterParser.class;

	/**
	 * Validate field.
	 *
	 * @return true, if successful
	 */
	boolean validateField() default false;

	/**
	 * Validate using.
	 *
	 * @return the class
	 */
	Class<? extends RequestParameterValidator> validateUsing() default NoOpRequestParameterValidator.class;

	/**
	 * Value.
	 *
	 * @return the string
	 */
	String value() default DEFAULT;
}
