package io.crnk.core.queryspec.pagingspec;

import io.crnk.core.engine.internal.utils.CompareUtils;

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
		return "OffsetLimitPagingSpec{" +
				(offset != null ? "offset=" + offset : "") +
				(limit != null ? ", limit=" + limit : "") +
				'}';
	}
}
