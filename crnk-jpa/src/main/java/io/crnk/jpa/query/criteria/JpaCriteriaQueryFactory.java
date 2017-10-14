package io.crnk.jpa.query.criteria;

import io.crnk.jpa.internal.JpaQueryFactoryBase;
import io.crnk.jpa.internal.query.backend.criteria.JpaCriteriaQueryImpl;
import io.crnk.jpa.query.JpaQueryFactory;

import java.lang.reflect.Type;
import java.util.List;

public class JpaCriteriaQueryFactory extends JpaQueryFactoryBase implements JpaQueryFactory {

	private JpaCriteriaQueryFactory() {
	}

	public static JpaCriteriaQueryFactory newInstance() {
		return new JpaCriteriaQueryFactory();
	}

	@Override
	public <T> JpaCriteriaQuery<T> query(Class<T> entityClass) {
		return new JpaCriteriaQueryImpl<>(context.getMetaPartition(), em, entityClass, computedAttrs);
	}

	@Override
	public <T> JpaCriteriaQuery<T> query(Class<?> entityClass, String attrName, List<?> entityIds) {
		return new JpaCriteriaQueryImpl<>(context.getMetaPartition(), em, entityClass, computedAttrs, attrName, entityIds);
	}

	public void registerComputedAttribute(Class<?> targetClass, String attributeName, Type attributeType,
										  JpaCriteriaExpressionFactory<?> expressionFactory) {
		computedAttrs.register(targetClass, attributeName, expressionFactory, attributeType);
	}

}
