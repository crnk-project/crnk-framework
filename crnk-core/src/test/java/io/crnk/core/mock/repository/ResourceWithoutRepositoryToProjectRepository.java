package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.ResourceWithoutRepository;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.LegacyRelationshipRepository;

public class ResourceWithoutRepositoryToProjectRepository
		implements LegacyRelationshipRepository<ResourceWithoutRepository, String, Project, Long> {
	@Override
	public void setRelation(ResourceWithoutRepository source, Long targetId, String fieldName) {

	}

	@Override
	public void setRelations(ResourceWithoutRepository source, Iterable<Long> targetIds, String fieldName) {

	}

	@Override
	public void addRelations(ResourceWithoutRepository source, Iterable<Long> targetIds, String fieldName) {

	}

	@Override
	public void removeRelations(ResourceWithoutRepository source, Iterable<Long> targetIds, String fieldName) {

	}

	@Override
	public Project findOneTarget(String sourceId, String fieldName, QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<Project> findManyTargets(String sourceId, String fieldName, QueryParams queryParams) {
		return null;
	}
}
