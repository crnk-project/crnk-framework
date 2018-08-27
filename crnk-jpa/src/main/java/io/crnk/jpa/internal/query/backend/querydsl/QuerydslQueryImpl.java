package io.crnk.jpa.internal.query.backend.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.crnk.jpa.internal.query.AbstractJpaQueryImpl;
import io.crnk.jpa.internal.query.ComputedAttributeRegistryImpl;
import io.crnk.jpa.query.querydsl.QuerydslQuery;
import io.crnk.jpa.query.querydsl.QuerydslTranslationInterceptor;
import io.crnk.meta.provider.MetaPartition;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

public class QuerydslQueryImpl<T> extends AbstractJpaQueryImpl<T, QuerydslQueryBackend<T>> implements QuerydslQuery<T> {

	private JPAQueryFactory queryFactory;

	private List<QuerydslTranslationInterceptor> translationInterceptors;

	public QuerydslQueryImpl(MetaPartition metaPartition, EntityManager em, Class<T> clazz, ComputedAttributeRegistryImpl computedAttrs,
							 List<QuerydslTranslationInterceptor> translationInterceptors) {
		super(metaPartition, em, clazz, computedAttrs);
		this.translationInterceptors = translationInterceptors;
		queryFactory = new JPAQueryFactory(em);
	}

	public QuerydslQueryImpl(MetaPartition metaPartition, EntityManager em, Class<?> clazz, ComputedAttributeRegistryImpl virtualAttrs,
							 List<QuerydslTranslationInterceptor> translationInterceptors, String attrName, String parentKey, List<?> entityIds) {
		super(metaPartition, em, clazz, virtualAttrs, attrName, parentKey, entityIds);
		this.translationInterceptors = translationInterceptors;
		queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public QuerydslExecutorImpl<T> buildExecutor() {
		return (QuerydslExecutorImpl<T>) super.buildExecutor();
	}

	protected JPAQueryFactory getQueryFactory() {
		return queryFactory;
	}

	@Override
	protected QuerydslQueryBackend<T> newBackend() {
		return new QuerydslQueryBackend<>(this, clazz, parentMeta, parentAttr, parentIdSelection);
	}

	@Override
	protected QuerydslExecutorImpl<T> newExecutor(QuerydslQueryBackend<T> ctx, int numAutoSelections,
												  Map<String, Integer> selectionBindings) {

		for (QuerydslTranslationInterceptor translationInterceptor : translationInterceptors) {
			translationInterceptor.intercept(this, ctx);
		}

		return new QuerydslExecutorImpl<>(em, meta, ctx.getQuery(), numAutoSelections, selectionBindings);
	}
}
