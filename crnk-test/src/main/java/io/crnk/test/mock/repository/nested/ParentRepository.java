package io.crnk.test.mock.repository.nested;

import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.test.mock.models.nested.ParentResource;

public class ParentRepository extends InMemoryResourceRepository<ParentResource, String> {

	public ParentRepository() {
		super(ParentResource.class);
	}
}



