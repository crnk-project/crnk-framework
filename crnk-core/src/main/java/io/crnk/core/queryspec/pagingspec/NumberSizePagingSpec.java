package io.crnk.core.queryspec.pagingspec;

import io.crnk.core.engine.internal.utils.CompareUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;

public class NumberSizePagingSpec implements PagingSpec{

	private Integer size = null;

	private int number = 1;

	public NumberSizePagingSpec() {
	}

	public NumberSizePagingSpec(final int number, final Integer size) {
		PreconditionUtil.verify(number > 0, "number=%s must be larger than zero", number);
		this.number = number;
		this.size = size;
	}


	public Integer getSize() {
		return size;
	}

	public NumberSizePagingSpec setSize(Integer size) {
		this.size = size;
		return this;
	}

	public <T extends PagingSpec> T convert(Class<T> pagingSpecType) {
		if (pagingSpecType.equals(OffsetLimitPagingSpec.class)) {
			if (number == 1 && size == null) {
				return (T) new OffsetLimitPagingSpec(0L, null);
			}
			else if (number == 1) {
				return (T) new OffsetLimitPagingSpec(0L, size.longValue());
			}
			else if (number != 1 && size == null) {
				throw new UnsupportedOperationException("cannot use page number without page size");
			}
			else {
				return (T) new OffsetLimitPagingSpec(size.longValue() * (number - 1), size.longValue());
			}
		}
		throw new UnsupportedOperationException("cannot converted to " + pagingSpecType);
	}

	@Override
	public NumberSizePagingSpec clone() {
		return new NumberSizePagingSpec(number, size);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((size == null) ? 0 : size.hashCode());
		result = prime * result + Long.valueOf(number).hashCode();
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
		NumberSizePagingSpec other = (NumberSizePagingSpec) obj;
		return CompareUtils.isEquals(number, other.number)
				&& CompareUtils.isEquals(size, other.size);
	}

	@Override
	public String toString() {
		return "NumberSizePagingSpec[number=" + number +
				(size != null ? ", size=" + size : "") + ']';
	}

	public int getNumber() {
		return number;
	}

	public NumberSizePagingSpec setNumber(int number) {
		this.number = number;
		return this;
	}
}
