package io.crnk.test.mock.dynamic;

import io.crnk.core.engine.document.Resource;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.repository.UntypedResourceRepository;
import io.crnk.core.resource.list.DefaultResourceList;

import java.util.HashMap;
import java.util.Map;

// tag::docs1[]
public class DynamicResourceRepository extends ResourceRepositoryBase<Resource, String> implements UntypedResourceRepository<Resource, String> {

	private static Map<String, Resource> RESOURCES = new HashMap<>();

	private final String resourceType;

	public DynamicResourceRepository(String resourceType) {
		super(Resource.class);
		this.resourceType = resourceType;
	}

	@Override
	public String getResourceType() {
		return resourceType;
	}

	@Override
	public Class<Resource> getResourceClass() {
		return Resource.class;
	}

	@Override
	public DefaultResourceList<Resource> findAll(QuerySpec querySpec) {
		return querySpec.apply(RESOURCES.values());
	}
	// end::docs1[]

	@Override
	public <S extends Resource> S create(S entity) {
		return save(entity);
	}

	@Override
	public <S extends Resource> S save(S entity) {
		RESOURCES.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public void delete(String id) {
		RESOURCES.remove(id);
	}

	public static void clear() {
		RESOURCES.clear();
	}
	// tag::docs2[]
}
// end::docs2[]