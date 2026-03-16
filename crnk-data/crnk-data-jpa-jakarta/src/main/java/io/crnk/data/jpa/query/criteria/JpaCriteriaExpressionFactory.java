package io.crnk.data.jpa.query.criteria;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;

public interface JpaCriteriaExpressionFactory<T extends From<?, ?>> {

	Expression getExpression(T parent, CriteriaQuery<?> query);
}
