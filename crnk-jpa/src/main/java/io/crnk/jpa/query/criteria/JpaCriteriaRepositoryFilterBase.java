package io.crnk.jpa.query.criteria;

import javax.persistence.criteria.CriteriaQuery;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.jpa.JpaRepositoryFilterBase;

public class JpaCriteriaRepositoryFilterBase extends JpaRepositoryFilterBase implements JpaCriteriaRepositoryFilter {

	@Override
	public void filterCriteriaQuery(Object repository, QuerySpec querySpec,
			CriteriaQuery criteriaQuery) {
		// nothing to do
	}
}
