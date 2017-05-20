package io.crnk.core.queryspec;

import io.crnk.core.engine.internal.utils.StringUtils;

import java.io.Serializable;
import java.util.List;

public abstract class IncludeSpec extends AbstractPathSpec implements Serializable {

	private static final long serialVersionUID = -2629584104921925080L;

	public IncludeSpec(List<String> path) {
		super(path);
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Parameters may not be empty");
		}
	}

	@Override
	public String toString() {
		return StringUtils.join(".", attributePath);
	}
}
