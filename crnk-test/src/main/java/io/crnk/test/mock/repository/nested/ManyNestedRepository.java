package io.crnk.test.mock.repository.nested;

import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.test.mock.models.nested.ManyNestedResource;
import io.crnk.test.mock.models.nested.NestedId;

public class ManyNestedRepository extends InMemoryResourceRepository<ManyNestedResource, NestedId> {

	public ManyNestedRepository() {
		super(ManyNestedResource.class);
	}
}