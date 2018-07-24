package com.github.lancethomps.lava.common.expr.spel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.util.ClassUtils;

import com.github.lancethomps.lava.common.collections.FastHashMap;

/**
 * The Class NonSandboxedSpelTypeLocator.
 */
public class NonSandboxedSpelTypeLocator extends StandardTypeLocator {

	/** The class loader. */
	private final ClassLoader classLoader;

	/** The known package prefixes. */
	private final List<String> knownPackagePrefixes = new LinkedList<>();

	/** The type lookup cache. */
	private final Map<String, Class<?>> typeLookupCache = new FastHashMap<>();

	/**
	 * Instantiates a new non sandboxed spel type locator.
	 */
	public NonSandboxedSpelTypeLocator() {
		classLoader = ClassUtils.getDefaultClassLoader();
		registerImport("java.lang");
	}

	/**
	 * Find type.
	 *
	 * @param typeName the type name
	 * @return the class
	 * @throws EvaluationException the evaluation exception
	 */
	@Override
	public Class<?> findType(String typeName) throws EvaluationException {
		Class<?> type;
		if (typeName.contains(".")) {
			type = findTypeByName(typeName);
			if (type == null) {
				type = findTypeByPrefixes(typeName);
			}
		} else {
			type = typeLookupCache.computeIfAbsent(typeName, k -> {
				Class<?> foundType = findTypeByPrefixes(typeName);
				if (foundType == null) {
					foundType = findTypeByName(typeName);
				}
				return foundType;
			});
		}
		if (type != null) {
			return type;
		}
		throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
	}

	/**
	 * Gets the import prefixes.
	 *
	 * @return the import prefixes
	 */
	@Override
	public List<String> getImportPrefixes() {
		return Collections.unmodifiableList(knownPackagePrefixes);
	}

	/**
	 * Register import.
	 *
	 * @param prefix the prefix
	 */
	@Override
	public void registerImport(String prefix) {
		if (knownPackagePrefixes != null) {
			knownPackagePrefixes.add(prefix);
		}
	}

	/**
	 * Removes the import.
	 *
	 * @param prefix the prefix
	 */
	@Override
	public void removeImport(String prefix) {
		knownPackagePrefixes.remove(prefix);
	}

	/**
	 * Find type by name.
	 *
	 * @param typeName the type name
	 * @return the class
	 */
	private Class<?> findTypeByName(String typeName) {
		try {
			return ClassUtils.forName(typeName, classLoader);
		} catch (ClassNotFoundException ey) {
			// try any registered prefixes before giving up
		}
		return null;
	}

	/**
	 * Find type by prefixes.
	 *
	 * @param typeName the type name
	 * @return the class
	 */
	private Class<?> findTypeByPrefixes(String typeName) {
		for (String prefix : knownPackagePrefixes) {
			try {
				return ClassUtils.forName(prefix + '.' + typeName, classLoader);
			} catch (ClassNotFoundException ex) {
				// might be a different prefix
			}
		}
		return null;
	}
}
