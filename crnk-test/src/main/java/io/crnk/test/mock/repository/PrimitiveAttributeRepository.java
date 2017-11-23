package io.crnk.test.mock.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.PrimitiveAttributeResource;

public class PrimitiveAttributeRepository extends ResourceRepositoryBase<PrimitiveAttributeResource, Long> {

	public PrimitiveAttributeRepository() {
		super(PrimitiveAttributeResource.class);
	}

	@Override
	public ResourceList<PrimitiveAttributeResource> findAll(QuerySpec querySpec) {
		return null;
	}
}
