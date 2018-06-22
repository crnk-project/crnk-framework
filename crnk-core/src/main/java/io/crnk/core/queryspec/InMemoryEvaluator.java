package io.crnk.core.queryspec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import io.crnk.core.engine.internal.utils.PropertyException;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;

/**
 * Applies the given QuerySpec to the provided list in memory. Result available
 * with getResult(). Use QuerySpec.apply to make use of this class.
 */
public class InMemoryEvaluator {

	public static boolean matches(Object object, FilterSpec filterSpec) {
		List<FilterSpec> expressions = filterSpec.getExpression();
		if (expressions == null) {
			return matchesPrimitiveOperator(object, filterSpec);
		}
		else if (filterSpec.getOperator() == FilterOperator.OR) {
			return matchesOr(object, expressions);
		}
		else if (filterSpec.getOperator() == FilterOperator.AND) {
			return matchesAnd(object, expressions);
		}
		else if (filterSpec.getOperator() == FilterOperator.NOT) {
			return !matches(object, FilterSpec.and(expressions));
		}
		throw new UnsupportedOperationException("not implemented " + filterSpec);
	}

	private static boolean matchesPrimitiveOperator(Object object, FilterSpec filterSpec) {
		if (filterSpec.getAttributePath() == null) {
			throw new BadRequestException("no attribute specified for filter parameter");
		}
		Object value = getProperty(object, filterSpec.getAttributePath());
		FilterOperator operator = filterSpec.getOperator();
		Object filterValue = filterSpec.getValue();
		if (value instanceof Collection) {
			return matchesAny((Collection<?>) value, operator, filterValue);
		}
		else {
			return operator.matches(value, filterValue);
		}
	}

	private static Object getProperty(Object object, List<String> attributePath) {
		Object value = PropertyUtils.getProperty(object, attributePath);

		// TODO access to ResourceInformation needed for support of custom naming
		int pathLength = attributePath.size();
		if (value == null && pathLength >= 2 && attributePath.get(pathLength - 1).equals("id")) {
			// check ID field as well for relationships
			List<String> idAttributePath = new ArrayList<>(attributePath.subList(0, pathLength - 2));
			idAttributePath.add(attributePath.get(pathLength - 2) + "Id");
			try {
				return PropertyUtils.getProperty(object, idAttributePath);
			}
			catch (PropertyException e) {
				return null; // property does not exist
			}
		}
		return value;
	}

	private static boolean matchesAny(Collection<?> col, FilterOperator operator, Object filterValue) {
		for (Object elem : col) {
			boolean matches = operator.matches(elem, filterValue);
			if (matches) {
				return true;
			}
		}
		return false;
	}

	private static boolean matchesOr(Object object, List<FilterSpec> expressions) {
		for (FilterSpec expr : expressions) {
			if (matches(object, expr)) {
				return true;
			}
		}
		return false;
	}

	private static boolean matchesAnd(Object object, List<FilterSpec> expressions) {
		for (FilterSpec expr : expressions) {
			if (!matches(object, expr)) {
				return false;
			}
		}
		return true;
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

	private <T> void applySorting(List<T> results, List<SortSpec> sortSpec) {
		if (!sortSpec.isEmpty()) {
			Collections.sort(results, new SortSpecComparator<>(sortSpec));
		}
	}

	private <T> void applyPaging(List<T> results, QuerySpec querySpec) {
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

	private <T> void applyFilter(List<T> results, FilterSpec filterSpec) {
		if (filterSpec != null) {
			Iterator<T> iterator = results.iterator();
			while (iterator.hasNext()) {
				T next = iterator.next();
				if (!matches(next, filterSpec)) {
					iterator.remove();
				}
			}
		}
	}

	static class SortSpecComparator<T> implements Comparator<T> {

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
