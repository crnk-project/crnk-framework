package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Pojo;
import io.crnk.core.repository.InMemoryResourceRepository;

public class PojoRepository extends InMemoryResourceRepository<Pojo, Long> {


    public PojoRepository() {
        super(Pojo.class);
    }
}
