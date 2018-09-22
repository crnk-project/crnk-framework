package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;

import java.util.HashMap;
import java.util.Map;

public class RelationIdTestRepository extends ResourceRepositoryBase<RelationIdTestResource, Long> {

	private static Map<Long, RelationIdTestResource> resources = new HashMap<>();

	public RelationIdTestRepository() {
		super(RelationIdTestResource.class);
	}

	public static void clear() {
		resources.clear();
	}

	@Override
	public ResourceList<RelationIdTestResource> findAll(QuerySpec querySpec) {
		ResourceList<RelationIdTestResource> list = new DefaultResourceList<>();
		list.addAll(querySpec.apply(resources.values()));
		return list;
	}

	@Override
	public <S extends RelationIdTestResource> S save(S entity) {
		resources.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public void delete(Long id) {
		resources.remove(id);
	}
}