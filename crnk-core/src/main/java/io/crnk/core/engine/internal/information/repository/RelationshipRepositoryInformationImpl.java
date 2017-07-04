package io.crnk.core.engine.internal.information.repository;

import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.utils.Optional;

public class RelationshipRepositoryInformationImpl implements
		RelationshipRepositoryInformation {

	private final String sourceResourceType;

	private final String targetResourceType;

	private Optional<Class> sourceResourceClass;

	public RelationshipRepositoryInformationImpl(Class sourceResourceClass, String sourceResourceType,
												 String targetResourceType) {
		this.sourceResourceClass = Optional.of(sourceResourceClass);
		this.sourceResourceType = sourceResourceType;
		this.targetResourceType = targetResourceType;
	}

	public RelationshipRepositoryInformationImpl(String sourceResourceType,
												 String targetResourceType) {
		this.sourceResourceClass = Optional.empty();
		this.sourceResourceType = sourceResourceType;
		this.targetResourceType = targetResourceType;
	}

	@Override
	public Optional<Class> getSourceResourceClass() {
		return sourceResourceClass;
	}

	@Override
	public String getSourceResourceType() {
		return sourceResourceType;
	}

	@Override
	public String getTargetResourceType() {
		return targetResourceType;
	}
}
