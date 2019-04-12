package io.crnk.jpa;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.BulkRelationshipRepository;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;
import io.crnk.jpa.internal.JpaRepositoryBase;
import io.crnk.jpa.internal.JpaRepositoryUtils;
import io.crnk.jpa.internal.JpaRequestContext;
import io.crnk.jpa.mapping.JpaMapper;
import io.crnk.jpa.meta.MetaEntity;
import io.crnk.jpa.query.ComputedAttributeRegistry;
import io.crnk.jpa.query.JpaQuery;
import io.crnk.jpa.query.JpaQueryExecutor;
import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.Tuple;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaType;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @deprecated in favor of {@link io.crnk.core.repository.foward.ForwardingRelationshipRepository}
 */
@Deprecated
public class JpaRelationshipRepository<S, I , T, J > extends JpaRepositoryBase<T>
        implements RelationshipRepository<S, I, T, J>, BulkRelationshipRepository<S, I, T, J> {

    private final ResourceField resourceField;

    private final String sourceKeyName;

    private Class<S> sourceResourceClass;

    private Class<?> sourceEntityClass;

    private MetaEntity entityMeta;

    private JpaMapper<?, S> sourceMapper;

    /**
     * JPA relationship directly exposed as repository
     *
     * @param module           that manages this repository
     * @param resourceField    from this relation
     * @param repositoryConfig from this relation
     */
    public JpaRelationshipRepository(JpaModule module, ResourceField resourceField, JpaRepositoryConfig<T>
            repositoryConfig) {
        super(repositoryConfig);

        JpaRepositoryUtils.setDefaultConfig(module.getConfig(), repositoryConfig);

        this.sourceResourceClass = (Class<S>) resourceField.getParentResourceInformation().getResourceClass();
        this.resourceField = resourceField;
        this.sourceKeyName = resourceField.getParentResourceInformation().getIdField().getUnderlyingName();


        JpaRepositoryConfig<S> sourceMapping = module.getRepositoryConfig(sourceResourceClass);
        this.sourceEntityClass = sourceMapping.getEntityClass();
        this.sourceMapper = sourceMapping.getMapper();
        this.entityMeta = module.getJpaMetaProvider().discoverMeta(sourceEntityClass);
    }

    @Override
    public RelationshipMatcher getMatcher() {
        return new RelationshipMatcher().rule().field(resourceField).add();
    }

    @Override
    public void setRelation(S source, J targetId, String fieldName) {
        MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
        MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
        Class<?> targetType = getElementType(attrMeta);
        EntityManager em = getEntityManager();

        Object sourceEntity = sourceMapper.unmap(source);

        Object target = targetId != null ? em.find(targetType, targetId) : null;
        attrMeta.setValue(sourceEntity, target);

        if (target != null && oppositeAttrMeta != null) {
            if (oppositeAttrMeta.getType().isCollection()) {
                oppositeAttrMeta.addValue(target, sourceEntity);
            } else {
                oppositeAttrMeta.setValue(target, sourceEntity);
            }
            em.persist(target);
        }
    }

    @Override
    public void setRelations(S source, Collection<J> targetIds, String fieldName) {
        MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
        MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
        Class<?> targetType = getElementType(attrMeta);
        EntityManager em = getEntityManager();

        Object sourceEntity = sourceMapper.unmap(source);

        Collection<Object> targets = attrMeta.getType().asCollection().newInstance();
        for (J targetId : targetIds) {

            Object target = em.find(targetType, targetId);
            targets.add(target);
        }

        // detach current
        if (oppositeAttrMeta != null) {
            Collection<?> col = (Collection<?>) attrMeta.getValue(sourceEntity);
            Iterator<?> iterator = col.iterator();
            while (iterator.hasNext()) {
                Object prevTarget = iterator.next();
                iterator.remove();
                if (oppositeAttrMeta.getType().isCollection()) {
                    oppositeAttrMeta.removeValue(prevTarget, sourceEntity);
                } else {
                    oppositeAttrMeta.setValue(prevTarget, null);
                }
            }
        }

        // attach new targets
        for (Object target : targets) {
            if (oppositeAttrMeta != null) {
                if (oppositeAttrMeta.getType().isCollection()) {
                    oppositeAttrMeta.addValue(target, sourceEntity);
                } else {
                    oppositeAttrMeta.setValue(target, sourceEntity);
                }
                em.persist(target);
            }
        }
        attrMeta.setValue(sourceEntity, targets);
    }

    private Class<?> getElementType(MetaAttribute attrMeta) {
        MetaType type = attrMeta.getType();
        if (type.isCollection()) {
            return type.asCollection().getElementType().getImplementationClass();
        } else {
            return type.getImplementationClass();
        }
    }

    @Override
    public void addRelations(S source, Collection<J> targetIds, String fieldName) {
        MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
        MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
        Class<?> targetType = getElementType(attrMeta);

        Object sourceEntity = sourceMapper.unmap(source);
        EntityManager em = getEntityManager();

        for (J targetId : targetIds) {
            Object target = em.find(targetType, targetId);
            attrMeta.addValue(sourceEntity, target);

            if (oppositeAttrMeta != null) {
                if (oppositeAttrMeta.getType().isCollection()) {
                    oppositeAttrMeta.addValue(target, sourceEntity);
                } else {
                    oppositeAttrMeta.setValue(target, sourceEntity);
                }
                em.persist(target);
            }
        }
        em.persist(sourceEntity);
    }

    @Override
    public void removeRelations(S source, Collection<J> targetIds, String fieldName) {
        MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
        MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
        Class<?> targetType = getElementType(attrMeta);

        Object sourceEntity = sourceMapper.unmap(source);
        EntityManager em = getEntityManager();

        for (J targetId : targetIds) {
            Object target = em.find(targetType, targetId);
            attrMeta.removeValue(sourceEntity, target);

            if (target != null && oppositeAttrMeta != null) {
                if (oppositeAttrMeta.getType().isCollection()) {
                    oppositeAttrMeta.removeValue(target, sourceEntity);
                } else {
                    oppositeAttrMeta.setValue(target, null);
                }
            }
        }
    }

    @Override
    public MultivaluedMap<I, T> findTargets(Collection<I> sourceIds, String fieldName, QuerySpec querySpec) {
        List<I> sourceIdLists = new ArrayList<>();
        for (I sourceId : sourceIds) {
            sourceIdLists.add(sourceId);
        }

        if (querySpec.getLimit() != null && sourceIdLists.size() > 1) {
            throw new UnsupportedOperationException("page limit not supported for bulk inclusions");
        }
        // support paging for non-bulk requests
        boolean singleRequest = sourceIdLists.size() == 1;
        boolean pagedSingleRequest = singleRequest && querySpec.getLimit() != null;
        boolean fetchNext = pagedSingleRequest && repositoryConfig.isNextFetched(querySpec);

        QuerySpec bulkQuerySpec = querySpec.clone();

        QuerySpec filteredQuerySpec = JpaRepositoryUtils.filterQuerySpec(repositoryConfig, this, bulkQuerySpec);

        JpaQueryFactory queryFactory = repositoryConfig.getQueryFactory();
        JpaQuery<?> query = queryFactory.query(sourceEntityClass, fieldName, sourceKeyName, sourceIdLists);
        query.setPrivateData(new JpaRequestContext(this, querySpec));
        query.addParentIdSelection();
        query = JpaRepositoryUtils.filterQuery(repositoryConfig, this, filteredQuerySpec, query);

        Class<?> entityClass = repositoryConfig.getEntityClass();
        ComputedAttributeRegistry computedAttributesRegistry = queryFactory.getComputedAttributes();
        Set<String> computedAttrs = computedAttributesRegistry.getForType(entityClass);

        JpaRepositoryUtils.prepareQuery(query, filteredQuerySpec, computedAttrs);

        JpaQueryExecutor<?> executor = query.buildExecutor();
        JpaRepositoryUtils.prepareExecutor(executor, filteredQuerySpec, fetchRelations(fieldName));
        executor = JpaRepositoryUtils.filterExecutor(repositoryConfig, this, filteredQuerySpec, executor);
        if (fetchNext) {
            executor.setLimit(executor.getLimit() + 1);
        }


        List<Tuple> tuples = executor.getResultTuples();
        Boolean hasNext = null;
        if (fetchNext) {
            hasNext = tuples.size() == querySpec.getLimit() + 1;
            if (hasNext) {
                tuples = tuples.subList(0, querySpec.getLimit().intValue());
            }
        }

        tuples = JpaRepositoryUtils.filterTuples(repositoryConfig, this, bulkQuerySpec, tuples);

        MultivaluedMap<I, T> map = mapTuples(tuples);

        if (singleRequest) {
            I sourceId = sourceIdLists.get(0);

            ResourceList<T> Collection;
            if (map.containsKey(sourceId)) {
                Collection = (ResourceList<T>) map.getList(sourceId);
            } else {
                Collection = repositoryConfig.newResultList();
                map.set(sourceId, Collection);
            }

            if (pagedSingleRequest) {
                MetaInformation metaInfo = Collection.getMeta();
                boolean fetchTotal = repositoryConfig.isTotalFetched(filteredQuerySpec);
                if (fetchTotal) {
                    long totalRowCount = executor.getTotalRowCount();
                    ((PagedMetaInformation) metaInfo).setTotalResourceCount(totalRowCount);
                }
                if (fetchNext) {
                    ((HasMoreResourcesMetaInformation) metaInfo).setHasMoreResources(hasNext);
                }
            }
        }

        return map;
    }

    /**
     * By default LookupIncludeBehavior.ALWAYS is in place and we let the relationship repositories load the relations. There
     * is no need to do join fetches, which can lead to problems with paging (evaluated in memory instead of the db).
     *
     * @param fieldName of the relation to fetch
     * @return relation will be eagerly fetched if true
     */
    protected boolean fetchRelations(String fieldName) { // NOSONAR
        return false;
    }

    @SuppressWarnings("unchecked")
    private MultivaluedMap<I, T> mapTuples(List<Tuple> tuples) {
        MultivaluedMap<I, T> map = new MultivaluedMap<I, T>() {

            @Override
            protected List<T> newList() {
                return repositoryConfig.newResultList();
            }
        };
        for (Tuple tuple : tuples) {
            I sourceId = (I) tuple.get(0, Object.class);
            tuple.reduce(1);
            JpaMapper<Object, T> mapper = repositoryConfig.getMapper();
            map.add(sourceId, mapper.map(tuple));
        }
        return map;
    }

    @Override
    public T findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
        MultivaluedMap<I, T> map = findTargets(Arrays.asList(sourceId), fieldName, querySpec);
        if (!map.containsKey(sourceId)) {
            return null;
        }
        List<T> list = map.getList(sourceId);
        return list.isEmpty() ? null : map.getUnique(sourceId);
    }

    @Override
    public ResourceList<T> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
        MultivaluedMap<I, T> map = findTargets(Arrays.asList(sourceId), fieldName, querySpec);
        PreconditionUtil.verify(map.containsKey(sourceId), "result must always include request for single element");
        return (ResourceList<T>) map.getList(sourceId);
    }

    @Override
    public Class<S> getSourceResourceClass() {
        return sourceResourceClass;
    }

    @Override
    public Class<T> getTargetResourceClass() {
        return repositoryConfig.getResourceClass();
    }

    public Class<?> getTargetEntityClass() {
        return repositoryConfig.getEntityClass();
    }
}
