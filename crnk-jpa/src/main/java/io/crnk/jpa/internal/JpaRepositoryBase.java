package io.crnk.jpa.internal;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;
import io.crnk.jpa.JpaModule;
import io.crnk.jpa.JpaRepositoryConfig;
import io.crnk.jpa.JpaRepositoryFilter;
import io.crnk.jpa.mapping.JpaMapper;
import io.crnk.jpa.query.JpaQuery;
import io.crnk.jpa.query.JpaQueryExecutor;
import io.crnk.jpa.query.Tuple;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryExecutor;
import io.crnk.jpa.query.criteria.JpaCriteriaRepositoryFilter;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

public abstract class JpaRepositoryBase<T> {

	protected EntityManager em;

	protected JpaRepositoryConfig<T> repositoryConfig;

	/**
	 * @deprecated use other constructor. Deprecated to remove JpaModule dependency and allow using this class stand-alone.
	 */
	@Deprecated
	protected <E> JpaRepositoryBase(JpaModule module, JpaRepositoryConfig<T> repositoryConfig) {
		JpaRepositoryUtils.setDefaultConfig(module.getConfig(), repositoryConfig);
		this.em = repositoryConfig.getQueryFactory().getEntityManager();
		this.repositoryConfig = repositoryConfig;
	}

	protected <E> JpaRepositoryBase(JpaRepositoryConfig<T> repositoryConfig) {
		this.em = repositoryConfig.getQueryFactory().getEntityManager();
		this.repositoryConfig = repositoryConfig;
		PreconditionUtil.verify(em != null,
				"entityManager not available, make sure to pass an EntityManager to XyQueryFactory.create(em) when using " + getClass().getSimpleName()
						+ " in standalone without a JpaModule");
	}


	protected boolean isNextFetched(QuerySpec querySpec) {
		return querySpec.getLimit() != null && !repositoryConfig.isTotalAvailable()
				&& repositoryConfig.getListMetaClass() != null
				&& HasMoreResourcesMetaInformation.class.isAssignableFrom(repositoryConfig
				.getListMetaClass());
	}

	protected boolean isTotalFetched(QuerySpec querySpec) {
		return querySpec.getLimit() != null && repositoryConfig.isTotalAvailable()
				&& repositoryConfig.getListMetaClass() != null
				&& PagedMetaInformation.class.isAssignableFrom(repositoryConfig.getListMetaClass());
	}

	protected <D> D getUnique(List<D> list, Object id) {
		if (list.isEmpty()) {
			throw new ResourceNotFoundException(
					"resource not found: type=" + repositoryConfig.getResourceClass().getSimpleName() + " id=" + id);
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			throw new IllegalStateException(
					"unique result expected: " + repositoryConfig.getResourceClass().getSimpleName() + " id=" + id);
		}
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

	public QuerySpec filterQuerySpec(QuerySpec querySpec) {
		JpaMapper<Object, T> mapper = repositoryConfig.getMapper();
		QuerySpec filteredQuerySpec = mapper.unmapQuerySpec(querySpec);
		for (JpaRepositoryFilter filter : repositoryConfig.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredQuerySpec = filter.filterQuerySpec(this, filteredQuerySpec);
			}
		}
		return filteredQuerySpec;
	}

	public <E> JpaQuery<E> filterQuery(QuerySpec querySpec, JpaQuery<E> query) {
		JpaQuery<E> filteredQuery = query;
		for (JpaRepositoryFilter filter : repositoryConfig.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredQuery = filter.filterQuery(this, querySpec, filteredQuery);
			}
		}
		return filteredQuery;
	}

	protected <E> JpaQueryExecutor<E> filterExecutor(QuerySpec querySpec, JpaQueryExecutor<E> executor) {
		JpaQueryExecutor<E> filteredExecutor = executor;
		for (JpaRepositoryFilter filter : repositoryConfig.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredExecutor = filter.filterExecutor(this, querySpec, filteredExecutor);

				if (filter instanceof JpaCriteriaRepositoryFilter && executor instanceof JpaCriteriaQueryExecutor) {
					JpaCriteriaRepositoryFilter criteriaFilter = (JpaCriteriaRepositoryFilter) filter;
					CriteriaQuery criteriaQuery = ((JpaCriteriaQueryExecutor<E>) executor).getQuery();
					criteriaFilter.filterCriteriaQuery(this, querySpec, criteriaQuery);
				}
			}
		}
		return filteredExecutor;
	}

	protected List<Tuple> filterTuples(QuerySpec querySpec, List<Tuple> tuples) {
		List<Tuple> filteredTuples = tuples;
		for (JpaRepositoryFilter filter : repositoryConfig.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredTuples = filter.filterTuples(this, querySpec, filteredTuples);
			}
		}
		return filteredTuples;
	}

	protected ResourceList<T> filterResults(QuerySpec querySpec, ResourceList<T> resources) {
		ResourceList<T> filteredResources = resources;
		for (JpaRepositoryFilter filter : repositoryConfig.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredResources = filter.filterResults(this, querySpec, filteredResources);
			}
		}
		return filteredResources;
	}

	protected ResourceList<T> fillResourceList(List<Tuple> tuples, ResourceList<T> resources) {
		for (Tuple tuple : tuples) {
			JpaMapper<Object, T> mapper = repositoryConfig.getMapper();
			resources.add(mapper.map(tuple));
		}
		return resources;
	}
}
