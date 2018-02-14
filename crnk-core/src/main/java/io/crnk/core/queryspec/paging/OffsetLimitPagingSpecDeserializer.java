package io.crnk.core.queryspec.paging;

import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ParametersDeserializationException;

import java.util.Set;

public class OffsetLimitPagingSpecDeserializer implements PagingSpecDeserializer<OffsetLimitPagingSpec> {

	private final static String OFFSET_PARAMETER = "offset";

	private final static String LIMIT_PARAMETER = "limit";

	private long defaultOffset = 0;

	private Long defaultLimit = null;

	private Long maxPageLimit = null;

	@Override
	public OffsetLimitPagingSpec init() {
		return new OffsetLimitPagingSpec(defaultOffset, defaultLimit);
	}

	@Override
	public void deserialize(OffsetLimitPagingSpec pagingSpec, String name, Set<String> values) {
		if (OFFSET_PARAMETER.equalsIgnoreCase(name)) {
			pagingSpec.setOffset(getValue(name, values));
		} else if (LIMIT_PARAMETER.equalsIgnoreCase(name)) {
			Long limit = getValue(name, values);
			if (maxPageLimit != null && limit != null && limit > maxPageLimit) {
				throw new BadRequestException(
						String.format("%s legacy value %d is larger than the maximum allowed of %d",
								LIMIT_PARAMETER, limit, maxPageLimit)
				);
			}
			pagingSpec.setLimit(limit);
		} else {
			throw new ParametersDeserializationException(name);
		}
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

	public OffsetLimitPagingSpecDeserializer setDefaultOffset(final long defaultOffset) {
		this.defaultOffset = defaultOffset;
		return this;
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
