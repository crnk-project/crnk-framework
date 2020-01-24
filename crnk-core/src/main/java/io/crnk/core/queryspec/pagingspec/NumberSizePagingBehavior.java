package io.crnk.core.queryspec.pagingspec;

import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.module.Module;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.ResourceList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NumberSizePagingBehavior extends PagingBehaviorBase<NumberSizePagingSpec> {

	private final static String NUMBER_PARAMETER = "number";

	private final static String SIZE_PARAMETER = "size";

	private int defaultNumber = 1;


	public NumberSizePagingBehavior() {
	}

	@Override
	public boolean supports(Class<? extends PagingSpec> pagingSpecType) {
		return pagingSpecType.isAssignableFrom(NumberSizePagingSpec.class) || pagingSpecType == OffsetLimitPagingSpec.class;
	}

	@Override
	public Map<String, Set<String>> serialize(final NumberSizePagingSpec pagingSpec, final String resourceType) {
		Map<String, Set<String>> values = new HashMap<>();

		// don't show page number if paging is not used
		if (pagingSpec.getSize() != null || pagingSpec.getNumber() != 1) {
			values.put(String.format("page[%s]", NUMBER_PARAMETER),
					new HashSet<>(Arrays.asList(Long.toString(pagingSpec.getNumber()))));
		}
		if (pagingSpec.getSize() != null) {
			values.put(String.format("page[%s]", SIZE_PARAMETER),
					new HashSet<>(Arrays.asList(Long.toString(pagingSpec.getSize()))));
		}

		return values;
	}

	@Override
	public NumberSizePagingSpec deserialize(final Map<String, Set<String>> parameters) {
		NumberSizePagingSpec result = createDefaultPagingSpec();

		for (Map.Entry<String, Set<String>> param : parameters.entrySet()) {
			if (NUMBER_PARAMETER.equalsIgnoreCase(param.getKey())) {
				result.setNumber(getValue(param.getKey(), param.getValue()).intValue());
			} else if (SIZE_PARAMETER.equalsIgnoreCase(param.getKey())) {
				Long size = getValue(param.getKey(), param.getValue());
				if (maxPageLimit != null && size != null && size > maxPageLimit) {
					throw new BadRequestException(
							String.format("%s value %d is larger than the maximum allowed of %d", SIZE_PARAMETER, size,
									maxPageLimit)
					);
				}
				result.setSize(size.intValue());
			} else {
				throw new ParametersDeserializationException(param.getKey());
			}
		}

		return result;
	}

	@Override
	public NumberSizePagingSpec createEmptyPagingSpec() {
		return new NumberSizePagingSpec();
	}

	@Override
	public NumberSizePagingSpec createDefaultPagingSpec() {
		return new NumberSizePagingSpec(defaultNumber, defaultLimit != null ? defaultLimit.intValue() : null);
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
	public boolean isRequired(final NumberSizePagingSpec pagingSpec) {
		return pagingSpec.getNumber() != 1 || pagingSpec.getSize() != null;
	}

	private void doEnrichPageLinksInformation(PagedLinksInformation linksInformation, Long total,
											  Boolean isNextPageAvailable, QueryAdapter queryAdapter,
											  boolean hasResults,
											  PagingSpecUrlBuilder urlBuilder) {
		NumberSizePagingSpec pagingSpec = (NumberSizePagingSpec) queryAdapter.getPagingSpec();
		int size = pagingSpec.getSize();
		int number = pagingSpec.getNumber();
		if (total != null) {
			isNextPageAvailable = size * number < total;
		}

		if (number > 1 || hasResults) {
			Long totalPages = total != null ? (total + size - 1) / size : null;
			QueryAdapter pageSpec = queryAdapter.duplicate();
			pageSpec.setPagingSpec(new NumberSizePagingSpec(1, size));
			linksInformation.setFirst(urlBuilder.build(pageSpec));

			if (totalPages != null && totalPages > 0) {
				pageSpec.setPagingSpec(new NumberSizePagingSpec(totalPages.intValue(), size));
				linksInformation.setLast(urlBuilder.build(pageSpec));
			}

			if (number > 1) {
				pageSpec.setPagingSpec(new NumberSizePagingSpec(number - 1, size));
				linksInformation.setPrev(urlBuilder.build(pageSpec));
			}


			if (isNextPageAvailable) {
				pageSpec.setPagingSpec(new NumberSizePagingSpec(number + 1, size));
				linksInformation.setNext(urlBuilder.build(pageSpec));
			}
		}
	}

	public void setDefaultNumber(int defaultNumber) {
		this.defaultNumber = defaultNumber;
	}

	/**
	 * @return a module providing a NumberSizePagingBehavior implementation
	 */
	public static Module createModule() {
		SimpleModule module = new SimpleModule("numberPaging");
		module.addPagingBehavior(new NumberSizePagingBehavior());
		return module;
	}
}
