package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.FancyProject;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.util.Relation;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.RelationshipRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TaskToProjectRepository extends AbstractRelationShipRepository<Task>
		implements RelationshipRepository<Task, Long, Project, Long> {

	private final static ConcurrentMap<Relation<Task>, Integer> STATIC_REPOSITORY = new ConcurrentHashMap<>();


	public static void clear() {
		STATIC_REPOSITORY.clear();
	}

	@Override
	ConcurrentMap<Relation<Task>, Integer> getRepo() {
		return STATIC_REPOSITORY;
	}

	@Override
	public void setRelation(Task source, Long targetId, String fieldName) {
		super.setRelation(source, targetId, fieldName);
	}

	@Override
	public void setRelations(Task source, Iterable<Long> targetIds, String fieldName) {
		super.setRelations(source, targetIds, fieldName);
	}

	@Override
	public void addRelations(Task source, Iterable<Long> targetIds, String fieldName) {
		super.addRelations(source, targetIds, fieldName);
	}

	@Override
	public void removeRelations(Task source, Iterable<Long> targetIds, String fieldName) {
		super.removeRelations(source, targetIds, fieldName);
	}

	@Override
	public Project findOneTarget(Long sourceId, String fieldName, QueryParams queryParams) {
		Map<Relation<Task>, Integer> repo = getRepo();
		for (Relation<Task> relation : repo.keySet()) {
			if (relation.getSource().getId().equals(sourceId) &&
					relation.getFieldName().equals(fieldName)) {
				Project project = new Project();
				if (relation.getTargetId().equals(ProjectRepository.FANCY_PROJECT_ID)) {
					project = new FancyProject();
				}
				project.setId((Long) relation.getTargetId());
				return project;
			}
		}
		return null;
	}

	@Override
	public Iterable<Project> findManyTargets(Long sourceId, String fieldName, QueryParams queryParams) {
		List<Project> projects = new LinkedList<>();
		for (Relation<Task> relation : getRepo().keySet()) {
			if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
				Project project = new Project();
				if (relation.getTargetId().equals(ProjectRepository.FANCY_PROJECT_ID)) {
					project = new FancyProject();
				}
				project.setId((Long) relation.getTargetId());
				projects.add(project);
			}
		}
		return projects;
	}
}
