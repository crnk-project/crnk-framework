package io.crnk.jpa.query.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;

public interface QuerydslExpressionFactory<T extends Expression<?>> {

	@SuppressWarnings("rawtypes")
	Expression getExpression(T parent, JPAQuery<?> jpaQuery);
}
