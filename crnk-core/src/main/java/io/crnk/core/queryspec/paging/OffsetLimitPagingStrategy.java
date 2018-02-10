package io.crnk.core.queryspec.paging;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;

public class OffsetLimitPagingStrategy implements PagingStrategy {

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
	public boolean requirePaging(final PagingSpec pagingSpec) {
		if (pagingSpec instanceof OffsetLimitPagingSpec) {
			OffsetLimitPagingSpec offsetLimitPagingSpec = (OffsetLimitPagingSpec) pagingSpec;

			return offsetLimitPagingSpec.getOffset() != 0 || offsetLimitPagingSpec.getLimit() != null;
		}

		return false;
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
			pageSpec.setPagingSpec(new OffsetLimitPagingSpec(pageSize, 0L));
			linksInformation.setFirst(toUrl(pageSpec, requestSpec, resourceRegistry));

			if (totalPages != null && totalPages > 0) {
				pageSpec.setPagingSpec(new OffsetLimitPagingSpec(pageSize, (totalPages - 1) * pageSize));
				linksInformation.setLast(toUrl(pageSpec, requestSpec, resourceRegistry));
			}

			if (currentPage > 0) {
				pageSpec.setPagingSpec(new OffsetLimitPagingSpec(pageSize, (currentPage - 1) * pageSize));
				linksInformation.setPrev(toUrl(pageSpec, requestSpec, resourceRegistry));
			}


			if (isNextPageAvailable) {
				pageSpec.setPagingSpec(new OffsetLimitPagingSpec(pageSize, (currentPage + 1) * pageSize));
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

	private String toUrl(QueryAdapter queryAdapter, RepositoryRequestSpec requestSpec,
						 ResourceRegistry resourceRegistry) {
		JsonApiUrlBuilder urlBuilder = new JsonApiUrlBuilder(resourceRegistry);
		Object relationshipSourceId = requestSpec.getId();
		ResourceField relationshipField = requestSpec.getRelationshipField();

		ResourceInformation rootInfo;
		if (relationshipField == null) {
			rootInfo = queryAdapter.getResourceInformation();
		} else {
			rootInfo = relationshipField.getParentResourceInformation();
		}
		return urlBuilder.buildUrl(rootInfo, relationshipSourceId, queryAdapter,
				relationshipField != null ? relationshipField.getJsonName() : null);
	}
}
