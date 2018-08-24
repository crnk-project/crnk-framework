package io.crnk.core.queryspec;

import java.io.Serializable;

public abstract class IncludeSpec extends AbstractPathSpec implements Serializable {

	private static final long serialVersionUID = -2629584104921925080L;

	public IncludeSpec(PathSpec path) {
		super(path);
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Parameters may not be empty");
		}
	}

	@Override
	public String toString() {
		return path.toString();
	}
}
