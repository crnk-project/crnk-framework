package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.TopTaskWrapper;
import io.crnk.core.repository.InMemoryResourceRepository;

public class TopTaskWrapperRepository extends InMemoryResourceRepository<TopTaskWrapper, Long> {

	public TopTaskWrapperRepository() {
		super(TopTaskWrapper.class);
	}
}