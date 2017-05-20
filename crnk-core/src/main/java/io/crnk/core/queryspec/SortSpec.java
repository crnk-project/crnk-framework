package io.crnk.core.queryspec;

import io.crnk.core.engine.internal.utils.StringUtils;

import java.io.Serializable;
import java.util.List;

public class SortSpec extends AbstractPathSpec implements Serializable {

	private static final long serialVersionUID = -3547744992729509448L;

	private Direction direction;

	public SortSpec(List<String> path, Direction direction) {
		super(path);
		if (path == null || path.isEmpty() || direction == null)
			throw new IllegalArgumentException("Parameters may not be empty");
		this.direction = direction;
	}

	public static SortSpec asc(List<String> expression) {
		return new SortSpec(expression, Direction.ASC);
	}

	public static SortSpec desc(List<String> attributeName) {
		return new SortSpec(attributeName, Direction.DESC);
	}

	public Direction getDirection() {
		return direction;
	}

	public SortSpec reverse() {
		return new SortSpec(attributePath, direction == Direction.ASC ? Direction.DESC : Direction.ASC);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		return super.hashCode() | result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SortSpec other = (SortSpec) obj;
		return direction == other.direction;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(StringUtils.join(".", attributePath));
		b.append(' ');
		b.append(direction);
		return b.toString();
	}
}
