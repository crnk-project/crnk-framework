package io.crnk.core.engine.information;

import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.resource.annotations.JsonIncludeStrategy;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.PatchStrategy;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;

import java.lang.reflect.Type;

public interface InformationBuilder {

    FieldInformationBuilder createResourceField();

    interface RelationshipRepositoryInformationBuilder {

        void setAccess(RepositoryMethodAccess access);

        RelationshipRepositoryInformation build();

    }

    interface ResourceRepositoryInformationBuilder {

        void from(ResourceRepositoryInformation information);

        void setResourceInformation(ResourceInformation resourceInformation);

        void setAccess(RepositoryMethodAccess access);

        void setExposed(boolean exposed);

        ResourceRepositoryInformation build();

    }

    interface ResourceInformationBuilder {

        void from(ResourceInformation information);

        FieldInformationBuilder addField();

        FieldInformationBuilder addField(String name, ResourceFieldType id1, Class<?> clazz);

        /**
         * @deprecated use {@link #implementationType(Type)}
         */
        @Deprecated
        ResourceInformationBuilder resourceClass(Class<?> resourceClass);

        ResourceInformationBuilder implementationType(Type implementationType);

        ResourceInformationBuilder resourceType(String resourceType);

        ResourceInformationBuilder resourcePath(String resourcePath);

        ResourceInformationBuilder superResourceType(String superResourceType);

        /**
         * @deprecated use pagingSpecType
         */
        @Deprecated
        ResourceInformationBuilder pagingBehavior(PagingBehavior pagingBehavior);

        ResourceInformationBuilder pagingSpecType(Class<PagingSpec> pagingSpecType);

        ResourceInformation build();

    }

    interface FieldInformationBuilder {

        ResourceField build();

        void from(ResourceField field);

        FieldInformationBuilder relationshipRepositoryBehavior(
                RelationshipRepositoryBehavior relationshipRepositoryBehavior);

        FieldInformationBuilder jsonName(String jsonName);

        FieldInformationBuilder underlyingName(String underlyingName);

        FieldInformationBuilder name(String name);

        FieldInformationBuilder type(Class<?> type);

        FieldInformationBuilder genericType(Type genericType);

        FieldInformationBuilder serializeType(SerializeType serializeType);

        FieldInformationBuilder jsonIncludeStrategy(JsonIncludeStrategy jsonIncludeStrategy);

        FieldInformationBuilder oppositeResourceType(String oppositeResourceType);

        FieldInformationBuilder lookupIncludeBehavior(LookupIncludeBehavior lookupIncludeBehavior);

        FieldInformationBuilder fieldType(ResourceFieldType fieldType);

        FieldInformationBuilder oppositeName(String oppositeName);

        FieldInformationBuilder accessor(ResourceFieldAccessor accessor);

        FieldInformationBuilder access(ResourceFieldAccess access);

        FieldInformationBuilder idAccessor(ResourceFieldAccessor idAccessor);

        FieldInformationBuilder idName(String idName);

        FieldInformationBuilder idType(Class idType);

        FieldInformationBuilder patchStrategy(PatchStrategy patchStrategy);

        FieldInformationBuilder setMappedBy(boolean mappedBy);
    }

    RelationshipRepositoryInformationBuilder createRelationshipRepository(String sourceResourceType, String targeResourceType);

    RelationshipRepositoryInformationBuilder createRelationshipRepository(RelationshipMatcher matcher);

    ResourceRepositoryInformationBuilder createResourceRepository();

    ResourceInformationBuilder createResource(Class<?> resourceClass, String resourceType, String resourcePath);

    ResourceInformationBuilder createResource(Class<?> resourceClass, String resourceType);

}
