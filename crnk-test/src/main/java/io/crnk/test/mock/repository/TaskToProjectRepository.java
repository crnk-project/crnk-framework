package io.crnk.test.mock.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TaskToProjectRepository implements RelationshipRepository<Task, Long, Project, Long> {

	private static final ConcurrentMap<Relation<Task>, Integer> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	public static void clear() {
		THREAD_LOCAL_REPOSITORY.clear();
	}

	public TaskToProjectRepository() {

	}

	@Override
	public void setRelation(Task source, Long targetId, String fieldName) {
		removeRelations(fieldName);
		if (targetId != null) {
			THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
		}
	}

	@Override
	public void setRelations(Task source, Collection<Long> targetIds, String fieldName) {
		removeRelations(fieldName);
		if (targetIds != null) {
			for (Long targetId : targetIds) {
				THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
			}
		}
	}

	@Override
	public void addRelations(Task source, Collection<Long> targetIds, String fieldName) {
		for (Long targetId : targetIds) {
			THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
		}
	}

	@Override
	public void removeRelations(Task source, Collection<Long> targetIds, String fieldName) {
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
	public Project findOneTarget(Long sourceId, String fieldName, QuerySpec queryParams) {
		for (Relation<Task> relation : THREAD_LOCAL_REPOSITORY.keySet()) {
			if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
				Project project = new Project();
				project.setId((Long) relation.getTargetId());
				return project;
			}
		}
		return null;
	}

	@Override
	public ResourceList<Project> findManyTargets(Long sourceId, String fieldName, QuerySpec queryParams) {
		DefaultResourceList<Project> projects = new DefaultResourceList<>();
		for (Relation<Task> relation : THREAD_LOCAL_REPOSITORY.keySet()) {
			if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
				Project project = new Project();
				project.setId((Long) relation.getTargetId());
				projects.add(project);
			}
		}
		return projects;
	}

	@Override
	public Class<Task> getSourceResourceClass() {
		return Task.class;
	}

	@Override
	public Class<Project> getTargetResourceClass() {
		return Project.class;
	}
}
