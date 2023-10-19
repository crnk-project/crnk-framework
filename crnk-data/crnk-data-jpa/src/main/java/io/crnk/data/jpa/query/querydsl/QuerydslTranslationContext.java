package io.crnk.data.jpa.query.querydsl;

import jakarta.persistence.criteria.JoinType;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.crnk.meta.model.MetaAttributePath;

public interface QuerydslTranslationContext<T> {

	JPAQueryFactory getQueryFactory();

	JPAQuery<T> getQuery();

	Path<T> getRoot();

	<P> EntityPath<P> getParentRoot();

	<E> Expression<E> getAttribute(MetaAttributePath attrPath);

	<E> EntityPath<E> getJoin(MetaAttributePath path, JoinType defaultJoinType);

	void addPredicate(Predicate predicate);

	void addSelection(Expression<?> expression, String name);

	<U> QuerydslTranslationContext<U> castFor(Class<U> type);
}
