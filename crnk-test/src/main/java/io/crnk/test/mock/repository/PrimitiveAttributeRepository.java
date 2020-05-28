package io.crnk.test.mock.repository;

import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.test.mock.models.PrimitiveAttributeResource;

public class PrimitiveAttributeRepository extends InMemoryResourceRepository<PrimitiveAttributeResource, Long> {

	public PrimitiveAttributeRepository() {
		super(PrimitiveAttributeResource.class);
	}
}
