package io.crnk.data.jpa.internal.query;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.JoinType;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.data.jpa.internal.query.backend.JpaQueryBackend;
import io.crnk.data.jpa.query.AnyTypeObject;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaAttributeFinder;
import io.crnk.meta.model.MetaAttributePath;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.MetaMapType;
import io.crnk.meta.model.MetaType;

public class QuerySortBuilder<T, E, O> {

	protected JpaQueryBackend<?, O, ?, E> backend;
	protected AbstractJpaQueryImpl<T, ?> query;
	private MetaAttributeFinder attributeFinder;

	public QuerySortBuilder(AbstractJpaQueryImpl<T, ?> query, JpaQueryBackend<?, O, ?, E> backend, MetaAttributeFinder attributeFinder) {
		this.backend = backend;
		this.query = query;
		this.attributeFinder = attributeFinder;
	}

	public void applySortSpec() {
		List<SortSpec> sortSpecs = query.getSortSpecs();
		if (!sortSpecs.isEmpty()) {
			backend.setOrder(sortSpecListToArray());
		}

		// ensure a total order, add primary key if necessary necssary
		if (query.getEnsureTotalOrder() && !QueryUtil.hasTotalOrder(query.getMeta(), sortSpecs)) {
			List<O> totalOrderList = backend.getOrderList();
			MetaKey primaryKey = query.getMeta().getPrimaryKey();
			if (primaryKey != null) {
				for (MetaAttribute primaryKeyElem : primaryKey.getElements()) {
					E primaryKeyExpr = backend.getAttribute(new MetaAttributePath(primaryKeyElem));
					O primaryKeyElemOrder = backend.newSort(primaryKeyExpr, Direction.ASC);
					totalOrderList.add(primaryKeyElemOrder);
				}
			}
			backend.setOrder(totalOrderList);
		}
	}

	protected List<O> sortSpecListToArray() {
		ArrayList<O> orderList = new ArrayList<>();
		for (SortSpec sortSpec : query.getSortSpecs()) {
			orderList.addAll(sortSpecToOrder(sortSpec));
		}
		return orderList;
	}

	private List<O> sortSpecToOrder(SortSpec sortSpec) {
		List<O> orders = new ArrayList<>();

		// check for AnyType
		MetaAttributePath path = query.getMeta().resolvePath(sortSpec.getAttributePath(), attributeFinder);
		MetaAttribute attr = path.getLast();
		MetaType valueType = attr.getType();
		if (valueType instanceof MetaMapType) {
			valueType = valueType.getElementType();
		}
		boolean anyType = AnyTypeObject.class.isAssignableFrom(valueType.getImplementationClass());
		if (anyType) {
			// order by anything, if types are not mixed for a given key, it
			// will be ok
			MetaDataObject anyMeta = valueType.asDataObject();
			for (MetaAttribute anyAttr : anyMeta.getAttributes()) {
				if (!anyAttr.isDerived()) {
					E expr = backend.getAttribute(path.concat(anyAttr), JoinType.LEFT);
					orders.add(backend.newSort(expr, sortSpec.getDirection()));
				}
			}
		} else {
			E expr = backend.getAttribute(path, JoinType.LEFT);
			orders.add(backend.newSort(expr, sortSpec.getDirection()));
		}
		return orders;
	}

}
