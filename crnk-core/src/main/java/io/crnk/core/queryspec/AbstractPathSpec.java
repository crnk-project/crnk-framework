package io.crnk.core.queryspec;

import java.util.List;

public class AbstractPathSpec {

	protected PathSpec path;

	protected AbstractPathSpec() {
		this.path = null;
	}

	protected AbstractPathSpec(List<String> attributePath) {
		this.path = PathSpec.of(attributePath);
	}

	protected AbstractPathSpec(PathSpec path) {
		this.path = path;
	}

	public PathSpec getPath() {
		return path;
	}

	public void setPath(PathSpec path) {
		this.path = path;
	}

	public List<String> getAttributePath() {
		return path != null ? path.getElements() : null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractPathSpec other = (AbstractPathSpec) obj;
		if (path == null) {
			return other.path == null;
		} else return path.equals(other.path);
	}

}
