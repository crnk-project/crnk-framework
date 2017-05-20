package io.crnk.core.queryspec;

import java.util.List;

public class AbstractPathSpec {

	protected List<String> attributePath;

	protected AbstractPathSpec() {
	}

	protected AbstractPathSpec(List<String> attributePath) {
		this.attributePath = attributePath;
	}

	public List<String> getAttributePath() {
		return attributePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributePath == null) ? 0 : attributePath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractPathSpec other = (AbstractPathSpec) obj;
		if (attributePath == null) {
			if (other.attributePath != null)
				return false;
		} else if (!attributePath.equals(other.attributePath))
			return false;
		return true;
	}

}
