package io.crnk.example.vertx.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.vertx.model.Project;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ProjectRepository extends ResourceRepositoryBase<Project, Long> {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(124);

    private Map<Long, Project> projects = new ConcurrentHashMap<>();

    public ProjectRepository() {
        super(Project.class);
        save(new Project(123L, "Great Project"));
    }

    @Override
    public synchronized void delete(Long id) {
        projects.remove(id);
    }

    @Override
    public synchronized <S extends Project> S save(S project) {
        if (project.getId() == null) {
            project.setId(ID_GENERATOR.getAndIncrement());
        }
        projects.put(project.getId(), project);
        return project;
    }

    @Override
    public synchronized ResourceList<Project> findAll(QuerySpec querySpec) {
        return querySpec.apply(projects.values());
    }

}
