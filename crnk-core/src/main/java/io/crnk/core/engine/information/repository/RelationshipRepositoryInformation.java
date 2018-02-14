package io.crnk.core.engine.information.repository;

import io.crnk.core.repository.RelationshipMatcher;

/**
 * Holds information about the type of a relationship repository.
 */
public interface RelationshipRepositoryInformation extends RepositoryInformation {

	RelationshipMatcher getMatcher();

}
