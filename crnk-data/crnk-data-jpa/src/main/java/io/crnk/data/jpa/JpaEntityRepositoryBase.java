package io.crnk.data.jpa;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.IncludeRelationSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;
import io.crnk.data.jpa.internal.JpaRepositoryBase;
import io.crnk.data.jpa.internal.JpaRepositoryUtils;
import io.crnk.data.jpa.internal.JpaRequestContext;
import io.crnk.data.jpa.mapping.JpaMapper;
import io.crnk.data.jpa.meta.internal.JpaMetaUtils;
import io.crnk.data.jpa.query.ComputedAttributeRegistry;
import io.crnk.data.jpa.query.JpaQuery;
import io.crnk.data.jpa.query.JpaQueryExecutor;
import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.Tuple;

/**
 * Exposes a JPA entity as ResourceRepository. Inherit from this class to setup
 * a repository.
 */
public class JpaEntityRepositoryBase<T, I> extends JpaRepositoryBase<T> implements ResourceRepository<T, I>,
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
	public T findOne(I id, QuerySpec querySpec) {
		String idField = getIdField().getUnderlyingName();
		QuerySpec idQuerySpec = querySpec.clone();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(idField), FilterOperator.EQ, id));
		List<T> results = findAll(idQuerySpec);
		return getUnique(results, id);
	}

	@Override
	public ResourceList<T> findAll(Collection<I> ids, QuerySpec querySpec) {
		String idField = getIdField().getUnderlyingName();
		QuerySpec idQuerySpec = querySpec.clone();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(idField), FilterOperator.EQ, ids));
		return findAll(idQuerySpec);
	}

	@Override
	public ResourceList<T> findAll(QuerySpec querySpec) {
		Class<?> entityClass = repositoryConfig.getEntityClass();
		QuerySpec filteredQuerySpec = JpaRepositoryUtils.filterQuerySpec(repositoryConfig, this, querySpec);
		QuerySpec optimizedQuerySpec = optimizeQuerySpec(filteredQuerySpec);

		JpaQueryFactory queryFactory = repositoryConfig.getQueryFactory();
		JpaQuery<?> query = queryFactory.query(entityClass);
		query.setPrivateData(new JpaRequestContext(this, querySpec));
		configureQuery(query);

		ComputedAttributeRegistry computedAttributesRegistry = queryFactory.getComputedAttributes();
		Set<String> computedAttrs = computedAttributesRegistry.getForType(entityClass);

		JpaRepositoryUtils.prepareQuery(query, optimizedQuerySpec, computedAttrs);
		query = JpaRepositoryUtils.filterQuery(repositoryConfig, this, optimizedQuerySpec, query);
		JpaQueryExecutor<?> executor = createExecutor(query, optimizedQuerySpec);

		boolean fetchNext = isNextFetched(optimizedQuerySpec);
		boolean fetchTotal = isTotalFetched(optimizedQuerySpec);
		int limit = executor.getLimit();
		if (fetchNext) {
			executor.setLimit(limit + 1);
		}

		executor = JpaRepositoryUtils.filterExecutor(repositoryConfig, this, optimizedQuerySpec, executor);

		List<Tuple> tuples = executor.getResultTuples();

		Boolean hasNext = null;
		if (fetchNext) {
			hasNext = tuples.size() == querySpec.getLimit() + 1;
			if (hasNext) {
				tuples = tuples.subList(0, querySpec.getLimit().intValue());
			}
		}

		tuples = JpaRepositoryUtils.filterTuples(repositoryConfig, this, optimizedQuerySpec, tuples);

		ResourceList<T> resources = repositoryConfig.newResultList();
		MetaInformation metaInfo = resources.getMeta();
		JpaRepositoryUtils.fillResourceList(repositoryConfig, tuples, resources);
		resources = JpaRepositoryUtils.filterResults(repositoryConfig, this, optimizedQuerySpec, resources);
		if (fetchTotal) {
			((PagedMetaInformation) metaInfo).setTotalResourceCount(computeTotalCount(executor, tuples));
		}
		if (fetchNext) {
			((HasMoreResourcesMetaInformation) metaInfo).setHasMoreResources(hasNext);
		}

		return resources;
	}

	protected Long computeTotalCount(JpaQueryExecutor<?> executor, List<Tuple> tuples) {
		int limit = executor.getLimit();
		int offset = executor.getOffset();
		if (limit == -1 || tuples.size() < limit) {
			// we don't need to count again if we did not limit or we did not reach the limit
			// avoids an additional query
			return (long) offset + tuples.size();
		} else {
			return executor.getTotalRowCount();
		}
	}

	protected boolean isNextFetched(QuerySpec querySpec) {
		return repositoryConfig.isNextFetched(querySpec);
	}

	protected boolean isTotalFetched(QuerySpec querySpec) {
		return repositoryConfig.isTotalFetched(querySpec);
	}

	protected JpaQueryExecutor<?> createExecutor(JpaQuery<?> query, QuerySpec querySpec) {
		JpaQueryExecutor<?> executor = query.buildExecutor();

		if (optimizeForInclusion(querySpec)) {
			IncludeRelationSpec includedRelationSpec = querySpec.getIncludedRelations().get(0);
			executor.fetch(includedRelationSpec.getAttributePath());
		}

		JpaRepositoryUtils.prepareExecutor(executor, querySpec, fetchRelations());
		return executor;
	}


	/**
	 * override to customize query.
	 */
	protected void configureQuery(JpaQuery<?> query) {
		// override to customize  query
	}

	/**
	 * Make use of @JsonApiRelationId to avoid joins to foreign tables. Further allows
	 * the use of @JsonRelation fields that are not backed by a @JoinColumn but just
	 * a @JsonApiRelationId field.
	 */
	protected QuerySpec optimizeQuerySpec(QuerySpec filteredQuerySpec) {
		QuerySpec clone = filteredQuerySpec.clone();

		String resourceType = filteredQuerySpec.getResourceType();
		RegistryEntry entry = resourceType != null ? resourceRegistry.getEntry(resourceType) : resourceRegistry.getEntry(filteredQuerySpec.getResourceClass());
		ResourceInformation resourceInformation = entry.getResourceInformation();
		List<FilterSpec> filters = clone.getFilters();
		for (FilterSpec filter : filters) {
			PathSpec path = filter.getPath();
			if (path == null || path.getElements().size() < 2) {
				continue;
			}
			List<String> elements = path.getElements();
			String attr1 = elements.get(elements.size() - 2);
			String attr2 = elements.get(elements.size() - 1);
			ResourceField firstField = resourceInformation.findFieldByUnderlyingName(attr1);
			if (firstField != null && firstField.getResourceFieldType() == ResourceFieldType.RELATIONSHIP && firstField.hasIdField() && isRequestingOppositeId(firstField, attr2)) {
				// use primitive field directly rather than joining
				PathSpec optimizedPath = PathSpec.of(elements.subList(0, elements.size() - 2)).append(firstField.getIdName());
				filter.setPath(optimizedPath);
			}
		}

		return clone;
	}

	private boolean isRequestingOppositeId(ResourceField firstField, String requestedField) {
		String oppositeResourceType = firstField.getOppositeResourceType();
		ResourceInformation oppositeInformation = resourceRegistry.getEntry(oppositeResourceType).getResourceInformation();
		ResourceField oppositeField = oppositeInformation.findFieldByUnderlyingName(requestedField);
		return oppositeField.getResourceFieldType() == ResourceFieldType.ID;
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
	 * By default LookupIncludeBehavior.ALWAYS is in place and relationships are loaded by querying the
	 * respective resource repository. This happens internally by Crnk when resolving requested inclusions.
	 * This should be sufficient for most applications. The implementation is highly efficient also for
	 * complex object graphs. And every single subgraph honors Crnk properties like enforced security.
	 * <p>
	 * Still, this flags allows to change the behavior and let Hibernate build a subgraph to query relationships
	 * similar to the Crnk inclusion mechanism. While generally not necessary, it can proof useful if some
	 * of the relationships should be  fetched eagerly do be available on the backend (either directly in the repository
	 * or by backend code calling the repository without involving the rest layer).
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

		// fetch again since we may have to fetch tuple data and do DTO mapping
		QuerySpec querySpec = new QuerySpec(repositoryConfig.getResourceClass());

		// id may differ from primary key
		ResourceField idField = getIdField();
		I id = (I) idField.getAccessor().getValue(resource);
		// id could have been created during persist
		if (id == null) {
			id = getIdFromEntity(em, entity, idField);
		}
		PreconditionUtil.verify(id != null, "id not available for entity %s", resource);
		return (S) findOne(id, querySpec);
	}

	/**
	 * Extracts the resource ID from the entity.
	 * By default it uses the entity's primary key if the field name matches the DTO's ID field.
	 * Override in subclasses if a different entity field should be used.
	 *
	 * @return the resource ID or <code>null</code> when it could not be determined
	 */
	@SuppressWarnings("unchecked")
	protected I getIdFromEntity(EntityManager em, Object entity, ResourceField idField) {
		Object pk = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
		PreconditionUtil.verify(pk != null, "pk not available for entity %s", entity);
		if (pk != null && primaryKeyAttribute.getName().equals(idField.getUnderlyingName()) && idField.getElementType().isAssignableFrom(pk.getClass())) {
			return (I) pk;
		}
		return null;
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
