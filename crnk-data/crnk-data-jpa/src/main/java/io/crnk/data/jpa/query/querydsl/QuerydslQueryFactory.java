package io.crnk.data.jpa.query.querydsl;

import io.crnk.data.jpa.internal.JpaQueryFactoryBase;
import io.crnk.data.jpa.internal.query.backend.querydsl.QuerydslQueryImpl;
import io.crnk.data.jpa.query.JpaQueryFactory;

import javax.persistence.EntityManager;
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

	public static QuerydslQueryFactory newInstance(EntityManager em) {
		QuerydslQueryFactory factory = new QuerydslQueryFactory();
		factory.initalize(createDefaultContext(em));
		return factory;
	}

	public void addInterceptor(QuerydslTranslationInterceptor interceptor) {
		interceptors.add(interceptor);
	}

	@Override
	public <T> QuerydslQuery<T> query(Class<T> entityClass) {
		return new QuerydslQueryImpl<>(context.getMetaPartition(), getEntityManager(), entityClass, computedAttrs, interceptors);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public <T> QuerydslQuery<T> query(Class<?> entityClass, String attrName, String parentKey, List<?> entityIds) {
		return new QuerydslQueryImpl(context.getMetaPartition(), getEntityManager(), entityClass, computedAttrs, interceptors, attrName, parentKey,
				entityIds);
	}

	public void registerComputedAttribute(Class<?> targetClass, String attributeName, Type attributeType,
										  QuerydslExpressionFactory<?> expressionFactory) {
		computedAttrs.register(targetClass, attributeName, expressionFactory, attributeType);
	}
}
