package io.crnk.core.queryspec.pagingspec;

import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.ResourceList;

import java.util.Map;
import java.util.Set;

/**
 * Acts as the default value for {@link JsonApiResource#pagingBehavior()}.
 * It's not a functional implementation of paging. All methods throw an
 * {@link UnsupportedOperationException}. If not overridden per resource,
 * then the first registered {@link PagingBehavior} will be used for pagination.
 *
 * @deprecated obsoleted by @JsonApiResource.pagingSpec
 */
@Deprecated
public class VoidPagingBehavior implements PagingBehavior<PagingSpec> {

	@Override
	public Map<String, Set<String>> serialize(PagingSpec pagingSpec, String resourceType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PagingSpec deserialize(Map<String, Set<String>> parameters) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PagingSpec createEmptyPagingSpec() {
		throw new UnsupportedOperationException();
	}

	@Override
	public PagingSpec createDefaultPagingSpec() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void build(PagedLinksInformation linksInformation, ResourceList<?> resources,
					  QueryAdapter queryAdapter, PagingSpecUrlBuilder urlBuilder) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequired(PagingSpec pagingSpec) {
		throw new UnsupportedOperationException();
	}
}
