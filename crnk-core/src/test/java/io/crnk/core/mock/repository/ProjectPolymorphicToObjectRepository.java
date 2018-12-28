package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.ProjectPolymorphic;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.util.Relation;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.LegacyRelationshipRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProjectPolymorphicToObjectRepository extends AbstractRelationShipRepository<ProjectPolymorphic> implements LegacyRelationshipRepository<ProjectPolymorphic, Long, Object, Long> {

	private final static ConcurrentMap<Relation<ProjectPolymorphic>, Integer> STATIC_REPOSITORY = new ConcurrentHashMap<>();

	@Override
	ConcurrentMap<Relation<ProjectPolymorphic>, Integer> getRepo() {
		return STATIC_REPOSITORY;
	}

	@Override
	public void setRelation(ProjectPolymorphic source, Long targetId, String fieldName) {
		super.setRelation(source, targetId, fieldName);
	}

	@Override
	public void setRelations(ProjectPolymorphic source, Iterable<Long> targetIds, String fieldName) {
		super.setRelations(source, targetIds, fieldName);
	}

	@Override
	public void addRelations(ProjectPolymorphic source, Iterable<Long> targetIds, String fieldName) {
		super.addRelations(source, targetIds, fieldName);
	}

	@Override
	public void removeRelations(ProjectPolymorphic source, Iterable<Long> targetIds, String fieldName) {
		super.removeRelations(source, targetIds, fieldName);
	}

	@Override
	public Object findOneTarget(Long sourceId, String fieldName, QueryParams queryParams) {
		for (Relation<ProjectPolymorphic> relation : getRepo().keySet()) {
			if (relation.getSource().getId().equals(sourceId) &&
					relation.getFieldName().equals(fieldName)) {
				Task task = new Task();
				task.setId((Long) relation.getTargetId());
				return task;
			}
		}
		return null;
	}

	@Override
	public Iterable<Object> findManyTargets(Long sourceId, String fieldName, QueryParams queryParams) {
		List<Object> tasks = new LinkedList<>();
		for (Relation<ProjectPolymorphic> relation : getRepo().keySet()) {
			if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
				Task task = new Task();
				task.setId((Long) relation.getTargetId());
				tasks.add(task);
			}
		}
		return tasks;
	}

}
