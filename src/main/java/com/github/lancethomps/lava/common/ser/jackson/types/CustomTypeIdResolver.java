package com.github.lancethomps.lava.common.ser.jackson.types;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.lancethomps.lava.common.collections.MapUtil;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.google.common.collect.Sets;

/**
 * The Class CustomTypeIdResolver.
 */
public class CustomTypeIdResolver extends TypeIdResolverBase {

	/** The Constant FIX_PACKAGE_PREFIX. */
	public static final String FIX_PACKAGE_PREFIX = "com.github.lancethomps.server.api.domain";

	/** The Constant OLD_ROOT. */
	public static final String OLD_ROOT = "com.github.lancethomps.server.";

	/** The Constant REMOVE_PACKAGE_PREFIX. */
	public static final String REMOVE_PACKAGE_PREFIX = "com.github.lancethomps.lava.api.domain";

	/** The Constant REPLACE_WITH_PACKAGE_PREFIX. */
	public static final String REPLACE_WITH_PACKAGE_PREFIX = "_bws";

	/** The Constant ROOT_PACKAGE. */
	public static final String ROOT_PACKAGE = "com.github.lancethomps.lava.";

	/** The Constant FIX_PACKAGE_PREFIX. */
	public static final Set<String> SHARED_PACKAGE_PREFIXES = Sets.newHashSet("api", "factory", "utils");

	/** The json shortened type. */
	private static boolean jsonShortenedType;

	/** The type id fixes. */
	private static Map<String, String> typeIdFixes;

	/** The type id package fixes. */
	private static Map<String, String> typeIdPackageFixes = MapUtil.createFrom("com.github.lancethomps.server.", "com.github.lancethomps.lava.");

	/** The shortened type override. */
	private boolean shortenedTypeOverride;

	/**
	 * Instantiates a new custom type id resolver.
	 *
	 * @param baseType the base type
	 * @param typeFactory the type factory
	 */
	public CustomTypeIdResolver(JavaType baseType, TypeFactory typeFactory) {
		this(baseType, typeFactory, false);
	}

	/**
	 * Instantiates a new custom type id resolver.
	 *
	 * @param baseType the base type
	 * @param typeFactory the type factory
	 * @param shortenedTypeOverride the shortened type override
	 */
	public CustomTypeIdResolver(JavaType baseType, TypeFactory typeFactory, boolean shortenedTypeOverride) {
		super(baseType, typeFactory);
		this.shortenedTypeOverride = shortenedTypeOverride;
	}

	/**
	 * Gets the correct class name.
	 *
	 * @param id the id
	 * @return the correct class name
	 */
	public static String getCorrectClassName(String id) {
		String resolvedId;
		if ((id != null)) {
			Entry<String, String> replacePackage;
			if (id.startsWith(REPLACE_WITH_PACKAGE_PREFIX)) {
				resolvedId = REMOVE_PACKAGE_PREFIX + StringUtils.removeStart(id, REPLACE_WITH_PACKAGE_PREFIX);
			} else if ((typeIdPackageFixes != null) && ((replacePackage = typeIdPackageFixes.entrySet().stream().filter(e -> id.startsWith(e.getKey())).findAny().orElse(null)) != null)) {
				final String idSuffix = StringUtils.removeStart(id, replacePackage.getKey());
				resolvedId = replacePackage.getValue() + (SHARED_PACKAGE_PREFIXES.stream().anyMatch(idSuffix::startsWith) ? "" : "dtts.") + idSuffix;
			} else {
				resolvedId = id;
			}
			if (typeIdFixes != null) {
				return typeIdFixes.getOrDefault(resolvedId, resolvedId);
			}
		} else {
			resolvedId = null;
		}
		return resolvedId;
	}

	/**
	 * @return the typeIdFixes
	 */
	public static Map<String, String> getTypeIdFixes() {
		return typeIdFixes;
	}

	/**
	 * @return the typeIdPackageFixes
	 */
	public static Map<String, String> getTypeIdPackageFixes() {
		return typeIdPackageFixes;
	}

	/**
	 * @return the jsonShortenedType
	 */
	public static boolean isJsonShortenedType() {
		return jsonShortenedType;
	}

	/**
	 * @param jsonShortenedType the jsonShortenedType to set
	 */
	public static void setJsonShortenedType(boolean jsonShortenedType) {
		CustomTypeIdResolver.jsonShortenedType = jsonShortenedType;
	}

	/**
	 * @param typeIdFixes the typeIdFixes to set
	 */
	public static void setTypeIdFixes(Map<String, String> typeIdFixes) {
		CustomTypeIdResolver.typeIdFixes = typeIdFixes;
	}

	/**
	 * @param typeIdPackageFixes the typeIdPackageFixes to set
	 */
	public static void setTypeIdPackageFixes(Map<String, String> typeIdPackageFixes) {
		CustomTypeIdResolver.typeIdPackageFixes = typeIdPackageFixes;
	}

	@Override
	public String getDescForKnownTypeIds() {
		return "class name used as type id";
	}

	@Override
	public JsonTypeInfo.Id getMechanism() {
		return JsonTypeInfo.Id.CLASS;
	}

	@Override
	public String idFromValue(Object value) {
		return idFrom(value, value.getClass());
	}

	@Override
	public String idFromValueAndType(Object value, Class<?> type) {
		return idFrom(value, type);
	}

	/**
	 * @return the shortenedTypeOverride
	 */
	public boolean isShortenedTypeOverride() {
		return shortenedTypeOverride;
	}

	/**
	 * Register subtype.
	 *
	 * @param type the type
	 * @param name the name
	 */
	public void registerSubtype(Class<?> type, String name) {
		// not used with class name - based resolvers
	}

	/**
	 * @param shortenedTypeOverride the shortenedTypeOverride to set
	 */
	public void setShortenedTypeOverride(boolean shortenedTypeOverride) {
		this.shortenedTypeOverride = shortenedTypeOverride;
	}

	@Override
	public JavaType typeFromId(DatabindContext context, String id) {
		return resolveTypeFromId(id, context.getTypeFactory());
	}

	/**
	 * Id from.
	 *
	 * @param value the value
	 * @param cls the cls
	 * @return the string
	 */
	protected final String idFrom(Object value, Class<?> cls) {
		if (Enum.class.isAssignableFrom(cls)) {
			if (!cls.isEnum()) {
				cls = cls.getSuperclass();
			}
		}
		String str = cls.getName();
		if (str.startsWith("java.util")) {
			if (value instanceof EnumSet<?>) { // Regular- and JumboEnumSet...
				Class<?> enumClass = ClassUtil.findEnumType((EnumSet<?>) value);
				str = TypeFactory.defaultInstance().constructCollectionType(EnumSet.class, enumClass).toCanonical();
			} else if (value instanceof EnumMap<?, ?>) {
				Class<?> enumClass = ClassUtil.findEnumType((EnumMap<?, ?>) value);
				Class<?> valueClass = Object.class;
				str = TypeFactory.defaultInstance().constructMapType(EnumMap.class, enumClass, valueClass).toCanonical();
			} else {
				String end = str.substring(9);
				if ((end.startsWith(".Arrays$") || end.startsWith(".Collections$"))
					&& (str.indexOf("List") >= 0)) {
					str = "java.util.ArrayList";
				}
			}
		} else if (str.indexOf('$') >= 0) {
			Class<?> outer = ClassUtil.getOuterClass(cls);
			if (outer != null) {
				Class<?> staticType = _baseType.getRawClass();
				if (ClassUtil.getOuterClass(staticType) == null) {
					cls = _baseType.getRawClass();
					str = cls.getName();
				}
			}
			if (str.startsWith(REMOVE_PACKAGE_PREFIX)) {
				str = (jsonShortenedType || shortenedTypeOverride ? REPLACE_WITH_PACKAGE_PREFIX : FIX_PACKAGE_PREFIX) + StringUtils.removeStart(str, REMOVE_PACKAGE_PREFIX);
			}
		} else if (str.startsWith(REMOVE_PACKAGE_PREFIX)) {
			str = (jsonShortenedType || shortenedTypeOverride ? REPLACE_WITH_PACKAGE_PREFIX : FIX_PACKAGE_PREFIX) + StringUtils.removeStart(str, REMOVE_PACKAGE_PREFIX);
		}
		return str;
	}

	/**
	 * Resolve type from id.
	 *
	 * @param id the id
	 * @param typeFactory the type factory
	 * @return the java type
	 */
	protected JavaType resolveTypeFromId(String id, TypeFactory typeFactory) {
		if (id.indexOf('<') > 0) {
			JavaType t = typeFactory.constructFromCanonical(id);
			return t;
		}
		id = getCorrectClassName(id);
		try {
			Class<?> cls = typeFactory.findClass(id);
			return typeFactory.constructSpecializedType(_baseType, cls);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Invalid type id '" + id + "' (for id type 'Id.class'): no such class found");
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid type id '" + id + "' (for id type 'Id.class'): " + e.getMessage(), e);
		}
	}

}
