package io.crnk.core.queryspec.repository;

import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;

import java.util.Map;
import java.util.Set;

public class CustomOffsetLimitPagingBehavior implements PagingBehavior<OffsetLimitPagingSpec> {

	private OffsetLimitPagingBehavior delegate;

	public CustomOffsetLimitPagingBehavior() {
		delegate = new OffsetLimitPagingBehavior();
		delegate.setDefaultOffset(1);
		delegate.setDefaultLimit(10L);
		delegate.setMaxPageLimit(20L);
	}

	@Override
	public Map<String, Set<String>> serialize(final OffsetLimitPagingSpec pagingSpec, final String resourceType) {
		return delegate.serialize(pagingSpec, resourceType);
	}

	@Override
	public OffsetLimitPagingSpec deserialize(final Map<String, Set<String>> parameters) {
		return delegate.deserialize(parameters);
	}

	@Override
	public OffsetLimitPagingSpec createEmptyPagingSpec() {
		return delegate.createEmptyPagingSpec();
	}
}