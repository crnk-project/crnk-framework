package io.crnk.core.resource.paging.total;

import io.crnk.core.repository.InMemoryResourceRepository;

public class TotalResourceCountTestRepository extends InMemoryResourceRepository<TotalResourceCountResource, Long> {

    public TotalResourceCountTestRepository() {
        super(TotalResourceCountResource.class);
    }
}