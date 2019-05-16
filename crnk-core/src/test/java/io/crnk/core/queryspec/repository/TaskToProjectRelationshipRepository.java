package io.crnk.core.queryspec.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.LinksRepository;
import io.crnk.core.repository.MetaRepository;
import io.crnk.core.repository.RelationshipRepositoryBase;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

import java.util.Collection;

public class TaskToProjectRelationshipRepository extends RelationshipRepositoryBase<Task, Long, Project, Long>
        implements MetaRepository<Project>, LinksRepository<Project> {

    public TaskToProjectRelationshipRepository() {
        super(Task.class, Project.class);
    }

    @Override
    public LinksInformation getLinksInformation(Collection<Project> resources, QuerySpec querySpec) {
        return new LinksInformation() {

            public String name = "value";
        };
    }

    @Override
    public MetaInformation getMetaInformation(Collection<Project> resources, QuerySpec querySpec) {
        return new MetaInformation() {

            public String name = "value";
        };
    }
}