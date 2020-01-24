package io.crnk.example.springboot.domain.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.springboot.domain.model.History;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

/**
 * See HistoryRelationshipRepository for more information/actual use case
 */
@Component
public class HistoryResourceRepository extends ResourceRepositoryBase<History, UUID> {

	protected HistoryResourceRepository() {
		super(History.class);
	}

	@Override
	public History findOne(UUID id, QuerySpec querySpec) {
		History history = new History();
		history.setId(id);
		history.setName("test");
		return history;
	}

	@Override
	public ResourceList<History> findAll(Collection<UUID> ids, QuerySpec querySpec) {
		DefaultResourceList list = new DefaultResourceList();
		for (UUID id : ids) {
			History history = new History();
			history.setId(id);
			history.setName("test");
			list.add(history);
		}
		return list;
	}

	@Override
	public ResourceList<History> findAll(QuerySpec querySpec) {
		throw new UnsupportedOperationException("no implemented");
	}
}
