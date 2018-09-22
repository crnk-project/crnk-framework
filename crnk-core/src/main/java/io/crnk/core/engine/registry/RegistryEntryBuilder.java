package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.InformationBuilder;

public interface RegistryEntryBuilder {

	/**
	 * Builds up the entry from the provided implementation
	 */
	void fromImplementation(Object repository);

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

	RelationshipRepository relationshipRepositoryForField(String fieldName);

	RegistryEntry build();

}
