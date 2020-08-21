package io.crnk.data.jpa.internal.query.backend.querydsl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.criteria.JoinType;

import com.google.common.collect.ImmutableList;
import com.querydsl.core.support.FetchableSubQueryBase;
import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OperationImpl;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.QTuple;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.CollectionExpressionBase;
import com.querydsl.core.types.dsl.CollectionPathBase;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.MapExpressionBase;
import com.querydsl.core.types.dsl.MapPath;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAQueryBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.data.jpa.internal.query.ComputedAttributeRegistryImpl;
import io.crnk.data.jpa.internal.query.JoinRegistry;
import io.crnk.data.jpa.internal.query.MetaComputedAttribute;
import io.crnk.data.jpa.internal.query.QueryUtil;
import io.crnk.data.jpa.internal.query.backend.JpaQueryBackend;
import io.crnk.data.jpa.query.querydsl.QuerydslExpressionFactory;
import io.crnk.data.jpa.query.querydsl.QuerydslTranslationContext;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaAttributePath;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaKey;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class QuerydslQueryBackend<T>
		implements QuerydslTranslationContext<T>, JpaQueryBackend<Expression<?>, OrderSpecifier<?>, Predicate, Expression<?>> {

	private JoinRegistry<Expression<?>, Expression<?>> joinHelper;

	private Path<T> root;

	private EntityPath<?> parentFrom;

	private QuerydslQueryImpl<T> queryImpl;

	private JPAQueryBase querydslQuery;

	private List<OrderSpecifier<?>> orderList = new ArrayList<>();

	public QuerydslQueryBackend(QuerydslQueryImpl<T> queryImpl, Class<T> clazz, MetaDataObject parentMeta,
			MetaAttribute parentAttr, boolean addParentSelection) {
		this.queryImpl = queryImpl;

		JPAQueryFactory queryFactory = queryImpl.getQueryFactory();

		if (parentMeta != null) {
			parentFrom = QuerydslUtils.getEntityPath(parentMeta.getImplementationClass());
			root = QuerydslUtils.getEntityPath(clazz);

			Path joinPath = (Path) QuerydslUtils.get(parentFrom, parentAttr.getName());
			joinHelper = new JoinRegistry<>(this, queryImpl);

			joinHelper.putJoin(new MetaAttributePath(), root);

			if (addParentSelection) {
				Expression<Object> parentIdExpr = getParentIdExpression(parentMeta, parentAttr);
				querydslQuery = queryFactory.select(parentIdExpr, root);
			}
			else {
				querydslQuery = queryFactory.select(root);
			}

			querydslQuery = querydslQuery.from(parentFrom);
			if (joinPath instanceof CollectionExpression) {
				querydslQuery = querydslQuery.join((CollectionExpression) joinPath, root);
			}
			else {
				querydslQuery = querydslQuery.join((EntityPath) joinPath, root);
			}
		}
		else {
			root = QuerydslUtils.getEntityPath(clazz);
			joinHelper = new JoinRegistry<>(this, queryImpl);
			joinHelper.putJoin(new MetaAttributePath(), root);
			querydslQuery = queryFactory.select(root);
			querydslQuery = querydslQuery.from((EntityPath) root);
		}
	}

	private Expression<Object> getParentIdExpression(MetaDataObject parentMeta, MetaAttribute parentAttr) {
		MetaKey primaryKey = parentMeta.getPrimaryKey();
		PreconditionUtil.verify(primaryKey != null, "no primary key specified for parentAttribute %s", parentAttr.getId());
		List<MetaAttribute> elements = primaryKey.getElements();
		PreconditionUtil.verifyEquals(1, elements.size(), "composite primary keys for %s not supported yet",
				parentMeta.getImplementationClass());
		MetaAttribute primaryKeyAttr = elements.get(0);
		return QuerydslUtils.get(parentFrom, primaryKeyAttr.getName());
	}

	public JPAQuery<T> getQuery() {
		JPAQueryBase finalQuery = querydslQuery;
		for (OrderSpecifier<?> order : orderList) {
			finalQuery = (JPAQueryBase) finalQuery.orderBy(order);
		}
		return (JPAQuery<T>) finalQuery;
	}

	@Override
	public Expression<?> getAttribute(MetaAttributePath attrPath) {
		return joinHelper.getEntityAttribute(attrPath);
	}

	@Override
	public Expression<?> getAttribute(MetaAttributePath attrPath, JoinType joinType) {
		return joinHelper.getEntityAttribute(attrPath, joinType);
	}

	@Override
	public void addPredicate(Predicate predicate) {
		querydslQuery = (JPAQueryBase) querydslQuery.where(predicate);
	}

	@Override
	public Path<T> getRoot() {
		return root;
	}

	@Override
	public void setOrder(List<OrderSpecifier<?>> list) {
		this.orderList = list;
	}

	@Override
	public List<OrderSpecifier<?>> getOrderList() {
		return orderList;
	}

	@Override
	public OrderSpecifier<?> newSort(Expression<?> expr, Direction dir) {
		if (dir == Direction.ASC) {
			return new OrderSpecifier(Order.ASC, expr);
		}
		else {
			return new OrderSpecifier(Order.DESC, expr);
		}
	}

	@Override
	public void distinct() {
		querydslQuery = (JPAQueryBase) querydslQuery.distinct();
	}

	@Override
	public void addParentPredicate(MetaAttribute primaryKeyAttr) {
		List<?> parentIds = queryImpl.getParentIds();
		SimpleExpression<?> parentIdPath = (SimpleExpression<?>) QuerydslUtils.get(parentFrom, primaryKeyAttr.getName());
		addPredicate(parentIdPath.in((List) parentIds));
	}

	@Override
	public boolean hasManyRootsFetchesOrJoins() {
		return QuerydslUtils.hasManyRootsFetchesOrJoins((JPAQuery<?>) querydslQuery);
	}

	@Override
	public void addSelection(Expression<?> expression, String name) {
		Expression<?> selection = querydslQuery.getMetadata().getProjection();

		List<Expression<?>> newSelection = new ArrayList<>();
		if (selection != null) {
			if (selection instanceof QTuple) {
				newSelection.addAll(((QTuple) selection).getArgs());
			}
			else {
				newSelection.add(selection);
			}
		}
		newSelection.add(expression);
		querydslQuery = (JPAQuery) querydslQuery.select(newSelection.toArray(new Expression[newSelection.size()]));
	}

	@Override
	public Expression<?> getExpression(OrderSpecifier<?> order) {
		return order.getTarget();
	}

	@Override
	public boolean containsRelation(Expression<?> expression) {
		return QueryUtil.containsRelation(expression);
	}

	@Override
	public Predicate buildPredicate(FilterOperator operator, MetaAttributePath attrPath, Object value) {
		Expression<?> attr = getAttribute(attrPath);
		return buildPredicate(operator, attr, value);
	}

	public Predicate buildPredicate(FilterOperator operator, Expression<?> expressionObj, Object value) {
		Expression expression = expressionObj;

		expression = handleConversions(expression, operator);

		return handle(expression, operator, value);

	}

	private Predicate handle(Expression expression, FilterOperator operator, Object value) { // NOSONAR
		// checking multiple comparision implementations is a mess, created
		// https://github.com/querydsl/querydsl/issues/2028
		if (operator == FilterOperator.EQ || operator == FilterOperator.NEQ) {
			return handleEquals(expression, operator, value);
		}

		if (value instanceof Collection) {
			// map collection to OR statement (expect for EQUALS where a IN is used)
			List<Predicate> predicates = new ArrayList();
			for (Object element : (Collection) value) {
				predicates.add(handle(expression, operator, element));
			}
			return or(predicates);
		}

		if (operator == FilterOperator.LIKE) {
			return ((StringExpression) expression).lower().like(value.toString().toLowerCase());
		}
		else if (operator == FilterOperator.GT) {
			if (expression instanceof FetchableSubQueryBase) {
				return ((FetchableSubQueryBase) expression).gt(value);
			}
			else if (expression instanceof NumberExpression) {
				return ((NumberExpression) expression).gt((Number) value);
			}
			else {
				return ((ComparableExpression) expression).gt((Comparable) value);
			}
		}
		else if (operator == FilterOperator.LT) {
			if (expression instanceof FetchableSubQueryBase) {
				return ((FetchableSubQueryBase) expression).lt(value);
			}
			else if (expression instanceof NumberExpression) {
				return ((NumberExpression) expression).lt((Number) value);
			}
			else {
				return ((ComparableExpression) expression).lt((Comparable) value);
			}
		}
		else if (operator == FilterOperator.GE) {
			if (expression instanceof FetchableSubQueryBase) {
				return ((FetchableSubQueryBase) expression).goe(value);
			}
			else if (expression instanceof NumberExpression) {
				return ((NumberExpression) expression).goe((Number) value);
			}
			else {
				return ((ComparableExpression) expression).goe((Comparable) value);
			}
		}
		else if (operator == FilterOperator.LE) {
			if (expression instanceof FetchableSubQueryBase) {
				return ((FetchableSubQueryBase) expression).loe(value);
			}
			else if (expression instanceof NumberExpression) {
				return ((NumberExpression) expression).loe((Number) value);
			}
			else {
				return ((ComparableExpression) expression).loe((Comparable) value);
			}
		}
		else {
			throw new IllegalStateException("unexpected operator " + operator);
		}

	}

	private Predicate handleEquals(Expression<?> leftExpression, FilterOperator operator, Object value) {
		Expression<?> expression = leftExpression;
		if (Collection.class.isAssignableFrom(expression.getType())) {
			CollectionPathBase collectionExpr = (CollectionPathBase) expression;
			expression = collectionExpr.any();
		}

		if (value instanceof List) {
			Predicate p = ((SimpleExpression) expression).in((List) value);
			return negateIfNeeded(p, operator);
		}
		else if (expression instanceof MapExpressionBase) {
			MapExpressionBase mapExpression = (MapExpressionBase) expression;
			Predicate p = mapExpression.containsValue(value);
			return negateIfNeeded(p, operator);
		}
		else if (value == null) {
			return negateIfNeeded(((SimpleExpression) expression).isNull(), operator);
		}
		return negateIfNeeded(((SimpleExpression) expression).eq(value), operator);
	}

	private Expression<?> handleConversions(Expression<?> expression, FilterOperator operator) {
		// convert to String for LIKE operators
		if (expression.getType() != String.class && (operator == FilterOperator.LIKE)) {
			return Expressions.stringOperation(Ops.STRING_CAST, expression);
		}
		else {
			return expression;
		}
	}

	@Override
	public Predicate and(List<Predicate> predicates) {
		if (predicates.size() == 1) {
			return predicates.get(0);
		}
		else {
			// only two elements for each operation supported, needs querydsl fix?
			Predicate result = predicates.get(0);
			for (int i = 1; i < predicates.size(); i++) {
				result = new BooleanPredicateOperation(Ops.AND, ImmutableList.of(result, predicates.get(i)));
			}
			return result;
		}
	}

	@Override
	public Predicate not(Predicate predicate) {
		return predicate.not();
	}

	@Override
	public Predicate or(List<Predicate> predicates) {
		if (predicates.size() == 1) {
			return predicates.get(0);
		}
		else {
			// only two elements for each operation supported, needs querydsl fix?
			Predicate result = predicates.get(0);
			for (int i = 1; i < predicates.size(); i++) {
				result = new BooleanPredicateOperation(Ops.OR, ImmutableList.of(result, predicates.get(i)));
			}
			return result;
		}
	}

	private Predicate negateIfNeeded(Predicate p, FilterOperator fc) {
		if (fc.equals(FilterOperator.NEQ)) {
			return p.not();
		}
		return p;
	}

	@Override
	public Expression<?> joinMapValue(Expression<?> currentCriteriaPath, MetaAttribute pathElement, Object key) {
		MapPath mapPath = (MapPath) QuerydslUtils.get(currentCriteriaPath, pathElement.getName());
		return mapPath.get(key);
	}

	@Override
	public Expression<?> joinMapRelation(Expression<?> currentCriteriaPath, MetaAttribute pathElement, Object key) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public Class<?> getJavaElementType(Expression<?> expression) {
		if (expression instanceof CollectionExpressionBase) {
			return ((CollectionExpressionBase) expression).getElementType();
		}
		return expression.getType();
	}

	@Override
	public Expression<?> getAttribute(final Expression<?> expression, MetaAttribute pathElement) {
		if (pathElement instanceof MetaComputedAttribute) {
			ComputedAttributeRegistryImpl virtualAttrs = queryImpl.getComputedAttrs();
			QuerydslExpressionFactory expressionFactory = (QuerydslExpressionFactory) virtualAttrs
					.get((MetaComputedAttribute) pathElement);
			return expressionFactory.getExpression(expression, getQuery());
		}
		else {
			return QuerydslUtils.get(expression, pathElement.getName());
		}
	}

	@Override
	public Expression<?> joinSubType(Expression<?> expression, Class<?> entityClass) {
		BeanPath beanPath = (BeanPath) expression;
		Class<?> queryClass = QuerydslUtils.getQueryClass(entityClass);
		return beanPath.as(queryClass);
	}

	@Override
	public Expression<?> doJoin(MetaAttribute targetAttr, JoinType joinType, Expression<?> parent) {
		if (targetAttr instanceof MetaComputedAttribute) {

			MetaComputedAttribute computedAttr = (MetaComputedAttribute) targetAttr;
			QuerydslExpressionFactory expressionFactory = (QuerydslExpressionFactory<?>) queryImpl.getComputedAttrs()
					.get(computedAttr);

			return expressionFactory.getExpression(parent, getQuery());
		}
		else {
			Expression<Object> expression = QuerydslUtils.get(parent, targetAttr.getName());
			querydslQuery.getMetadata().addJoin(QuerydslUtils.convertJoinType(joinType), expression);
			return expression;
		}
	}

	@Override
	public JPAQueryFactory getQueryFactory() {
		return queryImpl.getQueryFactory();
	}

	@Override
	public EntityPath getParentRoot() {
		return parentFrom;
	}

	@Override
	public <E> EntityPath<E> getJoin(MetaAttributePath path, JoinType defaultJoinType) {
		return (EntityPath<E>) joinHelper.getOrCreateJoin(path, defaultJoinType);
	}

	@Override
	public <U> QuerydslTranslationContext<U> castFor(Class<U> type) {
		return (QuerydslTranslationContext<U>) this;
	}

	public final class BooleanPredicateOperation extends OperationImpl<Boolean> implements Predicate {

		private static final long serialVersionUID = -5371430939203772072L;

		@Nullable
		private transient volatile Predicate not;

		protected BooleanPredicateOperation(Ops ops, ImmutableList<Expression<?>> list) {
			super(Boolean.class, ops, list);
			if (list.isEmpty()) {
				throw new IllegalArgumentException("list cannot be empty");
			}
		}

		@Override
		public Predicate not() {
			if (not == null) {
				not = ExpressionUtils.predicate(Ops.NOT, this);
			}
			return not;
		}
	}
}
