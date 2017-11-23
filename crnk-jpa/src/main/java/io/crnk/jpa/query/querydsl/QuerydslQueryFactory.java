package io.crnk.jpa.query.querydsl;

import io.crnk.jpa.internal.JpaQueryFactoryBase;
import io.crnk.jpa.internal.query.backend.querydsl.QuerydslQueryImpl;
import io.crnk.jpa.query.JpaQueryFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuerydslQueryFactory extends JpaQueryFactoryBase implements JpaQueryFactory {

	private List<QuerydslTranslationInterceptor> interceptors = new CopyOnWriteArrayList<>();

	private QuerydslQueryFactory() {
	}

	public static QuerydslQueryFactory newInstance() {
		return new QuerydslQueryFactory();
	}

	public void addInterceptor(QuerydslTranslationInterceptor interceptor) {
		interceptors.add(interceptor);
	}

	@Override
	public <T> QuerydslQuery<T> query(Class<T> entityClass) {
		return new QuerydslQueryImpl<>(context.getMetaPartition(), em, entityClass, computedAttrs, interceptors);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public <T> QuerydslQuery<T> query(Class<?> entityClass, String attrName, List<?> entityIds) {
		return new QuerydslQueryImpl(context.getMetaPartition(), em, entityClass, computedAttrs, interceptors, attrName, entityIds);
	}

	public void registerComputedAttribute(Class<?> targetClass, String attributeName, Type attributeType,
										  QuerydslExpressionFactory<?> expressionFactory) {
		computedAttrs.register(targetClass, attributeName, expressionFactory, attributeType);
	}
}
