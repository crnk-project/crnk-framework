package io.crnk.data.facet.setup;

import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.resource.annotations.JsonApiExposed;

@JsonApiExposed(false) // => test not exposed repositories, should not be listed
public class FacetedTaskRepository extends InMemoryResourceRepository<FacetedTask, Long> {

	public FacetedTaskRepository() {
		super(FacetedTask.class);
	}
}
