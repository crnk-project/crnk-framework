package io.crnk.reactive.model;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.reactive.repository.ReactiveOneRelationshipRepository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ReactiveTaskToProjectRepository implements ReactiveOneRelationshipRepository<ReactiveTask, Long, ReactiveProject, Long> {

	private Map<Long, ReactiveProject> projectMap;

	private Map<Long, Long> relationMap = new HashMap<>();

	public ReactiveTaskToProjectRepository(Map<Long, ReactiveProject> projectMap) {
		this.projectMap = projectMap;
	}

	@Override
	public Mono<Map<Long, ReactiveProject>> findOneTargets(Collection<Long> sourceIds, ResourceField field, QuerySpec querySpec) {
		Map<Long, ReactiveProject> result = new HashMap<>();
		for (Long sourceId : sourceIds) {
			Long projectId = relationMap.get(sourceId);
			if (projectId != null) {
				ReactiveProject project = projectMap.get(projectId);
				if (project == null) {
					throw new ResourceNotFoundException(projectId.toString());
				}
				result.put(sourceId, project);
			}
		}
		return Mono.just(result);
	}

	@Override
	public Mono<Void> setRelation(ReactiveTask source, Long targetId, ResourceField field) {
		relationMap.put(source.getId(), targetId);
		return Mono.justOrEmpty(null);
	}

	@Override
	public RelationshipMatcher getMatcher() {
		RelationshipMatcher matcher = new RelationshipMatcher();
		return matcher.rule().source(ReactiveTask.class).target(ReactiveProject.class).add();
	}

	public Map<Long, Long> getRelationMap() {
		return relationMap;
	}
}
