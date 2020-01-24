package io.crnk.core.queryspec.pagingspec;

import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;

import java.util.Set;

abstract class PagingBehaviorBase<T extends PagingSpec> implements LimitBoundedPagingBehavior<T> {

	protected Long defaultLimit;

	protected Long maxPageLimit;

	public Long getDefaultLimit() {
		return defaultLimit;
	}

	public void setDefaultLimit(final Long defaultLimit) {
		this.defaultLimit = defaultLimit;
	}

	public Long getMaxPageLimit() {
		return maxPageLimit;
	}

	public void setMaxPageLimit(final Long maxPageLimit) {
		this.maxPageLimit = maxPageLimit;
	}


	protected Long getTotalCount(ResourceList<?> resources) {
		PagedMetaInformation pagedMeta = resources.getMeta(PagedMetaInformation.class);
		if (pagedMeta != null) {
			return pagedMeta.getTotalResourceCount();
		}

		return null;
	}

	protected Boolean isNextPageAvailable(ResourceList<?> resources) {
		HasMoreResourcesMetaInformation pagedMeta = resources.getMeta(HasMoreResourcesMetaInformation.class);
		if (pagedMeta != null) {
			return pagedMeta.getHasMoreResources();
		}

		return null;
	}

	protected boolean hasPageLinks(PagedLinksInformation pagedLinksInformation) {
		return pagedLinksInformation.getFirst() != null || pagedLinksInformation.getLast() != null
				|| pagedLinksInformation.getPrev() != null || pagedLinksInformation.getNext() != null;
	}

	protected Long getValue(final String name, final Set<String> values) {
		if (values.size() > 1) {
			throw new ParametersDeserializationException(name);
		}

		try {
			return Long.parseLong(values.iterator().next());
		} catch (RuntimeException e) {
			throw new ParametersDeserializationException(name);
		}
	}

}
