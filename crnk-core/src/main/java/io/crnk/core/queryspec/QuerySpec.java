package io.crnk.core.queryspec;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.CompareUtils;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;

import java.util.*;
import java.util.Map.Entry;

public class QuerySpec {

	private Class<?> resourceClass;

	private String resourceType;

	private Long limit = null;

	private long offset = 0;

	private List<FilterSpec> filters = new ArrayList<>();

	private List<SortSpec> sort = new ArrayList<>();

	private List<IncludeFieldSpec> includedFields = new ArrayList<>();

	private List<IncludeRelationSpec> includedRelations = new ArrayList<>();

	private Map<Object, QuerySpec> relatedSpecs = new HashMap<>();

	private Map<String, Set<String>> queryParams = new HashMap<>();

	public QuerySpec(Class<?> resourceClass) {
		this.resourceClass = resourceClass;
	}

	public QuerySpec(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceType() {
		return resourceType;
	}

	public Class<?> getResourceClass() {
		return resourceClass;
	}

	/**
	 * Evaluates this querySpec against the provided list in memory. It supports
	 * sorting, filter and paging.
	 * <p>
	 * TODO currently ignores relations and inclusions, has room for
	 * improvements
	 *
	 * @param <T>       the type of resources in this Iterable
	 * @param resources resources
	 * @return sorted, filtered list.
	 */
	public <T> DefaultResourceList<T> apply(Iterable<T> resources) {
		DefaultResourceList<T> resultList = new DefaultResourceList<>();
		resultList.setMeta(new DefaultPagedMetaInformation());
		apply(resources, resultList);
		return resultList;
	}

	/**
	 * Evaluates this querySpec against the provided list in memory. It supports
	 * sorting, filter and paging. Make sure that the resultList carries meta
	 * and links information of type PagedMetaInformation resp.
	 * PagedLinksInformation to let Crnk compute pagination links.
	 * <p>
	 * TODO currently ignores relations and inclusions
	 *
	 * @param <T>        resource type
	 * @param resources  to apply the querySpec to
	 * @param resultList used to return the result (including paging meta information)
	 */
	public <T> void apply(Iterable<T> resources, ResourceList<T> resultList) {
		InMemoryEvaluator eval = new InMemoryEvaluator();
		eval.eval(resources, this, resultList);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filters == null) ? 0 : filters.hashCode());
		result = prime * result + ((includedFields == null) ? 0 : includedFields.hashCode());
		result = prime * result + ((includedRelations == null) ? 0 : includedRelations.hashCode());
		result = prime * result + ((limit == null) ? 0 : limit.hashCode());
		result = prime * result + Long.valueOf(offset).hashCode();
		result = prime * result + ((relatedSpecs == null) ? 0 : relatedSpecs.hashCode());
		result = prime * result + ((sort == null) ? 0 : sort.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		QuerySpec other = (QuerySpec) obj;
		return CompareUtils.isEquals(filters, other.filters) // NOSONAR
				&& CompareUtils.isEquals(includedFields, other.includedFields) && CompareUtils
				.isEquals(includedRelations, other.includedRelations) && CompareUtils.isEquals(limit, other.limit)
				&& CompareUtils.isEquals(offset, other.offset) && CompareUtils.isEquals(relatedSpecs, other.relatedSpecs)
				&& CompareUtils.isEquals(sort, other.sort);
	}

	public Long getLimit() {
		return limit;
	}

	public void setLimit(Long limit) {
		this.limit = limit;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public List<FilterSpec> getFilters() {
		return filters;
	}

	public void setFilters(List<FilterSpec> filters) {
		this.filters = filters;
	}

	public List<SortSpec> getSort() {
		return sort;
	}

	public void setSort(List<SortSpec> sort) {
		this.sort = sort;
	}

	public List<IncludeFieldSpec> getIncludedFields() {
		return includedFields;
	}

	public void setIncludedFields(List<IncludeFieldSpec> includedFields) {
		this.includedFields = includedFields;
	}

	public List<IncludeRelationSpec> getIncludedRelations() {
		return includedRelations;
	}

	public void setIncludedRelations(List<IncludeRelationSpec> includedRelations) {
		this.includedRelations = includedRelations;
	}

	/**
	 * @deprecated make use of getNestedSpecs
	 */
	@Deprecated
	public Map<Class<?>, QuerySpec> getRelatedSpecs() {
		return (Map) relatedSpecs;
	}

	public Collection<QuerySpec> getNestedSpecs() {
		return Collections.unmodifiableCollection(relatedSpecs.values());
	}

	public void setNestedSpecs(Collection<QuerySpec> specs) {
		this.relatedSpecs.clear();
		for (QuerySpec spec : specs) {
			if (spec.getResourceClass() != null) {
				relatedSpecs.put(spec.getResourceClass(), spec);
			} else {
				relatedSpecs.put(spec.getResourceType(), spec);
			}
		}
	}

	@Deprecated
	public void setRelatedSpecs(Map<Class<?>, QuerySpec> relatedSpecs) {
		this.relatedSpecs = (Map) relatedSpecs;
	}

	public void addFilter(FilterSpec filterSpec) {
		this.filters.add(filterSpec);
	}

	public void addSort(SortSpec sortSpec) {
		this.sort.add(sortSpec);
	}

	public void includeField(List<String> attrPath) {
		this.includedFields.add(new IncludeFieldSpec(attrPath));
	}

	public void includeRelation(List<String> attrPath) {
		this.includedRelations.add(new IncludeRelationSpec(attrPath));
	}

	public QuerySpec getQuerySpec(String resourceType) {
		if (resourceType.equals(this.resourceType))
			return this;
		return relatedSpecs.get(resourceType);
	}

	public QuerySpec getOrCreateQuerySpec(String resourceType) {
		QuerySpec querySpec = getQuerySpec(resourceType);
		if (querySpec == null) {
			querySpec = new QuerySpec(resourceType);
			relatedSpecs.put(resourceType, querySpec);
		}
		return querySpec;
	}

	/**
	 * @param resourceClass resource class
	 * @return QuerySpec for the given class, either the root QuerySpec or any
	 * related QuerySpec.
	 */
	public QuerySpec getQuerySpec(Class<?> resourceClass) {
		if (resourceClass.equals(this.resourceClass)) {
			return this;
		}
		return relatedSpecs.get(resourceClass);
	}

	public QuerySpec getOrCreateQuerySpec(Class<?> resourceClass) {
		QuerySpec querySpec = getQuerySpec(resourceClass);
		if (querySpec == null) {
			querySpec = new QuerySpec(resourceClass);
			relatedSpecs.put(resourceClass, querySpec);
		}
		return querySpec;
	}

	public void putRelatedSpec(Class<?> relatedResourceClass, QuerySpec relatedSpec) {
		if (relatedResourceClass.equals(resourceClass)) {
			throw new IllegalArgumentException("cannot set related spec with root resourceClass");
		}
		relatedSpecs.put(relatedResourceClass, relatedSpec);
	}

	public QuerySpec duplicate() {
		QuerySpec copy = new QuerySpec(resourceClass);
		copy.limit = limit;
		copy.offset = offset;
		copy.includedFields.addAll(includedFields);
		copy.includedRelations.addAll(includedRelations);
		copy.sort.addAll(sort);
		copy.filters.addAll(filters);

		Iterator<Entry<Object, QuerySpec>> iterator = relatedSpecs.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Object, QuerySpec> entry = iterator.next();
			copy.relatedSpecs.put(entry.getKey(), entry.getValue().duplicate());
		}
		return copy;
	}

	public QuerySpec getQuerySpec(ResourceInformation resourceInformation) {
		return getQuerySpec(resourceInformation.getResourceClass());
	}

	public QuerySpec getOrCreateQuerySpec(ResourceInformation resourceInformation) {
		return getOrCreateQuerySpec(resourceInformation.getResourceClass());
	}

	public void addQueryParam(String name, Set<String> value) {
		this.queryParams.put(name, value);
	}

	public Map<String, Set<String>> getQueryParams() {
		return queryParams;
	}

	public Set<String> getQueryParamValues(String name) {
		return queryParams.get(name);
	}

	public String getQueryParamValue(String name) {
		return queryParams.get(name).iterator().next();
	}

	@Override
	public String toString() {
		return "QuerySpec{" +
				(resourceClass != null ? "resourceClass=" + resourceClass.getName() : "") +
				(resourceType != null ? "resourceType=" + resourceType : "") +
				(limit != null ? ", limit=" + limit : "") +
				(offset > 0 ? ", offset=" + offset : "") +
				(!filters.isEmpty() ? ", filters=" + filters : "") +
				(!sort.isEmpty() ? ", sort=" + sort : "") +
				(!includedFields.isEmpty() ? ", includedFields=" + includedFields : "") +
				(!includedRelations.isEmpty() ? ", includedRelations=" + includedRelations : "") +
				(!relatedSpecs.isEmpty() ? ", relatedSpecs=" + relatedSpecs : "") +
				'}';
	}
}
