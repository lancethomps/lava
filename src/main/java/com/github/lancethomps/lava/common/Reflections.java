package com.github.lancethomps.lava.common;

import static com.github.lancethomps.lava.common.logging.Logs.logError;
import static com.github.lancethomps.lava.common.ser.Serializer.fromJson;
import static com.github.lancethomps.lava.common.ser.Serializer.toJson;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import com.github.lancethomps.lava.common.collections.FastHashMap;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.string.StringUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The Class Reflections.
 *
 * @author lancethomps
 */
public class Reflections {

	/** The Constant FIELD_DISPLAY_PACKAGE_REGEX. */
	public static final Pattern FIELD_DISPLAY_PACKAGE_REGEX = Pattern.compile("([\\w\\.]+\\.(\\w+))+");

	/** The Constant ANNOTATED_CACHE. */
	private static final Map<Pair<Class<?>, Class<?>>, Map<String, Method>> ANNOTATED_CACHE = new FastHashMap<>(true);

	/** The Constant METHODS_CACHE. */
	private static final Map<String, AccessibleObject> INFO_CACHE = new FastHashMap<>(true);

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(Reflections.class);

	/** The Constant MAX_TRIED_SUPERS. */
	private static final int MAX_TRIED_SUPERS = 5;

	/**
	 * Call method unsafe.
	 *
	 * @param <T> the generic type
	 * @param obj the obj
	 * @param name the name
	 * @param args the args
	 * @return the t
	 */
	public static <T> T callMethodUnsafe(Object obj, String name, Object... args) {
		if ((obj != null) && (name != null)) {
			try {
				Method method = BeanUtils.findMethodWithMinimalParameters(obj.getClass(), name);
				if (!method.isAccessible()) {
					method.setAccessible(true);
				}
				return (T) method.invoke(obj, args);
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Issue getting field [%s] on object [%s]", name, obj);
			}
		}
		return null;
	}

	/**
	 * Class from type.
	 *
	 * @param type the type
	 * @return the class
	 */
	public static Class<?> classFromType(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			return (Class<?>) pType.getRawType();
		} else if (type instanceof WildcardType) {
			return Object.class;
		} else {
			return (Class<?>) type;
		}
	}

	/**
	 * Find class loaded from location.
	 *
	 * @param type the type
	 * @return the url
	 */
	public static URL findClassLoadedFromLocation(@Nonnull Class<?> type) {
		return type.getResource('/' + type.getName().replace('.', '/') + ".class");
	}

	/**
	 * Gets the classes in package.
	 *
	 * @param pkg the pkg
	 * @return the classes in package
	 */
	public static Set<Class<?>> getClassesInPackage(String pkg) {
		// Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(pkg)));
		org.reflections.Reflections reflections = new org.reflections.Reflections(pkg);
		return reflections.getSubTypesOf(Object.class);
	}

	/**
	 * Gets the correct args.
	 *
	 * @param method the method
	 * @param possibleArgs the possible args
	 * @return the correct args
	 */
	public static Object[] getCorrectArgs(Method method, Object... possibleArgs) {
		return getCorrectArgsFromList(method, Arrays.asList(possibleArgs));
	}

	/**
	 * Gets the correct args.
	 *
	 * @param method the method
	 * @param possibleArgs the possible args
	 * @return the correct args
	 */
	public static Object[] getCorrectArgsFromList(Method method, List<Object> possibleArgs) {
		List<Object> args = new ArrayList<>();
		for (int pos = 0; pos < method.getParameterCount(); pos++) {
			if ((possibleArgs != null) && (possibleArgs.size() > pos)) {
				args.add(possibleArgs.get(pos));
			} else {
				args.add(null);
			}
		}
		return args.toArray();
	}

	/**
	 * Gets the descriptors of type.
	 *
	 * @param objectClass the object class
	 * @param typeClass the type class
	 * @return the descriptors of type
	 */
	public static List<PropertyDescriptor> getDescriptorsOfType(Class<?> objectClass, Class<?> typeClass) {
		return getDescriptorsOfTypes(objectClass, Lists.newArrayList(typeClass));
	}

	/**
	 * Gets the descriptors of types.
	 *
	 * @param objectClass the object class
	 * @param typeClasses the type classes
	 * @return the descriptors of types
	 */
	public static List<PropertyDescriptor> getDescriptorsOfTypes(Class<?> objectClass, List<Class<?>> typeClasses) {
		return getDescriptorsOfTypes(objectClass, typeClasses, null);
	}

	/**
	 * Gets the descriptors of types.
	 *
	 * @param objectClass the object class
	 * @param typeClasses the type classes
	 * @param ignoreFields the ignore fields
	 * @return the descriptors of types
	 */
	public static List<PropertyDescriptor> getDescriptorsOfTypes(Class<?> objectClass, List<Class<?>> typeClasses, Set<String> ignoreFields) {
		List<PropertyDescriptor> descs = Lists.newArrayList();
		for (PropertyDescriptor d : BeanUtils.getPropertyDescriptors(objectClass)) {
			if ((d.getReadMethod() != null) && ((ignoreFields == null) || !ignoreFields.contains(d.getName()))) {
				for (Class<?> typeClass : typeClasses) {
					if (typeClass.isAssignableFrom(d.getPropertyType())) {
						descs.add(d);
						break;
					}
				}
			}
		}
		return descs;
	}

	/**
	 * Gets the field.
	 *
	 * @param rootClass the root class
	 * @param name the name
	 * @return the field
	 */
	public static Field getField(Class<?> rootClass, String name) {
		String cacheKey = rootClass.getName() + '@' + name;
		if (INFO_CACHE.containsKey(cacheKey)) {
			return (Field) INFO_CACHE.get(cacheKey);
		}
		Field field = FieldUtils.getField(rootClass, name, true);
		if (field != null) {
			INFO_CACHE.put(cacheKey, field);
		}
		return field;
	}

	/**
	 * Gets the field.
	 *
	 * @param clazz the clazz
	 * @param name the name
	 * @return the field
	 */
	public static Field getFieldBespoke(Class<?> clazz, String name) {
		String cacheKey = clazz.getName() + '@' + name;
		if (INFO_CACHE.containsKey(cacheKey)) {
			return (Field) INFO_CACHE.get(cacheKey);
		}
		Field field = null;
		int triedSupers = 0;
		Class<?> tryingClass = clazz;
		while (triedSupers < MAX_TRIED_SUPERS) {
			try {
				field = tryingClass.getDeclaredField(name);
				break;
			} catch (Throwable e) {
				tryingClass = tryingClass.getSuperclass();
				if (tryingClass == null) {
					logError(LOG, e, "Could not find field [%s] on class [%s] and all super classes.", name, clazz);
					return null;
				}
				triedSupers++;
			}
		}
		if (field == null) {
			Logs.logWarn(LOG, "Tried max of [%s] superclasses to class [%s] to find field [%s] and it did not exist!", MAX_TRIED_SUPERS, clazz, name);
		}
		INFO_CACHE.put(cacheKey, field);
		return field;
	}

	/**
	 * Gets the field display.
	 *
	 * @param element the element
	 * @param includeModifiers the include modifiers
	 * @param includeFullType the include full type
	 * @param includeDeclaringClass the include declaring class
	 * @return the field display
	 */
	public static String getFieldDisplay(final AnnotatedElement element, final boolean includeModifiers, final boolean includeFullType, final boolean includeDeclaringClass) {
		return getFieldDisplay(element, includeModifiers, includeFullType, includeDeclaringClass, null);
	}

	/**
	 * Gets the field display.
	 *
	 * @param element the element
	 * @param includeModifiers the include modifiers
	 * @param includeFullType the include full type
	 * @param includeDeclaringClass the include declaring class
	 * @param actualParentClass the actual parent class
	 * @return the field display
	 */
	public static String getFieldDisplay(
		@Nonnull final AnnotatedElement element,
		final boolean includeModifiers,
		final boolean includeFullType,
		final boolean includeDeclaringClass,
		@Nullable Class<?> actualParentClass
	) {
		final Field field = element instanceof Field ? (Field) element : null;
		final Parameter param = element instanceof Parameter ? (Parameter) element : null;
		if ((field == null) && (param == null)) {
			return element.toString();
		}
		int mod = field != null ? field.getModifiers() : param.getModifiers();
		Type fieldType = field != null ? field.getGenericType() : param.getParameterizedType();
		String display = (includeModifiers ? ((mod == 0) ? "" : (Modifier.toString(mod) + ' ')) : EMPTY) + fieldType.getTypeName();
		if (!includeFullType) {
			display = FIELD_DISPLAY_PACKAGE_REGEX.matcher(display).replaceAll("$2");
		}
		if ((actualParentClass != null) && (fieldType instanceof ParameterizedType) && Checks.isNotEmpty(((ParameterizedType) fieldType).getActualTypeArguments())) {
			// TODO: fix this to work for use cases beyond the simple case of replacing List<D> with List<WebUserData>
			ParameterizedType superClassWithParams = getSuperClassWithTypeArguments(actualParentClass);
			if ((superClassWithParams != null) && Checks.isNotEmpty(superClassWithParams.getActualTypeArguments())) {
				ParameterizedType fieldTypeWithParams = (ParameterizedType) fieldType;
				for (Type arg : fieldTypeWithParams.getActualTypeArguments()) {
					if (!(arg instanceof TypeVariable)) {
						continue;
					}
					TypeVariable<?> typedArg = (TypeVariable<?>) arg;
					Class<?> actualType = (Class<?>) superClassWithParams.getActualTypeArguments()[0];
					display = StringUtils.replace(display, String.format("<%s>", typedArg.getName()), String.format("<%s>", actualType.getSimpleName()));
				}
			}
		} else if ((actualParentClass != null) && (fieldType instanceof TypeVariable) && (((TypeVariable<?>) fieldType).getGenericDeclaration() != null)) {
			TypeVariable<?> fieldTypeWithParams = (TypeVariable<?>) fieldType;
			ParameterizedType superClassWithParams = getSuperClassWithTypeArguments(actualParentClass);
			if ((superClassWithParams != null) && Checks.isNotEmpty(superClassWithParams.getActualTypeArguments())) {
				Class<?> actualType = (Class<?>) superClassWithParams.getActualTypeArguments()[0];
				display = actualType.getSimpleName();
			}
		}
		if (includeDeclaringClass) {
			String declaringType = (field != null ? field.getDeclaringClass() : param.getDeclaringExecutable().getDeclaringClass()).getTypeName();
			display += ' ' + declaringType + '.' + (field != null ? field.getName() : param.getName());
		}
		return display;
	}

	/**
	 * Gets the field display.
	 *
	 * @param field the field
	 * @return the field display
	 */
	public static String getFieldDisplay(@Nonnull final Field field) {
		return getFieldDisplay(field, null);
	}

	/**
	 * Gets the field display.
	 *
	 * @param field the field
	 * @param actualParentClass the actual parent class
	 * @return the field display
	 */
	public static String getFieldDisplay(@Nonnull final Field field, @Nullable Class<?> actualParentClass) {
		return getFieldDisplay(field, false, false, false, actualParentClass);
	}

	/**
	 * Gets the field display.
	 *
	 * @param param the param
	 * @return the field display
	 */
	public static String getFieldDisplay(@Nonnull final Parameter param) {
		return getFieldDisplay(param, false, false, false);
	}

	/**
	 * Gets the fields for class.
	 *
	 * @param clazz the clazz
	 * @return the fields for class
	 */
	public static Map<String, JavaType> getFieldsForClass(Class<?> clazz) {
		Map<String, JavaType> fields = new HashMap<>();
		for (Field field : clazz.getDeclaredFields()) {
			if (!Modifier.isStatic(field.getModifiers())) {
				fields.put(field.getName(), Serializer.constructType(field.getGenericType()));
			}
		}
		if (clazz.getSuperclass() != Object.class) {
			Optional.ofNullable(getFieldsForClass(clazz.getSuperclass())).ifPresent(fields::putAll);
		}
		return fields;
	}

	/**
	 * Gets the fields for type.
	 *
	 * @param objectClass the object class
	 * @param fieldTypesAllowed the field types allowed
	 * @return the fields for type
	 */
	public static Set<String> getFieldsForType(Class<?> objectClass, Class<?>... fieldTypesAllowed) {
		List<Class<?>> allowed = Checks.isEmpty(fieldTypesAllowed) ? null : Arrays.asList(fieldTypesAllowed);
		return Stream
			.of(BeanUtils.getPropertyDescriptors(objectClass))
			.filter(d -> d.getReadMethod() != null)
			.filter(d -> Checks.isEmpty(allowed) || isInstanceofType(allowed, d.getPropertyType()))
			.map(PropertyDescriptor::getName)
			.collect(Collectors.toSet());
	}

	/**
	 * Gets the fields with annotation.
	 *
	 * @param clazz the clazz
	 * @param annotationType the annotation type
	 * @return the fields with annotation
	 */
	public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationType) {
		List<Field> fields = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			if (!Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(annotationType)) {
				fields.add(field);
			}
		}
		if (clazz.getSuperclass() != Object.class) {
			Optional.ofNullable(getFieldsWithAnnotation(clazz.getSuperclass(), annotationType)).ifPresent(fields::addAll);
		}
		return fields;
	}

	/**
	 * Gets the field type with resolved generics.
	 *
	 * @param field the field
	 * @param actualParentClass the actual parent class
	 * @return the field type with resolved generics
	 */
	public static Class<?> getFieldTypeWithResolvedGenerics(@Nonnull final Field field, @Nullable Class<?> actualParentClass) {
		Type fieldType = field.getGenericType();
		if ((actualParentClass != null) && (fieldType instanceof TypeVariable) && (((TypeVariable<?>) fieldType).getGenericDeclaration() != null)) {
			TypeVariable<?> fieldTypeWithParams = (TypeVariable<?>) fieldType;
			ParameterizedType superClassWithParams = getSuperClassWithTypeArguments(actualParentClass);
			if ((superClassWithParams != null) && Checks.isNotEmpty(superClassWithParams.getActualTypeArguments())) {
				Class<?> actualType = (Class<?>) superClassWithParams.getActualTypeArguments()[0];
				return actualType;
			}
		}
		return field.getType();
	}

	/**
	 * Gets the field value.
	 *
	 * @param <T> the generic type
	 * @param bean the bean
	 * @param name the name
	 * @param args the args
	 * @return the field value
	 */
	public static <T> T getFieldValue(Object bean, String name, Object... args) {
		return invokeSafely(getGetterForField(bean.getClass(), name), bean, args);
	}

	/**
	 * Gets the field value map from annotated fields.
	 *
	 * @param <T> the generic type
	 * @param <A> the generic type
	 * @param bean the bean
	 * @param annotationType the annotation type
	 * @param keyGetter the key getter
	 * @return the field value map from annotated fields
	 */
	public static <T, A extends Annotation> Map<String, Object> getFieldValueMapFromAnnotatedFields(
		@Nonnull T bean,
		@Nonnull Class<A> annotationType,
		@Nonnull Function<A, String> keyGetter
	) {
		Map<String, Method> methods = ANNOTATED_CACHE.get(Pair.of(bean.getClass(), annotationType));
		if (methods == null) {
			methods = new HashMap<>();
			for (Field field : getFieldsWithAnnotation(bean.getClass(), annotationType)) {
				A ann = field.getAnnotation(annotationType);
				methods.put(Checks.defaultIfBlank(keyGetter.apply(ann), field.getName()), getGetterForField(bean.getClass(), field.getName()));
			}
			ANNOTATED_CACHE.put(Pair.of(bean.getClass(), annotationType), methods);
		}
		Map<String, Object> map = new HashMap<>();
		for (Entry<String, Method> entry : methods.entrySet()) {
			Object val = invokeSafely(entry.getValue(), bean);
			if (val != null) {
				map.put(entry.getKey(), val);
			}
		}
		return map;
	}

	/**
	 * Gets the field value unsafe.
	 *
	 * @param <T> the generic type
	 * @param obj the obj
	 * @param name the name
	 * @return the field value unsafe
	 */
	public static <T> T getFieldValueUnsafe(Object obj, String name) {
		if ((obj != null) && (name != null)) {
			try {
				Field field = getField(obj.getClass(), name);
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				return (T) field.get(obj);
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Issue getting field [%s] on object [%s]", name, obj);
			}
		}
		return null;
	}

	/**
	 * Gets the getter for field.
	 *
	 * @param clazz the clazz
	 * @param name the name
	 * @return the getter for field
	 */
	public static Method getGetterForField(Class<?> clazz, String name) {
		Method method = getMethodForField(clazz, name, "get");
		return method == null ? getMethodForField(clazz, name, "is") : method;
	}

	/**
	 * Gets the list type.
	 *
	 * @param rootClass the root class
	 * @param fieldName the field name
	 * @return the list type
	 */
	public static Class<?> getListType(Class<?> rootClass, String fieldName) {
		return getTypeArgument(rootClass, fieldName, 0);
	}

	/**
	 * Gets the list type.
	 *
	 * @param field the field
	 * @return the list type
	 */
	public static Class<?> getListType(Field field) {
		return getTypeArgument(field, 0, true);
	}

	/**
	 * Gets the map value type.
	 *
	 * @param rootClass the root class
	 * @param fieldName the field name
	 * @return the map value type
	 */
	public static Class<?> getMapValueType(Class<?> rootClass, String fieldName) {
		return getTypeArgument(rootClass, fieldName, 1);
	}

	/**
	 * Gets the method args.
	 *
	 * @param stringArgs the values
	 * @param method the method
	 * @param startPos the start pos
	 * @return the method args
	 * @throws Exception the exception
	 */
	public static List<Object> getMethodArgs(String[] stringArgs, Method method, int startPos) throws Exception {
		List<Object> argsList = null;
		Parameter[] parameters = method.getParameters();
		if ((parameters != null) && (parameters.length > 0)) {
			argsList = Lists.newArrayList();
			for (int pos = startPos; pos < parameters.length; pos++) {
				Parameter param = parameters[pos];
				int stringArgsPos = pos - startPos;
				String val = stringArgs.length > stringArgsPos ? stringArgs[stringArgsPos] : null;
				argsList.add((val == null) || (param.getType() == String.class) ? val : Serializer.fromJson(val, param.getType()));
			}
		}
		return argsList;
	}

	/**
	 * Gets the method for field.
	 *
	 * @param clazz the clazz
	 * @param name the name
	 * @param prefix the prefix
	 * @return the method for field
	 */
	public static Method getMethodForField(Class<?> clazz, String name, String prefix) {
		String methodName = prefix + StringUtils.capitalize(name);
		String cacheKey = clazz.getName() + '@' + methodName;
		if (INFO_CACHE.containsKey(cacheKey)) {
			return (Method) INFO_CACHE.get(cacheKey);
		}
		Method method = BeanUtils.findMethodWithMinimalParameters(clazz, methodName);
		if (method == null) {
			String fixedName = prefix + StringUtils.capitalize(StringUtil.fixCamelCase(name));
			if (!fixedName.equals(methodName)) {
				method = BeanUtils.findMethodWithMinimalParameters(clazz, fixedName);
			}
		}
		INFO_CACHE.put(cacheKey, method);
		return method;
	}

	/**
	 * Gets the setter for field.
	 *
	 * @param clazz the clazz
	 * @param name the name
	 * @return the setter for field
	 */
	public static Method getSetterForField(Class<?> clazz, String name) {
		return getMethodForField(clazz, name, "set");
	}

	/**
	 * Gets the static fields for class.
	 *
	 * @param clazz the clazz
	 * @return the static fields for class
	 */
	public static Map<String, Object> getStaticFieldsForClass(Class<?> clazz) {
		return getStaticFieldsForClass(clazz, Object.class);
	}

	/**
	 * Gets the static fields for class.
	 *
	 * @param <V> the value type
	 * @param clazz the clazz
	 * @param valueClass the value class
	 * @return the static fields for class
	 */
	@SuppressWarnings("unchecked")
	public static <V> Map<String, V> getStaticFieldsForClass(Class<?> clazz, Class<V> valueClass) {
		V defaultObj = null;
		try {
			defaultObj = ClassUtil.createInstance(valueClass, false);
		} catch (Throwable e) {
			;
		}
		V finalDefault = defaultObj;
		Map<String, V> fields = Maps.newTreeMap();
		for (Field f : clazz.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers())) {
				if (!f.isAccessible()) {
					f.setAccessible(true);
				}
				V val = null;
				try {
					val = (V) f.get(null);
				} catch (Throwable e) {
					if (f.getType() == valueClass) {
						val = finalDefault;
					}
				}
				if ((val != null) && valueClass.isAssignableFrom(val.getClass())) {
					fields.put(f.getName(), val);
				}
			}
		}
		if (clazz.getSuperclass() != Object.class) {
			Map<String, V> superFields = getStaticFieldsForClass(clazz.getSuperclass(), valueClass);
			if (Checks.isNotEmpty(superFields)) {
				fields.putAll(superFields);
			}
		}
		return fields;
	}

	/**
	 * Gets the static field value.
	 *
	 * @param <T> the generic type
	 * @param parentClass the parent class
	 * @param name the name
	 * @param type the type
	 * @return the static field value
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getStaticFieldValue(Class<?> parentClass, String name, Class<T> type) {
		try {
			Object val = parentClass.getField(name).get(null);
			if ((val != null) && (type != null) && !type.isAssignableFrom(val.getClass())) {
				return fromJson(toJson(val), type);
			}
			return (T) val;
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue getting static field [%s] on class [%s]", name, parentClass);
		}
		return null;
	}

	/**
	 * Gets the sub types of.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @return the sub types of
	 */
	public static <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
		org.reflections.Reflections reflections = new org.reflections.Reflections();
		return reflections.getSubTypesOf(type);
	}

	/**
	 * Gets the class type argument.
	 *
	 * @param clazz the clazz
	 * @return the class type argument
	 */
	public static ParameterizedType getSuperClassWithTypeArguments(@Nonnull Class<?> clazz) {
		Class<?> testClass = clazz;
		while (testClass != null) {
			Type superClass = testClass.getGenericSuperclass();
			if (superClass instanceof ParameterizedType) {
				return (ParameterizedType) superClass;
			}
			testClass = testClass.getSuperclass();
		}
		return null;
	}

	/**
	 * Gets the type argument.
	 *
	 * @param rootClass the root class
	 * @param fieldName the field name
	 * @param position the position
	 * @return the type argument
	 */
	public static Class<?> getTypeArgument(Class<?> rootClass, String fieldName, int position) {
		return getTypeArgument(rootClass, fieldName, position, true);
	}

	/**
	 * Gets the type argument.
	 *
	 * @param rootClass the root class
	 * @param fieldName the name
	 * @param position the position
	 * @param recurse the recurse
	 * @return the type argument
	 */
	public static Class<?> getTypeArgument(Class<?> rootClass, String fieldName, int position, boolean recurse) {
		return getTypeArgument(getField(rootClass, fieldName), position, recurse);
	}

	/**
	 * Gets the type argument.
	 *
	 * @param field the field
	 * @param position the position
	 * @param recurse the recurse
	 * @return the type argument
	 */
	public static Class<?> getTypeArgument(Field field, int position, boolean recurse) {
		Class<?> fieldClass = null;
		try {
			ParameterizedType fieldType = (field != null) && (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null;
			Type type = fieldType == null ? null : fieldType.getActualTypeArguments()[position];
			return type == null ? null : recurse ? recurseParameterizedType(type) : classFromType(type);
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Error getting type argument in position [%s] for field [%s]!", position, field);
		}
		return fieldClass;
	}

	/**
	 * Invoke safely.
	 *
	 * @param <T> the generic type
	 * @param method the method
	 * @param obj the obj
	 * @param args the args
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invokeSafely(Method method, Object obj, Object... args) {
		try {
			if (method == null) {
				Logs.logError(
					LOG,
					new NullPointerException(),
					"Method to invoke was null for object [%s] with args [%s].",
					obj != null ? ToStringBuilder.reflectionToString(obj, CommonConstants.DEFAULT_TO_STRING_STYLE) : null,
					StringUtils.join(args)
				);
				return null;
			}
			return (T) method.invoke(obj, args);
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Problem when invoked method [%s] on object [%s] with args [%s].", method, obj, StringUtils.join(args));
		}
		return null;
	}

	/**
	 * Checks if is instanceof type.
	 *
	 * @param types the types
	 * @param type the type
	 * @return true, if is instanceof type
	 */
	public static boolean isInstanceofType(Collection<Class<?>> types, Class<?> type) {
		return Checks.isNotEmpty(types) && (type != null) && types.stream().anyMatch(check -> check.isAssignableFrom(type));
	}

	/**
	 * Checks if is list type.
	 *
	 * @param clazz the clazz
	 * @return true, if is list type
	 */
	public static boolean isListType(Class<?> clazz) {
		return Collection.class.isAssignableFrom(clazz);
	}

	/**
	 * Checks if is list type.
	 *
	 * @param obj the obj
	 * @return true, if is list type
	 */
	public static boolean isListType(Object obj) {
		return (obj != null) && Collection.class.isAssignableFrom(obj.getClass());
	}

	/**
	 * Checks if is map type.
	 *
	 * @param clazz the clazz
	 * @return true, if is map type
	 */
	public static boolean isMapType(Class<?> clazz) {
		return Map.class.isAssignableFrom(clazz);
	}

	/**
	 * Checks if is map type.
	 *
	 * @param obj the obj
	 * @return true, if is map type
	 */
	public static boolean isMapType(Object obj) {
		return (obj != null) && Map.class.isAssignableFrom(obj.getClass());
	}

	/**
	 * New instance.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @return the t
	 */
	public static <T> T newInstance(Class<T> type) {
		try {
			return ClassUtil.createInstance(type, false);
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Error creating a new instance of [%s]", type);
			return null;
		}
	}

	/**
	 * Recurse parameterized type.
	 *
	 * @param type the type
	 * @return the class
	 */
	public static Class<?> recurseParameterizedType(Type type) {
		if (type instanceof ParameterizedType) {
			Type[] types = ((ParameterizedType) type).getActualTypeArguments();
			Type lastType = types[types.length - 1];
			return recurseParameterizedType(lastType);
		} else if (type instanceof WildcardType) {
			return Object.class;
		} else if (type instanceof TypeVariable<?>) {
			//((TypeVariable<?>) type).get
			return Object.class;
		} else {
			return (Class<?>) type;
		}
	}

	/**
	 * To camel case.
	 *
	 * @param str the str
	 * @param replace the replace
	 * @return the string
	 */
	public static String toCamelCase(String str, String replace) {
		if (StringUtils.isNotBlank(str)) {
			if (str.contains(replace)) {
				str = str.toLowerCase().replace(replace, " ");
				String start = StringUtils.substringBefore(str, " ");
				String end = StringUtils.substringAfter(str, " ");
				end = WordUtils.capitalizeFully(end);
				str = start + end.replace(" ", "");
				str = str.trim();
			} else {
				str = str.toLowerCase().trim();
			}
		}
		return str;
	}

}
