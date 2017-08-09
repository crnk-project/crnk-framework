package io.crnk.core.engine.information;

import java.lang.reflect.Type;

import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

public interface InformationBuilder {

	interface RelationshipRepository {

		RelationshipRepositoryInformation build();

	}

	interface ResourceRepository {

		ResourceRepositoryInformation build();

	}

	interface Resource {

		InformationBuilder.Field addField(String name, ResourceFieldType id1, Class<?> clazz);

		Resource resourceClass(Class<?> resourceClass);

		Resource resourceType(String resourceType);

		Resource superResourceType(String superResourceType);

		ResourceInformation build();

	}

	interface Field {

		ResourceField build();

		Field jsonName(String jsonName);

		Field underlyingName(String underlyingName);

		Field type(Class<?> type);

		Field genericType(Type genericType);

		Field lazy(boolean lazy);

		Field oppositeResourceType(String oppositeResourceType);

		Field lookupIncludeBehavior(LookupIncludeBehavior lookupIncludeBehavior);

		Field includeByDefault(boolean includeByDefault);

		Field fieldType(ResourceFieldType fieldType);

		Field setOppositeName(String oppositeName);

		Field setAccessor(ResourceFieldAccessor accessor);

		Field setAccess(ResourceFieldAccess access);

	}

	RelationshipRepository createRelationshipRepository(String sourceResourceType, String targeResourceType);

	ResourceRepository createResourceRepository(Class<?> resourceClass, String resourceType);

	Resource createResource(Class<?> resourceClass, String resourceType);

}
