package io.crnk.data.jpa.query.criteria;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

public interface JpaCriteriaExpressionFactory<T extends From<?, ?>> {

	Expression getExpression(T parent, CriteriaQuery<?> query);
}
