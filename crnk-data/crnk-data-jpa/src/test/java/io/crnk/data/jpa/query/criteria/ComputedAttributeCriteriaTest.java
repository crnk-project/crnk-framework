package io.crnk.data.jpa.query.criteria;

import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.query.ComputedAttributeTestBase;
import io.crnk.data.jpa.query.JpaQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;

public class ComputedAttributeCriteriaTest extends ComputedAttributeTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(final EntityManager em) {
		JpaCriteriaQueryFactory factory = JpaCriteriaQueryFactory.newInstance();

		factory.registerComputedAttribute(TestEntity.class, ATTR_VIRTUAL_VALUE, String.class,
				new JpaCriteriaExpressionFactory<From<?, TestEntity>>() {

					@Override
					public Expression<String> getExpression(From<?, TestEntity> parent, CriteriaQuery<?> query) {
						CriteriaBuilder builder = em.getCriteriaBuilder();
						Path<String> stringValue = parent.get(TestEntity.ATTR_stringValue);
						return builder.upper(stringValue);
					}
				});

		return factory;
	}
}
