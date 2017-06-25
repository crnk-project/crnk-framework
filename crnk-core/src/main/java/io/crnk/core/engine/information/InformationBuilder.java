package io.crnk.core.engine.information;

import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.*;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

import java.lang.reflect.Type;

public interface InformationBuilder {

	RelationshipRepository createRelationshipRepository(ResourceInformation sourceInformation, ResourceInformation targetInformation);

	interface RelationshipRepository {

		RelationshipRepositoryInformation build();
	}

	interface ResourceRepository {

		Resource resource();

		ResourceRepositoryInformation build();
	}

	interface Resource {

		InformationBuilder.Field addField(String name, ResourceFieldType id1, Class<?> clazz);

		void resourceClass(Class<?> resourceClass);

		void resourceType(String resourceType);

		void superResourceType(String superResourceType);

		ResourceInformation build();
	}

	interface Field {

		ResourceField build();

		void jsonName(String jsonName);

		void underlyingName(String underlyingName);

		void type(Class<?> type);

		void genericType(Type genericType);

		void lazy(boolean lazy);

		void oppositeResourceType(String oppositeResourceType);

		void lookupIncludeBehavior(LookupIncludeBehavior lookupIncludeBehavior);

		void includeByDefault(boolean includeByDefault);

		void fieldType(ResourceFieldType fieldType);

		void setOppositeName(String oppositeName);

		void setAccessor(ResourceFieldAccessor accessor);

		void setAccess(ResourceFieldAccess access);
	}


	ResourceRepository createResourceRepository(Class<?> resourceClass, String resourceType);

}
