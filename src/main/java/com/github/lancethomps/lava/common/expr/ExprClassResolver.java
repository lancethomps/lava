package com.github.lancethomps.lava.common.expr;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import ognl.ClassResolver;

/**
 * The Class ExprClassResolver.
 */
public class ExprClassResolver implements ClassResolver {

	/** The allowed classes. */
	private static Set<String> allowedExprClasses = Sets.newHashSet("java.lang.Math", "java.lang.Double");

	/** The classes. */
	private static Map<String, Class<?>> classes = new HashMap<>(101);

	/**
	 * Instantiates a new expr class resolver.
	 */
	public ExprClassResolver() {
		super();
	}

	/**
	 * @return the allowedExprClasses
	 */
	public static Set<String> getAllowedExprClasses() {
		return allowedExprClasses;
	}

	/**
	 * @param allowedExprClasses the allowedExprClasses to set
	 */
	public static void setAllowedExprClasses(Set<String> allowedExprClasses) {
		ExprClassResolver.allowedExprClasses = allowedExprClasses;
		classes.clear();
	}

	@Override
	public Class classForName(String className, Map context) throws ClassNotFoundException {
		Class result = null;

		if ((result = classes.get(className)) == null) {
			try {
				result = allowedExprClasses.contains(className) ? Class.forName(className) : null;
			} catch (ClassNotFoundException ex) {
				if (className.indexOf('.') == -1) {
					result = Class.forName("java.lang." + className);
					classes.put("java.lang." + className, result);
				}
			}
			classes.put(className, result);
		}
		return result;
	}
}
