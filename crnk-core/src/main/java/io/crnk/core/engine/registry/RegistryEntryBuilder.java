package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.InformationBuilder;

public interface RegistryEntryBuilder {


	interface ResourceRepository {

		InformationBuilder.ResourceRepository information();

		void instance(Object repository);
	}

	interface RelationshipRepository {

		InformationBuilder.RelationshipRepository information();

		void instance(Object repository);
	}

	ResourceRepository resourceRepository();

	InformationBuilder.Resource resource();

	RelationshipRepository relationshipRepository(String targetResourceType);

	RegistryEntry build();

}
