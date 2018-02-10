package io.crnk.core.queryspec.paging;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.internal.utils.CompareUtils;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;

public class OffsetLimitPagingSpec extends AbstractPagingSpec {

	private Long limit = null;

	private Long offset = 0L;

	public OffsetLimitPagingSpec() {}

	public OffsetLimitPagingSpec(final Long offset, final Long limit) {
		this.offset = offset;
		this.limit = limit;
	}

	@Override
	public void buildPaging(final PagedLinksInformation linksInformation, final Iterable<?> resources,
							final QueryAdapter queryAdapter, final RepositoryRequestSpec requestSpec,
							final ResourceRegistry resourceRegistry) {
		Long totalCount = getTotalCount(resources);
		Boolean isNextPageAvailable = isNextPageAvailable(resources);
		if ((totalCount != null || isNextPageAvailable != null) && !hasPageLinks(linksInformation)) {
			// only enrich if not already set
			boolean hasResults = resources.iterator().hasNext();
			doEnrichPageLinksInformation(linksInformation, totalCount, isNextPageAvailable,
					queryAdapter, requestSpec, hasResults, resourceRegistry);
		}
	}

	@Override
	public boolean isPagingRequired() {
		return offset != 0 || limit != null;
	}

	private void doEnrichPageLinksInformation(PagedLinksInformation linksInformation, Long total,
											  Boolean isNextPageAvailable, QueryAdapter queryAdapter,
											  RepositoryRequestSpec requestSpec, boolean hasResults,
											  ResourceRegistry resourceRegistry) {
		// private method
		OffsetLimitPagingSpec offsetLimitPagingSpec = (OffsetLimitPagingSpec) queryAdapter.getPagingSpec();
		long pageSize = offsetLimitPagingSpec.getLimit();
		long offset = offsetLimitPagingSpec.getOffset();
		long currentPage = offset / pageSize;
		if (currentPage * pageSize != offset) {
			throw new BadRequestException("offset " + offset + " is not a multiple of limit " + pageSize);
		}
		if (total != null) {
			isNextPageAvailable = offset + pageSize < total;
		}

		if (offset > 0 || hasResults) {
			Long totalPages = total != null ? (total + pageSize - 1) / pageSize : null;
			QueryAdapter pageSpec = queryAdapter.duplicate();
			pageSpec.setPagingSpec(new OffsetLimitPagingSpec(0L, pageSize));
			linksInformation.setFirst(toUrl(pageSpec, requestSpec, resourceRegistry));

			if (totalPages != null && totalPages > 0) {
				pageSpec.setPagingSpec(new OffsetLimitPagingSpec((totalPages - 1) * pageSize, pageSize));
				linksInformation.setLast(toUrl(pageSpec, requestSpec, resourceRegistry));
			}

			if (currentPage > 0) {
				pageSpec.setPagingSpec(new OffsetLimitPagingSpec((currentPage - 1) * pageSize, pageSize));
				linksInformation.setPrev(toUrl(pageSpec, requestSpec, resourceRegistry));
			}


			if (isNextPageAvailable) {
				pageSpec.setPagingSpec(new OffsetLimitPagingSpec((currentPage + 1) * pageSize, pageSize));
				linksInformation.setNext(toUrl(pageSpec, requestSpec, resourceRegistry));
			}
		}
	}

	private Long getTotalCount(Iterable<?> resources) {
		if (resources instanceof ResourceList) {
			ResourceList<?> list = (ResourceList<?>) resources;
			PagedMetaInformation pagedMeta = list.getMeta(PagedMetaInformation.class);
			if (pagedMeta != null) {
				return pagedMeta.getTotalResourceCount();
			}
		}
		return null;
	}

	private Boolean isNextPageAvailable(Iterable<?> resources) {
		if (resources instanceof ResourceList) {
			ResourceList<?> list = (ResourceList<?>) resources;
			HasMoreResourcesMetaInformation pagedMeta = list.getMeta(HasMoreResourcesMetaInformation.class);
			if (pagedMeta != null) {
				return pagedMeta.getHasMoreResources();
			}
		}
		return null;
	}

	private boolean hasPageLinks(PagedLinksInformation pagedLinksInformation) {
		return pagedLinksInformation.getFirst() != null || pagedLinksInformation.getLast() != null
				|| pagedLinksInformation.getPrev() != null || pagedLinksInformation.getNext() != null;
	}

	public Long getLimit() {
		return limit;
	}

	public long getOffset() {
		return offset;
	}

	public OffsetLimitPagingSpec setLimit(final Long limit) {
		this.limit = limit;
		return this;
	}

	public OffsetLimitPagingSpec setOffset(final long offset) {
		this.offset = offset;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((limit == null) ? 0 : limit.hashCode());
		result = prime * result + Long.valueOf(offset).hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		OffsetLimitPagingSpec other = (OffsetLimitPagingSpec) obj;
		return CompareUtils.isEquals(limit, other.limit)
				&& CompareUtils.isEquals(offset, other.offset);
	}

	@Override
	public String toString() {
		return "OffsetLimitPagingSpec{" +
				(offset != null ? "offset=" + offset : "") +
				(limit != null ? ", limit=" + limit : "") +
				'}';
	}
}
