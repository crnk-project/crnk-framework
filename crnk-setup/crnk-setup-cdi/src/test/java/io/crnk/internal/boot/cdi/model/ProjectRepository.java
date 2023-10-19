package io.crnk.internal.boot.cdi.model;

import io.crnk.core.repository.InMemoryResourceRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProjectRepository extends InMemoryResourceRepository<Project, Long> {
    public ProjectRepository() {
        super(Project.class);
    }
}
