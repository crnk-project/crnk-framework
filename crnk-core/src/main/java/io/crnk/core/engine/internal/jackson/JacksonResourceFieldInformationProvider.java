package io.crnk.core.engine.internal.jackson;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.resource.ResourceFieldInformationProvider;
import io.crnk.core.engine.information.resource.ResourceFieldInformationProviderBase;
import io.crnk.core.engine.internal.information.resource.AnnotatedClassBuilder;
import io.crnk.core.engine.internal.information.resource.AnnotatedFieldBuilder;
import io.crnk.core.engine.internal.information.resource.AnnotatedMethodBuilder;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.resource.annotations.JsonIncludeStrategy;

/**
 * A Jackson-backed implementation of the {@link ResourceFieldInformationProvider} interface.
 *
 * @author Craig Setera, Remo Meier
 */
public class JacksonResourceFieldInformationProvider extends ResourceFieldInformationProviderBase {


	@Override
	public Optional<Boolean> isIgnored(BeanAttributeInformation attributeDesc) {
		Optional<JsonIgnore> ignoreAnnotation = attributeDesc.getAnnotation(JsonIgnore.class);
		if (ignoreAnnotation.isPresent()) {
			return Optional.of(ignoreAnnotation.get().value());
		}
		return Optional.empty();
	}

	@Override
	public Optional<JsonIncludeStrategy> getJsonIncludeStrategy(BeanAttributeInformation attributeDesc) {
		Optional<JsonInclude> includeAnnotation = attributeDesc.getAnnotation(JsonInclude.class);
		if (includeAnnotation.isPresent()) {
			JsonInclude.Include value = includeAnnotation.get().value();
			JsonIncludeStrategy strategy;
			if (NON_NULL.equals(value)) {
				strategy = JsonIncludeStrategy.NOT_NULL;
			}
			else if (JsonInclude.Include.NON_EMPTY.equals(value)) {
				strategy = JsonIncludeStrategy.NON_EMPTY;
			}
			else {
				strategy = JsonIncludeStrategy.DEFAULT;
			}
			return Optional.of(strategy);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> isPostable(BeanAttributeInformation attributeDesc) {
		return isReadOnly(attributeDesc);
	}

	@Override
	public Optional<Boolean> isPatchable(BeanAttributeInformation attributeDesc) {
		return isReadOnly(attributeDesc);
	}

	@Override
	public Optional<Boolean> isReadable(final BeanAttributeInformation attributeDesc) {
		return isAccessible(attributeDesc, JsonProperty.Access.WRITE_ONLY);
	}

	private Optional<Boolean> isReadOnly(BeanAttributeInformation attributeDesc) {
		return isAccessible(attributeDesc, JsonProperty.Access.READ_ONLY);
	}

	private Optional<Boolean> isAccessible(BeanAttributeInformation attributeDesc, JsonProperty.Access accessable) {
		Optional<JsonProperty> annotation = attributeDesc.getAnnotation(JsonProperty.class);
		if (annotation.isPresent()) {
			JsonProperty.Access access = annotation.get().access();
			if (access == JsonProperty.Access.READ_WRITE) {
				return Optional.of(true);
			}
			if (access == accessable) {
				return Optional.of(false);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> getJsonName(BeanAttributeInformation attributeDesc) {
		Optional<JsonProperty> ignoreAnnotation = attributeDesc.getAnnotation(JsonProperty.class);
		if (ignoreAnnotation.isPresent() && !ignoreAnnotation.get().value().isEmpty()) {
			return Optional.of(ignoreAnnotation.get().value());
		}

		Method getter = attributeDesc.getGetter();
		if (getter != null) {
			Optional<String> name = getName(getter);
			if (name.isPresent()) {
				return name;
			}
		}

		Field field = attributeDesc.getField();
		if (field != null) {
			Optional<String> name = getName(field);
			if (name.isPresent()) {
				return name;
			}
		}
		return Optional.empty();
	}

	private static AnnotationMap buildAnnotationMap(Annotation[] declaredAnnotations) {
		AnnotationMap annotationMap = new AnnotationMap();
		for (Annotation annotation : declaredAnnotations) {
			annotationMap.add(annotation);
		}
		return annotationMap;
	}

	protected Optional<String> getName(Field field) {
		ObjectMapper objectMapper = context.getObjectMapper();
		SerializationConfig serializationConfig = objectMapper.getSerializationConfig();

		if (serializationConfig != null && serializationConfig.getPropertyNamingStrategy() != null) {
			AnnotationMap annotationMap = buildAnnotationMap(field.getDeclaredAnnotations());

			AnnotatedClass annotatedClass = AnnotatedClassBuilder.build(field.getDeclaringClass(), serializationConfig);
			AnnotatedField annotatedField = AnnotatedFieldBuilder.build(annotatedClass, field, annotationMap);
			return Optional.of(serializationConfig.getPropertyNamingStrategy().nameForField(serializationConfig, annotatedField, field.getName()));
		}
		return Optional.empty();
	}

	protected Optional<String> getName(Method method) {
		ObjectMapper objectMapper = context.getObjectMapper();
		SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
		if (serializationConfig != null && serializationConfig.getPropertyNamingStrategy() != null) {
			String name = ClassUtils.getGetterFieldName(method);
			Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
			AnnotationMap annotationMap = buildAnnotationMap(declaredAnnotations);

			int paramsLength = method.getParameterAnnotations().length;
			AnnotationMap[] paramAnnotations = new AnnotationMap[paramsLength];
			for (int i = 0; i < paramsLength; i++) {
				AnnotationMap parameterAnnotationMap = buildAnnotationMap(method.getParameterAnnotations()[i]);
				paramAnnotations[i] = parameterAnnotationMap;
			}

			AnnotatedClass annotatedClass = AnnotatedClassBuilder.build(method.getDeclaringClass(), serializationConfig);
			AnnotatedMethod annotatedField = AnnotatedMethodBuilder.build(annotatedClass, method, annotationMap, paramAnnotations);
			return Optional.of(serializationConfig.getPropertyNamingStrategy().nameForGetterMethod(serializationConfig, annotatedField, name));
		}
		return Optional.empty();
	}
}
