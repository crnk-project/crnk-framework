package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.ProjectEager;
import io.crnk.core.mock.repository.util.Relation;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.RelationshipMatcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProjectToProjectEagerRepository extends AbstractRelationShipRepository<Project> implements OneRelationshipRepository<Project, Long, ProjectEager, Long> {

    private final static ConcurrentMap<Relation<Project>, Integer> STATIC_REPOSITORY = new ConcurrentHashMap<>();

    public static void clear() {
        STATIC_REPOSITORY.clear();
    }

    @Override
    ConcurrentMap<Relation<Project>, Integer> getRepo() {
        return STATIC_REPOSITORY;
    }


    @Override
    public RelationshipMatcher getMatcher() {
        RelationshipMatcher matcher = new RelationshipMatcher();
        matcher.rule().source(Project.class).target(ProjectEager.class).add();
        return matcher;
    }

    @Override
    public Map<Long, ProjectEager> findOneRelations(Collection<Long> sourceIds, String fieldName, QuerySpec querySpec) {
        Map<Long, ProjectEager> map = new HashMap<>();
        for (Long sourceId : sourceIds) {
            Map<Relation<Project>, Integer> repo = getRepo();
            for (Relation<Project> relation : repo.keySet()) {
                if (relation.getSource().getId().equals(sourceId) &&
                        relation.getFieldName().equals(fieldName)) {
                    ProjectEager task = new ProjectEager();
                    task.setId((Long) relation.getTargetId());
                    map.put(sourceId, task);
                }
            }
        }
        return map;
    }
}
