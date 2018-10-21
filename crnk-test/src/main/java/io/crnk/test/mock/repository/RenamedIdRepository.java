package io.crnk.test.mock.repository;

import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.test.mock.models.RenamedIdResource;

public class RenamedIdRepository extends InMemoryResourceRepository<RenamedIdResource, String> {

	public RenamedIdRepository() {
		super(RenamedIdResource.class);
	}
}
