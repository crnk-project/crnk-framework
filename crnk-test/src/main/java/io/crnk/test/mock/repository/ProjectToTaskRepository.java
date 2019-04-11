package io.crnk.test.mock.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import junit.framework.Assert;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProjectToTaskRepository implements RelationshipRepository<Project, Long, Task, Long> {

	private static final ConcurrentMap<Relation<Project>, Integer> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	private TaskRepository taskRepo = new TaskRepository();

	public static void clear() {
		THREAD_LOCAL_REPOSITORY.clear();
	}

	@Override
	public void setRelation(Project source, Long targetId, String fieldName) {
		removeRelations(fieldName);
		if (targetId != null) {
			THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
		}
	}

	@Override
	public void setRelations(Project source, Iterable<Long> targetIds, String fieldName) {
		removeRelations(fieldName);
		if (targetIds != null) {
			for (Long targetId : targetIds) {
				THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
			}
		}
	}

	@Override
	public void addRelations(Project source, Iterable<Long> targetIds, String fieldName) {
		for (Long targetId : targetIds) {
			THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
		}
	}

	@Override
	public void removeRelations(Project source, Iterable<Long> targetIds, String fieldName) {
		for (Long targetId : targetIds) {
			Iterator<Relation<Project>> iterator = THREAD_LOCAL_REPOSITORY.keySet().iterator();
			while (iterator.hasNext()) {
				Relation<Project> next = iterator.next();
				if (next.getFieldName().equals(fieldName) && next.getTargetId().equals(targetId)) {
					iterator.remove();
				}
			}
		}
	}

	public void removeRelations(String fieldName) {
		Iterator<Relation<Project>> iterator = THREAD_LOCAL_REPOSITORY.keySet().iterator();
		while (iterator.hasNext()) {
			Relation<Project> next = iterator.next();
			if (next.getFieldName().equals(fieldName)) {
				iterator.remove();
			}
		}
	}

	@Override
	public Task findOneTarget(Long sourceId, String fieldName, QuerySpec queryParams) {
		for (Relation<Project> relation : THREAD_LOCAL_REPOSITORY.keySet()) {
			if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
				Task task = taskRepo.findOne((long) relation.getTargetId(), null);
				Assert.assertNotNull(task);
				return task;
			}
		}
		return null;
	}

	@Override
	public ResourceList<Task> findManyTargets(Long sourceId, String fieldName, QuerySpec queryParams) {
		DefaultResourceList<Task> tasks = new DefaultResourceList<>();
		for (Relation<Project> relation : THREAD_LOCAL_REPOSITORY.keySet()) {
			if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
				Task task = taskRepo.findOne((long) relation.getTargetId(), null);
				Assert.assertNotNull(task);
				tasks.add(task);
			}
		}
		return tasks;
	}

	@Override
	public Class<Project> getSourceResourceClass() {
		return Project.class;
	}

	@Override
	public Class<Task> getTargetResourceClass() {
		return Task.class;
	}
}
