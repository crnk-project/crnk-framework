package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.ProjectPolymorphic;
import io.crnk.core.repository.InMemoryResourceRepository;

public class ProjectPolymorphicRepository extends InMemoryResourceRepository<ProjectPolymorphic, Long> {

    public ProjectPolymorphicRepository() {
        super(ProjectPolymorphic.class);
    }
}
