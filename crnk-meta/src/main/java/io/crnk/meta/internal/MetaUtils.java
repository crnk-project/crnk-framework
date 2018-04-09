package io.crnk.meta.internal;

import io.crnk.core.engine.query.QueryContext;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.MetaFilter;

import java.util.List;

public class MetaUtils {

	private MetaUtils() {

	}

	public static final String firstToLower(String value) {
		if (value.length() > 0) {
			return Character.toLowerCase(value.charAt(0)) + value.substring(1);
		}
		return value;
	}


	public static MetaElement adjustForRequest(MetaLookup lookup, MetaElement element, QueryContext queryContext) {
		List<MetaFilter> filters = lookup.getFilters();
		for (MetaFilter filter : filters) {
			element = filter.adjustForRequest(element, queryContext);
			if (element == null) {
				break;
			}
		}
		return element;
	}
}
