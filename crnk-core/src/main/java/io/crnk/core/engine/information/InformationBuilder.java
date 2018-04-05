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
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;

import java.lang.reflect.Type;

public interface InformationBuilder {

	Field createResourceField();

	interface RelationshipRepository {

		void setAccess(RepositoryMethodAccess access);

		RelationshipRepositoryInformation build();

	}

	interface ResourceRepository {

		void from(ResourceRepositoryInformation information);

		void setResourceInformation(ResourceInformation resourceInformation);

		void setAccess(RepositoryMethodAccess access);

		ResourceRepositoryInformation build();

	}

	interface Resource {

		void from(ResourceInformation information);

		InformationBuilder.Field addField(String name, ResourceFieldType id1, Class<?> clazz);

		Resource resourceClass(Class<?> resourceClass);

		Resource resourceType(String resourceType);

		Resource resourcePath(String resourcePath);

		Resource superResourceType(String superResourceType);

		Resource pagingBehavior(PagingBehavior pagingBehavior);

		ResourceInformation build();

	}

	interface Field {

		ResourceField build();

		void from(ResourceField field);

		Field relationshipRepositoryBehavior(
				RelationshipRepositoryBehavior relationshipRepositoryBehavior);

		Field jsonName(String jsonName);

		Field underlyingName(String underlyingName);

		Field name(String name);

		Field type(Class<?> type);

		Field genericType(Type genericType);

		Field serializeType(SerializeType serializeType);

		Field oppositeResourceType(String oppositeResourceType);

		Field lookupIncludeBehavior(LookupIncludeBehavior lookupIncludeBehavior);

		Field fieldType(ResourceFieldType fieldType);

		Field oppositeName(String oppositeName);

		Field accessor(ResourceFieldAccessor accessor);

		Field access(ResourceFieldAccess access);

		Field idAccessor(ResourceFieldAccessor idAccessor);

		Field idName(String idName);

		Field idType(Class idType);

	}

	RelationshipRepository createRelationshipRepository(String sourceResourceType, String targeResourceType);

	RelationshipRepository createRelationshipRepository(RelationshipMatcher matcher);

	ResourceRepository createResourceRepository();

	Resource createResource(Class<?> resourceClass, String resourceType, String resourcePath);
	Resource createResource(Class<?> resourceClass, String resourceType);

}
