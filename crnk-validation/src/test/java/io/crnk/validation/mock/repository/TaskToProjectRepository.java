package io.crnk.validation.mock.repository;

import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.RelationshipRepository;
import io.crnk.validation.mock.models.Project;
import io.crnk.validation.mock.models.Task;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TaskToProjectRepository implements RelationshipRepository<Task, Long, Project, Long> {

	private static final ConcurrentMap<Relation<Task>, Integer> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	@Override
	public void setRelation(Task source, Long targetId, String fieldName) {
		removeRelations(fieldName);
		if (targetId != null) {
			THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
		}
	}

	@Override
	public void setRelations(Task source, Iterable<Long> targetIds, String fieldName) {
		removeRelations(fieldName);
		if (targetIds != null) {
			for (Long targetId : targetIds) {
				THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
			}
		}
	}

	@Override
	public void addRelations(Task source, Iterable<Long> targetIds, String fieldName) {
		for (Long targetId : targetIds) {
			THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
		}
	}

	@Override
	public void removeRelations(Task source, Iterable<Long> targetIds, String fieldName) {
		for (Long targetId : targetIds) {
			Iterator<Relation<Task>> iterator = THREAD_LOCAL_REPOSITORY.keySet().iterator();
			while (iterator.hasNext()) {
				Relation<Task> next = iterator.next();
				if (next.getFieldName().equals(fieldName) && next.getTargetId().equals(targetId)) {
					iterator.remove();
				}
			}
		}
	}

	public void removeRelations(String fieldName) {
		Iterator<Relation<Task>> iterator = THREAD_LOCAL_REPOSITORY.keySet().iterator();
		while (iterator.hasNext()) {
			Relation<Task> next = iterator.next();
			if (next.getFieldName().equals(fieldName)) {
				iterator.remove();
			}
		}
	}

	@Override
	public Project findOneTarget(Long sourceId, String fieldName, QueryParams queryParams) {
		for (Relation<Task> relation : THREAD_LOCAL_REPOSITORY.keySet()) {
			if (relation.getSource().getId().equals(sourceId) &&
					relation.getFieldName().equals(fieldName)) {
				Project project = new Project();
				project.setId((Long) relation.getTargetId());
				return project;
			}
		}
		return null;
	}

	@Override
	public Iterable<Project> findManyTargets(Long sourceId, String fieldName, QueryParams queryParams) {
		List<Project> projects = new LinkedList<>();
		for (Relation<Task> relation : THREAD_LOCAL_REPOSITORY.keySet()) {
			if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
				Project project = new Project();
				project.setId((Long) relation.getTargetId());
				projects.add(project);
			}
		}
		return projects;
	}
}
