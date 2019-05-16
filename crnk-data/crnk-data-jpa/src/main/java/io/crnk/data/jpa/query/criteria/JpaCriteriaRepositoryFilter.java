package io.crnk.data.jpa.query.criteria;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.data.jpa.JpaRepositoryFilter;

import javax.persistence.criteria.CriteriaQuery;

public interface JpaCriteriaRepositoryFilter extends JpaRepositoryFilter {

	/**
	 * Allows to hook into the translation of the generic query into a criteria query.
	 *
	 * @param repository    invoked
	 * @param querySpec     provided by caller
	 * @param criteriaQuery to modify
	 * @return filtered query
	 */
	void filterCriteriaQuery(Object repository, QuerySpec querySpec,
							 CriteriaQuery criteriaQuery);
}
