package io.crnk.jpa;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;

import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;
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

/**
 * Exposes a JPA entity as ResourceRepository.
 */
public class JpaEntityRepository<T, I extends Serializable> extends JpaRepositoryBase<T> implements ResourceRepositoryV2<T, I> {

	private MetaEntity meta;

	private MetaAttribute primaryKeyAttr;

	public JpaEntityRepository(JpaModule module, JpaRepositoryConfig<T> config) {
		super(module, config);
		this.meta = module.getJpaMetaLookup().getMeta(config.getEntityClass(), MetaEntity.class);
		this.primaryKeyAttr = JpaRepositoryUtils.getPrimaryKeyAttr(meta);
	}

	@Override
	public final T findOne(I id, QuerySpec querySpec) {
		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(primaryKeyAttr.getName()), FilterOperator.EQ, id));
		List<T> results = findAll(idQuerySpec);
		return getUnique(results, id);
	}

	@Override
	public final ResourceList<T> findAll(Iterable<I> ids, QuerySpec querySpec) {
		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(primaryKeyAttr.getName()), FilterOperator.EQ, ids));
		return findAll(idQuerySpec);
	}

	@Override
	public ResourceList<T> findAll(QuerySpec querySpec) {
		Class<?> entityClass = repositoryConfig.getEntityClass();
		QuerySpec filteredQuerySpec = filterQuerySpec(querySpec);
		JpaQueryFactory queryFactory = module.getQueryFactory();
		JpaQuery<?> query = queryFactory.query(entityClass);
		query.setPrivateData(new JpaRequestContext(this, querySpec));

		ComputedAttributeRegistry computedAttributesRegistry = queryFactory.getComputedAttributes();
		Set<String> computedAttrs = computedAttributesRegistry.getForType(entityClass);

		JpaRepositoryUtils.prepareQuery(query, filteredQuerySpec, computedAttrs);
		query = filterQuery(filteredQuerySpec, query);
		JpaQueryExecutor<?> executor = query.buildExecutor();

		boolean fetchNext = isNextFetched(filteredQuerySpec);
		boolean fetchTotal = isTotalFetched(filteredQuerySpec);

		JpaRepositoryUtils.prepareExecutor(executor, filteredQuerySpec, fetchRelations(null));

		if (fetchNext) {
			executor.setLimit(executor.getLimit() + 1);
		}

		executor = filterExecutor(filteredQuerySpec, executor);

		List<Tuple> tuples = executor.getResultTuples();

		Boolean hasNext = null;
		if (fetchNext) {
			hasNext = tuples.size() == querySpec.getLimit() + 1;
			if (hasNext) {
				tuples = tuples.subList(0, querySpec.getLimit().intValue());
			}
		}

		tuples = filterTuples(filteredQuerySpec, tuples);

		ResourceList<T> resources = repositoryConfig.newResultList();
		MetaInformation metaInfo = resources.getMeta();
		fillResourceList(tuples, resources);
		resources = filterResults(filteredQuerySpec, resources);
		if (fetchTotal) {
			long totalRowCount = executor.getTotalRowCount();
			((PagedMetaInformation) metaInfo).setTotalResourceCount(totalRowCount);
		}
		if (fetchNext) {
			((HasMoreResourcesMetaInformation) metaInfo).setHasMoreResources(hasNext);
		}

		return resources;
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
		EntityManager em = module.getEntityManager();
		em.persist(entity);
		I id = (I) em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);

		// fetch again since we may have to fetch tuple data and do DTO mapping
		QuerySpec querySpec = new QuerySpec(repositoryConfig.getResourceClass());
		if (id == null) {
			throw new IllegalStateException("id not available for entity " + id);
		}
		return (S) findOne(id, querySpec);
	}

	@Override
	public void delete(I id) {
		EntityManager em = module.getEntityManager();

		Object object = em.find(repositoryConfig.getEntityClass(), id);
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

}
