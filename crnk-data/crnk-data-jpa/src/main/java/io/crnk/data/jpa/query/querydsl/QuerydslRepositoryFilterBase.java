package io.crnk.data.jpa.query.querydsl;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.data.jpa.JpaRepositoryFilterBase;

public class QuerydslRepositoryFilterBase extends JpaRepositoryFilterBase implements QuerydslRepositoryFilter {

	@Override
	public <T> void filterQueryTranslation(Object repository, QuerySpec querySpec,
										   QuerydslTranslationContext<T> translationContext) {
		// nothing to do
	}
}
