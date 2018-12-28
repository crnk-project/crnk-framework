package io.crnk.jpa;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.IncludeRelationSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;
import io.crnk.jpa.internal.JpaRepositoryBase;
import io.crnk.jpa.internal.JpaRepositoryUtils;
import io.crnk.jpa.internal.JpaRequestContext;
import io.crnk.jpa.mapping.JpaMapper;
import io.crnk.jpa.meta.internal.JpaMetaUtils;
import io.crnk.jpa.query.ComputedAttributeRegistry;
import io.crnk.jpa.query.JpaQuery;
import io.crnk.jpa.query.JpaQueryExecutor;
import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.Tuple;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Exposes a JPA entity as ResourceRepository. Inherit from this class to setup
 * a repository.
 */
public class JpaEntityRepositoryBase<T, I extends Serializable> extends JpaRepositoryBase<T> implements ResourceRepository<T, I>,
        ResourceRegistryAware {

    private final BeanAttributeInformation primaryKeyAttribute;

    private ResourceRegistry resourceRegistry;

    public JpaEntityRepositoryBase(Class<T> entityClass) {
        this(JpaRepositoryConfig.create(entityClass));
    }

    public JpaEntityRepositoryBase(JpaRepositoryConfig<T> config) {
        super(config);
        primaryKeyAttribute = JpaMetaUtils.getUniquePrimaryKey(config.getEntityClass());
    }

    public JpaQueryFactory getQueryFactory() {
        return repositoryConfig.getQueryFactory();
    }

    @Override
    public final T findOne(I id, QuerySpec querySpec) {
        String idField = getIdField().getUnderlyingName();
        QuerySpec idQuerySpec = querySpec.duplicate();
        idQuerySpec.addFilter(new FilterSpec(Arrays.asList(idField), FilterOperator.EQ, id));
        List<T> results = findAll(idQuerySpec);
        return getUnique(results, id);
    }

    @Override
    public final ResourceList<T> findAll(Iterable<I> ids, QuerySpec querySpec) {
        String idField = getIdField().getUnderlyingName();
        QuerySpec idQuerySpec = querySpec.duplicate();
        idQuerySpec.addFilter(new FilterSpec(Arrays.asList(idField), FilterOperator.EQ, ids));
        return findAll(idQuerySpec);
    }

    @Override
    public ResourceList<T> findAll(QuerySpec querySpec) {
        Class<?> entityClass = repositoryConfig.getEntityClass();
        QuerySpec filteredQuerySpec = JpaRepositoryUtils.filterQuerySpec(repositoryConfig, this, querySpec);
        JpaQueryFactory queryFactory = repositoryConfig.getQueryFactory();
        JpaQuery<?> query = queryFactory.query(entityClass);
        query.setPrivateData(new JpaRequestContext(this, querySpec));

        ComputedAttributeRegistry computedAttributesRegistry = queryFactory.getComputedAttributes();
        Set<String> computedAttrs = computedAttributesRegistry.getForType(entityClass);

        JpaRepositoryUtils.prepareQuery(query, filteredQuerySpec, computedAttrs);
        query = JpaRepositoryUtils.filterQuery(repositoryConfig, this, filteredQuerySpec, query);
        JpaQueryExecutor<?> executor = query.buildExecutor();

        if (optimizeForInclusion(querySpec)) {
            IncludeRelationSpec includedRelationSpec = querySpec.getIncludedRelations().get(0);
            executor.fetch(includedRelationSpec.getAttributePath());
        }

        boolean fetchNext = repositoryConfig.isNextFetched(filteredQuerySpec);
        boolean fetchTotal = repositoryConfig.isTotalFetched(filteredQuerySpec);

        JpaRepositoryUtils.prepareExecutor(executor, filteredQuerySpec, fetchRelations());

        if (fetchNext) {
            executor.setLimit(executor.getLimit() + 1);
        }

        executor = JpaRepositoryUtils.filterExecutor(repositoryConfig, this, filteredQuerySpec, executor);

        List<Tuple> tuples = executor.getResultTuples();

        Boolean hasNext = null;
        if (fetchNext) {
            hasNext = tuples.size() == querySpec.getLimit() + 1;
            if (hasNext) {
                tuples = tuples.subList(0, querySpec.getLimit().intValue());
            }
        }

        tuples = JpaRepositoryUtils.filterTuples(repositoryConfig, this, filteredQuerySpec, tuples);

        ResourceList<T> resources = repositoryConfig.newResultList();
        MetaInformation metaInfo = resources.getMeta();
        JpaRepositoryUtils.fillResourceList(repositoryConfig, tuples, resources);
        resources = JpaRepositoryUtils.filterResults(repositoryConfig, this, filteredQuerySpec, resources);
        if (fetchTotal) {
            long totalRowCount = executor.getTotalRowCount();
            ((PagedMetaInformation) metaInfo).setTotalResourceCount(totalRowCount);
        }
        if (fetchNext) {
            ((HasMoreResourcesMetaInformation) metaInfo).setHasMoreResources(hasNext);
        }

        return resources;
    }

    /**
     * if single relationship is requested, perform eager loading. For example, helps to
     * optimize loading uni-directional associations during inclusions (used the default
     * relationship repositories)
     */
    private boolean optimizeForInclusion(QuerySpec querySpec) {
        ResourceField idField = getIdField();
        return querySpec.getIncludedRelations().size() == 1 &&
                querySpec.getIncludedFields().size() == 1
                && idField.getUnderlyingName().equals(querySpec.getIncludedFields().get(0).getPath().toString());
    }

    /**
     * By default LookupIncludeBehavior.ALWAYS is in place and we let the relationship repositories load the relations. There
     * is no need to do join fetches, which can lead to problems with paging (evaluated in memory instead of the db).
     *
     * @return relation will be eagerly fetched if true
     */
    protected boolean fetchRelations() {
        return false;
    }


    @Override
    public <S extends T> S create(S resource) {
        return saveInternal(resource);
    }

    @Override
    public <S extends T> S save(S resource) {
        return saveInternal(resource);
    }

    @SuppressWarnings("unchecked")
    private <S extends T> S saveInternal(S resource) {
        JpaMapper<Object, T> mapper = repositoryConfig.getMapper();
        Object entity = mapper.unmap(resource);

        // PATCH reads, updates and saves entities, needs reattachment during
        // save since reads do a detach
        EntityManager em = getEntityManager();
        em.persist(entity);
        Object pk = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);

        // fetch again since we may have to fetch tuple data and do DTO mapping
        QuerySpec querySpec = new QuerySpec(repositoryConfig.getResourceClass());
        PreconditionUtil.verify(pk != null, "pk not available for entity %s", resource);

        // id may differ from primary key
        ResourceField idField = getIdField();
        I id = (I) idField.getAccessor().getValue(resource);
        PreconditionUtil.verify(id != null, "id not available for entity %s", resource);
        return (S) findOne(id, querySpec);
    }

    @Override
    public void delete(I id) {
        Object pk;
        ResourceField idField = getIdField();
        if (idField.getUnderlyingName().equals(primaryKeyAttribute.getName())) {
            pk = id;
        } else {
            T resource = findOne(id, new QuerySpec(getResourceClass()));
            pk = PropertyUtils.getProperty(resource, primaryKeyAttribute.getName());
            if (pk == null) {
                throw new IllegalStateException(
                        "no primary key available for type=" + getResourceClass().getSimpleName() + " id=" + id);
            }
        }

        EntityManager em = getEntityManager();
        Object object = em.find(repositoryConfig.getEntityClass(), pk);
        if (object != null) {
            em.remove(object);
        }
    }

    @Override
    public Class<T> getResourceClass() {
        return repositoryConfig.getResourceClass();
    }

    public Class<?> getEntityClass() {
        return repositoryConfig.getEntityClass();
    }

    @Override
    public void setResourceRegistry(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    public ResourceField getIdField() {
        RegistryEntry entry = resourceRegistry.getEntry(getResourceClass());
        ResourceInformation resourceInformation = entry.getResourceInformation();
        return resourceInformation.getIdField();
    }
}
