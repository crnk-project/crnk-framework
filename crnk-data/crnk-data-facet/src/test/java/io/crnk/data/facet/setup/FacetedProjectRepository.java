package io.crnk.data.facet.setup;

import io.crnk.core.repository.InMemoryResourceRepository;

public class FacetedProjectRepository extends InMemoryResourceRepository<FacetedProject, Long> {

	public FacetedProjectRepository() {
		super(FacetedProject.class);
	}
}
