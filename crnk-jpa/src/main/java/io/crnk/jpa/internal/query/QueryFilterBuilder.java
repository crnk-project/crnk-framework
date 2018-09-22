package io.crnk.jpa.internal.query;

import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.jpa.internal.query.backend.JpaQueryBackend;
import io.crnk.jpa.query.AnyTypeObject;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaAttributeFinder;
import io.crnk.meta.model.MetaAttributePath;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaMapType;
import io.crnk.meta.model.MetaType;

import javax.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class QueryFilterBuilder<P, F> {

	private static final int PARAM_LIMIT_FOR_ORACLE = 900;

	private MetaAttributeFinder attributeFinder;

	private JpaQueryBackend<F, ?, P, ?> backend;

	protected QueryFilterBuilder(JpaQueryBackend<F, ?, P, ?> backend,
								 MetaAttributeFinder attributeFinder) {
		this.backend = backend;
		this.attributeFinder = attributeFinder;
	}

	public List<P> filterSpecListToPredicateArray(MetaDataObject rootMeta, F root, List<FilterSpec> rowFilters) {
		return filterSpecListToPredicateArray(rootMeta, root, rowFilters, null);
	}

	public List<P> filterSpecListToPredicateArray(MetaDataObject rootMeta, F root, List<FilterSpec> rowFilters,
												  JoinType defaultPredicateJoinType) {
		ArrayList<P> predicateList = new ArrayList<>();
		for (FilterSpec rowFilter : rowFilters) {
			predicateList.add(filterSpecListToPredicate(rootMeta, root, rowFilter, defaultPredicateJoinType));
		}
		return predicateList;
	}

	protected P filterSpecListToPredicate(MetaDataObject rootMeta, F root, FilterSpec fs, JoinType defaultPredicateJoinType) {
		if ((fs.getOperator() == FilterOperator.EQ || fs.getOperator() == FilterOperator.NEQ)
				&& fs.getValue() instanceof Collection && ((Collection<?>) fs.getValue()).size() > PARAM_LIMIT_FOR_ORACLE) {

			return filterLargeValueSets(fs, rootMeta, root, defaultPredicateJoinType);
		} else {
			if (fs.hasExpressions()) {
				return filterExpressions(fs, rootMeta, root, defaultPredicateJoinType);
			} else {
				return filterSimpleOperation(fs, rootMeta);
			}
		}
	}

	private P filterLargeValueSets(FilterSpec filterSpec, MetaDataObject rootMeta, F root, JoinType defaultPredicateJoinType) {
		// Split filter values with two many elements. Oracle is limited to 1000.
		ArrayList<FilterSpec> filterSpecs = new ArrayList<>();
		List<?> list = new ArrayList<>((Collection<?>) filterSpec.getValue());
		for (int i = 0; i < list.size(); i += PARAM_LIMIT_FOR_ORACLE) {
			int nextOffset = i + Math.min(list.size() - i, PARAM_LIMIT_FOR_ORACLE);
			List<?> batchList = list.subList(i, nextOffset);
			filterSpecs.add(new FilterSpec(filterSpec.getAttributePath(), filterSpec.getOperator(), batchList));
		}

		FilterSpec orSpec = FilterSpec.or(filterSpecs);
		return filterSpecListToPredicate(rootMeta, root, orSpec, defaultPredicateJoinType);
	}

	private P filterSimpleOperation(FilterSpec fs, MetaDataObject rootMeta) {
		Object value = fs.getValue();
		if (value instanceof Set) {
			// HashSet not properly supported in ORM/JDBC, convert to
			// list
			Set<?> set = (Set<?>) value;
			value = new ArrayList<Object>(set);
		}
		MetaAttributePath path = rootMeta.resolvePath(fs.getAttributePath(), attributeFinder);
		path = enhanceAttributePath(path, value);
		return backend.buildPredicate(fs.getOperator(), path, value);
	}

	private P filterExpressions(FilterSpec fs, MetaDataObject rootMeta, F root, JoinType defaultPredicateJoinType) {
		// and, or, not.
		if (fs.getOperator() == FilterOperator.NOT) {
			return backend.not(
					backend.and(filterSpecListToPredicateArray(rootMeta, root, fs.getExpression(), defaultPredicateJoinType)));
		} else if (fs.getOperator() == FilterOperator.AND) {
			return backend.and(filterSpecListToPredicateArray(rootMeta, root, fs.getExpression(), defaultPredicateJoinType));
		} else if (fs.getOperator() == FilterOperator.OR) {
			return backend.or(filterSpecListToPredicateArray(rootMeta, root, fs.getExpression(), defaultPredicateJoinType));
		} else {
			throw new IllegalArgumentException(fs.toString());
		}
	}

	public MetaAttributePath enhanceAttributePath(MetaAttributePath attrPath, Object value) {
		MetaAttribute attr = attrPath.getLast();

		MetaType valueType = attr.getType();
		if (valueType instanceof MetaMapType) {
			valueType = valueType.getElementType();
		}

		boolean anyType = AnyTypeObject.class.isAssignableFrom(valueType.getImplementationClass());
		if (anyType) {
			// we have and AnyType and do need to select the proper attribute of
			// the embeddable
			MetaAttribute anyAttr = AnyUtils.findAttribute((MetaDataObject) valueType, value);
			return attrPath.concat(anyAttr);
		} else {
			return attrPath;
		}
	}
}
