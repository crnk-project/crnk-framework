package io.crnk.jpa.internal;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.jpa.JpaModule;
import io.crnk.jpa.JpaRepositoryConfig;
import io.crnk.jpa.JpaRepositoryFilter;
import io.crnk.jpa.mapping.JpaMapper;
import io.crnk.jpa.query.JpaQuery;
import io.crnk.jpa.query.JpaQueryExecutor;
import io.crnk.jpa.query.Tuple;

import java.util.List;

public abstract class JpaRepositoryBase<T> {

	protected JpaModule module;

	protected JpaRepositoryConfig<T> repositoryConfig;

	protected <E> JpaRepositoryBase(JpaModule module, JpaRepositoryConfig<T> repositoryConfig) {
		this.module = module;
		this.repositoryConfig = repositoryConfig;
	}

	protected static <D> D getUnique(List<D> list, Object id) {
		if (list.isEmpty()) {
			throw new ResourceNotFoundException("resource not found: id=" + id);
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			throw new IllegalStateException("unique result expected");
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

	protected QuerySpec filterQuerySpec(QuerySpec querySpec) {
		QuerySpec filteredQuerySpec = querySpec;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredQuerySpec = filter.filterQuerySpec(this, filteredQuerySpec);
			}
		}
		return filteredQuerySpec;
	}

	protected <E> JpaQuery<E> filterQuery(QuerySpec querySpec, JpaQuery<E> query) {
		JpaQuery<E> filteredQuery = query;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredQuery = filter.filterQuery(this, querySpec, filteredQuery);
			}
		}
		return filteredQuery;
	}

	protected <E> JpaQueryExecutor<E> filterExecutor(QuerySpec querySpec, JpaQueryExecutor<E> executor) {
		JpaQueryExecutor<E> filteredExecutor = executor;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredExecutor = filter.filterExecutor(this, querySpec, filteredExecutor);
			}
		}
		return filteredExecutor;
	}

	protected List<Tuple> filterTuples(QuerySpec querySpec, List<Tuple> tuples) {
		List<Tuple> filteredTuples = tuples;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredTuples = filter.filterTuples(this, querySpec, filteredTuples);
			}
		}
		return filteredTuples;
	}

	protected ResourceList<T> filterResults(QuerySpec querySpec, ResourceList<T> resources) {
		ResourceList<T> filteredResources = resources;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredResources = filter.filterResults(this, querySpec, filteredResources);
			}
		}
		return filteredResources;
	}

	protected ResourceList<T> map(List<Tuple> tuples) {
		ResourceList<T> resources = repositoryConfig.newResultList();
		for (Tuple tuple : tuples) {
			JpaMapper<Object, T> mapper = repositoryConfig.getMapper();
			resources.add(mapper.map(tuple));
		}
		return resources;
	}
}
