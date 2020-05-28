package io.crnk.data.jpa.internal.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.data.jpa.query.JpaQueryExecutor;
import io.crnk.meta.model.MetaAttributePath;
import io.crnk.meta.model.MetaDataObject;

public abstract class AbstractQueryExecutorImpl<T> implements JpaQueryExecutor<T> {

	private static final String ENTITY_GRAPH_BUILDER_IMPL = "io.crnk.data.jpa.internal.query.EntityGraphBuilderImpl";

	protected int offset = 0;

	protected int limit = -1;

	protected boolean cached = false;

	protected EntityManager em;

	protected int numAutoSelections;

	protected Set<MetaAttributePath> fetchPaths = new HashSet<>();

	protected MetaDataObject meta;

	protected Map<String, Integer> selectionBindings;

	public AbstractQueryExecutorImpl(EntityManager em, MetaDataObject meta, int numAutoSelections,
									 Map<String, Integer> selectionBindings) {
		this.em = em;
		this.meta = meta;
		this.numAutoSelections = numAutoSelections;
		this.selectionBindings = selectionBindings;
	}

	protected static List<Object[]> enforceDistinct(List<?> list) {
		HashSet<TupleElement> distinctSet = new HashSet<>();
		ArrayList<Object[]> distinctResult = new ArrayList<>();
		for (Object obj : list) {
			Object[] values = (Object[]) obj;
			TupleElement tuple = new TupleElement(values);
			if (!distinctSet.contains(tuple)) {
				distinctSet.add(tuple);
				distinctResult.add(values);
			}
		}

		return distinctResult;
	}

	@Override
	public JpaQueryExecutor<T> fetch(List<String> attrPath) {
		// include path an all prefix paths
		MetaAttributePath path = meta.resolvePath(attrPath);
		fetchPaths.add(path);
		return this;
	}

	@Override
	public JpaQueryExecutor<T> setCached(boolean cached) {
		this.cached = cached;
		return this;
	}

	@Override
	public JpaQueryExecutor<T> setOffset(int offset) {
		this.offset = offset;
		return this;
	}

	@Override
	public JpaQueryExecutor<T> setLimit(int limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public int getLimit() {
		return limit;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public void setPaging(PagingSpec pagingSpec) {
		OffsetLimitPagingSpec offsetLimit = pagingSpec.convert(OffsetLimitPagingSpec.class);
		setOffset((int) offsetLimit.getOffset());
		if (offsetLimit.getOffset() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("offset cannot be larger than Integer.MAX_VALUE");
		}
		if (offsetLimit.getLimit() != null) {
			if (offsetLimit.getLimit() > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("limit cannot be larger than Integer.MAX_VALUE");
			}
			setLimit((int) offsetLimit.getLimit().longValue());
		}
	}

	@Override
	public JpaQueryExecutor<T> setWindow(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getResultList() {
		List<?> list = executeQuery();
		// due to sorting & distinct we have a multiselect even
		// if we are only interested in the entites.
		List<T> resultList;
		if (isCompoundSelection()) {
			List<T> entityList = new ArrayList<>();
			for (Object obj : list) {
				Object[] values = (Object[]) obj;
				entityList.add((T) values[0]);
			}
			resultList = entityList;
		} else {
			resultList = (List<T>) list;
		}
		return resultList;
	}

	protected abstract boolean isCompoundSelection();

	@Override
	public T getUniqueResult(boolean nullable) {
		List<T> list = getResultList();
		if (list.size() > 1) {
			throw new IllegalStateException("query does not return unique value, " + list.size() + " results returned");
		}
		if (!list.isEmpty()) {
			return list.get(0);
		} else if (nullable) {
			return null;
		} else {
			throw new IllegalStateException("no result found");
		}
	}

	protected List<Object> truncateTuples(List<?> list, int numToRemove) {
		ArrayList<Object> truncatedList = new ArrayList<>();
		for (Object obj : list) {
			Object[] tuple = (Object[]) obj;
			Object[] truncatedTuple = new Object[tuple.length - numToRemove];
			System.arraycopy(tuple, 0, truncatedTuple, 0, truncatedTuple.length);
			truncatedList.add(truncatedTuple);
		}
		return truncatedList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getEntityClass() {
		return (Class<T>) meta.getImplementationClass();
	}

	protected void applyFetchPaths(Query criteriaQuery) {
		if (!fetchPaths.isEmpty()) {
			EntityGraphBuilder builder;

			// avoid compile-time dependency to support old JPA implementation as long
			// as no fetch paths are used
			ClassLoader classLoader = getClass().getClassLoader();
			Class builderClass = ClassUtils.loadClass(classLoader, ENTITY_GRAPH_BUILDER_IMPL);
			builder = (EntityGraphBuilder) ClassUtils.newInstance(builderClass);

			Class<T> entityClass = getEntityClass();
			builder.build(em, criteriaQuery, entityClass, fetchPaths);
		}
	}

	public abstract Query getTypedQuery();

	protected Query setupQuery(Query typedQuery) {
		// apply graph control
		if (!fetchPaths.isEmpty()) {
			applyFetchPaths(typedQuery);
		}

		// control Hibernate query caching
		if (cached) {
			typedQuery.setHint("org.hibernate.cacheable", Boolean.TRUE);
		}

		if (limit > 0) {
			typedQuery.setMaxResults(limit);
		}
		typedQuery.setFirstResult(offset);
		return typedQuery;
	}

	@SuppressWarnings("rawtypes")
	public List<T> executeQuery() {
		Query typedQuery = getTypedQuery();

		setupQuery(typedQuery);

		// query execution
		List resultList = typedQuery.getResultList();

		// post processing (distinct and tuples => views)
		if (isCompoundSelection() && isDistinct() && hasManyRootsFetchesOrJoins()) {
			resultList = enforceDistinct(resultList);
		}

		if (numAutoSelections > 0) {
			resultList = truncateTuples(resultList, numAutoSelections);
		}

		return resultList;
	}

	protected abstract boolean hasManyRootsFetchesOrJoins();

	protected abstract boolean isDistinct();

	static class TupleElement {

		private Object[] data;

		private int hashCode;

		TupleElement(Object[] data) {
			this.data = data;
			this.hashCode = Arrays.hashCode(data);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof TupleElement)) {
				return false;
			}
			TupleElement tuple = (TupleElement) obj;
			return tuple.hashCode == hashCode && Arrays.equals(data, tuple.data);
		}
	}

}
