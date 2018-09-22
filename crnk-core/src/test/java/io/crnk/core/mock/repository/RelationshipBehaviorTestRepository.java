package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.RelationshipBehaviorTestResource;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;

import java.util.HashMap;
import java.util.Map;

public class RelationshipBehaviorTestRepository extends ResourceRepositoryBase<RelationshipBehaviorTestResource, Long> {

	private static Map<Long, RelationshipBehaviorTestResource> resources = new HashMap<>();

	public RelationshipBehaviorTestRepository() {
		super(RelationshipBehaviorTestResource.class);
	}

	public static void clear() {
		resources.clear();
	}

	@Override
	public ResourceList<RelationshipBehaviorTestResource> findAll(QuerySpec querySpec) {
		ResourceList<RelationshipBehaviorTestResource> list = new DefaultResourceList<>();
		list.addAll(querySpec.apply(resources.values()));
		return list;
	}

	@Override
	public <S extends RelationshipBehaviorTestResource> S save(S entity) {
		resources.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public void delete(Long id) {
		resources.remove(id);
	}
}