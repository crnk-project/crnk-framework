package io.crnk.test.mock.repository.nested;

import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.test.mock.models.nested.NestedRelatedResource;

public class RelatedRepository extends InMemoryResourceRepository<NestedRelatedResource, String> {

	public RelatedRepository() {
		super(NestedRelatedResource.class);
	}
}