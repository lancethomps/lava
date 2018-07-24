package com.github.lancethomps.lava.common.web.requests.parsers;

import static com.github.lancethomps.lava.common.Checks.isNotEmpty;
import static com.github.lancethomps.lava.common.ser.Serializer.toJson;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.Enums;
import com.github.lancethomps.lava.common.Exceptions;
import com.github.lancethomps.lava.common.Reflections;
import com.github.lancethomps.lava.common.lambda.Lambdas;
import com.github.lancethomps.lava.common.lambda.ThrowingBiFunction;
import com.github.lancethomps.lava.common.lambda.ThrowingFunction;
import com.github.lancethomps.lava.common.lambda.ThrowingTriFunction;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.string.WordUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.google.common.collect.Lists;

/**
 * The Class RequestFieldInfo.
 *
 * @param <V> the value type
 */
@SuppressWarnings("unchecked")
public class RequestFieldInfo<V> {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(RequestFieldInfo.class);

	/** The annotation. */
	private RequestField annotation;

	/** The custom pojo field. */
	private boolean customPojoField;

	/** The default value function. */
	private ThrowingFunction<Object, V> defaultValueFunction;

	/** The field. */
	private Field field;

	/** The field display. */
	private String fieldDisplay;

	/** The field type. */
	private JavaType fieldType;

	/** The from super class. */
	private boolean fromSuperClass;

	/** The has additional parameter names. */
	private boolean hasAdditionalParameterNames;

	/** The param. */
	private Parameter param;

	/** The param name. */
	private String paramName;

	/** The parent class. */
	private Class<?> parentClass;

	/** The parser. */
	private RequestParameterParser<V> parser;

	/** The post process function. */
	private ThrowingBiFunction<Map<String, String[]>, Object, ?> postProcessFunction;

	/** The prefix. */
	private List<String> prefixes;

	/** The process function. */
	private ThrowingBiFunction<Map<String, String[]>, String, V> processFunction;

	/** The process function with prefix. */
	private ThrowingTriFunction<Map<String, String[]>, String, String, V> processFunctionWithPrefix;

	/** The setter method. */
	private Method setterMethod;

	/** The type. */
	private Class<V> type;

	/** The validator. */
	private RequestParameterValidator<V> validator;

	/** The valid field. */
	private boolean validField = true;

	/**
	 * Instantiates a new request field info.
	 *
	 * @param parentClass the parent class
	 */
	public RequestFieldInfo(Class<?> parentClass) {
		this.parentClass = parentClass;
		annotation = parentClass.getAnnotation(RequestField.class);
		prefixes = Checks.isNotBlank(annotation.prefix()) ? Lists.newArrayList(annotation.prefix()) : null;
		if (annotation.postProcess()) {
			postProcessFunction = (ThrowingBiFunction<Map<String, String[]>, Object, ?>) RequestFactory.getPostProcessMethod(parentClass);
		}
	}

	/**
	 * Instantiates a new request field info.
	 *
	 * @param parentClass the parent class
	 * @param member the member
	 * @param fromSuperClass the from super class
	 */
	public RequestFieldInfo(Class<?> parentClass, AccessibleObject member, boolean fromSuperClass) {
		this(parentClass, member, fromSuperClass, member.getAnnotation(RequestField.class));
	}

	/**
	 * Instantiates a new request field info.
	 *
	 * @param parentClass the parent class
	 * @param member the member
	 * @param fromSuperClass the from super class
	 * @param annotation the annotation
	 */
	public RequestFieldInfo(Class<?> parentClass, AccessibleObject member, boolean fromSuperClass, RequestField annotation) {
		this.parentClass = parentClass;
		this.fromSuperClass = fromSuperClass;
		if (member instanceof java.lang.reflect.Field) {
			field = (java.lang.reflect.Field) member;
		}
		this.annotation = annotation;
		type = (Class<V>) Reflections.getFieldTypeWithResolvedGenerics(field, parentClass);
		fieldDisplay = Reflections.getFieldDisplay(field, parentClass);
		finishInit(field.getName());
	}

	/**
	 * Instantiates a new request field info.
	 *
	 * @param param the param
	 * @param annotation the annotation
	 */
	public RequestFieldInfo(Parameter param, RequestField annotation) {
		this.param = param;
		this.annotation = annotation;
		type = (Class<V>) param.getType();
		fieldDisplay = Reflections.getFieldDisplay(param);
		finishInit(param.getName());
	}

	/**
	 * @return the annotation
	 */
	public RequestField getAnnotation() {
		return annotation;
	}

	/**
	 * @return the field
	 */
	public Field getField() {
		return field;
	}

	/**
	 * @return the fieldDisplay
	 */
	public String getFieldDisplay() {
		return fieldDisplay;
	}

	/**
	 * @return the fieldType
	 */
	public JavaType getFieldType() {
		return fieldType;
	}

	/**
	 * Gets the param names.
	 *
	 * @return the param names
	 */
	public List<String> getParamNames() {
		List<String> paramNames = new ArrayList<>();
		if (customPojoField) {
			return paramNames;
		}
		Lambdas.functionIfNonNull(paramName, paramNames::add);
		Lambdas.functionIfNonNull(hasAdditionalParameterNames ? Arrays.asList(annotation.additionalParameterNames()) : null, paramNames::addAll);
		Lambdas.functionIfTrue(paramNames.stream().map(WordUtil::getSingularVersionOfWord).filter(Objects::nonNull).collect(Collectors.toList()), Checks::isNotEmpty, paramNames::addAll);
		return isNotEmpty(prefixes) && isNotEmpty(paramNames) ? paramNames.stream().flatMap(name -> prefixes.stream().map(prefix -> prefix.concat(name))).collect(Collectors.toList())
			: paramNames;
	}

	/**
	 * @return the prefixes
	 */
	public List<String> getPrefixes() {
		return prefixes;
	}

	/**
	 * @return the processFunction
	 */
	public ThrowingBiFunction<Map<String, String[]>, String, ?> getProcessFunction() {
		return processFunction;
	}

	/**
	 * @return the processFunctionWithPrefix
	 */
	public ThrowingTriFunction<Map<String, String[]>, String, String, V> getProcessFunctionWithPrefix() {
		return processFunctionWithPrefix;
	}

	/**
	 * @return the type
	 */
	public Class<V> getType() {
		return type;
	}

	/**
	 * Gets the val.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param req the req
	 * @return the val
	 */
	public <T> T getVal(Object request, Map<String, String[]> req) {
		return getVal(request, req, null);
	}

	/**
	 * Gets the val.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param req the req
	 * @param prefix the prefix
	 * @return the val
	 */
	public <T> T getVal(Object request, Map<String, String[]> req, String prefix) {
		try {
			T val;
			if (processFunctionWithPrefix != null) {
				val = (T) processFunctionWithPrefix.apply(req, paramName, prefix);
			} else {
				String fullParamName = prefix == null ? paramName : (prefix + paramName);
				val = (T) processFunction.apply(req, fullParamName);
			}
			if ((val == null) && (defaultValueFunction != null)) {
				val = (T) defaultValueFunction.apply(request);
			}
			if (hasAdditionalParameterNames && (val == null)) {
				for (String paramKey : annotation.additionalParameterNames()) {
					if (processFunctionWithPrefix != null) {
						val = (T) processFunctionWithPrefix.apply(req, paramKey, prefix);
					} else {
						String fullParamKey = prefix == null ? paramKey : (prefix + paramKey);
						val = (T) processFunction.apply(req, fullParamKey);
					}
					if (val != null) {
						break;
					}
				}
			}
			return val;
		} catch (Throwable e) {
			Logs.logWarn(LOG, e, "Issue processing param [%s] for request [%s], parameters [%s] and RequestFieldInfo [%s]!", paramName, request, toJson(req), this);
			return Exceptions.sneakyThrow(e);
		}
	}

	/**
	 * @return the validator
	 */
	public RequestParameterValidator<V> getValidator() {
		return validator;
	}

	/**
	 * Checks if is custom pojo field.
	 *
	 * @return true, if is custom pojo field
	 */
	public boolean isCustomPojoField() {
		return customPojoField;
	}

	/**
	 * @return the fromSuperClass
	 */
	public boolean isFromSuperClass() {
		return fromSuperClass;
	}

	/**
	 * @return the hasAdditionalParameterNames
	 */
	public boolean isHasAdditionalParameterNames() {
		return hasAdditionalParameterNames;
	}

	/**
	 * @return the validField
	 */
	public boolean isValidField() {
		return validField;
	}

	/**
	 * Post process.
	 *
	 * @param <T> the generic type
	 * @param req the req
	 * @param request the request
	 * @return the t
	 */
	public <T extends Object> T postProcess(Map<String, String[]> req, T request) {
		if (postProcessFunction != null) {
			try {
				request = (T) postProcessFunction.apply(req, request);
			} catch (Throwable e) {
				Logs.logWarn(LOG, e, "Issue post processing request [%s] for map [%s] and RequestFieldInfo [%s]!", request, toJson(req), this);
			}
		}
		return request;
	}

	/**
	 * Process.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param req the req
	 */
	public <T> void process(T request, Map<String, String[]> req) {
		process(request, req, null);
	}

	/**
	 * Process.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param req the req
	 * @param prefix the prefix
	 */
	public <T> void process(T request, Map<String, String[]> req, String prefix) {
		Object val = getVal(request, req, prefix);
		try {
			setterMethod.invoke(request, val);
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue setting value [%s] for method [%s] and request field [%s] within [%s] for request [%s]!", val, setterMethod, field, parentClass, request);
		}
	}

	/**
	 * @param prefixes the prefixes to set
	 */
	public void setPrefixes(List<String> prefixes) {
		this.prefixes = prefixes;
	}

	@Override
	public String toString() {
		return Serializer.toLogString(this);
	}

	/**
	 * Check if custom pojo field.
	 *
	 * @return true, if successful
	 */
	private boolean checkIfCustomPojoField() {
		return type.getName().startsWith("com.github.lancethomps.lava");
	}

	/**
	 * Derive process method.
	 */
	@SuppressWarnings("rawtypes")
	private void deriveProcessMethod() {
		try {
			if (parser != null) {
				processFunction = (request, paramName) -> parser.process(this, request, paramName);
				processFunctionWithPrefix = (request, paramName, prefix) -> parser.process(this, request, paramName, prefix);
				return;
			}
			if (fieldDisplay != null) {
				processFunction = (ThrowingBiFunction<Map<String, String[]>, String, V>) RequestFactory.getProcessMethod(fieldDisplay);
				if (processFunction != null) {
					Logs.logDebug(LOG, "Found @RequestField field processing function via field display: fieldDisplay=%s", fieldDisplay);
					return;
				}
				if (!type.isEnum() && !checkIfCustomPojoField()) {
					Logs.logInfo(LOG, "Missing @RequestField field processing function via field display: fieldDisplay=%s", fieldDisplay);
				}
			}
			if (type.isEnum()) {
				processFunction = (request, paramName) -> (V) RequestFactory.getEnumParam(request, paramName, (Class<? extends Enum>) type);
			} else if (Collection.class.isAssignableFrom(type)) {
				ParameterizedType parameterizedType = (ParameterizedType) (field == null ? param.getParameterizedType() : field.getGenericType());
				Type innerType = parameterizedType.getActualTypeArguments()[0];
				Class<?> listType = innerType instanceof ParameterizedType ? (Class<?>) ((ParameterizedType) innerType).getRawType() : (Class<?>) innerType;
				Class<? extends Collection> collType = List.class.isAssignableFrom(type) ? ArrayList.class
					: Set.class.isAssignableFrom(type) ? HashSet.class : (Class<? extends Collection>) type;
				if (listType.isEnum()) {
					ThrowingFunction elementSupplier = str -> Enums.fromString((Class<? extends Enum>) listType, (String) str);
					processFunction = (request, paramName) -> (V) RequestFactory.getCollectionParam(request, paramName, collType, listType, elementSupplier);
				} else {
					Logs.logWarn(LOG, "No process method found for [%s] - field [%s] using generic collection processor.", fieldDisplay, field);
					ThrowingFunction elementSupplier = str -> Serializer.fromJson((String) str, listType);
					processFunction = (request, paramName) -> (V) RequestFactory.getCollectionParam(request, paramName, collType, listType, elementSupplier);
				}
			} else if (Map.class.isAssignableFrom(type)) {
				Logs.logWarn(LOG, "No process method found for [%s] - field [%s] - using generic map processor.", fieldDisplay, field);
				Class<?> mapValueType = field == null ? (Class<?>) ((ParameterizedType) param.getParameterizedType()).getActualTypeArguments()[0]
					: Reflections.getTypeArgument(
						parentClass,
						field.getName(),
						1,
						false
					);
				if (mapValueType != Object.class) {
					processFunction = (request, paramName) -> (V) RequestFactory.getJsonMapParam(request, paramName, null, mapValueType);
				} else {
					processFunction = (request, paramName) -> (V) RequestFactory.getMapParam(request, paramName, null);
				}
			} else if (checkIfCustomPojoField()) {
				customPojoField = true;
				boolean requireAnnotation = (field != null) && field.isAnnotationPresent(RequestField.class);
				Constructor<?>[] constructors = type.getConstructors();
				if ((constructors == null) || (constructors.length == 0) || !Stream.of(constructors).anyMatch(c -> c.getParameterCount() == 0)) {
					return;
				}
				String annPrefix = (annotation.prefixOrJson() || annotation.prefixOnly()) && Checks.isBlank(annotation.prefix()) ? (field.getName() + '.')
					: Checks.defaultIfBlank(annotation.prefix(), (String) null);
				processFunction = (request, paramName) -> {
					if (!(annotation.prefixOrJson() && request.containsKey(paramName)) && !shouldParseRequestBeanField(request, annPrefix)) {
						return null;
					}
					return RequestFactory.createBeanFromRequest(
						null,
						type,
						request,
						true,
						requireAnnotation,
						annotation.prefixOnly() ? null : paramName,
						annPrefix,
						this
					);
				};
				processFunctionWithPrefix = (request, paramName, prefix) -> {
					String paramNameWithPrefix = Checks.defaultIfNull(prefix, "") + paramName;
					String fullPrefix;
					if (Checks.isBlank(prefix) && Checks.isBlank(annPrefix)) {
						fullPrefix = null;
					} else {
						fullPrefix = Checks.defaultIfNull(prefix, "") + Checks.defaultIfNull(annPrefix, "");
					}
					if (!(annotation.prefixOrJson() && request.containsKey(paramNameWithPrefix)) && !shouldParseRequestBeanField(request, fullPrefix)) {
						return null;
					}
					return RequestFactory.createBeanFromRequest(null, type, request, true, requireAnnotation, annotation.prefixOnly() ? null : paramNameWithPrefix, fullPrefix, this);
				};
			} else {
				Logs.logWarn(LOG, "No process method found for [%s] - field [%s].", fieldDisplay, field);
				processFunction = (request, paramName) -> Lambdas.functionIfNonNull(RequestFactory.getRequestParam(request, paramName), val -> Serializer.parseString(val, type)).orElse(null);
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue processing field [%s] for parentClass [%s]!", field, parentClass);
		}
	}

	/**
	 * Derive validation method.
	 */
	private void deriveValidationMethod() {
		try {
			if (annotation.validateUsing() != NoOpRequestParameterValidator.class) {
				validator = ClassUtil.createInstance(annotation.validateUsing(), true);
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue deriving validation type [%s] for field [%s] in parentClass [%s]!", annotation.validateUsing(), field, parentClass);
		}
	}

	/**
	 * Finish init.
	 *
	 * @param name the name
	 */
	private void finishInit(String name) {
		if (field != null) {
			fieldType = Serializer.constructType(field.getGenericType());
		}
		paramName = annotation.value().equals(RequestField.DEFAULT) ? name : annotation.value();
		hasAdditionalParameterNames = (annotation.additionalParameterNames() != null) && (annotation.additionalParameterNames().length > 0)
			&& isNotBlank(annotation.additionalParameterNames()[0]);
		if (parentClass != null) {
			defaultValueFunction = field != null ? (obj) -> (V) field.get(obj) : (obj) -> Reflections.invokeSafely(Reflections.getGetterForField(parentClass, name), obj);
			setterMethod = Reflections.getSetterForField(parentClass, name);
			if (setterMethod != null) {
				setterMethod.setAccessible(true);
			} else {
				validField = false;
				return;
			}
		}
		if (annotation.processUsing() != DefaultRequestParameterParser.class) {
			try {
				parser = ClassUtil.createInstance(annotation.processUsing(), true);
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Issue creating RequestParameterParser instance of type [%s]", annotation.processUsing());
			}
		}
		deriveProcessMethod();
		if (annotation.validateField()) {
			deriveValidationMethod();
		}
	}

	/**
	 * Should parse request bean field.
	 *
	 * @param request the request
	 * @param prefix the prefix
	 * @return true, if successful
	 */
	private boolean shouldParseRequestBeanField(Map<String, String[]> request, String prefix) {
		if (annotation.prefixOnly() || annotation.prefixOrJson()) {
			return request.keySet().stream().anyMatch(key -> StringUtils.startsWith(key, prefix));
		}
		return true;
	}

}
