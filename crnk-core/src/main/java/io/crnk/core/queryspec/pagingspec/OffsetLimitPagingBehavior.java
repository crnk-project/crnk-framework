package io.crnk.core.queryspec.pagingspec;

import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ParametersDeserializationException;

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
		OffsetLimitPagingSpec result = createEmptyPagingSpec();

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
		return new OffsetLimitPagingSpec(defaultOffset, defaultLimit);
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
