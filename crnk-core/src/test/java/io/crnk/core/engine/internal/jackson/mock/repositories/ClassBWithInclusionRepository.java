package io.crnk.core.engine.internal.jackson.mock.repositories;

import io.crnk.core.engine.internal.jackson.mock.models.ClassBWithInclusion;
import io.crnk.core.repository.InMemoryResourceRepository;

public class ClassBWithInclusionRepository extends InMemoryResourceRepository<ClassBWithInclusion, Long> {
    public ClassBWithInclusionRepository() {
        super(ClassBWithInclusion.class);
    }
}
