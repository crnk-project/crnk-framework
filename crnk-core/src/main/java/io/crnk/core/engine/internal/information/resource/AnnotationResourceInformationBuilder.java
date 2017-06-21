package io.crnk.core.engine.internal.information.resource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.information.resource.ResourceInformationBuilderContext;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.FieldOrderedComparator;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.exception.RepositoryAnnotationNotFoundException;
import io.crnk.core.exception.ResourceIdNotFoundException;
import io.crnk.core.resource.annotations.JsonApiField;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiIncludeByDefault;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToMany;
import io.crnk.core.resource.annotations.JsonApiToOne;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.utils.Optional;

/**
 * A builder which creates ResourceInformation instances of a specific class. It
 * extracts information about a resource from annotations and information about
 * fields and getters.
 */
public class AnnotationResourceInformationBuilder implements ResourceInformationBuilder {

	private final ResourceFieldNameTransformer resourceFieldNameTransformer;
	private ResourceInformationBuilderContext context;

	public AnnotationResourceInformationBuilder(ResourceFieldNameTransformer resourceFieldNameTransformer) {
		this.resourceFieldNameTransformer = resourceFieldNameTransformer;
	}

	public static String getResourceType(Type genericType, ResourceInformationBuilderContext context) {
		Type elementType = genericType;
		if (Iterable.class.isAssignableFrom(ClassUtils.getRawType(genericType))) {
			elementType = ClassUtils.getRawType(((ParameterizedType) genericType).getActualTypeArguments()[0]);
		}
		Class<?> rawType = ClassUtils.getRawType(elementType);
		return context.accept(rawType) ? context.getResourceType(rawType) : null;
	}

	private static AnnotatedResourceField mergeAnnotations(AnnotatedResourceField fromField, AnnotatedResourceField fromMethod, ResourceInformationBuilderContext context) {
		List<Annotation> annotations = new ArrayList<>(fromField.getAnnotations());
		annotations.addAll(fromMethod.getAnnotations());

		Class<?> fieldType = mergeFieldType(fromField, fromMethod);
		Type fieldGenericType = mergeGenericType(fromField, fromMethod);
		String oppositeResourceType = fromField.getResourceFieldType() == ResourceFieldType.RELATIONSHIP ? getResourceType(fieldGenericType, context) : null;

		ResourceFieldAccess mergedAccess = fromField.getAccess().and(fromMethod.getAccess());
		return new AnnotatedResourceField(fromField.getJsonName(), fromField.getUnderlyingName(), fieldType, fieldGenericType, oppositeResourceType, annotations, mergedAccess);
	}

	private static Class<?> mergeFieldType(AnnotatedResourceField fromField, AnnotatedResourceField fromMethod) {
		if (hasJsonApiAnnotation(fromField.getAnnotations())) {
			return fromField.getType();
		} else {
			return fromMethod.getType();
		}
	}

	private static Type mergeGenericType(AnnotatedResourceField fromField, AnnotatedResourceField fromMethod) {
		if (hasJsonApiAnnotation(fromField.getAnnotations())) {
			return fromField.getGenericType();
		} else {
			return fromMethod.getGenericType();
		}
	}

	private static boolean hasJsonApiAnnotation(List<Annotation> annotations) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType() == JsonApiId.class
					|| annotation.annotationType() == JsonApiRelation.class
					|| annotation.annotationType() == JsonApiToOne.class
					|| annotation.annotationType() == JsonApiField.class
					|| annotation.annotationType() == JsonApiToMany.class
					|| annotation.annotationType() == JsonApiMetaInformation.class
					|| annotation.annotationType() == JsonApiLinksInformation.class) {
				return true;
			}
		}
		return false;
	}

	public static ResourceFieldAccess getResourceFieldAccess(ResourceFieldType resourceFieldType, boolean hasSetter, Collection<Annotation> annotations) {
		boolean postable = hasSetter;
		boolean patchable = hasSetter && resourceFieldType != ResourceFieldType.ID;
		boolean sortable = true;
		boolean filterable = true;

		JsonApiField fieldAnnotation = AnnotatedResourceField.getFieldAnnotation(annotations);
		JsonProperty jsonProperty = AnnotatedResourceField.getJsonPropertyAnnotation(annotations);
		if (fieldAnnotation != null) {
			postable = fieldAnnotation.postable();
			patchable = fieldAnnotation.patchable();
			sortable = fieldAnnotation.sortable();
			filterable = fieldAnnotation.filterable();
		} else if (jsonProperty != null) {
			JsonProperty.Access access = jsonProperty.access();
			switch (access) {
				case READ_WRITE:
					postable = true;
					patchable = true;
					break;
				case AUTO:
					// nothing to do
					break;
				case READ_ONLY:
					postable = false;
					patchable = false;
					break;
				case WRITE_ONLY:
					// probably makes not that much sense
					throw new IllegalStateException("WRITE_ONLY policy not (yet) supported");
				default:
					throw new IllegalStateException("unknown access policy " + access);
			}
		}
		return new ResourceFieldAccess(postable, patchable, sortable, filterable);
	}

	public static boolean hasSetter(Class<?> resourceClass, String underlyingName) {
		Field field = ClassUtils.findClassField(resourceClass, underlyingName);
		Class<?> type = PropertyUtils.getPropertyClass(resourceClass, underlyingName);
		Method setter = ClassUtils.findSetter(resourceClass, underlyingName, type);
		return setter != null || field != null && Modifier.isPublic(field.getModifiers());
	}

	@Override
	public boolean accept(Class<?> resourceClass) {
		return resourceClass.getAnnotation(JsonApiResource.class) != null;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public ResourceInformation build(Class<?> resourceClass) {
		return build(resourceClass, false);
	}

	public ResourceInformation build(Class<?> resourceClass, boolean allowNonResourceBaseClass) {
		List<AnnotatedResourceField> resourceFields = getResourceFields(resourceClass);

		String resourceType = getResourceType(resourceClass, allowNonResourceBaseClass);

		Optional<JsonPropertyOrder> propertyOrder = ClassUtils.getAnnotation(resourceClass, JsonPropertyOrder.class);
		if (propertyOrder.isPresent()) {
			JsonPropertyOrder propertyOrderAnnotation = propertyOrder.get();
			Collections.sort(resourceFields, new FieldOrderedComparator(propertyOrderAnnotation.value(), propertyOrderAnnotation.alphabetic()));
		}

		DefaultResourceInstanceBuilder<?> instanceBuilder = new DefaultResourceInstanceBuilder(resourceClass);

		Class<?> superclass = resourceClass.getSuperclass();
		String superResourceType = superclass != Object.class && context.accept(superclass) ? context.getResourceType(superclass) : null;

		ResourceInformation information = new ResourceInformation(context.getTypeParser(), resourceClass, resourceType, superResourceType, instanceBuilder, (List) resourceFields);
		if (!allowNonResourceBaseClass && information.getIdField() == null) {
			throw new ResourceIdNotFoundException(resourceClass.getCanonicalName());
		}
		return information;
	}

	@Override
	public String getResourceType(Class<?> resourceClass) {
		return getResourceType(resourceClass, false);
	}

	private String getResourceType(Class<?> resourceClass, boolean allowNonResourceBaseClass) {
		Annotation[] annotations = resourceClass.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof JsonApiResource) {
				JsonApiResource apiResource = (JsonApiResource) annotation;
				return apiResource.value();
			}
		}
		if (allowNonResourceBaseClass) {
			return null;
		}
		// won't reach this
		throw new RepositoryAnnotationNotFoundException(resourceClass.getName());
	}

	protected List<AnnotatedResourceField> getResourceFields(Class<?> resourceClass) {
		List<Field> classFields = ClassUtils.getClassFields(resourceClass);
		List<Method> classGetters = ClassUtils.getClassGetters(resourceClass);

		List<ResourceFieldWrapper> resourceClassFields = getFieldResourceFields(resourceClass, classFields);
		List<ResourceFieldWrapper> resourceGetterFields = getGetterResourceFields(resourceClass, classGetters);
		return getResourceFields(resourceClassFields, resourceGetterFields);
	}

	private List<ResourceFieldWrapper> getFieldResourceFields(Class<?> resourceClass, List<Field> classFields) {
		List<ResourceFieldWrapper> fieldWrappers = new ArrayList<>(classFields.size());
		for (Field field : classFields) {
			String jsonName = resourceFieldNameTransformer.getName(field);
			String underlyingName = field.getName();
			fieldWrappers.add(getResourceField(resourceClass, field, underlyingName, jsonName, field.getType(), field.getGenericType(), Arrays.asList(field.getAnnotations())));
		}
		return fieldWrappers;
	}

	private List<ResourceFieldWrapper> getGetterResourceFields(Class<?> resourceClass, List<Method> classGetters) {
		List<ResourceFieldWrapper> fieldWrappers = new ArrayList<>(classGetters.size());
		for (Method getter : classGetters) {
			String underlyingName = ClassUtils.getGetterFieldName(getter);
			if (underlyingName == null) {
				continue;
			}
			String jsonName = resourceFieldNameTransformer.getName(getter);
			fieldWrappers.add(getResourceField(resourceClass, getter, jsonName, underlyingName, getter.getReturnType(), getter.getGenericReturnType(), Arrays.asList(getter.getAnnotations())));
		}
		return fieldWrappers;
	}

	private ResourceFieldWrapper getResourceField(Class<?> resourceClass, Member member, String underlyingName, String jsonName, Class<?> type, Type genericType, List<Annotation> annotations) {
		ResourceFieldType resourceFieldType = AnnotatedResourceField.getResourceFieldType(annotations);
		String oppositeResourceType = resourceFieldType == ResourceFieldType.RELATIONSHIP ? getResourceType(genericType, context) : null;

		boolean hasSetter = hasSetter(resourceClass, underlyingName);

		ResourceFieldAccess access = getResourceFieldAccess(resourceFieldType, hasSetter, annotations);
		AnnotatedResourceField resourceField = new AnnotatedResourceField(jsonName, underlyingName, type, genericType, oppositeResourceType, annotations, access);
		if (Modifier.isTransient(member.getModifiers()) || Modifier.isStatic(member.getModifiers())) {
			return new ResourceFieldWrapper(resourceField, true);
		} else {
			return new ResourceFieldWrapper(resourceField, false);
		}
	}

	private List<AnnotatedResourceField> getResourceFields(List<ResourceFieldWrapper> resourceClassFields, List<ResourceFieldWrapper> resourceGetterFields) {
		Map<String, Integer> resourceFieldPositions = new HashMap<>();
		List<AnnotatedResourceField> resourceFields = new ArrayList<>();

		HashSet<String> discardedFieldNames = new HashSet<>();

		for (ResourceFieldWrapper fieldWrapper : resourceClassFields) {
			if (!fieldWrapper.isDiscarded()) {
				resourceFieldPositions.put(fieldWrapper.getResourceField().getUnderlyingName(), resourceFields.size());
				resourceFields.add(fieldWrapper.getResourceField());
			}else{
				discardedFieldNames.add(fieldWrapper.getResourceField().getUnderlyingName());
			}
		}

		for (ResourceFieldWrapper fieldWrapper : resourceGetterFields) {
			if (!fieldWrapper.isDiscarded()) {
				String originalName = fieldWrapper.getResourceField().getUnderlyingName();
				AnnotatedResourceField field = fieldWrapper.getResourceField();
				if (resourceFieldPositions.containsKey(originalName)) {
					int pos = resourceFieldPositions.get(originalName);
					resourceFields.set(pos, mergeAnnotations(resourceFields.get(pos), field, context));
				} else if (!discardedFieldNames.contains(fieldWrapper.getResourceField().getUnderlyingName())) {
					resourceFieldPositions.put(originalName, resourceFields.size());
					resourceFields.add(field);
				}
			}
		}

		return discardIgnoredField(resourceFields);
	}

	private List<AnnotatedResourceField> discardIgnoredField(Collection<AnnotatedResourceField> resourceFieldValues) {
		List<AnnotatedResourceField> resourceFields = new LinkedList<>();
		for (AnnotatedResourceField resourceField : resourceFieldValues) {
			if (!resourceField.isAnnotationPresent(JsonIgnore.class)) {
				resourceFields.add(resourceField);
			}
		}

		return resourceFields;
	}

	@Override
	public void init(ResourceInformationBuilderContext context) {
		this.context = context;
	}

	public static class ResourceFieldWrapper {
		private AnnotatedResourceField resourceField;
		private boolean discarded;

		public ResourceFieldWrapper(AnnotatedResourceField resourceField, boolean discarded) {
			this.resourceField = resourceField;
			this.discarded = discarded;
		}

		public AnnotatedResourceField getResourceField() {
			return resourceField;
		}

		public boolean isDiscarded() {
			return discarded;
		}
	}

	public static class AnnotatedResourceField extends ResourceFieldImpl {

		private List<Annotation> annotations;

		public AnnotatedResourceField(String jsonName, String underlyingName, Class<?> type, Type genericType, String oppositeResourceType, List<Annotation> annotations, ResourceFieldAccess access) {
			super(jsonName, underlyingName, getResourceFieldType(annotations), type, genericType, oppositeResourceType, getOppositeName(annotations), isLazy(annotations), getIncludeByDefault(annotations),
					getLookupIncludeBehavior(annotations), access);
			this.annotations = annotations;
		}

		private static String getOppositeName(List<Annotation> annotations) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof JsonApiToMany) {
					return StringUtils.emptyToNull(((JsonApiToMany) annotation).opposite());
				}
				if (annotation instanceof JsonApiToOne) {
					return StringUtils.emptyToNull(((JsonApiToOne) annotation).opposite());
				}
				if (annotation instanceof JsonApiRelation) {
					return StringUtils.emptyToNull(((JsonApiRelation) annotation).opposite());
				}
			}
			return null;
		}

		public static boolean getIncludeByDefault(Collection<Annotation> annotations) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof JsonApiRelation) {
					JsonApiRelation jsonApiRelation = (JsonApiRelation) annotation;
					return jsonApiRelation.serialize() == SerializeType.EAGER;
				}
				if (annotation instanceof JsonApiIncludeByDefault) {
					return true;
				}
			}
			return false;
		}

		public static JsonApiField getFieldAnnotation(Collection<Annotation> annotations) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof JsonApiField) {
					return (JsonApiField) annotation;
				}
			}
			return null;
		}

		public static JsonProperty getJsonPropertyAnnotation(Collection<Annotation> annotations) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof JsonProperty) {
					return (JsonProperty) annotation;
				}
			}
			return null;
		}

		public static LookupIncludeBehavior getLookupIncludeBehavior(Collection<Annotation> annotations) {
			return getLookupIncludeBehavior(annotations, LookupIncludeBehavior.NONE);
		}

		public static LookupIncludeBehavior getLookupIncludeBehavior(Collection<Annotation> annotations, LookupIncludeBehavior defaultBehavior) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof JsonApiRelation) {
					JsonApiRelation jsonApiRelation = (JsonApiRelation) annotation;
					return jsonApiRelation.lookUp();
				}
				if (annotation instanceof JsonApiLookupIncludeAutomatically) {
					JsonApiLookupIncludeAutomatically includeAnnotation = (JsonApiLookupIncludeAutomatically) annotation;
					if (includeAnnotation.overwrite())
						return LookupIncludeBehavior.AUTOMATICALLY_ALWAYS;
					else
						return LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL;
				}
			}
			return defaultBehavior;
		}

		public static boolean isLazy(List<Annotation> annotations) {
			return isLazy(annotations, false);
		}

		/**
		 * Returns a flag which indicate if a field should not be serialized
		 * automatically.
		 *
		 * @param annotations  attribute annotations
		 * @param defaultValue default value if it cannot be determined
		 * @return is lazy
		 */
		public static boolean isLazy(Collection<Annotation> annotations, boolean defaultValue) {
			JsonApiRelation jsonApiRelation = null;
			JsonApiIncludeByDefault includeByDefaultAnnotation = null;
			JsonApiToMany toManyAnnotation = null;
			JsonApiToOne toOneAnnotation = null;
			for (Annotation annotation : annotations) {

				if (annotation instanceof JsonApiRelation) {
					jsonApiRelation = (JsonApiRelation) annotation;
					break;
				}
				if (annotation.annotationType().equals(JsonApiIncludeByDefault.class)) {
					includeByDefaultAnnotation = (JsonApiIncludeByDefault) annotation;
				}
				if (annotation.annotationType().equals(JsonApiToMany.class)) {
					toManyAnnotation = (JsonApiToMany) annotation;
				}
				if (annotation.annotationType().equals(JsonApiToOne.class)) {
					toOneAnnotation = (JsonApiToOne) annotation;
				}
			}
			if (jsonApiRelation != null) {
				switch (jsonApiRelation.serialize()) {
					case LAZY:
						return true;
					case ONLY_ID:
						return false;
					case EAGER:
						return false;
					default:
						throw new UnsupportedOperationException("Unknown serialize type " + jsonApiRelation.serialize());
				}
			} else if (includeByDefaultAnnotation != null) {
				return false;
			} else if (toManyAnnotation != null) {
				return toManyAnnotation.lazy();
			} else if (toOneAnnotation != null) {
				return toOneAnnotation.lazy();
			}
			return defaultValue;
		}

		public static ResourceFieldType getResourceFieldType(List<Annotation> annotations) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof JsonApiId) {
					return ResourceFieldType.ID;
				} else if (annotation instanceof JsonApiToOne || annotation instanceof JsonApiToMany || annotation instanceof JsonApiRelation) {
					return ResourceFieldType.RELATIONSHIP;
				} else if (annotation instanceof JsonApiMetaInformation) {
					return ResourceFieldType.META_INFORMATION;
				} else if (annotation instanceof JsonApiLinksInformation) {
					return ResourceFieldType.LINKS_INFORMATION;
				}
			}
			return ResourceFieldType.ATTRIBUTE;
		}

		public List<Annotation> getAnnotations() {
			return annotations;
		}

		public boolean isAnnotationPresent(Class<?> annotationClass) {
			for (Annotation annotation : annotations) {
				if (annotation.annotationType().equals(annotationClass)) {
					return true;
				}
			}
			return false;
		}
	}

}
