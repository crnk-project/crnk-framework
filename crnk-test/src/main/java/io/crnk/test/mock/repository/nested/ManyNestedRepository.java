package io.crnk.test.mock.repository.nested;

import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.test.mock.models.nested.NestedId;
import io.crnk.test.mock.models.nested.ManyNestedResource;

public class ManyNestedRepository extends InMemoryResourceRepository<ManyNestedResource, NestedId> {

	public ManyNestedRepository() {
		super(ManyNestedResource.class);
	}
}