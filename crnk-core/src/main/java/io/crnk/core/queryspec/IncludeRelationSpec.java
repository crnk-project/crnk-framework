package io.crnk.core.queryspec;

import java.util.List;

public class IncludeRelationSpec extends IncludeSpec {

	private static final long serialVersionUID = -1343366742266390343L;

	public IncludeRelationSpec(List<String> path) {
		super(PathSpec.of(path));
	}

	public IncludeRelationSpec(PathSpec path) {
		super(path);
	}

	@Override
	public IncludeRelationSpec clone() {
		return new IncludeRelationSpec(path.clone());
	}
}
