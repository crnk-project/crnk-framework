package io.crnk.test.mock.repository.nested;

import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.test.mock.models.nested.PostHeader;

public class OneNestedRepository extends InMemoryResourceRepository<PostHeader, String> {

	public OneNestedRepository() {
		super(PostHeader.class);
	}
}