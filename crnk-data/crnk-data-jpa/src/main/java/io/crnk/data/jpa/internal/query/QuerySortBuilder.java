package io.crnk.data.jpa.internal.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.JoinType;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.data.jpa.internal.query.backend.JpaQueryBackend;
import io.crnk.data.jpa.meta.MetaEmbeddable;
import io.crnk.data.jpa.query.AnyTypeObject;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaAttributeFinder;
import io.crnk.meta.model.MetaAttributePath;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
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

		Set<PathSpec> existingPaths = sortSpecs.stream().map(it -> it.getPath()).collect(Collectors.toSet());

		// ensure a total order, add primary key if necessary necssary
		if (query.getEnsureTotalOrder() && !QueryUtil.hasTotalOrder(query.getMeta(), sortSpecs)) {
			List<O> totalOrderList = backend.getOrderList();
			MetaKey primaryKey = query.getMeta().getPrimaryKey();
			if (primaryKey != null) {
				List<MetaAttribute> primaryKeyElements = primaryKey.getElements();
				for (MetaAttribute primaryKeyElement : primaryKeyElements) {
					sortSpecToOrder(new MetaAttributePath(primaryKeyElement), Direction.ASC, totalOrderList, existingPaths);
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

		MetaAttributePath path = query.getMeta().resolvePath(sortSpec.getAttributePath(), attributeFinder);
		sortSpecToOrder(path, sortSpec.getDirection(), orders, null);
		return orders;
	}

	private void sortSpecToOrder(MetaAttributePath path, Direction direction, List<O> orders, Set<PathSpec> existingPaths) {
		PathSpec pathSpec = PathSpec.of(path.toString());
		if (existingPaths != null && existingPaths.contains(pathSpec)) {
			return; // already sorted
		}


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
					orders.add(backend.newSort(expr, direction));
				}
			}
		}
		else if (valueType instanceof MetaEmbeddable) {
			MetaEmbeddable embeddable = (MetaEmbeddable) valueType;

			List<? extends MetaAttribute> attributes = embeddable.getAttributes();
			if (query.alphabeticEmbeddableElementOrder) {
				attributes = new ArrayList<>(attributes);
				// TODO move to column name, but general should follow  similar pattern
				// see https://stackoverflow.com/questions/8139437/how-to-set-the-column-order-of-a-composite-primary-key-using-jpa-hibernate
				Collections.sort(attributes, Comparator.comparing(MetaElement::getName));
			}

			for (MetaAttribute embeddableAttr : attributes) {
				sortSpecToOrder(path.concat(embeddableAttr), direction, orders, existingPaths);
			}
		}
		else {
			E expr = backend.getAttribute(path, JoinType.LEFT);
			orders.add(backend.newSort(expr, direction));
		}
	}

}
