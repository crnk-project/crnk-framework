package io.crnk.jpa.internal;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.IncludeSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.repository.WrappedResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.jpa.JpaModuleConfig;
import io.crnk.jpa.JpaRepositoryConfig;
import io.crnk.jpa.JpaRepositoryFilter;
import io.crnk.jpa.mapping.JpaMapper;
import io.crnk.jpa.query.JpaQuery;
import io.crnk.jpa.query.JpaQueryExecutor;
import io.crnk.jpa.query.Tuple;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryExecutor;
import io.crnk.jpa.query.criteria.JpaCriteriaRepositoryFilter;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaKey;

import javax.persistence.criteria.CriteriaQuery;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class JpaRepositoryUtils {

    private JpaRepositoryUtils() {
    }

    /**
     * @param meta of the entity
     * @return Gets the primary key attribute of the given entity. Assumes a
     * primary key is available and no compound primary keys are
     * supported.
     */
    public static MetaAttribute getPrimaryKeyAttr(MetaDataObject meta) {
        MetaKey primaryKey = meta.getPrimaryKey();
        PreconditionUtil.verify(primaryKey != null, "no primary key for %s", meta);
        PreconditionUtil.verifyEquals(1, primaryKey.getElements().size(), "non-compound primary key expected for %s", meta);
        return primaryKey.getElements().get(0);
    }

    public static void prepareQuery(JpaQuery<?> query, QuerySpec querySpec, Set<String> computedAttrs) {
        for (String computedAttr : computedAttrs) {
            query.addSelection(Arrays.asList(computedAttr));
        }
        for (FilterSpec filter : querySpec.getFilters()) {
            query.addFilter(filter);
        }
        for (SortSpec sortSpec : querySpec.getSort()) {
            query.addSortBy(sortSpec);
        }
    }

    public static void prepareExecutor(JpaQueryExecutor<?> executor, QuerySpec querySpec, boolean includeRelations) {
        if (includeRelations) {
            for (IncludeSpec included : querySpec.getIncludedRelations()) {
                executor.fetch(included.getAttributePath());
            }
        }
        executor.setOffset((int) querySpec.getOffset());
        if (querySpec.getOffset() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("offset cannot be larger than Integer.MAX_VALUE");
        }
        if (querySpec.getLimit() != null) {
            if (querySpec.getLimit() > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("limit cannot be larger than Integer.MAX_VALUE");
            }
            executor.setLimit((int) querySpec.getLimit().longValue());
        }
    }

    public static void setDefaultConfig(JpaModuleConfig moduleConfig, JpaRepositoryConfig<?> repositoryConfig) {
        if (!repositoryConfig.hasQueryFactory()) {
            repositoryConfig.setQueryFactory(moduleConfig::getQueryFactory);
        }
        if (!repositoryConfig.hasTotalAvailable()) {
            repositoryConfig.setTotalAvailable(moduleConfig.isTotalResourceCountUsed());
        }

        moduleConfig.getFilters().stream()
                .filter(filter -> !repositoryConfig.getFilters().contains(filter))
                .forEach(filter -> repositoryConfig.addFilter(filter));
    }

    public static <T> QuerySpec filterQuerySpec(JpaRepositoryConfig repositoryConfig, Object repository, QuerySpec querySpec) {
        JpaMapper<Object, T> mapper = repositoryConfig.getMapper();
        QuerySpec filteredQuerySpec = mapper.unmapQuerySpec(querySpec);
        for (JpaRepositoryFilter filter : (Collection<JpaRepositoryFilter>) repositoryConfig.getFilters()) {
            if (filter.accept(repositoryConfig.getResourceClass())) {
                filteredQuerySpec = filter.filterQuerySpec(repository, filteredQuerySpec);
            }
        }
        return filteredQuerySpec;
    }

    public static <E> JpaQuery<E> filterQuery(JpaRepositoryConfig repositoryConfig, Object repository, QuerySpec querySpec, JpaQuery<E> query) {
        JpaQuery<E> filteredQuery = query;
        for (JpaRepositoryFilter filter : (Collection<JpaRepositoryFilter>) repositoryConfig.getFilters()) {
            if (filter.accept(repositoryConfig.getResourceClass())) {
                filteredQuery = filter.filterQuery(repository, querySpec, filteredQuery);
            }
        }
        return filteredQuery;
    }

    public static <E> JpaQueryExecutor<E> filterExecutor(JpaRepositoryConfig repositoryConfig, Object repository, QuerySpec querySpec, JpaQueryExecutor<E> executor) {
        JpaQueryExecutor<E> filteredExecutor = executor;
        for (JpaRepositoryFilter filter : (Collection<JpaRepositoryFilter>) repositoryConfig.getFilters()) {
            if (filter.accept(repositoryConfig.getResourceClass())) {
                filteredExecutor = filter.filterExecutor(repository, querySpec, filteredExecutor);

                if (filter instanceof JpaCriteriaRepositoryFilter && executor instanceof JpaCriteriaQueryExecutor) {
                    JpaCriteriaRepositoryFilter criteriaFilter = (JpaCriteriaRepositoryFilter) filter;
                    CriteriaQuery criteriaQuery = ((JpaCriteriaQueryExecutor<E>) executor).getQuery();
                    criteriaFilter.filterCriteriaQuery(repository, querySpec, criteriaQuery);
                }
            }
        }
        return filteredExecutor;
    }

    public static <T> List<Tuple> filterTuples(JpaRepositoryConfig<T> repositoryConfig, Object repository, QuerySpec querySpec, List<Tuple> tuples) {
        List<Tuple> filteredTuples = tuples;
        for (JpaRepositoryFilter filter : repositoryConfig.getFilters()) {
            if (filter.accept(repositoryConfig.getResourceClass())) {
                filteredTuples = filter.filterTuples(repository, querySpec, filteredTuples);
            }
        }
        return filteredTuples;
    }

    public static <T> ResourceList<T> filterResults(JpaRepositoryConfig<T> repositoryConfig, Object repository, QuerySpec querySpec, ResourceList<T> resources) {
        ResourceList<T> filteredResources = resources;
        for (JpaRepositoryFilter filter : repositoryConfig.getFilters()) {
            if (filter.accept(repositoryConfig.getResourceClass())) {
                filteredResources = filter.filterResults(repository, querySpec, filteredResources);
            }
        }
        return filteredResources;
    }

    public static <T> ResourceList<T> fillResourceList(JpaRepositoryConfig<T> repositoryConfig, List<Tuple> tuples, ResourceList<T> resources) {
        for (Tuple tuple : tuples) {
            JpaMapper<Object, T> mapper = repositoryConfig.getMapper();
            resources.add(mapper.map(tuple));
        }
        return resources;
    }

    public static Object unwrap(Object repository) {
        while (repository instanceof WrappedResourceRepository) {
            repository = ((WrappedResourceRepository) repository).getWrappedRepository();
        }
        return repository;
    }
}
