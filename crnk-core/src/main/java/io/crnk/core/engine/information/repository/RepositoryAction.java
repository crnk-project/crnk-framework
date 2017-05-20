package io.crnk.core.engine.information.repository;

public interface RepositoryAction {

	String getName();

	/**
	 * @return whether a resource or repository action
	 */
	RepositoryActionType getActionType();

	enum RepositoryActionType {
		REPOSITORY,
		RESOURCE
	}
}
