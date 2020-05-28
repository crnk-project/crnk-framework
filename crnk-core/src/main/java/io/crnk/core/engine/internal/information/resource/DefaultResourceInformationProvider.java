package io.crnk.core.engine.internal.information.resource;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.VersionRange;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.exception.RepositoryAnnotationNotFoundException;
import io.crnk.core.exception.ResourceIdNotFoundException;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiVersion;
import io.crnk.core.utils.Prioritizable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A builder which creates ResourceInformation instances of a specific class. It
 * extracts information about a resource from annotations and information about
 * fields and getters.
 */
public class DefaultResourceInformationProvider extends ResourceInformationProviderBase implements Prioritizable {

    public static final int PRIORITY = 100;

    private final List<? extends PagingBehavior> pagingBehaviors;

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

        this.pagingBehaviors = pagingBehaviors;
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
        ResourceFieldAccess resourceAccess = getResourceAccess(resourceClass);

        List<ResourceField> resourceFields = getResourceFields(resourceClass, resourceAccess, false);

        String resourceType = getResourceType(resourceClass, allowNonResourceBaseClass);
        String resourcePath = getResourcePath(resourceClass, allowNonResourceBaseClass);

        DefaultResourceInstanceBuilder<?> instanceBuilder = new DefaultResourceInstanceBuilder(resourceClass);

        Class<?> superclass = resourceClass.getSuperclass();
        String superResourceType =
                superclass != Object.class && context.accept(superclass) ? context.getResourceType(superclass) : null;


        JsonApiResource annotation = ClassUtils.getAnnotation(resourceClass, JsonApiResource.class).get();

        Class<PagingSpec> pagingSpec = (Class<PagingSpec>) annotation.pagingSpec();
        ResourceInformation information = new ResourceInformation(context.getTypeParser(),
                resourceClass, resourceType, resourcePath, superResourceType, instanceBuilder, resourceFields,
                pagingSpec);
        information.setAccess(resourceAccess);
        information.setVersionRange(getVersionRange(resourceClass));
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
            resourcePath = StringUtils.isBlank(jsonApiResourceClass.resourcePath()) ? getResourceType(resourceClass,
                    allowNonResourceBaseClass) : jsonApiResourceClass.resourcePath();
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


    @Override
    public int getPriority() {
        return PRIORITY;
    }
}
