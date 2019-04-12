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
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MemberUsageScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterNamesScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.scanners.TypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.github.lancethomps.lava.common.collections.FastHashMap;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.string.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Reflections {

  public static final Pattern FIELD_DISPLAY_PACKAGE_REGEX = Pattern.compile("([\\w\\.]+\\.(\\w+))+");

  private static final Map<Pair<Class<?>, Class<?>>, Map<String, Method>> ANNOTATED_CACHE = new FastHashMap<>(true);

  private static final Map<String, AccessibleObject> INFO_CACHE = new FastHashMap<>(true);

  private static final Logger LOG = Logger.getLogger(Reflections.class);

  private static final int MAX_TRIED_SUPERS = 5;

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

  public static org.reflections.Reflections createFullReflectionsInstance(String... packages) {
    ConfigurationBuilder builder = new ConfigurationBuilder()
      .addScanners(
        new FieldAnnotationsScanner(),
        new MemberUsageScanner(),
        new MethodAnnotationsScanner(),
        new MethodParameterNamesScanner(),
        new MethodParameterScanner(),
        new ResourcesScanner(),
        new SubTypesScanner(),
        new TypeAnnotationsScanner(),
        new TypeElementsScanner(),
        new TypesScanner()
      );
    if (Checks.isNotEmpty(packages)) {
      for (String pack : packages) {
        builder.addUrls(ClasspathHelper.forPackage(pack));
      }
    } else {
      builder.addUrls(ClasspathHelper.forClassLoader());
    }
    return new org.reflections.Reflections(builder);
  }

  public static URL findClassLoadedFromLocation(@Nonnull Class<?> type) {
    return type.getResource('/' + type.getName().replace('.', '/') + ".class");
  }

  public static Set<Class<?>> getClassesInPackage(String pkg) {

    org.reflections.Reflections reflections = new org.reflections.Reflections(pkg);
    return reflections.getSubTypesOf(Object.class);
  }

  public static Object[] getCorrectArgs(Method method, Object... possibleArgs) {
    return getCorrectArgsFromList(method, Arrays.asList(possibleArgs));
  }

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

  public static List<PropertyDescriptor> getDescriptorsOfType(Class<?> objectClass, Class<?> typeClass) {
    return getDescriptorsOfTypes(objectClass, Lists.newArrayList(typeClass));
  }

  public static List<PropertyDescriptor> getDescriptorsOfTypes(Class<?> objectClass, List<Class<?>> typeClasses) {
    return getDescriptorsOfTypes(objectClass, typeClasses, null);
  }

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

  public static String getFieldDisplay(
    final AnnotatedElement element,
    final boolean includeModifiers,
    final boolean includeFullType,
    final boolean includeDeclaringClass
  ) {
    return getFieldDisplay(element, includeModifiers, includeFullType, includeDeclaringClass, null);
  }

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
    if ((actualParentClass != null) && (fieldType instanceof ParameterizedType) &&
      Checks.isNotEmpty(((ParameterizedType) fieldType).getActualTypeArguments())) {
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
    } else if ((actualParentClass != null) && (fieldType instanceof TypeVariable) &&
      (((TypeVariable<?>) fieldType).getGenericDeclaration() != null)) {
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

  public static String getFieldDisplay(@Nonnull final Field field) {
    return getFieldDisplay(field, null);
  }

  public static String getFieldDisplay(@Nonnull final Field field, @Nullable Class<?> actualParentClass) {
    return getFieldDisplay(field, false, false, false, actualParentClass);
  }

  public static String getFieldDisplay(@Nonnull final Parameter param) {
    return getFieldDisplay(param, false, false, false);
  }

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

  public static <T> T getFieldValue(Object bean, String name, Object... args) {
    return invokeSafely(getGetterForField(bean.getClass(), name), bean, args);
  }

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

  public static Set<String> getFieldsForType(Class<?> objectClass, Class<?>... fieldTypesAllowed) {
    List<Class<?>> allowed = Checks.isEmpty(fieldTypesAllowed) ? null : Arrays.asList(fieldTypesAllowed);
    return Stream
      .of(BeanUtils.getPropertyDescriptors(objectClass))
      .filter(d -> d.getReadMethod() != null)
      .filter(d -> Checks.isEmpty(allowed) || isInstanceofType(allowed, d.getPropertyType()))
      .map(PropertyDescriptor::getName)
      .collect(Collectors.toSet());
  }

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

  public static Method getGetterForField(Class<?> clazz, String name) {
    Method method = getMethodForField(clazz, name, "get");
    return method == null ? getMethodForField(clazz, name, "is") : method;
  }

  public static Class<?> getListType(Class<?> rootClass, String fieldName) {
    return getTypeArgument(rootClass, fieldName, 0);
  }

  public static Class<?> getListType(Field field) {
    return getTypeArgument(field, 0, true);
  }

  public static Class<?> getMapValueType(Class<?> rootClass, String fieldName) {
    return getTypeArgument(rootClass, fieldName, 1);
  }

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

  public static Method getSetterForField(Class<?> clazz, String name) {
    return getMethodForField(clazz, name, "set");
  }

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

  public static Map<String, Object> getStaticFieldsForClass(Class<?> clazz) {
    return getStaticFieldsForClass(clazz, Object.class);
  }

  @SuppressWarnings("unchecked")
  public static <V> Map<String, V> getStaticFieldsForClass(Class<?> clazz, Class<V> valueClass) {
    V defaultObj = null;
    try {
      defaultObj = ClassUtil.createInstance(valueClass, false);
    } catch (Throwable e) {
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

  public static <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
    org.reflections.Reflections reflections = new org.reflections.Reflections();
    return reflections.getSubTypesOf(type);
  }

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

  public static Class<?> getTypeArgument(Class<?> rootClass, String fieldName, int position) {
    return getTypeArgument(rootClass, fieldName, position, true);
  }

  public static Class<?> getTypeArgument(Class<?> rootClass, String fieldName, int position, boolean recurse) {
    return getTypeArgument(getField(rootClass, fieldName), position, recurse);
  }

  public static Class<?> getTypeArgument(Field field, int position, boolean recurse) {
    Class<?> fieldClass = null;
    try {
      ParameterizedType fieldType =
        (field != null) && (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null;
      Type type = fieldType == null ? null : fieldType.getActualTypeArguments()[position];
      return type == null ? null : recurse ? recurseParameterizedType(type) : classFromType(type);
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Error getting type argument in position [%s] for field [%s]!", position, field);
    }
    return fieldClass;
  }

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

  public static boolean isInstanceofType(Collection<Class<?>> types, Class<?> type) {
    return Checks.isNotEmpty(types) && (type != null) && types.stream().anyMatch(check -> check.isAssignableFrom(type));
  }

  public static boolean isListType(Class<?> clazz) {
    return Collection.class.isAssignableFrom(clazz);
  }

  public static boolean isListType(Object obj) {
    return (obj != null) && Collection.class.isAssignableFrom(obj.getClass());
  }

  public static boolean isMapType(Class<?> clazz) {
    return Map.class.isAssignableFrom(clazz);
  }

  public static boolean isMapType(Object obj) {
    return (obj != null) && Map.class.isAssignableFrom(obj.getClass());
  }

  public static <T> T newInstance(Class<T> type) {
    try {
      return ClassUtil.createInstance(type, false);
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Error creating a new instance of [%s]", type);
      return null;
    }
  }

  public static Class<?> recurseParameterizedType(Type type) {
    if (type instanceof ParameterizedType) {
      Type[] types = ((ParameterizedType) type).getActualTypeArguments();
      Type lastType = types[types.length - 1];
      return recurseParameterizedType(lastType);
    } else if (type instanceof WildcardType) {
      return Object.class;
    } else if (type instanceof TypeVariable<?>) {

      return Object.class;
    } else {
      return (Class<?>) type;
    }
  }

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
