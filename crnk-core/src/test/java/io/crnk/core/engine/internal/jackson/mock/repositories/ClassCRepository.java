package io.crnk.core.engine.internal.jackson.mock.repositories;

import io.crnk.core.engine.internal.jackson.mock.models.ClassC;
import io.crnk.core.repository.InMemoryResourceRepository;

public class ClassCRepository extends InMemoryResourceRepository<ClassC, Long> {

    public ClassCRepository() {
        super(ClassC.class);
    }
}
