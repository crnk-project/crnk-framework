package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.ProjectEager;
import io.crnk.core.mock.repository.util.Relation;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.LegacyRelationshipRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProjectToProjectEagerRepository extends AbstractRelationShipRepository<Project> implements LegacyRelationshipRepository<Project, Long, ProjectEager, Long> {

	private final static ConcurrentMap<Relation<Project>, Integer> STATIC_REPOSITORY = new ConcurrentHashMap<>();

	public static void clear() {
		STATIC_REPOSITORY.clear();
	}

	@Override
	ConcurrentMap<Relation<Project>, Integer> getRepo() {
		return STATIC_REPOSITORY;
	}

	@Override
	public ProjectEager findOneTarget(Long sourceId, String fieldName, QueryParams queryParams) {
		Map<Relation<Project>, Integer> repo = getRepo();
		for (Relation<Project> relation : repo.keySet()) {
			if (relation.getSource().getId().equals(sourceId) &&
					relation.getFieldName().equals(fieldName)) {
				ProjectEager task = new ProjectEager();
				task.setId((Long) relation.getTargetId());
				return task;
			}
		}
		return null;
	}

	@Override
	public Iterable<ProjectEager> findManyTargets(Long sourceId, String fieldName, QueryParams queryParams) {
		return null;
	}
}
