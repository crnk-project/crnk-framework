package io.crnk.core.engine.internal.information.resource;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.FieldOrderedComparator;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.exception.RepositoryAnnotationNotFoundException;
import io.crnk.core.exception.ResourceIdNotFoundException;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.queryspec.pagingspec.VoidPagingBehavior;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.utils.Optional;

/**
 * A builder which creates ResourceInformation instances of a specific class. It
 * extracts information about a resource from annotations and information about
 * fields and getters.
 */
public class DefaultResourceInformationProvider extends ResourceInformationProviderBase {

	private final ArrayList<? extends PagingBehavior> pagingBehaviors;

	public DefaultResourceInformationProvider(PropertiesProvider propertiesProvider,
											  PagingBehavior pagingBehavior,
											  ResourceFieldInformationProvider... resourceFieldInformationProviders) {
		this(propertiesProvider,
				Collections.unmodifiableList(Arrays.asList(pagingBehavior)),
				Arrays.asList(resourceFieldInformationProviders));
	}

	public DefaultResourceInformationProvider(PropertiesProvider propertiesProvider,
											  List<? extends PagingBehavior> pagingBehaviors,
											  ResourceFieldInformationProvider... resourceFieldInformationProviders) {
		this(propertiesProvider, pagingBehaviors, Arrays.asList(resourceFieldInformationProviders));
	}

	public DefaultResourceInformationProvider(PropertiesProvider propertiesProvider,
											  List<? extends PagingBehavior> pagingBehaviors,
											  List<ResourceFieldInformationProvider> resourceFieldInformationProviders) {
		super(propertiesProvider, resourceFieldInformationProviders);

		// order of behaviors must stay the same
		this.pagingBehaviors = new ArrayList<>(pagingBehaviors);
	}

	@Override
	public boolean accept(Class<?> resourceClass) {
		JsonApiResource annotation = resourceClass.getAnnotation(JsonApiResource.class);
		return annotation != null;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public ResourceInformation build(Class<?> resourceClass) {
		return build(resourceClass, false);
	}

	public ResourceInformation build(Class<?> resourceClass, boolean allowNonResourceBaseClass) {
		List<ResourceField> resourceFields = getResourceFields(resourceClass);

		String resourceType = getResourceType(resourceClass, allowNonResourceBaseClass);
		String resourcePath = getResourcePath(resourceClass, allowNonResourceBaseClass);

		Optional<JsonPropertyOrder> propertyOrder = ClassUtils.getAnnotation(resourceClass, JsonPropertyOrder.class);
		if (propertyOrder.isPresent()) {
			JsonPropertyOrder propertyOrderAnnotation = propertyOrder.get();
			Collections.sort(resourceFields, new FieldOrderedComparator(propertyOrderAnnotation.value(), propertyOrderAnnotation.alphabetic()));
		}

		DefaultResourceInstanceBuilder<?> instanceBuilder = new DefaultResourceInstanceBuilder(resourceClass);

		Class<?> superclass = resourceClass.getSuperclass();
		String superResourceType = superclass != Object.class && context.accept(superclass) ? context.getResourceType(superclass) : null;

		Class<? extends PagingBehavior> pagingBehaviorType = ClassUtils.getAnnotation(resourceClass, JsonApiResource.class).get().pagingBehavior();

		// cross check if desired resource paging behavior is a registered behavior, error out if not.
		// if no behavior is set for the resource, we pick the first from registered behaviors.
		java.util.Optional<? extends PagingBehavior> optPagingBehavior;
		if (!pagingBehaviorType.equals(VoidPagingBehavior.class)) {
			optPagingBehavior = pagingBehaviors.stream()
							.filter(it -> pagingBehaviorType.isInstance(it))
							.findFirst();

			if (!optPagingBehavior.isPresent()) {
				throw new IllegalStateException("paging behavior not found: " + pagingBehaviorType);
			}

		} else {
			optPagingBehavior = pagingBehaviors.stream().findFirst();
		}

		ResourceInformation information = new ResourceInformation(context.getTypeParser(),
				resourceClass, resourceType, resourcePath, superResourceType, instanceBuilder, resourceFields,
				optPagingBehavior.get());
		if (!allowNonResourceBaseClass && information.getIdField() == null) {
			throw new ResourceIdNotFoundException(resourceClass.getCanonicalName());
		}
		return information;
	}

	@Override
	public String getResourceType(Class<?> resourceClass) {
		return getResourceType(resourceClass, false);
	}

	@Override
	public String getResourcePath(Class<?> resourceClass) {
		return getResourcePath(resourceClass, false);
	}

	private String getResourcePath(Class<?> resourceClass, boolean allowNonResourceBaseClass) {
		JsonApiResource jsonApiResourceClass = resourceClass.getAnnotation(JsonApiResource.class);
		String resourcePath = null;
		if (jsonApiResourceClass != null) {
			resourcePath = StringUtils.isBlank(jsonApiResourceClass.resourcePath()) ? getResourceType(resourceClass, allowNonResourceBaseClass) : jsonApiResourceClass.resourcePath();
		}
		return resourcePath;
	}

	private String getResourceType(Class<?> resourceClass, boolean allowNonResourceBaseClass) {
		Annotation[] annotations = resourceClass.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof JsonApiResource) {
				JsonApiResource apiResource = (JsonApiResource) annotation;
				return apiResource.type();
			}
		}
		if (allowNonResourceBaseClass) {
			return null;
		}
		// won't reach this
		throw new RepositoryAnnotationNotFoundException(resourceClass.getName());
	}


}
