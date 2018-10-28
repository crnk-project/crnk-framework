package io.crnk.jpa.internal.query;

import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.IncludeFieldSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.jpa.internal.query.backend.JpaQueryBackend;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaAttributeFinder;
import io.crnk.meta.model.MetaAttributePath;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryBuilder<T, F, O, P, E> {

	private static final String SORT_COLUMN_ALIAS_PREFIX = "__sort";

	private JpaQueryBackend<F, O, P, E> backend;

	private AbstractJpaQueryImpl<T, ?> query;

	private MetaAttributeFinder attributeFinder;

	public QueryBuilder(AbstractJpaQueryImpl<T, ?> query, JpaQueryBackend<F, O, P, E> backend) {
		this.query = query;
		this.backend = backend;

		final ComputedAttributeRegistryImpl virtualAttrs = query.getComputedAttrs();
		this.attributeFinder = new MetaAttributeFinder() {

			@Override
			public MetaAttribute getAttribute(MetaDataObject meta, String name) {
				MetaComputedAttribute attr = virtualAttrs.get(meta, name);
				if (attr != null) {
					return attr;
				}
				return meta.findAttribute(name, true);
			}
		};
	}

	/**
	 * Adds order expressions to selection if in "auto distinct" mode and
	 * the query performs a join or fetch on a relation. In
	 * this case, attributes from referenced entities inlucded in the sort
	 * clause must be added to the select clause as well.
	 *
	 * @return number of attributes that were needed to compute a distinct
	 */
	protected int applyDistinct() {
		int numAutoSelections = 0;
		boolean distinct;
		if (query.autoDistinct) {
			// distinct for many join/fetches or manual
			// in case of ViewQuery we may not need this here, but we need to do
			// the selection of order-by columns below
			distinct = query.autoDistinct && !query.autoGroupBy && backend.hasManyRootsFetchesOrJoins();
			if (distinct) {
				// we also need to select sorted attributes (for references)
				numAutoSelections = addOrderExpressionsToSelection();
			}
		} else {
			distinct = query.distinct;
		}
		if (distinct) {
			backend.distinct();
		}
		return numAutoSelections;
	}

	public Map<String, Integer> applySelectionSpec() {
		MetaDataObject meta = query.getMeta();

		Map<String, Integer> selectionBindings = new HashMap<>();

		int index = 1;

		List<IncludeFieldSpec> includedFields = query.getIncludedFields();
		for (IncludeFieldSpec includedField : includedFields) {
			MetaAttributePath path = meta.resolvePath(includedField.getAttributePath(), attributeFinder);
			E attr = backend.getAttribute(path);

			backend.addSelection(attr, path.toString());
			selectionBindings.put(path.toString(), index++);
		}
		return selectionBindings;
	}

	private int addOrderExpressionsToSelection() {
		int numAutoSelections = 0;
		int prefixIndex = 0;
		List<O> newOrderList = new ArrayList<>();
		for (O order : backend.getOrderList()) {
			E expression = backend.getExpression(order);
			if (backend.containsRelation(expression)) {
				backend.addSelection(expression, SORT_COLUMN_ALIAS_PREFIX + prefixIndex++);
				numAutoSelections++;
			}
			newOrderList.add(order);
		}
		backend.setOrder(newOrderList);
		return numAutoSelections;
	}

	public void applySortSpec() {
		QuerySortBuilder<T, E, O> orderBuilder = new QuerySortBuilder<>(query, backend, attributeFinder);
		orderBuilder.applySortSpec();
	}

	public void applyFilterSpec() {
		QueryFilterBuilder<P, F> predicateBuilder = new QueryFilterBuilder<>(backend, attributeFinder);

		MetaDataObject meta = query.getMeta();
		List<FilterSpec> filters = query.getFilterSpecs();
		List<P> predicates = predicateBuilder.filterSpecListToPredicateArray(meta, backend.getRoot(), filters);
		if (predicates != null && !predicates.isEmpty()) {
			backend.addPredicate(backend.and(predicates));
		}

		MetaAttribute parentAttr = query.getParentAttr();
		if (parentAttr != null) {
			MetaDataObject parentMeta = query.getParentMeta();
			MetaKey primaryKey = parentMeta.getPrimaryKey();
			if (primaryKey == null) {
				throw new IllegalStateException("primary key not found for " + parentAttr.getId());
			}
			MetaAttribute primaryKeyAttr = primaryKey.getUniqueElement();

			backend.addParentPredicate(primaryKeyAttr);
		}
	}

	public E getExpression(PathSpec path) {
		MetaDataObject meta = query.getMeta();
		MetaAttributePath metaAttributePath = meta.resolvePath(path.getElements());
		return backend.getAttribute(metaAttributePath);
	}
}
