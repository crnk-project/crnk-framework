package io.crnk.core.engine.internal.jackson.mock.repositories;

import io.crnk.core.engine.internal.jackson.mock.models.ClassCWithInclusion;
import io.crnk.core.repository.InMemoryResourceRepository;

public class ClassCWithInclusionRepository extends InMemoryResourceRepository<ClassCWithInclusion, Long> {

    public ClassCWithInclusionRepository() {
        super(ClassCWithInclusion.class);
    }
}
