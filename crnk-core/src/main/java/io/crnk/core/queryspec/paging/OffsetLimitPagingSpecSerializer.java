package io.crnk.core.queryspec.paging;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OffsetLimitPagingSpecSerializer implements PagingSpecSerializer<OffsetLimitPagingSpec> {

	@Override
	public void serialize(final OffsetLimitPagingSpec pagingSpec, final String resourceType, final Map<String, Set<String>> map) {
		if (pagingSpec.getOffset() != 0) {
			map.put("page[offset]", new HashSet<>(Arrays.asList(Long.toString(pagingSpec.getOffset()))));
		}
		if (pagingSpec.getLimit() != null) {
			map.put("page[limit]", new HashSet<>(Arrays.asList(Long.toString(pagingSpec.getLimit()))));
		}
	}
}
