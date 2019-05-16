package io.crnk.data.jpa.query.criteria;

import io.crnk.data.jpa.internal.JpaQueryFactoryBase;
import io.crnk.data.jpa.internal.query.backend.criteria.JpaCriteriaQueryImpl;
import io.crnk.data.jpa.query.JpaQueryFactory;

import javax.persistence.EntityManager;
import java.lang.reflect.Type;
import java.util.List;

public class JpaCriteriaQueryFactory extends JpaQueryFactoryBase implements JpaQueryFactory {

	private JpaCriteriaQueryFactory() {
	}

	public static JpaCriteriaQueryFactory newInstance() {
		return new JpaCriteriaQueryFactory();
	}

	public static JpaCriteriaQueryFactory newInstance(EntityManager em) {
		JpaCriteriaQueryFactory factory = new JpaCriteriaQueryFactory();
		factory.initalize(createDefaultContext(em));
		return factory;
	}

	@Override
	public <T> JpaCriteriaQuery<T> query(Class<T> entityClass) {
		return new JpaCriteriaQueryImpl<>(context.getMetaPartition(), getEntityManager(), entityClass, computedAttrs);
	}

	@Override
	public <T> JpaCriteriaQuery<T> query(Class<?> entityClass, String attrName, String parentKey, List<?> entityIds) {
		return new JpaCriteriaQueryImpl<>(context.getMetaPartition(), getEntityManager(), entityClass, computedAttrs, attrName, parentKey, entityIds);
	}

	public void registerComputedAttribute(Class<?> targetClass, String attributeName, Type attributeType,
										  JpaCriteriaExpressionFactory<?> expressionFactory) {
		computedAttrs.register(targetClass, attributeName, expressionFactory, attributeType);
	}

}
