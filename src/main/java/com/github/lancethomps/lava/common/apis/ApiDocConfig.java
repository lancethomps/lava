package com.github.lancethomps.lava.common.apis;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The Interface OpenApiIgnore.
 *
 * @author lancethomps
 */
@Target({ FIELD, TYPE })
@Retention(RUNTIME)
public @interface ApiDocConfig {

	/**
	 * Group.
	 *
	 * @return the string
	 */
	String group() default "";

	/**
	 * Ignore.
	 *
	 * @return true, if successful
	 */
	boolean ignore() default false;

	/**
	 * Initially displayed.
	 *
	 * @return true, if successful
	 */
	boolean initiallyDisplayed() default false;

	/**
	 * Initially hidden.
	 *
	 * @return true, if successful
	 */
	boolean initiallyHidden() default false;

	/**
	 * Value.
	 *
	 * @return the string
	 */
	String value() default "";

}
