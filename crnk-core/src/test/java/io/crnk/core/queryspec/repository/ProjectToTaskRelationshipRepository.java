package io.crnk.core.queryspec.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.util.Relation;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.RelationshipRepositoryBase;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProjectToTaskRelationshipRepository extends RelationshipRepositoryBase<Project, Long, Task, Long> {


    private final static ConcurrentMap<Relation<Project>, Integer> STATIC_REPOSITORY = new ConcurrentHashMap<>();


    public static void clear() {
        STATIC_REPOSITORY.clear();
    }

    public ProjectToTaskRelationshipRepository() {
        super(Project.class, Task.class);
    }

    @Override
    public RelationshipMatcher getMatcher() {
        RelationshipMatcher matcher = new RelationshipMatcher();
        matcher.rule().source(Project.class, true).target(Task.class, true).add();
        return matcher;
    }


}