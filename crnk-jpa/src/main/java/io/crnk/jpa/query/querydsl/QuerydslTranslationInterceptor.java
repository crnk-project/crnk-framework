package io.crnk.jpa.query.querydsl;

import io.crnk.jpa.internal.query.backend.querydsl.QuerydslQueryImpl;

public interface QuerydslTranslationInterceptor { // NOSONAR not a functional interface

	<T> void intercept(QuerydslQueryImpl<T> query, QuerydslTranslationContext<T> translationContext);

}
