package io.crnk.core.engine.internal.jackson.mock.repositories;

import io.crnk.core.engine.internal.jackson.mock.models.ClassAWithInclusion;
import io.crnk.core.repository.InMemoryResourceRepository;

public class ClassAWithInclusionRepository extends InMemoryResourceRepository<ClassAWithInclusion, Long> {

    public ClassAWithInclusionRepository() {
        super(ClassAWithInclusion.class);
    }
}
