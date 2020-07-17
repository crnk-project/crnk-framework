package io.crnk.data.jpa.internal.query.backend;

import java.util.List;
import javax.persistence.criteria.JoinType;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaAttributePath;

public interface JpaQueryBackend<F, O, P, E> {

	void distinct();

	F getRoot();

	void setOrder(List<O> orderSpecListToOrderArray);

	List<O> getOrderList();

	O newSort(E expression, Direction dir);

	void addPredicate(P predicate);

	E getAttribute(MetaAttributePath attrPath);

	E getAttribute(MetaAttributePath attrPath, JoinType joinType);

	void addParentPredicate(MetaAttribute primaryKeyAttr);

	boolean hasManyRootsFetchesOrJoins();

	void addSelection(E expression, String name);

	E getExpression(O order);

	boolean containsRelation(E expression);

	P buildPredicate(FilterOperator operator, MetaAttributePath path, Object value);

	P and(List<P> predicates);

	P not(P predicate);

	P or(List<P> predicates);

	Class<?> getJavaElementType(E currentCriteriaPath);

	E getAttribute(E currentCriteriaPath, MetaAttribute pathElement);

	E joinSubType(E currentCriteriaPath, Class<?> entityType);

	E joinMapValue(E currentCriteriaPath, MetaAttribute pathElement, Object key);

	F joinMapRelation(F currentCriteriaPath, MetaAttribute pathElement, Object key);

	F doJoin(MetaAttribute targetAttr, JoinType joinType, F parent);
}
