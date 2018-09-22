package io.crnk.core.queryspec;

import io.crnk.core.engine.internal.utils.PreconditionUtil;

import java.io.Serializable;
import java.util.List;

public class SortSpec extends AbstractPathSpec implements Serializable {

	private static final long serialVersionUID = -3547744992729509448L;

	private final Direction direction;

	public SortSpec(List<String> path, Direction direction) {
		this(PathSpec.of(path), direction);
	}

	public SortSpec(PathSpec path, Direction direction) {
		super(path);
		PreconditionUtil.verify(path != null && !path.isEmpty(), "path cannot be empty");
		PreconditionUtil.verify(direction != null, "direction cannot be null for path %s", path);
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
		return new SortSpec(path, direction == Direction.ASC ? Direction.DESC : Direction.ASC);
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
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		SortSpec other = (SortSpec) obj;
		return direction == other.direction;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(path.toString());
		b.append(' ');
		b.append(direction);
		return b.toString();
	}

	@Override
	public SortSpec clone() {
		return new SortSpec(path, direction);
	}
}
