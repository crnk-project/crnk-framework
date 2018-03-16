package io.crnk.example.springboot.domain.repository;

import java.io.Serializable;
import java.util.UUID;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyRelationshipRepositoryBase;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.springboot.domain.model.History;
import org.springframework.stereotype.Component;

/**
 * Generic repository that introduces a history relationship for project and task resource without touching
 * those resources.
 */
// tag::docs[]
@Component
public class HistoryRelationshipRepository extends ReadOnlyRelationshipRepositoryBase<Object, Serializable, History, UUID> {

	@Override
	public RelationshipMatcher getMatcher() {
		return new RelationshipMatcher().rule().target(History.class).add();
	}

	@Override
	public ResourceList<History> findManyTargets(Serializable sourceId, String fieldName, QuerySpec querySpec) {
		DefaultResourceList list = new DefaultResourceList();
		for (int i = 0; i < 10; i++) {
			History history = new History();
			history.setId(UUID.nameUUIDFromBytes(("historyElement" + i).getBytes()));
			history.setName("historyElement" + i);
			list.add(history);
		}
		return list;
	}
}
// end::docs[]