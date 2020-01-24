package io.crnk.core.engine.internal.jackson.mock.repositories;

import io.crnk.core.engine.internal.jackson.mock.models.ClassA;
import io.crnk.core.repository.InMemoryResourceRepository;

public class ClassARepository extends InMemoryResourceRepository<ClassA, Long> {

    public ClassARepository() {
        super(ClassA.class);
    }
}
