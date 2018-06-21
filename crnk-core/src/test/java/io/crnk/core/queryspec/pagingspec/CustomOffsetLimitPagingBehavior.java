package io.crnk.core.queryspec.pagingspec;

import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.ResourceList;

public class CustomOffsetLimitPagingBehavior implements PagingBehavior<CustomOffsetLimitPagingSpec> {

	private OffsetLimitPagingBehavior delegate;

	public CustomOffsetLimitPagingBehavior() {
		delegate = new OffsetLimitPagingBehavior();
		delegate.setDefaultOffset(1);
		delegate.setDefaultLimit(10L);
		delegate.setMaxPageLimit(20L);
	}

	@Override
	public Map<String, Set<String>> serialize(final CustomOffsetLimitPagingSpec pagingSpec, final String resourceType) {
		return delegate.serialize(pagingSpec, resourceType);
	}

	@Override
	public CustomOffsetLimitPagingSpec deserialize(final Map<String, Set<String>> parameters) {
		return convert(delegate.deserialize(parameters));
	}

	@Override
	public CustomOffsetLimitPagingSpec createEmptyPagingSpec() {
		return convert(delegate.createEmptyPagingSpec());
	}

	@Override
	public CustomOffsetLimitPagingSpec createDefaultPagingSpec() {
		return convert(delegate.createDefaultPagingSpec());
	}

	private CustomOffsetLimitPagingSpec convert(OffsetLimitPagingSpec spec) {
		CustomOffsetLimitPagingSpec copy = new CustomOffsetLimitPagingSpec();
		copy.setOffset(spec.getOffset());
		copy.setLimit(spec.getLimit());
		return copy;
	}

	@Override
	public void build(final PagedLinksInformation linksInformation, final ResourceList<?> resources,
			final QueryAdapter queryAdapter, final PagingSpecUrlBuilder urlBuilder) {
		delegate.build(linksInformation, resources, queryAdapter, urlBuilder);
	}

	@Override
	public boolean isRequired(final CustomOffsetLimitPagingSpec pagingSpec) {
		return delegate.isRequired(pagingSpec);
	}
}