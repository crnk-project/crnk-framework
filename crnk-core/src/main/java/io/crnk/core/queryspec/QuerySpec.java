package io.crnk.core.queryspec;

import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.CompareUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;

import java.util.*;
import java.util.Map.Entry;

public class QuerySpec {

    private Class<?> resourceClass;

    private String resourceType;

    private List<FilterSpec> filters = new ArrayList<>();

    private List<SortSpec> sort = new ArrayList<>();

    private List<IncludeFieldSpec> includedFields = new ArrayList<>();

    private List<IncludeRelationSpec> includedRelations = new ArrayList<>();

    private Map<String, QuerySpec> typeRelatedSpecs = new HashMap<>();
    private Map<Class<?>, QuerySpec> classRelatedSpecs = new HashMap<>();

    private PagingSpec pagingSpec;

    public QuerySpec(Class<?> resourceClass) {
        this(resourceClass, null);
    }

    public QuerySpec(String resourceType) {
        this(null, resourceType);
    }

    public QuerySpec(Class<?> resourceClass, String resourceType) {
        verifyNotNull(resourceClass, resourceType);
        if (resourceClass != Resource.class) {
            this.resourceClass = resourceClass;
        }
        if (resourceType == null) {
			JsonApiResource annotation = resourceClass.getAnnotation(JsonApiResource.class);
			if (annotation != null) {
				this.resourceType = annotation.type();
			}
		} else {
			this.resourceType = resourceType;
		}
        this.pagingSpec = new OffsetLimitPagingSpec();
    }

    public QuerySpec(ResourceInformation resourceInformation) {
        this(resourceInformation.getResourceClass(), resourceInformation.getResourceType());
    }

    public void accept(QuerySpecVisitor visitor) {
        if (visitor.visitStart(this)) {
            visitFilters(visitor, filters);
            for (SortSpec spec : sort) {
                if (visitor.visitSort(spec)) {
                    visitor.visitPath(spec.getPath());
                }
            }
            for (IncludeFieldSpec spec : includedFields) {
                if (visitor.visitField(spec)) {
                    visitor.visitPath(spec.getPath());
                }
            }
            for (IncludeRelationSpec spec : includedRelations) {
                if (visitor.visitInclude(spec)) {
                    visitor.visitPath(spec.getPath());
                }
            }
            if (pagingSpec != null) {
                visitor.visitPaging(pagingSpec);
            }
            typeRelatedSpecs.values().forEach(it -> it.accept(visitor));
			classRelatedSpecs.values().forEach(it -> it.accept(visitor));
            visitor.visitEnd(this);
        }
    }

    private void visitFilters(QuerySpecVisitor visitor, List<FilterSpec> filters) {
        for (FilterSpec spec : filters) {
            if (visitor.visitFilterStart(spec)) {
                if (spec.hasExpressions()) {
                    visitFilters(visitor, spec.getExpression());
                } else {
                    visitor.visitPath(spec.getPath());
                }
                visitor.visitFilterEnd(spec);
            }
        }
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
        result = prime * result + ((pagingSpec == null) ? 0 : pagingSpec.hashCode());
        result = prime * result + ((typeRelatedSpecs == null) ? 0 : typeRelatedSpecs.hashCode());
		result = prime * result + ((classRelatedSpecs == null) ? 0 : classRelatedSpecs.hashCode());
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
                && CompareUtils.isEquals(includedFields, other.includedFields)
                && CompareUtils.isEquals(includedRelations, other.includedRelations)
                && CompareUtils.isEquals(pagingSpec, other.pagingSpec)
                && CompareUtils.isEquals(typeRelatedSpecs, other.typeRelatedSpecs)
				&& CompareUtils.isEquals(classRelatedSpecs, other.classRelatedSpecs)
                && CompareUtils.isEquals(sort, other.sort);
    }

    public Long getLimit() {
        OffsetLimitPagingSpec offsetLimitPagingSpec = getPaging(OffsetLimitPagingSpec.class);
        return offsetLimitPagingSpec.getLimit();
    }

    public void setLimit(Long limit) {
        OffsetLimitPagingSpec offsetLimitPagingSpec = getPaging(OffsetLimitPagingSpec.class);
        offsetLimitPagingSpec.setLimit(limit);
    }

    public long getOffset() {
        OffsetLimitPagingSpec offsetLimitPagingSpec = getPaging(OffsetLimitPagingSpec.class);
        return offsetLimitPagingSpec.getOffset();
    }

    public void setOffset(long offset) {
        OffsetLimitPagingSpec offsetLimitPagingSpec = getPaging(OffsetLimitPagingSpec.class);
        offsetLimitPagingSpec.setOffset(offset);
    }

    public PagingSpec getPaging() {
        return pagingSpec;
    }

    public <T extends PagingSpec> T getPaging(Class<T> pagingSpecType) {
        if (pagingSpec == null) {
            return null;
        }
        if (pagingSpecType.isInstance(pagingSpec)) {
            return (T) pagingSpec;
        }
        return pagingSpec.convert(pagingSpecType);
    }

    public QuerySpec setPaging(final PagingSpec pagingSpec) {
        this.pagingSpec = pagingSpec;
        return this;
    }

    public List<FilterSpec> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterSpec> filters) {
        this.filters = filters;
    }

    public Optional<FilterSpec> findFilter(final PathSpec pathSpec) {
        for (FilterSpec filterSpec : filters) {
            if (filterSpec.getPath().equals(pathSpec)) {
                return Optional.of(filterSpec);
            }
        }
        return Optional.empty();
    }

    public Optional<FilterSpec> findFilter(final PathSpec pathSpec, FilterOperator operator) {
        for (FilterSpec filterSpec : filters) {
            if (filterSpec.getPath().equals(pathSpec) && operator.equals(filterSpec.getOperator())) {
                return Optional.of(filterSpec);
            }
        }
        return Optional.empty();
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

    public Collection<QuerySpec> getNestedSpecs() {
    	// Using a set to remove duplicate querySpec between typeRelatedSpecs and classRelatedSpecs
    	Set<QuerySpec> allRelatedSpecs = new HashSet(typeRelatedSpecs.values());
    	allRelatedSpecs.addAll(classRelatedSpecs.values());
        return Collections.unmodifiableCollection(allRelatedSpecs);
    }

    public void setNestedSpecs(Collection<QuerySpec> specs) {
    	this.typeRelatedSpecs.clear();
        this.classRelatedSpecs.clear();
        for (QuerySpec spec : specs) {
	    if (spec.getResourceType() != null) {
                typeRelatedSpecs.put(spec.getResourceType(), spec);
            } 
        if (spec.getResourceClass() != null) {
                classRelatedSpecs.put(spec.getResourceClass(), spec);
            }
        }
    }

    public void addFilter(FilterSpec filterSpec) {
        this.filters.add(filterSpec);
    }

    public void addSort(SortSpec sortSpec) {
        this.sort.add(sortSpec);
    }

    public void includeField(List<String> attrPath) {
        includeField(PathSpec.of(attrPath));
    }

    public void includeField(PathSpec path) {
        this.includedFields.add(new IncludeFieldSpec(path));
    }

    public void includeRelation(List<String> attrPath) {
        includeRelation(PathSpec.of(attrPath));
    }

    public void includeRelation(PathSpec path) {
        this.includedRelations.add(new IncludeRelationSpec(path));
    }

    public QuerySpec getQuerySpec(String resourceType) {
        if (resourceType.equals(this.resourceType)) {
            return this;
        }
        return typeRelatedSpecs.get(resourceType);
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
        return classRelatedSpecs.get(resourceClass);
    }


    public QuerySpec getQuerySpec(ResourceInformation resourceInformation) {
        QuerySpec querySpec = getQuerySpec(resourceInformation.getResourceType());
        if (querySpec == null) {
            querySpec = getQuerySpec(resourceInformation.getResourceClass());
        }
        return querySpec;
    }

    public QuerySpec getOrCreateQuerySpec(String resourceType) {
        return getOrCreateQuerySpec(null, resourceType);
    }

    public QuerySpec getOrCreateQuerySpec(ResourceInformation resourceInformation) {
        return getOrCreateQuerySpec(resourceInformation.getResourceClass(), resourceInformation.getResourceType());
    }

    public QuerySpec getOrCreateQuerySpec(Class<?> targetResourceClass) {
        return getOrCreateQuerySpec(targetResourceClass, null);
    }

    public QuerySpec getOrCreateQuerySpec(Class<?> targetResourceClass, String targetResourceType) {
        verifyNotNull(targetResourceClass, targetResourceType);

        QuerySpec querySpec = null;
        // First work with resourceType not null
		if (targetResourceType != null) {
			querySpec = getQuerySpec(targetResourceType);
			if (querySpec == null) {
				querySpec = new QuerySpec(targetResourceClass, targetResourceType);
				typeRelatedSpecs.put(targetResourceType, querySpec);
				if (targetResourceClass != null) {
					classRelatedSpecs.put(targetResourceClass, querySpec);
				}
			}
		} else { // Fallback to targetResourceClass which can't be null because of verifyNotNull
			querySpec = getQuerySpec(targetResourceClass);
			if (querySpec == null) {
				querySpec = new QuerySpec(targetResourceClass);
				classRelatedSpecs.put(targetResourceClass, querySpec);
			}
		}
        querySpec.setPaging(pagingSpec);
        return querySpec;
    }

    private static void verifyNotNull(Class<?> targetResourceClass, String targetResourceType) {
        PreconditionUtil
                .verify(targetResourceClass != null || targetResourceType != null, "at least one parameter must not be null");
        if (targetResourceClass == Resource.class && targetResourceType == null) {
            throw new IllegalArgumentException("must specify resourceType if io.crnk.core.engine.document.Resource is used");
        }
    }


    public void putRelatedSpec(Class<?> relatedResourceClass, QuerySpec relatedSpec) {
        if (relatedResourceClass.equals(resourceClass)) {
            throw new IllegalArgumentException("cannot set related spec with root resourceClass");
        }
        classRelatedSpecs.put(relatedResourceClass, relatedSpec);
    }

    public QuerySpec clone() {
        QuerySpec copy = new QuerySpec(resourceClass, resourceType);
        if (pagingSpec != null) {
            copy.pagingSpec = pagingSpec.clone();
        }
        for (IncludeFieldSpec includedField : includedFields) {
            copy.includedFields.add(includedField.clone());
        }
        for (IncludeRelationSpec includeRelationSpec : includedRelations) {
            copy.includedRelations.add(includeRelationSpec.clone());
        }
        for (SortSpec sortSpec : sort) {
            copy.sort.add(sortSpec.clone());
        }
        for (FilterSpec filterSpec : filters) {
            copy.filters.add(filterSpec.clone());
        }
		for (Entry<String, QuerySpec> entry : typeRelatedSpecs.entrySet()) {
			copy.typeRelatedSpecs.put(entry.getKey(), entry.getValue().clone());
		}
		for (Entry<Class<?>, QuerySpec> entry : classRelatedSpecs.entrySet()) {
			copy.classRelatedSpecs.put(entry.getKey(), entry.getValue().clone());
		}
        return copy;
    }


    @Override
    public String toString() {
        return "QuerySpec[" +
                (resourceClass != null ? "resourceClass=" + resourceClass.getName() : "") +
                (resourceType != null && resourceClass != null ? ", " : "") +
                (resourceType != null ? "resourceType=" + resourceType : "") +
                (pagingSpec != null ? ", paging=" + pagingSpec : "") +
                (!filters.isEmpty() ? ", filters=" + filters : "") +
                (!sort.isEmpty() ? ", sort=" + sort : "") +
                (!includedFields.isEmpty() ? ", includedFields=" + includedFields : "") +
                (!includedRelations.isEmpty() ? ", includedRelations=" + includedRelations : "") +
                (!typeRelatedSpecs.isEmpty() ? ", typeRelatedSpecs=" + typeRelatedSpecs : "") +
				(!classRelatedSpecs.isEmpty() ? ", classRelatedSpecs=" + classRelatedSpecs : "") +
                ']';
    }
}
