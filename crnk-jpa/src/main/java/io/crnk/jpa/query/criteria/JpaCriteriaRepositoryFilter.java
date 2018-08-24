package io.crnk.jpa.query.criteria;

import javax.persistence.criteria.CriteriaQuery;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.jpa.JpaRepositoryFilter;

public interface JpaCriteriaRepositoryFilter extends JpaRepositoryFilter {

	/**
	 * Allows to hook into the translation of the generic query into a criteria query.
	 *
	 * @param repository         invoked
	 * @param querySpec          provided by caller
	 * @param criteriaQuery      to modify
	 * @return filtered query
	 */
	void filterCriteriaQuery(Object repository, QuerySpec querySpec,
			CriteriaQuery criteriaQuery);
}
