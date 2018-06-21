package io.crnk.core.queryspec.pagingspec;

import io.crnk.core.engine.internal.utils.CompareUtils;
import io.crnk.core.exception.BadRequestException;

public class OffsetLimitPagingSpec implements PagingSpec {

	private Long limit = null;

	private Long offset = 0L;

	public OffsetLimitPagingSpec() {
	}

	public OffsetLimitPagingSpec(final Long offset, final Long limit) {
		this.offset = offset;
		this.limit = limit;
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
	public OffsetLimitPagingSpec clone() {
		return new OffsetLimitPagingSpec(offset, limit);
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
		return "OffsetLimitPagingSpec[" +
				(offset != null ? "offset=" + offset : "") +
				(limit != null ? ", limit=" + limit : "") +
				']';
	}

	public <T extends PagingSpec> T convert(Class<T> pagingSpecType) {
		if (pagingSpecType.equals(NumberSizePagingSpec.class)) {
			if (offset == 0 && limit == null) {
				return (T) new NumberSizePagingSpec(0, null);
			}
			else if (offset == 0) {
				return (T) new NumberSizePagingSpec(0, limit.intValue());
			}
			else if (offset != 0 && limit == null) {
				throw new UnsupportedOperationException("cannot use page offset without page limit");
			}
			else {
				int number = (int) (offset / limit);
				if (number * limit != offset) {
					throw new BadRequestException(
							String.format("offset=%s must be multiple of limit=%s to support page number/size conversion",
									offset, limit));
				}
				return (T) new NumberSizePagingSpec(number, limit.intValue());
			}
		}
		throw new UnsupportedOperationException("cannot converted to " + pagingSpecType);
	}
}
