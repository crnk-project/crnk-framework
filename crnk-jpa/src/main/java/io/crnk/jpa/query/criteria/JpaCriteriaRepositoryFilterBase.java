package io.crnk.jpa.query.criteria;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.jpa.JpaRepositoryFilterBase;

import javax.persistence.criteria.CriteriaQuery;

public class JpaCriteriaRepositoryFilterBase extends JpaRepositoryFilterBase implements JpaCriteriaRepositoryFilter {

	@Override
	public void filterCriteriaQuery(Object repository, QuerySpec querySpec,
									CriteriaQuery criteriaQuery) {
		// nothing to do
	}
}
