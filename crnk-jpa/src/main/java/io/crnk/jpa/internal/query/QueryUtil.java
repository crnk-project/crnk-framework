package io.crnk.jpa.internal.query;

import io.crnk.core.queryspec.SortSpec;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaKey;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.Set;

public class QueryUtil {

	private QueryUtil() {
	}

	public static boolean hasTotalOrder(MetaDataObject meta, List<SortSpec> sortSpecs) {
		boolean hasTotalOrder = contains(meta.getPrimaryKey(), sortSpecs);
		if (hasTotalOrder)
			return true;
		for (MetaKey key : meta.getDeclaredKeys()) {
			if (key.isUnique() && contains(key, sortSpecs)) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(MetaKey key, List<SortSpec> entitySortSpecs) {
		for (MetaAttribute attr : key.getElements()) {
			boolean contains = false;
			for (SortSpec sortSpec : entitySortSpecs) {
				List<String> sortAttrPath = sortSpec.getAttributePath();
				if (sortAttrPath.size() == 1 && sortAttrPath.get(0).equals(attr.getName())) {
					contains = true;
					break;
				}
			}
			if (!contains)
				return false;
		}
		return true;

	}

	public static boolean hasManyRootsFetchesOrJoins(CriteriaQuery<?> criteriaQuery) {
		Set<Root<?>> roots = criteriaQuery.getRoots();

		// more than one root, user is supposed to handle this manually
		if (roots.size() != 1)
			return false;

		for (Root<?> root : roots) {
			if (containsMultiRelationFetch(root.getFetches()))
				return true;

			if (containsMultiRelationJoin(root.getJoins()))
				return true;
		}
		return false;
	}

	private static boolean containsMultiRelationFetch(Set<?> fetches) {
		for (Object fetchObj : fetches) {
			Fetch<?, ?> fetch = (Fetch<?, ?>) fetchObj;

			Attribute<?, ?> attr = fetch.getAttribute();
			if (attr.isAssociation() && attr.isCollection())
				return true;

			if (containsMultiRelationFetch(fetch.getFetches()))
				return true;
		}
		return false;
	}

	private static boolean containsMultiRelationJoin(Set<?> fetches) {
		for (Object fetchObj : fetches) {
			Fetch<?, ?> fetch = (Fetch<?, ?>) fetchObj;
			Attribute<?, ?> attr = fetch.getAttribute();
			if (attr.isAssociation() && attr.isCollection())
				return true;

			if (containsMultiRelationFetch(fetch.getFetches()))
				return true;
		}
		return false;
	}

	public static boolean containsRelation(Object expr) {
		if (expr instanceof Join) {
			return true;
		} else if (expr instanceof SingularAttribute) {
			SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) expr;
			return attr.isAssociation();
		} else if (expr instanceof Path) {
			Path<?> attrPath = (Path<?>) expr;
			Bindable<?> model = attrPath.getModel();
			Path<?> parent = attrPath.getParentPath();
			return containsRelation(parent) || containsRelation(model);
		} else {
			// we may can do better here...
			return false;
		}
	}

}
