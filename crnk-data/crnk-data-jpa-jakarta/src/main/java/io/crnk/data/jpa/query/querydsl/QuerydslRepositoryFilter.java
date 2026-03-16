package io.crnk.data.jpa.query.querydsl;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.data.jpa.JpaRepositoryFilter;

public interface QuerydslRepositoryFilter extends JpaRepositoryFilter {

	/**
	 * Allows to hook into the translation of the generic query into a querydsl query.
	 *
	 * @param repository         invoked
	 * @param querySpec          provided by caller
	 * @param translationContext to modify the translation
	 * @return filtered query
	 */
	<T> void filterQueryTranslation(Object repository, QuerySpec querySpec,
									QuerydslTranslationContext<T> translationContext);
}
