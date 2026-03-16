package io.crnk.data.jpa.query.criteria;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.data.jpa.JpaRepositoryFilterBase;

import jakarta.persistence.criteria.CriteriaQuery;

public class JpaCriteriaRepositoryFilterBase extends JpaRepositoryFilterBase implements JpaCriteriaRepositoryFilter {

	@Override
	public void filterCriteriaQuery(Object repository, QuerySpec querySpec,
									CriteriaQuery criteriaQuery) {
		// nothing to do
	}
}
