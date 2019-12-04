package com.lancethomps.lava.common.expr.spel;

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

import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.SimpleDomainObject;
import com.lancethomps.lava.common.collections.FastHashMap;
import com.lancethomps.lava.common.logging.Logs;

public class SandboxedContextConfig extends SimpleDomainObject {

  private static final Logger LOG = Logger.getLogger(SandboxedContextConfig.class);
  private static final long serialVersionUID = -7384574349724321652L;
  private final Set<String> blackList;

  private final Set<Pattern> blackListPatterns;
  private final Map<Class<?>, SandboxedContextConfig> configByType;
  private final Set<Class<?>> superTypesBlackList;
  private final Set<Class<?>> superTypesWhiteList;
  private final Set<Class<?>> typesBlackList;
  private final Set<Class<?>> typesWhiteList;
  private final Set<String> whiteList;
  private final Set<Pattern> whiteListPatterns;
  private List<Pair<Class<?>, SandboxedContextConfig>> configBySuperType;

  public SandboxedContextConfig() {
    this(null, null, null, null, null, null, null, null, null, null);
  }

  public SandboxedContextConfig(
    Set<String> blackList,
    Set<String> whiteList,
    Set<Pattern> blackListPatterns,
    Set<Pattern> whiteListPatterns,
    Set<Class<?>> typesBlackList,
    Set<Class<?>> typesWhiteList,
    Set<Class<?>> superTypesBlackList,
    Set<Class<?>> superTypesWhiteList,
    Map<Class<?>, SandboxedContextConfig> configByType,
    List<Pair<Class<?>, SandboxedContextConfig>> configBySuperType
  ) {
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

  public static void checkAllowed(SandboxedContextConfig config, Object target, String name) throws AccessException {
    if (target == null) {
      throw new AccessException("Target is null.");
    }
    Class<?> type = (target instanceof Class ? (Class<?>) target : target.getClass());
    SandboxedContextConfig superTypeConfig;
    if (config.getConfigByType().containsKey(type)) {
      config = config.getConfigByType().get(type);
    } else if ((config.getConfigBySuperType() != null)
      && ((superTypeConfig =
      config.getConfigBySuperType().stream().filter(conf -> conf.getLeft().isAssignableFrom(type)).findFirst().map(Pair::getRight).orElse(null)) !=
      null)) {
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
      Pair<Boolean, Object> matched = Checks.passesWhiteAndBlackListCheck(
        name,
        config.getWhiteList(),
        config.getBlackList(),
        config.getWhiteListPatterns(),
        config.getBlackListPatterns(),
        false
      );
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

  public SandboxedContextConfig addConfigForSuperType(Class<?> type, SandboxedContextConfig config) {
    synchronized (configBySuperType) {
      List<Pair<Class<?>, SandboxedContextConfig>> copy = new ArrayList<>(configBySuperType);
      copy.removeIf(conf -> conf.getLeft() == type);
      copy.add(Pair.of(type, config));
      configBySuperType = copy;
    }
    return this;
  }

  public SandboxedContextConfig addConfigForType(Class<?> type, SandboxedContextConfig config) {
    configByType.put(type, config);
    return this;
  }

  public SandboxedContextConfig addToBlackList(Collection<String> keys) {
    blackList.addAll(keys);
    return this;
  }

  public SandboxedContextConfig addToBlackList(String... keys) {
    return addToBlackList(Arrays.asList(keys));
  }

  public SandboxedContextConfig addToBlackListPatterns(Collection<Pattern> patterns) {
    blackListPatterns.addAll(patterns);
    return this;
  }

  public SandboxedContextConfig addToBlackListPatterns(Pattern... patterns) {
    return addToBlackListPatterns(Arrays.asList(patterns));
  }

  public SandboxedContextConfig addToSuperTypesBlackList(Class<?>... keys) {
    return addToSuperTypesBlackList(Arrays.asList(keys));
  }

  public SandboxedContextConfig addToSuperTypesBlackList(Collection<Class<?>> keys) {
    superTypesBlackList.addAll(keys);
    return this;
  }

  public SandboxedContextConfig addToSuperTypesWhiteList(Class<?>... keys) {
    return addToSuperTypesWhiteList(Arrays.asList(keys));
  }

  public SandboxedContextConfig addToSuperTypesWhiteList(Collection<Class<?>> keys) {
    superTypesWhiteList.addAll(keys);
    return this;
  }

  public SandboxedContextConfig addToTypesBlackList(Class<?>... keys) {
    return addToTypesBlackList(Arrays.asList(keys));
  }

  public SandboxedContextConfig addToTypesBlackList(Collection<Class<?>> keys) {
    typesBlackList.addAll(keys);
    return this;
  }

  public SandboxedContextConfig addToTypesWhiteList(Class<?>... keys) {
    return addToTypesWhiteList(Arrays.asList(keys));
  }

  public SandboxedContextConfig addToTypesWhiteList(Collection<Class<?>> keys) {
    typesWhiteList.addAll(keys);
    return this;
  }

  public SandboxedContextConfig addToWhiteList(Collection<String> keys) {
    whiteList.addAll(keys);
    return this;
  }

  public SandboxedContextConfig addToWhiteList(String... keys) {
    return addToWhiteList(Arrays.asList(keys));
  }

  public SandboxedContextConfig addToWhiteListPatterns(Collection<Pattern> patterns) {
    whiteListPatterns.addAll(patterns);
    return this;
  }

  public SandboxedContextConfig addToWhiteListPatterns(Pattern... patterns) {
    return addToWhiteListPatterns(Arrays.asList(patterns));
  }

  public void checkAllowed(Object target, String name) throws AccessException {
    checkAllowed(this, target, name);
  }

  public Set<String> getBlackList() {
    return blackList;
  }

  public Set<Pattern> getBlackListPatterns() {
    return blackListPatterns;
  }

  public List<Pair<Class<?>, SandboxedContextConfig>> getConfigBySuperType() {
    return configBySuperType;
  }

  public Map<Class<?>, SandboxedContextConfig> getConfigByType() {
    return configByType;
  }

  public Set<Class<?>> getSuperTypesBlackList() {
    return superTypesBlackList;
  }

  public Set<Class<?>> getSuperTypesWhiteList() {
    return superTypesWhiteList;
  }

  public Set<Class<?>> getTypesBlackList() {
    return typesBlackList;
  }

  public Set<Class<?>> getTypesWhiteList() {
    return typesWhiteList;
  }

  public Set<String> getWhiteList() {
    return whiteList;
  }

  public Set<Pattern> getWhiteListPatterns() {
    return whiteListPatterns;
  }

  public SandboxedContextConfig removeConfigForSuperType(Class<?> type) {
    synchronized (configBySuperType) {
      List<Pair<Class<?>, SandboxedContextConfig>> copy = new ArrayList<>(configBySuperType);
      copy.removeIf(conf -> conf.getLeft() == type);
      configBySuperType = copy;
    }
    return this;
  }

  public SandboxedContextConfig removeConfigForType(Class<?> type) {
    configByType.remove(type);
    return this;
  }

  public SandboxedContextConfig removeFromBlackList(Collection<String> keys) {
    for (String key : keys) {
      blackList.remove(key);
    }
    return this;
  }

  public SandboxedContextConfig removeFromBlackList(String... keys) {
    for (String key : keys) {
      blackList.remove(key);
    }
    return this;
  }

  public SandboxedContextConfig removeFromBlackListPatterns(Collection<Pattern> patterns) {
    for (Pattern pattern : patterns) {
      blackListPatterns.remove(pattern);
    }
    return this;
  }

  public SandboxedContextConfig removeFromBlackListPatterns(Pattern... patterns) {
    for (Pattern pattern : patterns) {
      blackListPatterns.remove(pattern);
    }
    return this;
  }

  public SandboxedContextConfig removeFromSuperTypesBlackList(Class<?>... keys) {
    for (Class<?> key : keys) {
      superTypesBlackList.remove(key);
    }
    return this;
  }

  public SandboxedContextConfig removeFromSuperTypesBlackList(Collection<Class<?>> keys) {
    for (Class<?> key : keys) {
      superTypesBlackList.remove(key);
    }
    return this;
  }

  public SandboxedContextConfig removeFromSuperTypesWhiteList(Class<?>... keys) {
    for (Class<?> key : keys) {
      superTypesWhiteList.remove(key);
    }
    return this;
  }

  public SandboxedContextConfig removeFromSuperTypesWhiteList(Collection<Class<?>> keys) {
    for (Class<?> key : keys) {
      superTypesWhiteList.remove(key);
    }
    return this;
  }

  public SandboxedContextConfig removeFromTypesBlackList(Class<?>... keys) {
    for (Class<?> key : keys) {
      typesBlackList.remove(key);
    }
    return this;
  }

  public SandboxedContextConfig removeFromTypesBlackList(Collection<Class<?>> keys) {
    for (Class<?> key : keys) {
      typesBlackList.remove(key);
    }
    return this;
  }

  public SandboxedContextConfig removeFromTypesWhiteList(Class<?>... keys) {
    for (Class<?> key : keys) {
      typesWhiteList.remove(key);
    }
    return this;
  }

  public SandboxedContextConfig removeFromTypesWhiteList(Collection<Class<?>> keys) {
    for (Class<?> key : keys) {
      typesWhiteList.remove(key);
    }
    return this;
  }

  public SandboxedContextConfig removeFromWhiteList(Collection<String> keys) {
    for (String key : keys) {
      whiteList.remove(key);
    }
    return this;
  }

  public SandboxedContextConfig removeFromWhiteList(String... keys) {
    for (String key : keys) {
      whiteList.remove(key);
    }
    return this;
  }

  public SandboxedContextConfig removeFromWhiteListPatterns(Collection<Pattern> patterns) {
    for (Pattern pattern : patterns) {
      whiteListPatterns.remove(pattern);
    }
    return this;
  }

  public SandboxedContextConfig removeFromWhiteListPatterns(Pattern... patterns) {
    for (Pattern pattern : patterns) {
      whiteListPatterns.remove(pattern);
    }
    return this;
  }

}
