package io.crnk.legacy.internal;

import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;

public class DirectResponseRelationshipEntry implements ResponseRelationshipEntry {

    private RepositoryInstanceBuilder repositoryInstanceBuilder;

    public DirectResponseRelationshipEntry(RepositoryInstanceBuilder repositoryInstanceBuilder) {
        this.repositoryInstanceBuilder = repositoryInstanceBuilder;
    }

    public Object getRepositoryInstanceBuilder() {
        return repositoryInstanceBuilder.buildRepository();
    }

    @Override
    public String toString() {
        return repositoryInstanceBuilder.buildRepository().toString();
    }
}
