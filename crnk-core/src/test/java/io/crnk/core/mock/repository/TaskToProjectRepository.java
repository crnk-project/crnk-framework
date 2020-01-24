package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.LinksRepository;
import io.crnk.core.repository.MetaRepository;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.RelationshipRepositoryBase;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

import java.util.Collection;

public class TaskToProjectRepository extends RelationshipRepositoryBase<Task, Long, Project, Long>
        implements MetaRepository<Project>, LinksRepository<Project> {

    public TaskToProjectRepository() {
        super(Task.class, Project.class);
    }

    @Override
    public RelationshipMatcher getMatcher() {
        RelationshipMatcher matcher = new RelationshipMatcher();
        matcher.rule().source(Task.class).target(Project.class).field("project").add();
        return matcher;
    }


    @Override
    public LinksInformation getLinksInformation(Collection<Project> resources, QuerySpec querySpec, LinksInformation current) {
        return new LinksInformation() {

            public String name = "value";
        };
    }

    @Override
    public MetaInformation getMetaInformation(Collection<Project> resources, QuerySpec querySpec, MetaInformation current) {
        return new MetaInformation() {

            public String name = "value";
        };
    }
}