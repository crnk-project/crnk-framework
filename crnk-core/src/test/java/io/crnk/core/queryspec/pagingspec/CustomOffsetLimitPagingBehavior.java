package io.crnk.core.queryspec.pagingspec;

import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.ResourceList;

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

	@Override
	public OffsetLimitPagingSpec createDefaultPagingSpec() {
		return delegate.createDefaultPagingSpec();
	}

	@Override
	public void build(final PagedLinksInformation linksInformation, final ResourceList<?> resources, final QueryAdapter queryAdapter, final PagingSpecUrlBuilder urlBuilder) {
		delegate.build(linksInformation, resources, queryAdapter, urlBuilder);
	}

	@Override
	public boolean isRequired(final OffsetLimitPagingSpec pagingSpec) {
		return delegate.isRequired(pagingSpec);
	}
}