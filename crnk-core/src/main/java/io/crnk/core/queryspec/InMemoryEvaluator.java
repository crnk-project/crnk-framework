package io.crnk.core.queryspec;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyException;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Applies the given QuerySpec to the provided list in memory. Result available
 * with getResult(). Use QuerySpec.apply to make use of this class.
 * <p>
 * InMemoryEvaluator can be used in two different ways: standalone or backed by a ResourceRegistry.
 * In the later
 * case it also properly supportes @JsonApiRelationId and customized resources (like custom ResourceFieldAccessor implementation).
 * Without a ResourceRegistry, the implementation makes use of heuristics that work well for typical/default use cases.
 */
public class InMemoryEvaluator {

	private ResourceRegistry resourceRegistry;

	public InMemoryEvaluator() {

	}

	public InMemoryEvaluator(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

	public static boolean matches(Object object, FilterSpec filterSpec) {
		InMemoryEvaluator evaluator = new InMemoryEvaluator();
		return evaluator.matchesFilter(object, filterSpec);
	}

	public boolean matchesFilter(Object object, FilterSpec filterSpec) {
		List<FilterSpec> expressions = filterSpec.getExpression();
		if (expressions == null) {
			return matchesPrimitiveOperator(object, filterSpec);
		} else if (filterSpec.getOperator() == FilterOperator.OR) {
			return matchesOr(object, expressions);
		} else if (filterSpec.getOperator() == FilterOperator.AND) {
			return matchesAnd(object, expressions);
		} else if (filterSpec.getOperator() == FilterOperator.NOT) {
			return !matchesFilter(object, FilterSpec.and(expressions));
		}
		throw new UnsupportedOperationException("not implemented " + filterSpec);
	}

	protected boolean matchesPrimitiveOperator(Object object, FilterSpec filterSpec) {
		if (filterSpec.getAttributePath() == null) {
			throw new BadRequestException("no attribute specified for filter parameter");
		}
		Object value = getProperty(object, filterSpec.getAttributePath());
		FilterOperator operator = filterSpec.getOperator();
		Object filterValue = filterSpec.getValue();
		if (value instanceof Collection) {
			return matchesAny((Collection<?>) value, operator, filterValue);
		} else {
			return operator.matches(value, filterValue);
		}
	}

	protected Object getProperty(Object object, List<String> pathElements) {
		if (pathElements.isEmpty()) {
			return object;
		}
		Class<?> clazz = object.getClass();
		if (resourceRegistry != null && resourceRegistry.hasEntry(clazz)) {
			return getResourceProperty(object, pathElements);
		}


		Object value = PropertyUtils.getProperty(object, pathElements);

		// TODO access to ResourceInformation needed for support of custom naming
		int pathLength = pathElements.size();
		if (value == null && pathLength >= 2 && pathElements.get(pathLength - 1).equals("id")) {
			// check ID field as well for relationships
			List<String> idAttributePath = new ArrayList<>(pathElements.subList(0, pathLength - 2));
			idAttributePath.add(pathElements.get(pathLength - 2) + "Id");
			try {
				return PropertyUtils.getProperty(object, idAttributePath);
			} catch (PropertyException e) {
				return null; // property does not exist
			}
		}
		return value;
	}

	private Object getResourceProperty(Object object, List<String> pathElements) {
		Class<?> clazz = object.getClass();
		RegistryEntry entry = resourceRegistry.getEntry(clazz);
		ResourceInformation resourceInformation = entry.getResourceInformation();

		int consumedElements = 1;
		String firstPathElement = pathElements.get(0);
		ResourceField resourceField = resourceInformation.findFieldByUnderlyingName(firstPathElement);
		PreconditionUtil.verify(resourceField != null, "resource field %s in %s not found", firstPathElement, clazz);

		Object value = resourceField.getAccessor().getValue(object);
		boolean isRelationship = resourceField.getResourceFieldType() == ResourceFieldType.RELATIONSHIP;
		if (value == null && pathElements.size() >= 2 && isRelationship && resourceField.hasIdField()) {
			String secondPathElement = pathElements.get(1);

			RegistryEntry oppositeEntry = resourceRegistry.getEntry(resourceField.getOppositeResourceType());
			ResourceField idField = oppositeEntry.getResourceInformation().getIdField();
			if (secondPathElement.equals(idField.getUnderlyingName())) {
				value = resourceField.getIdAccessor().getValue(object);
				consumedElements++;
			}
		}

		List<String> childElements = pathElements.subList(consumedElements, pathElements.size());
		return getProperty(value, childElements);
	}

	protected boolean matchesAny(Collection<?> col, FilterOperator operator, Object filterValue) {
		for (Object elem : col) {
			boolean matches = operator.matches(elem, filterValue);
			if (matches) {
				return true;
			}
		}
		return false;
	}

	protected boolean matchesOr(Object object, List<FilterSpec> expressions) {
		for (FilterSpec expr : expressions) {
			if (matchesFilter(object, expr)) {
				return true;
			}
		}
		return false;
	}

	protected boolean matchesAnd(Object object, List<FilterSpec> expressions) {
		for (FilterSpec expr : expressions) {
			if (!matchesFilter(object, expr)) {
				return false;
			}
		}
		return true;
	}

	public <T> ResourceList<T> eval(Iterable<T> resources, QuerySpec querySpec) {
		DefaultResourceList<T> resultList = new DefaultResourceList<>();
		resultList.setMeta(new DefaultPagedMetaInformation());
		eval(resources, querySpec, resultList);
		return resultList;
	}

	public <T> void eval(Iterable<T> resources, QuerySpec querySpec, ResourceList<T> resultList) {
		Iterator<T> iterator = resources.iterator();
		while (iterator.hasNext()) {
			resultList.add(iterator.next());
		}

		// filter
		if (!querySpec.getFilters().isEmpty()) {
			FilterSpec filterSpec = FilterSpec.and(querySpec.getFilters());
			applyFilter(resultList, filterSpec);
		}
		long totalCount = resultList.size();

		// sort
		applySorting(resultList, querySpec.getSort());

		// offset/limit
		applyPaging(resultList, querySpec);

		// set page information
		if (querySpec.getLimit() != null || querySpec.getOffset() != 0) {
			MetaInformation meta = resultList.getMeta();
			if (meta instanceof PagedMetaInformation) {
				PagedMetaInformation pagedMeta = (PagedMetaInformation) meta;
				pagedMeta.setTotalResourceCount(totalCount);
			}
			if (meta instanceof HasMoreResourcesMetaInformation) {
				HasMoreResourcesMetaInformation pagedMeta = (HasMoreResourcesMetaInformation) meta;
				pagedMeta.setHasMoreResources(totalCount > querySpec.getOffset() + querySpec.getLimit());
			}
		}
	}

	protected <T> void applySorting(List<T> results, List<SortSpec> sortSpec) {
		if (!sortSpec.isEmpty()) {
			Collections.sort(results, new SortSpecComparator<>(sortSpec));
		}
	}

	protected <T> void applyPaging(List<T> results, QuerySpec querySpec) {
		int offset = (int) Math.min(querySpec.getOffset(), Integer.MAX_VALUE);
		int limit = (int) Math.min(Integer.MAX_VALUE, querySpec.getLimit() != null ? querySpec.getLimit() : Integer.MAX_VALUE);
		if (offset > results.size()) {
			throw new BadRequestException("page offset out of range, cannot move beyond data set");
		}
		limit = Math.min(results.size() - offset, limit);
		if (offset > 0 || limit < results.size()) {
			List<T> subList = new ArrayList<>(results.subList(offset, offset + limit));
			results.clear();
			results.addAll(subList);
		}
	}

	protected <T> void applyFilter(List<T> results, FilterSpec filterSpec) {
		if (filterSpec != null) {
			Iterator<T> iterator = results.iterator();
			while (iterator.hasNext()) {
				T next = iterator.next();
				if (!matchesFilter(next, filterSpec)) {
					iterator.remove();
				}
			}
		}
	}

	class SortSpecComparator<T> implements Comparator<T> {

		private List<SortSpec> sortSpecs;

		public SortSpecComparator(List<SortSpec> sortSpecs) {
			this.sortSpecs = sortSpecs;
		}

		@Override
		@SuppressWarnings("unchecked")
		public int compare(T o1, T o2) {
			for (SortSpec orderSpec : sortSpecs) {
				Comparable<Object> value1 = (Comparable<Object>) getProperty(o1, orderSpec.getAttributePath());
				Comparable<Object> value2 = (Comparable<Object>) getProperty(o2, orderSpec.getAttributePath());

				int d = compare(value1, value2);
				if (orderSpec.getDirection() == Direction.DESC) {
					d = -d;
				}
				if (d != 0) {
					return d;
				}
			}
			return 0;
		}

		private int compare(Comparable<Object> value1, Comparable<Object> value2) {
			if (value1 == null && value2 == null) {
				return 0;
			}
			if (value1 == null) {
				return -1;
			}
			if (value2 == null) {
				return 1;
			}

			return value1.compareTo(value2);
		}
	}
}
