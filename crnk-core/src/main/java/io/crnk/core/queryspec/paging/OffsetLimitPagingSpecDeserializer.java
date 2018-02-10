package io.crnk.core.queryspec.paging;

import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.queryspec.DefaultQuerySpecDeserializer;

public class OffsetLimitPagingSpecDeserializer implements PagingSpecDeserializer {

	private final static String OFFSET_PARAMETER = "offset";

	private final static String LIMIT_PARAMETER = "limit";

	private long defaultOffset = 0;

	private Long defaultLimit = null;

	private Long maxPageLimit = null;

	@Override
	public PagingSpec init() {
		return new OffsetLimitPagingSpec(defaultLimit, defaultOffset);
	}

	@Override
	public void deserialize(PagingSpec pagingSpec, DefaultQuerySpecDeserializer.Parameter parameter) {
		OffsetLimitPagingSpec offsetLimitPagingSpec = (OffsetLimitPagingSpec) pagingSpec;

		if (OFFSET_PARAMETER.equalsIgnoreCase(parameter.getPageParameter())) {
			offsetLimitPagingSpec.setOffset(parameter.getLongValue());
		}
		else if (LIMIT_PARAMETER.equalsIgnoreCase(parameter.getPageParameter())) {
			Long limit = parameter.getLongValue();
			if (maxPageLimit != null && limit != null && limit > maxPageLimit) {
				throw new BadRequestException(
						String.format("%s legacy value %d is larger than the maximum allowed of %d",
								LIMIT_PARAMETER, limit, maxPageLimit)
				);
			}
			offsetLimitPagingSpec.setLimit(limit);
		}
		else {
			throw new ParametersDeserializationException(parameter.toString());
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
