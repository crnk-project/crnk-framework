package io.crnk.data.jpa;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.jpa.query.JpaQuery;
import io.crnk.data.jpa.query.JpaQueryExecutor;
import io.crnk.data.jpa.query.Tuple;

import java.util.List;

/**
 * Empty default implementation for {@link JpaRepositoryFilter}.
 */
public class JpaRepositoryFilterBase implements JpaRepositoryFilter {

    @Override
    public boolean accept(Class<?> resourceType) {
        return true;
    }

    @Override
    public QuerySpec filterQuerySpec(Object repository, QuerySpec querySpec) {
        return querySpec;
    }

    @Override
    public <T> JpaQuery<T> filterQuery(Object repository, QuerySpec querySpec, JpaQuery<T> query) {
        return query;
    }

    @Override
    public <T> JpaQueryExecutor<T> filterExecutor(Object repository, QuerySpec querySpec, JpaQueryExecutor<T> executor) {
        return executor;
    }

    @Override
    public List<Tuple> filterTuples(Object repository, QuerySpec querySpec, List<Tuple> tuples) {
        return tuples;
    }

    @Override
    public <T> ResourceList<T> filterResults(Object repository, QuerySpec querySpec, ResourceList<T> resources) {
        return resources;
    }

    @Override
    public <T, I> JpaEntityRepository<T, I> filterCreation(JpaEntityRepository<T, I> repository) {
        return repository;
    }
}
