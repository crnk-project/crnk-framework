package io.crnk.data.jpa.query.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import io.crnk.data.jpa.model.QTestEntity;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.query.ComputedAttributeTestBase;
import io.crnk.data.jpa.query.JpaQueryFactory;

import javax.persistence.EntityManager;

public class ComputedAttributeQuerydslTest extends ComputedAttributeTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		QuerydslQueryFactory factory = QuerydslQueryFactory.newInstance();

		factory.registerComputedAttribute(TestEntity.class, ATTR_VIRTUAL_VALUE, String.class,
				new QuerydslExpressionFactory<QTestEntity>() {

					@Override
					public Expression<?> getExpression(QTestEntity test, JPAQuery<?> query) {
						return test.stringValue.toUpperCase();
					}
				});

		return factory;
	}
}
