package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Thing;
import io.crnk.core.repository.InMemoryResourceRepository;

public class ThingRepository extends InMemoryResourceRepository<Thing, Long> {

    public ThingRepository() {
        super(Thing.class);
    }
}