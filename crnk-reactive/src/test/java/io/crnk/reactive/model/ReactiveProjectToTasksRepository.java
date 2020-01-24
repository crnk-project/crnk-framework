package io.crnk.reactive.model;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.reactive.repository.ReactiveManyRelationshipRepository;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReactiveProjectToTasksRepository
		implements ReactiveManyRelationshipRepository<ReactiveProject, Long, ReactiveTask, Long> {

	private Map<Long, ReactiveTask> taskMap;

	private MultivaluedMap<Long, Long> relationMap = new MultivaluedMap<>();

	public ReactiveProjectToTasksRepository(Map<Long, ReactiveTask> taskMap) {
		this.taskMap = taskMap;
	}


	@Override
	public RelationshipMatcher getMatcher() {
		RelationshipMatcher matcher = new RelationshipMatcher();
		return matcher.rule().source(ReactiveProject.class).target(ReactiveTask.class).add();
	}

	public MultivaluedMap<Long, Long> getRelationMap() {
		return relationMap;
	}

	@Override
	public Mono<Void> setRelations(ReactiveProject source, Collection<Long> targetIds, ResourceField field) {
		relationMap.set(source.getId(), new ArrayList<>(targetIds));
		return Mono.justOrEmpty(null);
	}

	@Override
	public Mono<Void> addRelations(ReactiveProject source, Collection<Long> targetIds, ResourceField field) {
		ArrayList<Long> ids = new ArrayList<>();
		if (relationMap.containsKey(source.getId())) {
			ids.addAll(relationMap.getList(source.getId()));
		}
		ids.addAll(targetIds);
		relationMap.set(source.getId(), ids);
		return Mono.justOrEmpty(null);
	}

	@Override
	public Mono<Void> removeRelations(ReactiveProject source, Collection<Long> targetIds, ResourceField field) {
		ArrayList<Long> ids = new ArrayList<>();
		if (relationMap.containsKey(source.getId())) {
			ids.addAll(relationMap.getList(source.getId()));
		}
		ids.removeAll(targetIds);
		relationMap.set(source.getId(), ids);
		return Mono.justOrEmpty(null);
	}

	@Override
	public Mono<Map<Long, ResourceList<ReactiveTask>>> findManyTargets(Collection<Long> sourceIds, ResourceField field,
																	   QuerySpec querySpec) {
		Map<Long, ResourceList<ReactiveTask>> result = new HashMap<>();
		for (Long sourceId : sourceIds) {
			List<Long> taskIds = relationMap.getList(sourceId);
			DefaultResourceList<ReactiveTask> tasks = new DefaultResourceList<>();
			for (Long taskId : taskIds) {
				ReactiveTask task = taskMap.get(taskId);
				if (task == null) {
					throw new ResourceNotFoundException(taskId.toString());
				}
				tasks.add(task);
			}
			result.put(sourceId, tasks);
		}
		return Mono.just(result);
	}
}
