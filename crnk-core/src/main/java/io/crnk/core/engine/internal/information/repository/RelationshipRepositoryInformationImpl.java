package io.crnk.core.engine.internal.information.repository;

import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;

import java.util.Optional;

public class RelationshipRepositoryInformationImpl implements
		RelationshipRepositoryInformation {

	private final String sourceResourceType;

	private final String targetResourceType;

	private final RepositoryMethodAccess access;

	private Optional<Class> sourceResourceClass;

	public RelationshipRepositoryInformationImpl(Class sourceResourceClass, String sourceResourceType,
												 String targetResourceType, RepositoryMethodAccess access) {
		this.sourceResourceClass = Optional.ofNullable(sourceResourceClass);
		this.sourceResourceType = sourceResourceType;
		this.targetResourceType = targetResourceType;
		this.access = access;
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

	@Override
	public RepositoryMethodAccess getAccess() {
		return access;
	}
}
