package io.crnk.core.queryspec.paging;

public class OffsetLimitPagingSpec implements PagingSpec {

	private Long limit = null;

	private Long offset = 0L;

	public OffsetLimitPagingSpec() {}

	public OffsetLimitPagingSpec(final Long limit, final Long offset) {
		this.limit = limit;
		this.offset = offset;
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
	public String toString() {
		return "OffsetLimitPagingSpec{" +
				(limit != null ? "limit=" + limit : "") +
				(offset != null ? "offset=" + offset : "") +
				'}';
	}
}
