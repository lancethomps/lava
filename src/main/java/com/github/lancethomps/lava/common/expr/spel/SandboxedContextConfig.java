package com.github.lancethomps.lava.common.expr.spel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.expression.AccessException;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.github.lancethomps.lava.common.collections.FastHashMap;
import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class SandboxedContextConfig.
 */
public class SandboxedContextConfig extends SimpleDomainObject {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7384574349724321652L;

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(SandboxedContextConfig.class);

	/** The black list. */
	private final Set<String> blackList;

	/** The black list patterns. */
	private final Set<Pattern> blackListPatterns;

	/** The config by super type. */
	private List<Pair<Class<?>, SandboxedContextConfig>> configBySuperType;

	/** The config by type. */
	private final Map<Class<?>, SandboxedContextConfig> configByType;

	/** The super types black list. */
	private final Set<Class<?>> superTypesBlackList;

	/** The super types white list. */
	private final Set<Class<?>> superTypesWhiteList;

	/** The types black list. */
	private final Set<Class<?>> typesBlackList;

	/** The types white list. */
	private final Set<Class<?>> typesWhiteList;

	/** The white list. */
	private final Set<String> whiteList;

	/** The white list patterns. */
	private final Set<Pattern> whiteListPatterns;

	/**
	 * Instantiates a new sandboxed context config.
	 */
	public SandboxedContextConfig() {
		this(null, null, null, null, null, null, null, null, null, null);
	}

	/**
	 * Instantiates a new sandboxed context config.
	 *
	 * @param blackList the black list
	 * @param whiteList the white list
	 * @param blackListPatterns the black list patterns
	 * @param whiteListPatterns the white list patterns
	 * @param typesBlackList the types black list
	 * @param typesWhiteList the types white list
	 * @param superTypesBlackList the super types black list
	 * @param superTypesWhiteList the super types white list
	 * @param configByType the config by type
	 * @param configBySuperType the config by super type
	 */
	public SandboxedContextConfig(Set<String> blackList, Set<String> whiteList, Set<Pattern> blackListPatterns, Set<Pattern> whiteListPatterns, Set<Class<?>> typesBlackList,
		Set<Class<?>> typesWhiteList, Set<Class<?>> superTypesBlackList, Set<Class<?>> superTypesWhiteList, Map<Class<?>, SandboxedContextConfig> configByType,
		List<Pair<Class<?>, SandboxedContextConfig>> configBySuperType) {
		super();
		this.blackList = FastHashMap.createBackedSet(blackList);
		this.whiteList = FastHashMap.createBackedSet(whiteList);
		this.blackListPatterns = FastHashMap.createBackedSet(blackListPatterns);
		this.whiteListPatterns = FastHashMap.createBackedSet(whiteListPatterns);
		this.typesBlackList = FastHashMap.createBackedSet(typesBlackList);
		this.typesWhiteList = FastHashMap.createBackedSet(typesWhiteList);
		this.superTypesBlackList = FastHashMap.createBackedSet(superTypesBlackList);
		this.superTypesWhiteList = FastHashMap.createBackedSet(superTypesWhiteList);
		this.configByType = configByType == null ? new FastHashMap<>(true) : new FastHashMap<>(configByType, true);
		this.configBySuperType = configBySuperType == null ? new ArrayList<>() : configBySuperType;
	}

	/**
	 * Check allowed.
	 *
	 * @param config the config
	 * @param target the target
	 * @param name the name
	 * @throws AccessException the access exception
	 */
	public static void checkAllowed(SandboxedContextConfig config, Object target, String name) throws AccessException {
		if (target == null) {
			throw new AccessException("Target is null.");
		}
		Class<?> type = (target instanceof Class ? (Class<?>) target : target.getClass());
		SandboxedContextConfig superTypeConfig;
		if (config.getConfigByType().containsKey(type)) {
			config = config.getConfigByType().get(type);
		} else if ((config.getConfigBySuperType() != null)
			&& ((superTypeConfig = config.getConfigBySuperType().stream().filter(conf -> conf.getLeft().isAssignableFrom(type)).findFirst().map(Pair::getRight).orElse(null)) != null)) {
			config = superTypeConfig;
		} else {
			if (config.getTypesBlackList().contains(type)) {
				throw new AccessException(String.format("Type [%s] is black listed.", type));
			}
			for (Class<?> blackList : config.getSuperTypesBlackList()) {
				if (blackList.isAssignableFrom(type)) {
					throw new AccessException(String.format("Type [%s] is black listed by super type [%s].", type, blackList));
				}
			}
			if (!config.getTypesWhiteList().isEmpty()) {
				if (!config.getTypesWhiteList().contains(type)) {
					if (config.getSuperTypesWhiteList().isEmpty()) {
						throw new AccessException(String.format("Type [%s] is not in the white list.", type));
					}
				}
			}
			if (!config.getSuperTypesWhiteList().isEmpty()) {
				boolean allowed = false;
				for (Class<?> whiteList : config.getSuperTypesWhiteList()) {
					if (whiteList.isAssignableFrom(type)) {
						allowed = true;
						Logs.logTrace(LOG, "Type [%s] allowed from super type [%s].", type, whiteList);
						break;
					}
				}
				if (!allowed) {
					throw new AccessException(String.format("Type [%s] is not in the super type white list.", type));
				}
			}
		}
		if (name != null) {
			Pair<Boolean, Object> matched = Checks.passesWhiteAndBlackListCheck(name, config.getWhiteList(), config.getBlackList(), config.getWhiteListPatterns(), config.getBlackListPatterns(),
				false);
			if (!matched.getLeft()) {
				if (matched.getRight() == null) {
					throw new AccessException(String.format("Value with name [%s] did not match any white list.", name));
				} else if (matched.getRight() instanceof Pattern) {
					throw new AccessException(String.format("Value with name [%s] is black listed by pattern [%s].", name, matched.getRight()));
				}
				throw new AccessException(String.format("Value with name [%s] is black listed.", name));
			}
			Logs.logTrace(LOG, "Value with name [%s] on type [%s] allowed from match [%s].", name, type, matched.getRight());
		}
	}

	/**
	 * Adds the config for super type.
	 *
	 * @param type the type
	 * @param config the config
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addConfigForSuperType(Class<?> type, SandboxedContextConfig config) {
		synchronized (configBySuperType) {
			List<Pair<Class<?>, SandboxedContextConfig>> copy = new ArrayList<>(configBySuperType);
			copy.removeIf(conf -> conf.getLeft() == type);
			copy.add(Pair.of(type, config));
			configBySuperType = copy;
		}
		return this;
	}

	/**
	 * Adds the config for type.
	 *
	 * @param type the type
	 * @param config the config
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addConfigForType(Class<?> type, SandboxedContextConfig config) {
		configByType.put(type, config);
		return this;
	}

	/**
	 * Adds the to black list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToBlackList(Collection<String> keys) {
		blackList.addAll(keys);
		return this;
	}

	/**
	 * Adds the to black list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToBlackList(String... keys) {
		return addToBlackList(Arrays.asList(keys));
	}

	/**
	 * Adds the to black list patterns.
	 *
	 * @param patterns the patterns
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToBlackListPatterns(Collection<Pattern> patterns) {
		blackListPatterns.addAll(patterns);
		return this;
	}

	/**
	 * Adds the to black list patterns.
	 *
	 * @param patterns the patterns
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToBlackListPatterns(Pattern... patterns) {
		return addToBlackListPatterns(Arrays.asList(patterns));
	}

	/**
	 * Adds the to super types black list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToSuperTypesBlackList(Class<?>... keys) {
		return addToSuperTypesBlackList(Arrays.asList(keys));
	}

	/**
	 * Adds the to super types black list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToSuperTypesBlackList(Collection<Class<?>> keys) {
		superTypesBlackList.addAll(keys);
		return this;
	}

	/**
	 * Adds the to super types white list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToSuperTypesWhiteList(Class<?>... keys) {
		return addToSuperTypesWhiteList(Arrays.asList(keys));
	}

	/**
	 * Adds the to super types white list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToSuperTypesWhiteList(Collection<Class<?>> keys) {
		superTypesWhiteList.addAll(keys);
		return this;
	}

	/**
	 * Adds the to types black list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToTypesBlackList(Class<?>... keys) {
		return addToTypesBlackList(Arrays.asList(keys));
	}

	/**
	 * Adds the to types black list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToTypesBlackList(Collection<Class<?>> keys) {
		typesBlackList.addAll(keys);
		return this;
	}

	/**
	 * Adds the to types white list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToTypesWhiteList(Class<?>... keys) {
		return addToTypesWhiteList(Arrays.asList(keys));
	}

	/**
	 * Adds the to types white list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToTypesWhiteList(Collection<Class<?>> keys) {
		typesWhiteList.addAll(keys);
		return this;
	}

	/**
	 * Adds the to white list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToWhiteList(Collection<String> keys) {
		whiteList.addAll(keys);
		return this;
	}

	/**
	 * Adds the to white list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToWhiteList(String... keys) {
		return addToWhiteList(Arrays.asList(keys));
	}

	/**
	 * Adds the to white list patterns.
	 *
	 * @param patterns the patterns
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToWhiteListPatterns(Collection<Pattern> patterns) {
		whiteListPatterns.addAll(patterns);
		return this;
	}

	/**
	 * Adds the to white list patterns.
	 *
	 * @param patterns the patterns
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig addToWhiteListPatterns(Pattern... patterns) {
		return addToWhiteListPatterns(Arrays.asList(patterns));
	}

	/**
	 * Check allowed.
	 *
	 * @param target the target
	 * @param name the name
	 * @throws AccessException the access exception
	 */
	public void checkAllowed(Object target, String name) throws AccessException {
		checkAllowed(this, target, name);
	}

	/**
	 * Gets the black list.
	 *
	 * @return the blackList
	 */
	public Set<String> getBlackList() {
		return blackList;
	}

	/**
	 * Gets the black list patterns.
	 *
	 * @return the blackListPatterns
	 */
	public Set<Pattern> getBlackListPatterns() {
		return blackListPatterns;
	}

	/**
	 * Gets the config by super type.
	 *
	 * @return the configBySuperType
	 */
	public List<Pair<Class<?>, SandboxedContextConfig>> getConfigBySuperType() {
		return configBySuperType;
	}

	/**
	 * Gets the config by type.
	 *
	 * @return the configByType
	 */
	public Map<Class<?>, SandboxedContextConfig> getConfigByType() {
		return configByType;
	}

	/**
	 * Gets the super types black list.
	 *
	 * @return the superTypesBlackList
	 */
	public Set<Class<?>> getSuperTypesBlackList() {
		return superTypesBlackList;
	}

	/**
	 * Gets the super types white list.
	 *
	 * @return the superTypesWhiteList
	 */
	public Set<Class<?>> getSuperTypesWhiteList() {
		return superTypesWhiteList;
	}

	/**
	 * Gets the types black list.
	 *
	 * @return the typesBlackList
	 */
	public Set<Class<?>> getTypesBlackList() {
		return typesBlackList;
	}

	/**
	 * Gets the types white list.
	 *
	 * @return the typesWhiteList
	 */
	public Set<Class<?>> getTypesWhiteList() {
		return typesWhiteList;
	}

	/**
	 * Gets the white list.
	 *
	 * @return the whiteList
	 */
	public Set<String> getWhiteList() {
		return whiteList;
	}

	/**
	 * Gets the white list patterns.
	 *
	 * @return the whiteListPatterns
	 */
	public Set<Pattern> getWhiteListPatterns() {
		return whiteListPatterns;
	}

	/**
	 * Removes the config for super type.
	 *
	 * @param type the type
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeConfigForSuperType(Class<?> type) {
		synchronized (configBySuperType) {
			List<Pair<Class<?>, SandboxedContextConfig>> copy = new ArrayList<>(configBySuperType);
			copy.removeIf(conf -> conf.getLeft() == type);
			configBySuperType = copy;
		}
		return this;
	}

	/**
	 * Removes the config for type.
	 *
	 * @param type the type
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeConfigForType(Class<?> type) {
		configByType.remove(type);
		return this;
	}

	/**
	 * Removes the from black list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromBlackList(Collection<String> keys) {
		for (String key : keys) {
			blackList.remove(key);
		}
		return this;
	}

	/**
	 * Removes the from black list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromBlackList(String... keys) {
		for (String key : keys) {
			blackList.remove(key);
		}
		return this;
	}

	/**
	 * Removes the from black list patterns.
	 *
	 * @param patterns the patterns
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromBlackListPatterns(Collection<Pattern> patterns) {
		for (Pattern pattern : patterns) {
			blackListPatterns.remove(pattern);
		}
		return this;
	}

	/**
	 * Removes the from black list patterns.
	 *
	 * @param patterns the patterns
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromBlackListPatterns(Pattern... patterns) {
		for (Pattern pattern : patterns) {
			blackListPatterns.remove(pattern);
		}
		return this;
	}

	/**
	 * Removes the from super types black list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromSuperTypesBlackList(Class<?>... keys) {
		for (Class<?> key : keys) {
			superTypesBlackList.remove(key);
		}
		return this;
	}

	/**
	 * Removes the from super types black list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromSuperTypesBlackList(Collection<Class<?>> keys) {
		for (Class<?> key : keys) {
			superTypesBlackList.remove(key);
		}
		return this;
	}

	/**
	 * Removes the from super types white list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromSuperTypesWhiteList(Class<?>... keys) {
		for (Class<?> key : keys) {
			superTypesWhiteList.remove(key);
		}
		return this;
	}

	/**
	 * Removes the from super types white list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromSuperTypesWhiteList(Collection<Class<?>> keys) {
		for (Class<?> key : keys) {
			superTypesWhiteList.remove(key);
		}
		return this;
	}

	/**
	 * Removes the from types black list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromTypesBlackList(Class<?>... keys) {
		for (Class<?> key : keys) {
			typesBlackList.remove(key);
		}
		return this;
	}

	/**
	 * Removes the from types black list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromTypesBlackList(Collection<Class<?>> keys) {
		for (Class<?> key : keys) {
			typesBlackList.remove(key);
		}
		return this;
	}

	/**
	 * Removes the from types white list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromTypesWhiteList(Class<?>... keys) {
		for (Class<?> key : keys) {
			typesWhiteList.remove(key);
		}
		return this;
	}

	/**
	 * Removes the from types white list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromTypesWhiteList(Collection<Class<?>> keys) {
		for (Class<?> key : keys) {
			typesWhiteList.remove(key);
		}
		return this;
	}

	/**
	 * Removes the from white list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromWhiteList(Collection<String> keys) {
		for (String key : keys) {
			whiteList.remove(key);
		}
		return this;
	}

	/**
	 * Removes the from white list.
	 *
	 * @param keys the keys
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromWhiteList(String... keys) {
		for (String key : keys) {
			whiteList.remove(key);
		}
		return this;
	}

	/**
	 * Removes the from white list patterns.
	 *
	 * @param patterns the patterns
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromWhiteListPatterns(Collection<Pattern> patterns) {
		for (Pattern pattern : patterns) {
			whiteListPatterns.remove(pattern);
		}
		return this;
	}

	/**
	 * Removes the from white list patterns.
	 *
	 * @param patterns the patterns
	 * @return the sandboxed context config
	 */
	public SandboxedContextConfig removeFromWhiteListPatterns(Pattern... patterns) {
		for (Pattern pattern : patterns) {
			whiteListPatterns.remove(pattern);
		}
		return this;
	}
}
