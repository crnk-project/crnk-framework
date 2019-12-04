package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.ResourceWithoutRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.RelationshipMatcher;

import java.util.Collection;
import java.util.Map;

public class ResourceWithoutRepositoryToProjectRepository
        implements OneRelationshipRepository<ResourceWithoutRepository, String, Project, Long> {

    @Override
    public RelationshipMatcher getMatcher() {
        RelationshipMatcher matcher = new RelationshipMatcher();
        matcher.rule().source(ResourceWithoutRepository.class).target(Project.class).add();
        return matcher;
    }

    @Override
    public void setRelation(ResourceWithoutRepository source, Long targetId, String fieldName) {
    }

    @Override
    public Map<String, Project> findOneRelations(Collection<String> sourceIds, String fieldName, QuerySpec querySpec) {
        return null;
    }
}
