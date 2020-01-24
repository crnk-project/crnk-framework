package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.repository.MatchedRelationshipRepository;

public interface RegistryEntryBuilder {

    /**
     * Builds up the entry from the provided implementation
     */
    void fromImplementation(Object repository);

    interface ResourceRepositoryEntryBuilder {

        InformationBuilder.ResourceRepositoryInformationBuilder information();

        void instance(Object repository);
    }

    interface RelationshipRepositoryEntryBuilder {

        InformationBuilder.RelationshipRepositoryInformationBuilder information();

        void instance(MatchedRelationshipRepository repository);
    }

    ResourceRepositoryEntryBuilder resourceRepository();

    InformationBuilder.ResourceInformationBuilder resource();

    RelationshipRepositoryEntryBuilder relationshipRepositoryForField(String fieldName);

    RegistryEntry build();

}
