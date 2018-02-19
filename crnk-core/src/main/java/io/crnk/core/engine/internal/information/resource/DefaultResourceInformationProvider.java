package io.crnk.core.engine.internal.information.resource;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.FieldOrderedComparator;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.exception.RepositoryAnnotationNotFoundException;
import io.crnk.core.exception.ResourceIdNotFoundException;
import io.crnk.core.queryspec.pagingspec.PagingSpecDeserializer;
import io.crnk.core.queryspec.pagingspec.PagingSpecSerializer;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.PagingBehavior;
import io.crnk.core.utils.Optional;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A builder which creates ResourceInformation instances of a specific class. It
 * extracts information about a resource from annotations and information about
 * fields and getters.
 */
public class DefaultResourceInformationProvider extends ResourceInformationProviderBase {

	private final Map<Class<? extends PagingSpecSerializer>, PagingSpecSerializer> pagingSpecSerializers;

	private final Map<Class<? extends PagingSpecDeserializer>, PagingSpecDeserializer> pagingSpecDeserializers;

	public DefaultResourceInformationProvider(PropertiesProvider propertiesProvider,
											  PagingSpecSerializer pagingSpecSerializer,
											  PagingSpecDeserializer pagingSpecDeserializer,
											  ResourceFieldInformationProvider... resourceFieldInformationProviders) {
		this(propertiesProvider,
				ImmutableList.of(pagingSpecSerializer),
				ImmutableList.of(pagingSpecDeserializer),
				Arrays.asList(resourceFieldInformationProviders));
	}

	public DefaultResourceInformationProvider(PropertiesProvider propertiesProvider,
											  List<? extends PagingSpecSerializer> pagingSpecSerializers,
											  List<? extends PagingSpecDeserializer> pagingSpecDeserializers,
											  ResourceFieldInformationProvider... resourceFieldInformationProviders) {
		this(propertiesProvider, pagingSpecSerializers, pagingSpecDeserializers, Arrays.asList(resourceFieldInformationProviders));
	}

	public DefaultResourceInformationProvider(PropertiesProvider propertiesProvider,
											  List<? extends PagingSpecSerializer> pagingSpecSerializers,
											  List<? extends PagingSpecDeserializer> pagingSpecDeserializers,
											  List<ResourceFieldInformationProvider> resourceFieldInformationProviders) {
		super(propertiesProvider, resourceFieldInformationProviders);

		this.pagingSpecSerializers = new HashMap<>(pagingSpecSerializers.size());
		for (int i = 0; i < pagingSpecSerializers.size(); i++) {
			this.pagingSpecSerializers.put(pagingSpecSerializers.get(i).getClass(), pagingSpecSerializers.get(i));
		}
		this.pagingSpecDeserializers = new HashMap<>(pagingSpecDeserializers.size());
		for (int i = 0; i < pagingSpecDeserializers.size(); i++) {
			this.pagingSpecDeserializers.put(pagingSpecDeserializers.get(i).getClass(), pagingSpecDeserializers.get(i));
		}
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
		List<ResourceField> resourceFields = getResourceFields(resourceClass);

		String resourceType = getResourceType(resourceClass, allowNonResourceBaseClass);

		Optional<JsonPropertyOrder> propertyOrder = ClassUtils.getAnnotation(resourceClass, JsonPropertyOrder.class);
		if (propertyOrder.isPresent()) {
			JsonPropertyOrder propertyOrderAnnotation = propertyOrder.get();
			Collections.sort(resourceFields, new FieldOrderedComparator(propertyOrderAnnotation.value(), propertyOrderAnnotation.alphabetic()));
		}

		DefaultResourceInstanceBuilder<?> instanceBuilder = new DefaultResourceInstanceBuilder(resourceClass);

		Class<?> superclass = resourceClass.getSuperclass();
		String superResourceType = superclass != Object.class && context.accept(superclass) ? context.getResourceType(superclass) : null;

		PagingBehavior pagingBehavior = ClassUtils.getAnnotation(resourceClass, JsonApiResource.class).get().paging();

		ResourceInformation information = new ResourceInformation(context.getTypeParser(),
				resourceClass, resourceType, superResourceType, instanceBuilder, resourceFields,
				pagingSpecSerializers.get(pagingBehavior.serializer()), pagingSpecDeserializers.get(pagingBehavior.deserializer()));
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
