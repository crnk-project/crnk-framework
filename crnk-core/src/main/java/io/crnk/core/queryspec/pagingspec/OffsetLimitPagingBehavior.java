package io.crnk.core.queryspec.pagingspec;

import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OffsetLimitPagingBehavior implements PagingBehavior<OffsetLimitPagingSpec> {

	private final static String OFFSET_PARAMETER = "offset";

	private final static String LIMIT_PARAMETER = "limit";

	private long defaultOffset = 0;

	private Long defaultLimit = null;

	private Long maxPageLimit = null;

	@Override
	public Map<String, Set<String>> serialize(final OffsetLimitPagingSpec pagingSpec, final String resourceType) {
		Map<String, Set<String>> values = new HashMap<>();
		if (pagingSpec.getOffset() != 0) {
			values.put(String.format("page[%s]", OFFSET_PARAMETER), new HashSet<>(Arrays.asList(Long.toString(pagingSpec.getOffset()))));
		}
		if (pagingSpec.getLimit() != null) {
			values.put(String.format("page[%s]", LIMIT_PARAMETER), new HashSet<>(Arrays.asList(Long.toString(pagingSpec.getLimit()))));
		}

		return values;
	}

	@Override
	public OffsetLimitPagingSpec deserialize(final Map<String, Set<String>> parameters) {
		OffsetLimitPagingSpec result = createDefaultPagingSpec();

		for (Map.Entry<String, Set<String>> param : parameters.entrySet()) {
			if (OFFSET_PARAMETER.equalsIgnoreCase(param.getKey())) {
				result.setOffset(getValue(param.getKey(), param.getValue()));
			} else if (LIMIT_PARAMETER.equalsIgnoreCase(param.getKey())) {
				Long limit = getValue(param.getKey(), param.getValue());
				if (maxPageLimit != null && limit != null && limit > maxPageLimit) {
					throw new BadRequestException(
							String.format("%s legacy value %d is larger than the maximum allowed of %d", LIMIT_PARAMETER, limit, maxPageLimit)
					);
				}
				result.setLimit(limit);
			} else {
				throw new ParametersDeserializationException(param.getKey());
			}
		}

		return result;
	}

	@Override
	public OffsetLimitPagingSpec createEmptyPagingSpec() {
		return new OffsetLimitPagingSpec();
	}

	@Override
	public OffsetLimitPagingSpec createDefaultPagingSpec() {
		return new OffsetLimitPagingSpec(defaultOffset, defaultLimit);
	}

	@Override
	public void build(final PagedLinksInformation linksInformation,
					  final ResourceList<?> resources,
					  final QueryAdapter queryAdapter,
					  final PagingSpecUrlBuilder urlBuilder) {
		Long totalCount = getTotalCount(resources);
		Boolean isNextPageAvailable = isNextPageAvailable(resources);
		if ((totalCount != null || isNextPageAvailable != null) && !hasPageLinks(linksInformation)) {
			// only enrich if not already set
			boolean hasResults = resources.iterator().hasNext();
			doEnrichPageLinksInformation(linksInformation, totalCount, isNextPageAvailable,
					queryAdapter, hasResults, urlBuilder);
		}
	}

	@Override
	public boolean isRequired(final OffsetLimitPagingSpec pagingSpec) {
		return pagingSpec.getOffset() != 0 || pagingSpec.getLimit() != null;
	}

	private void doEnrichPageLinksInformation(PagedLinksInformation linksInformation, Long total,
											  Boolean isNextPageAvailable, QueryAdapter queryAdapter,
											  boolean hasResults,
											  PagingSpecUrlBuilder urlBuilder) {
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
			linksInformation.setFirst(urlBuilder.build(pageSpec));

			if (totalPages != null && totalPages > 0) {
				pageSpec.setPagingSpec(new OffsetLimitPagingSpec((totalPages - 1) * pageSize, pageSize));
				linksInformation.setLast(urlBuilder.build(pageSpec));
			}

			if (currentPage > 0) {
				pageSpec.setPagingSpec(new OffsetLimitPagingSpec((currentPage - 1) * pageSize, pageSize));
				linksInformation.setPrev(urlBuilder.build(pageSpec));
			}


			if (isNextPageAvailable) {
				pageSpec.setPagingSpec(new OffsetLimitPagingSpec((currentPage + 1) * pageSize, pageSize));
				linksInformation.setNext(urlBuilder.build(pageSpec));
			}
		}
	}

	private Long getTotalCount(ResourceList<?> resources) {
		PagedMetaInformation pagedMeta = resources.getMeta(PagedMetaInformation.class);
		if (pagedMeta != null) {
			return pagedMeta.getTotalResourceCount();
		}

		return null;
	}

	private Boolean isNextPageAvailable(ResourceList<?> resources) {
		HasMoreResourcesMetaInformation pagedMeta = resources.getMeta(HasMoreResourcesMetaInformation.class);
		if (pagedMeta != null) {
			return pagedMeta.getHasMoreResources();
		}

		return null;
	}

	private boolean hasPageLinks(PagedLinksInformation pagedLinksInformation) {
		return pagedLinksInformation.getFirst() != null || pagedLinksInformation.getLast() != null
				|| pagedLinksInformation.getPrev() != null || pagedLinksInformation.getNext() != null;
	}

	private Long getValue(final String name, final Set<String> values) {
		if (values.size() > 1) {
			throw new ParametersDeserializationException(name);
		}

		try {
			return Long.parseLong(values.iterator().next());
		} catch (RuntimeException e) {
			throw new ParametersDeserializationException(name);
		}
	}

	public long getDefaultOffset() {
		return defaultOffset;
	}

	public void setDefaultOffset(final long defaultOffset) {
		this.defaultOffset = defaultOffset;
	}

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
}
